package com.mario.random.entity;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
public enum GameType {
    GAME_TYPE_UNKNOWN(0, false),
    GAME_TYPE_BIG_SMALL(1, true),
    GAME_TYPE_COIN_FLIP(2, true),
    GAME_TYPE_GOURD_CRAB(3, true),
    GAME_TYPE_BIG_SMALL_TRADITION(4, false),
    GAME_TYPE_COIN_FLIP_TRADITION(5, false),
    GAME_TYPE_GOURD_CRAB_TRADITION(6, false);

    private final int value;
    private final boolean isTwoPhaseResult;
    private static final Map<Integer, GameType> map = new HashMap<>();

    static {
        for (GameType type : GameType.values()) {
            map.put(type.value, type);
        }
    }

    GameType(int value, boolean isTwoPhaseResult) {
        this.value = value;
        this.isTwoPhaseResult = isTwoPhaseResult;
    }

    public static GameType fromValue(int value) {
        return map.getOrDefault(value, GAME_TYPE_UNKNOWN);
    }
}