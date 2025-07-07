package com.mario.random.entity;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import lombok.With;

import java.util.Date;

@Builder
@Getter
@With
@ToString
public class RandomResultHistoryLog {

    private final String id;
    private final String studioId;
    private final String gameId;
    private final String gameName;
    private final String sessionId;

    private final String request;

    private final String detail;

    private final String result;
    private final String resultHash;
    private final String resultMD5;
    private final String gameType;
    private final Long studioGameResult;

    private final boolean isMaintenanceMode;
    private final Long maintainAt;
    private final Long maintainDuration;

    private final String errorMessage;

    private final Date createdAt;
    private final Date updatedAt;
    private final Status status;

    private final Long sessionStartsAt;
    private final Long sessionEndsAt;
    private final Boolean ended;
    private final String roundId;

    // Game results

    public enum Status {
        NEW, RESULT_REQUESTED, HASH_RESULT_GENERATED, MD5_RESULT_GENERATED, COMPLETED, MAINTENANCE, ERROR, UNKNOWN
    }
}