package com.mario.random.entity;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
public enum GetResultStatus {
    GET_RESULT_STATUS_FOUND(0),
    GET_RESULT_STATUS_NOT_FOUND(1);

    private final int value;
    private static final Map<Integer, GetResultStatus> map = new HashMap<>();

    static {
        for (GetResultStatus status : GetResultStatus.values()) {
            map.put(status.value, status);
        }
    }

    GetResultStatus(int value) {
        this.value = value;
    }

    public static GetResultStatus fromValue(int value) {
        return map.getOrDefault(value, GET_RESULT_STATUS_NOT_FOUND);
    }
}