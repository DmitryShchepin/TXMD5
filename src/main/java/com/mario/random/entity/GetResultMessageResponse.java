package com.mario.random.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GetResultMessageResponse {
    @JsonProperty("status")
    private String status;

    @JsonProperty("result")
    private String result;
}