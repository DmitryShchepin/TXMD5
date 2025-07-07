package com.taixiu.unbalanced.core.randomizer;

import com.mario.random.entity.GameType;
import com.mario.random.entity.GeneratedResultMessageResponse;
import com.mario.random.entity.RandomResultHistoryLog;
import com.mario.random.entity.RoundConfig;
import com.mario.random.observers.GameResultObserver;
import com.mario.random.repository.RandomResultHistoryLogRepository;
import com.mario.random.service.RandomService;
import com.mario.random.service.impl.RandomServiceImpl;
import com.mario.random.utils.ConfigParametersUtil;
import com.nhb.common.data.PuObject;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Slf4j
@Data
public class RandomServiceManager {

    private static final String RANDOM_SERVICE_ENABLE_HISTORY_LOGGING = "enableHistoryLogging";
    private static final String RANDOM_SERVICE_HOST = "host";
    private static final String RANDOM_SERVICE_PORT = "port";
    private static final String RANDOM_SERVICE_GAME_ID = "gameId";
    private static final String RANDOM_SERVICE_GAME_TYPE = "gameType";
    private static final String RANDOM_SERVICE_STUDIO_GAME_ID = "studioGameId";
    private static final String RANDOM_SERVICE_ROUND_IN_SECONDS = "roundInSeconds";
    private static final String RANDOM_SERVICE_BUFFER_IN_SECONDS = "bufferInSeconds";
    private static final String RANDOM_SERVICE_MD5_PASS_LENGTH = "md5PassLength";
    private static final String RANDOM_SERVICE_MD5_PASS_EMOJI_LENGTH = "md5PassEmojiLength";

    private RandomService randomService;
    private RoundConfig config;

    private long lastMaintenanceAt;
    private long lastMaintenanceDuration;

    private boolean forceReconnectNextSession = false;

    public RandomServiceManager(PuObject params, GameResultObserver observer, RandomResultHistoryLogRepository repository) {
        config = RoundConfig.builder()
                .enableHistoryLogging(params.getBoolean(RANDOM_SERVICE_ENABLE_HISTORY_LOGGING, false))
                .gameId(ConfigParametersUtil.validateAndGetString(params, RANDOM_SERVICE_GAME_ID))
                .studioGameId(ConfigParametersUtil.validateAndGetString(params, RANDOM_SERVICE_STUDIO_GAME_ID))
                .gameType(GameType.valueOf(ConfigParametersUtil.validateAndGetString(params, RANDOM_SERVICE_GAME_TYPE)))
                .bufferInSeconds(ConfigParametersUtil.validateAndGetInteger(params, RANDOM_SERVICE_BUFFER_IN_SECONDS, 2))
                .roundInSeconds(ConfigParametersUtil.validateAndGetInteger(params, RANDOM_SERVICE_ROUND_IN_SECONDS, 10))
                .md5PassLength(ConfigParametersUtil.validateAndGetInteger(params, RANDOM_SERVICE_MD5_PASS_LENGTH, 40))
                .md5PassEmojiLength(ConfigParametersUtil.validateAndGetInteger(params, RANDOM_SERVICE_MD5_PASS_EMOJI_LENGTH, 10))
                .build();
        log.info("Round config initialized: {}", config);
        randomService = new RandomServiceImpl(config.getGameId(),
                ConfigParametersUtil.validateAndGetString(params, RANDOM_SERVICE_HOST, RandomServiceImpl.RANDOM_SERVER_HOST),
                ConfigParametersUtil.validateAndGetInteger(params, RANDOM_SERVICE_PORT, RandomServiceImpl.RANDOM_SERVER_PORT),
                new RandomManagerWrapperObserver(observer), repository);
    }

    public boolean canGenerateResult() {
        long now = System.currentTimeMillis();
        if (lastMaintenanceAt > 0) {
            if (now >= lastMaintenanceAt && now < lastMaintenanceAt + lastMaintenanceDuration) {
                return false; // During maintenance period
            }
            return now < lastMaintenanceAt - GeneratedResultMessageResponse.DEFAULT_MAINTENANCE_BUFFER || now >= lastMaintenanceAt + lastMaintenanceDuration; // Within 2 minutes before maintenance
        }
        return true;
    }

    public void generateResult(String sessionId, String roundId) {
        RoundConfig roundConfig = config.withSessionId(sessionId).withRoundId(roundId);
        if (forceReconnectNextSession) {
            roundConfig = roundConfig.withForceReconnect(forceReconnectNextSession);
            forceReconnectNextSession = false;
        }
        randomService.generateResult(roundConfig);
    }

    public RandomResultHistoryLog update(String sessionId, Map<String, Object> updates) {
        return randomService.update(sessionId, updates);
    }

    public class RandomManagerWrapperObserver implements GameResultObserver {

        private final GameResultObserver mainObserver;

        public RandomManagerWrapperObserver(GameResultObserver observer) {
            this.mainObserver = observer;
        }

        @Override
        public void onResultGenerated(GeneratedResultMessageResponse generatedResultMessageResponse) {
            lastMaintenanceAt = generatedResultMessageResponse.getServiceCloseAt();
            lastMaintenanceDuration = generatedResultMessageResponse.getServiceCloseDuration();
            mainObserver.onResultGenerated(generatedResultMessageResponse);
        }

        @Override
        public void onError(Throwable throwable) {
            mainObserver.onError(throwable);
        }
    }
}
