package com.taixiu.unbalanced.core;

public enum GameStatus {

    DO_RESERVE_USER_ENTER_ROOM(""),
    DO_RELEASE_USER_EXIT_ROOM(""),
    ON_SAVE_GAME_UPDATING_WINLOSE(""),
    ON_TAIXIU_ALL_IN(""),
    REFUND_UN_COMPLETED_SESSION(""),
    BETTING(""),
    COMMIT_TICKET_ID(""),
    CHANGE_MONEY(""),
    CALCULATE_MONEY_EXCHANGE(""),
    UPDATE_GUARRANTEED_BALANCE(""),
    ON_PLAYER_BUY_IN(""),
    ON_PLAYER_WITH_DRAW(""),
    KICK_USER_DISCONNECTED("");

    private String description;
    GameStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return this.description;
    }
}