package com.mario.random.entity;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
public enum GenerateRandomNumberResult {
    GENERATE_RANDOM_NUMBER_RESULT_SUCCESS(0),
    GENERATE_RANDOM_NUMBER_RESULT_FAILED(1);

    private final int value;
    private static final Map<Integer, GenerateRandomNumberResult> map = new HashMap<>();

    static {
        for (GenerateRandomNumberResult result : GenerateRandomNumberResult.values()) {
            map.put(result.value, result);
        }
    }

    GenerateRandomNumberResult(int value) {
        this.value = value;
    }

    public static GenerateRandomNumberResult fromValue(int value) {
        return map.getOrDefault(value, GENERATE_RANDOM_NUMBER_RESULT_FAILED);
    }
}