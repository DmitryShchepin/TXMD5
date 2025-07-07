package com.mario.random.service.impl;

public enum RandomStrategy {

    External, Internal, Unknown;

    public String getShortName() {
        return name().toLowerCase().substring(0,1);
    }
}
