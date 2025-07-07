package com.mario.random.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GetResultMessageRequest {
    @JsonProperty("studio_game_id")
    private String studioGameID;

    @JsonProperty("game_id")
    private String gameID;

    @JsonProperty("session_id")
    private String sessionID;
}
