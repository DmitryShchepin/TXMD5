package com.mario.random.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.protobuf.ByteString;
import com.mario.random.AesKeyException;
import com.mario.random.entity.GameType;
import com.mario.random.entity.GenerateResultMessageRequest;
import com.mario.random.entity.GeneratedResultMessageResponse;
import com.mario.random.entity.RandomResultHistoryLog;
import com.mario.random.entity.RoundConfig;
import com.mario.random.interceptors.GameIdClientInterceptor;
import com.mario.random.observers.GameResultObserver;
import com.mario.random.observers.RequestStreamObserver;
import com.mario.random.repository.RandomResultHistoryLogRepository;
import com.mario.random.service.KeyManager;
import com.mario.random.service.RandomChannel;
import com.mario.random.service.RandomService;
import com.mario.random.utils.AESUtils;
import io.grpc.ManagedChannel;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import random_generator.GeneratorServiceGrpc;
import random_generator.Random;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static com.mario.random.entity.RandomResultHistoryLog.Status.*;
import static java.util.Optional.ofNullable;

@Slf4j
public class RandomServiceImpl implements RandomService {

    public final static String RANDOM_SERVER_HOST = "18.143.78.3";
    public final static int RANDOM_SERVER_PORT = 8089;

    private StreamObserver<Random.GenerateRandomNumberEncryptedRequest> requestObserver;

    private final RandomResultHistoryLogRepository historyLogRepository;
    private final ObjectMapper objectMapper;
    private final GameResultObserver observer;
    private final String gameId;
    private final String host;
    private final int port;

    private KeyManager keyManager;
    private byte[] aesKey;
    private boolean isEnableHistoryLogging;
    private int reconnectAttempts = 0;
    private static final int MAX_RECONNECT_ATTEMPTS = 2;

    public RandomServiceImpl(String gameId, GameResultObserver observer, RandomResultHistoryLogRepository historyLogRepository) {
        this(gameId, RANDOM_SERVER_HOST, RANDOM_SERVER_PORT, observer, historyLogRepository);
    }

    public RandomServiceImpl(String gameId, String host, int port, GameResultObserver observer, RandomResultHistoryLogRepository historyLogRepository) {
        log.debug("RandomServiceImpl initialization started");
        this.gameId = gameId;
        this.host = host;
        this.port = port;
        this.observer = observer;
        this.historyLogRepository = historyLogRepository;
        this.objectMapper = new ObjectMapper();
        log.debug("RandomServiceImpl created with following parameters: {}, {}, {}", gameId, host, port);
        // for debug
        // ChannelKiller.scheduleChannelShutdown();
    }

    private boolean canGenerateResult() {
        log.debug("Checking if can generate result: {}, {}, {}, {}", RandomChannel.isAlive(), requestObserver, keyManager, aesKey);
        return RandomChannel.isAlive() && requestObserver != null && keyManager != null && aesKey != null;
    }

    @Override
    public void generateResult(RoundConfig config) {
        isEnableHistoryLogging = config.isEnableHistoryLogging();

        if (!canGenerateResult() || config.isForceReconnect()) {
            log.debug("Channel is not alive, reinitializing connection");
            reconnect(null);
            if (!canGenerateResult()) {
                log.debug("Result can not be generated");
                observer.onError(new Exception("Result can not be generated. Server not available"));
            }
        }

        GenerateResultMessageRequest request = new GenerateResultMessageRequest(config);
        try {
            String json = objectMapper.writeValueAsString(request);
            log.debug("Sending request: {}", json);
            byte[] encryptedMessage = AESUtils.encryptAES(json.getBytes(), aesKey);
            requestObserver.onNext(Random.GenerateRandomNumberEncryptedRequest.newBuilder()
                    .setEncryptedData(ByteString.copyFrom(encryptedMessage))
                    .build());

            if (config.isEnableHistoryLogging()) {
                createHistoryLog(config, json);
            }

        } catch (Exception e) {
            log.debug("Error while generating result: {}", e.getMessage());
            if (config.isEnableHistoryLogging()) {
                createHistoryErrorLog(config, request.toString(), e.getMessage());
            }
            reconnect(e);
        }
    }

    @Override
    public RandomResultHistoryLog update(String sessionId, Map<String, Object> updates) {
        historyLogRepository.update(sessionId, updates);
        return historyLogRepository.findBySessionId(sessionId);
    }

    private void createHistoryErrorLog(RoundConfig config, String requestDetails, String errorMessage) {
        log.debug("Creating history error log: {}", errorMessage);

        RandomResultHistoryLog historyLog = RandomResultHistoryLog.builder()
                .request(requestDetails)
                .gameType(config.getGameType().name())
                .gameId(config.getGameId())
                .gameName(config.getGameName())
                .studioId(config.getStudioGameId())
                .sessionId(config.getSessionId())
                .status(RandomResultHistoryLog.Status.ERROR)
                .errorMessage(errorMessage)
                .createdAt(new Date())
                .updatedAt(new Date())
                .build();

        historyLogRepository.create(historyLog);
        log.debug("History log created: {}", historyLog);
    }

    private void createHistoryLog(RoundConfig config, String requestDetails) {
        if (!isEnableHistoryLogging) {
            return;
        }

        RandomResultHistoryLog historyLog = RandomResultHistoryLog.builder()
                .request(requestDetails)
                .gameType(config.getGameType().name())
                .gameId(config.getGameId())
                .gameName(config.getGameName())
                .studioId(config.getStudioGameId())
                .sessionId(config.getSessionId())
                .status(RandomResultHistoryLog.Status.RESULT_REQUESTED)
                .createdAt(new Date())
                .updatedAt(new Date())
                .build();

        historyLogRepository.create(historyLog);
        log.debug("History log created");
    }

    private boolean checkOrInitAesKey(boolean forceChange) throws Exception {
        log.debug("Checking AES key: {}", keyManager);
        if (aesKey == null || forceChange) {
            aesKey = keyManager.getAesKey(forceChange);
            log.debug("AES key created: {}", aesKey);
        } else {
            log.debug("AES key already exists: {}", aesKey);
        }
        return aesKey != null;
    }

    @Override
    public void terminate() {
        requestObserver.onCompleted();
        RandomChannel.shutdown();
    }

    public void updateHistoryLog(GeneratedResultMessageResponse result) {
        log.debug("Updating history log: {}", result);
        if (!isEnableHistoryLogging) {
            return;
        }
        RandomResultHistoryLog historyLog = historyLogRepository.findBySessionId(result.getSessionID());

        if (historyLog == null) {
            log.debug("History log not found for session: " + result.getSessionID());
            return;
        }
        Map<String, Object> updates = new HashMap<>();
        GameType gameType = GameType.valueOf(historyLog.getGameType());

        if (result.isUnderMaintenance()) {
            updates.put(RandomResultHistoryLogRepository.Fields.IS_MAINTENANCE_MODE, true);
            updates.put(RandomResultHistoryLogRepository.Fields.STATUS, MAINTENANCE.name());
        } else if (gameType.isTwoPhaseResult()) {
            if (Objects.equals(RESULT_REQUESTED, historyLog.getStatus())) {
                // Phase one
                updates.put(RandomResultHistoryLogRepository.Fields.STATUS, HASH_RESULT_GENERATED.name());
            } else if (Objects.equals(HASH_RESULT_GENERATED, historyLog.getStatus())) {
                // Phase two
                updates.put(RandomResultHistoryLogRepository.Fields.STATUS, COMPLETED.name());
            }
        } else {
            updates.put(RandomResultHistoryLogRepository.Fields.STATUS, COMPLETED.name());
        }

        if (result.getResult() != null) {
            updates.put(RandomResultHistoryLogRepository.Fields.RESULT, result.getResult());
        }

        if (result.getHashResult() != null) {
            updates.put(RandomResultHistoryLogRepository.Fields.RESULT_HASH, result.getHashResult());
        }

        updates.put(RandomResultHistoryLogRepository.Fields.MAINTAIN_AT, result.getServiceCloseAt());
        updates.put(RandomResultHistoryLogRepository.Fields.MAINTAIN_DURATION, result.getServiceCloseDuration());
        updates.put(RandomResultHistoryLogRepository.Fields.DETAIL, result.getDetail());
        updates.put(RandomResultHistoryLogRepository.Fields.UPDATED_AT, new Date());

        historyLogRepository.update(historyLog.getSessionId(), updates);
        log.debug("History log updated sessionId: {}, updates: {}", historyLog.getSessionId(), updates);
    }

    public void logError(String message) {
        if (!isEnableHistoryLogging) {
            return;
        }
        //TODO
    }

    public void reconnect(Throwable e) {
        if (reconnectAttempts < MAX_RECONNECT_ATTEMPTS) {
            reconnectAttempts++; // Increment attempt count
            log.debug("Attempting to connect... Attempt {} of {}", reconnectAttempts, MAX_RECONNECT_ATTEMPTS);
            try {
                requestObserver = null; // Reset observer
                aesKey = null; // Reset AES key
                RandomChannel.shutdown(); // Shut down the old channel
                TimeUnit.SECONDS.sleep(reconnectAttempts); // Exponential backoff
                initializeConnection(e != null || reconnectAttempts > 0); // Replace the old observer with the new one

                log.debug("Successfully connected on attempt {}", reconnectAttempts);
                reconnectAttempts = 0; // Reset attempts after successful reconnection
            } catch (Exception ex) {
                log.debug("Reconnection attempt {} failed: {}", reconnectAttempts, ex.getMessage());
                if (reconnectAttempts >= MAX_RECONNECT_ATTEMPTS) {
                    log.debug("Max reconnection attempts reached. Failing permanently.");
                    logError(ex.getMessage());
                }
            }
        } else {
            reconnectAttempts = 0;
            log.debug("Max reconnection attempts reached. Failing permanently.", e);
            logError(ofNullable(e).orElse(new Throwable("Max reconnection attempts reached. Failing permanently.")).getMessage());
        }
    }

    private synchronized void initializeConnection(boolean forced) throws Exception {
        log.debug("Initializing connection to random server");

        ManagedChannel channel = RandomChannel.getInstance(host, port);

        if (!RandomChannel.isAlive()) {
            log.debug("Channel is not alive, skipping initialization");
            return;
        }

        keyManager = new KeyManagerImpl(gameId, channel);
        if (!checkOrInitAesKey(forced)) {
            throw new AesKeyException("AES key not available, result can not be generated");
        }

        GeneratorServiceGrpc.GeneratorServiceStub asyncStub = GeneratorServiceGrpc.newStub(channel)
                .withInterceptors(new GameIdClientInterceptor(gameId));
        StreamObserver<Random.GenerateRandomNumberEncryptedRequest> stub = asyncStub.bidirectionalStreamGenerateRandomNumberEncrypted(
                new RequestStreamObserver(aesKey, observer, this));
        log.debug("Connection to random server initialized");
        requestObserver = stub;
    }
}

