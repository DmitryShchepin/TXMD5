package com.mario.random.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class GeneratedResultMessageResponse {

    public static final long DEFAULT_MAINTENANCE_BUFFER = 120000L; // 2 minutes in milliseconds

    @JsonProperty("session_id")
    private String sessionID;

    @JsonProperty("round_id")
    private String roundID;

    @JsonProperty("result")
    private String result;

    @JsonProperty("hash_result")
    private String hashResult;

    @JsonProperty("detail")
    private String detail;

    @JsonProperty("service_close_at")
    private long serviceCloseAt;

    @JsonProperty("service_close_duration")
    private long serviceCloseDuration;

    public boolean isMaintenanceNotPlanned() {
        return serviceCloseDuration <= 0 || serviceCloseAt <= 0;
    }

    public boolean isUnderMaintenance() {
        if (isMaintenanceNotPlanned()) {
            return false;
        }
        return isMaintenanceIn();
    }

    private boolean isMaintenanceIn() {
        if (isMaintenanceNotPlanned()) {
            return false;
        }
        long currentTime = System.currentTimeMillis();
        return currentTime < serviceCloseAt + serviceCloseDuration &&
                serviceCloseAt - currentTime <= DEFAULT_MAINTENANCE_BUFFER;
    }
}
