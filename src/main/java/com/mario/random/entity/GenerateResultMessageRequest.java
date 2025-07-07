package com.mario.random.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@AllArgsConstructor
@ToString
public class GenerateResultMessageRequest {
    @JsonProperty("studio_game_id")
    private String studioGameID;

    @JsonProperty("game_id")
    private String gameID;

    @JsonProperty("session_id")
    private String sessionID;

    @JsonProperty("round_id")
    private String roundID;

    @JsonProperty("round_in_seconds")
    private long roundInSeconds;

    @JsonProperty("buffer_in_seconds")
    private long bufferInSeconds;

    @JsonProperty("game_type")
    private String gameType;

    @JsonProperty("md5_pass_length")
    private long md5PassLength;

    @JsonProperty("md5_pass_emoji_length")
    private long md5PassEmojiLength;

    public GenerateResultMessageRequest(RoundConfig gameConfig) {
        this.sessionID = gameConfig.getSessionId();
        this.studioGameID = gameConfig.getStudioGameId();
        this.gameID = gameConfig.getGameId();
        this.roundInSeconds = gameConfig.getRoundInSeconds();
        this.bufferInSeconds = gameConfig.getBufferInSeconds();
        this.gameType = gameConfig.getGameType() != null ? gameConfig.getGameType().name() : GameType.GAME_TYPE_UNKNOWN.name();
        this.md5PassLength = gameConfig.getMd5PassLength();
        this.md5PassEmojiLength = gameConfig.getMd5PassEmojiLength();
        this.roundID = gameConfig.getRoundId();
    }
}
