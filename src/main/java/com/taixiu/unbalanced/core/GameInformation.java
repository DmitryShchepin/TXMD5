package com.taixiu.unbalanced.core;


import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Builder
@Getter
@ToString
public class GameInformation {
    private final int gameId;
    private final String gameName;
    private final String gameCode;
    private final String gameDbTable;
    private final String hazelcastSessionIdSeed;
    private final int codeCommandOffset;
}
