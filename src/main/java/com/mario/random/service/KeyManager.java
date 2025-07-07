package com.mario.random.service;

public interface KeyManager {

    byte[] getAesKey(boolean forced) throws Exception;
}
