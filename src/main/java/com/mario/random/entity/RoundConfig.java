package com.mario.random.entity;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import lombok.With;

@Builder
@Getter
@With
@ToString
public class RoundConfig {

    private final String gameId;
    private final String studioGameId;
    private final String roundId;
    private final String gameName;
    private final GameType gameType;

    private final long roundInSeconds;
    private final long bufferInSeconds;
    private final long md5PassLength;
    private final long md5PassEmojiLength;

    private final String sessionId;

    private final boolean enableHistoryLogging;

    private final boolean forceReconnect;
}
