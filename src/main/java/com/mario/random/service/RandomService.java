package com.mario.random.service;

import com.mario.random.entity.RandomResultHistoryLog;
import com.mario.random.entity.RoundConfig;

import java.util.Map;

public interface RandomService {

    void generateResult(RoundConfig config);

    void terminate();

    RandomResultHistoryLog update(String sessionId, Map<String, Object> updates);
}
