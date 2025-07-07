package com.taixiu.unbalanced.core;

import lombok.Data;

@Data
public class GameActionInfo {

    private String transactionAction;

    private String gameId;
    private String gameName;

    public GameActionInfo(String gameId, String gameName) {
    	this.gameId = gameId;
    	this.gameName = gameName;
    }
}
