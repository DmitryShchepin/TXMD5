package com.mario.random.utils;

import com.nhb.common.data.PuObjectRO;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ConfigParametersUtil {
    public static int validateAndGetInteger(PuObjectRO params, String param, int defaultValue) {
        if (!params.variableExists(param)) {
            log.warn("Parameter '{}' not found, use default parameter: {}", params, defaultValue);
            return defaultValue;
        }
        return params.getInteger(param, defaultValue);
    }

    public static String validateAndGetString(PuObjectRO params, String param) {
        if (!params.variableExists(param)) {
            log.warn("Parameter '{}' and default parameter not found. Can not initiate service", params);
            throw new RuntimeException(String.format("Parameter %s and default parameter not found. Can not initiate service", param));
        }
        return params.getString(param);
    }

    public static String validateAndGetString(PuObjectRO params, String param, String defaultValue) {
        if (!params.variableExists(param)) {
            log.warn("Parameter '{}' not found, use default parameter: {}", params, defaultValue);
            return defaultValue;
        }
        return params.getString(param, defaultValue);
    }
}
