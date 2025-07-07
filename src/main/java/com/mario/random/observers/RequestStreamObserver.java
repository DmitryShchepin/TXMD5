package com.mario.random.observers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mario.random.entity.GeneratedResultMessageResponse;
import com.mario.random.service.impl.RandomServiceImpl;
import com.mario.random.utils.AESUtils;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import random_generator.Random;

@Slf4j
public class RequestStreamObserver implements StreamObserver<Random.GenerateRandomNumberEncryptedResponse> {

    private final byte[] aesKey;
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private final GameResultObserver gameResultObserver;
    private final RandomServiceImpl randomService;

    public RequestStreamObserver(byte[] aesKey, GameResultObserver gameResultObserver, RandomServiceImpl randomService) {
        this.aesKey = aesKey;
        this.gameResultObserver = gameResultObserver;
        this.randomService = randomService;
    }

    // TODO how to get config ???
    @Override
    public void onNext(Random.GenerateRandomNumberEncryptedResponse response) {
        try {
            byte[] decoded = response.getEncryptedData().toByteArray();
            byte[] decryptedData = AESUtils.decryptAES(decoded, aesKey).getBytes();
            log.debug("Decrypted data response: " + new String(decryptedData));
            GeneratedResultMessageResponse result = objectMapper.readValue(new String(decryptedData), GeneratedResultMessageResponse.class);
            gameResultObserver.onResultGenerated(result);

            randomService.updateHistoryLog(result);
        } catch (Exception e) {
            log.error("Failed to process response - Exception: ", e);
            randomService.logError(e.getMessage());
        }
    }

    @Override
    public void onError(Throwable t) {
        log.error("Stream error: " + t.getMessage());
        gameResultObserver.onError(t);
    }

    @Override
    public void onCompleted() {
        log.debug("Stream completed");
        // nothing to do
    }
}
