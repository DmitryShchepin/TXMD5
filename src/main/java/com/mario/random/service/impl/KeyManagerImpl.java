package com.mario.random.service.impl;

import com.mario.random.AesKeyException;
import com.mario.random.interceptors.GameIdClientInterceptor;
import com.mario.random.service.KeyManager;
import com.mario.random.utils.AESUtils;
import com.mario.random.utils.RSAUtils;
import io.grpc.Channel;
import lombok.extern.slf4j.Slf4j;
import random_generator.GeneratorServiceGrpc;
import random_generator.Random;

import java.security.PublicKey;
import java.util.Base64;

@Slf4j
public class KeyManagerImpl implements KeyManager {

    private final String gameId;
    private final Channel channel;

    private byte[] aesKey;


    public KeyManagerImpl(String gameId, Channel channel) {
        log.debug("KeyManagerImpl initialization");
        this.gameId = gameId;
        this.channel = channel;
    }

    @Override
    public byte[] getAesKey(boolean forced) throws Exception {
        if (aesKey == null || forced) {
            generateAesKey();
        }
        return aesKey;
    }

    private void generateAesKey() throws Exception {
        log.debug("Trying to generate AES key");
        aesKey = null;

        GeneratorServiceGrpc.GeneratorServiceBlockingStub blockingStub = GeneratorServiceGrpc.newBlockingStub(channel)
                .withInterceptors(new GameIdClientInterceptor(gameId));

        log.debug("GeneratorServiceGrpc opened for AES key generation");

        String publicKey = null;
        String aesKeyEncrypted = null;
        try {
            // Get the public key
            Random.GetPublicKeyRequest publicKeyRequest = Random.GetPublicKeyRequest.newBuilder().build();
            Random.GetPublicKeyResponse publicKeyResponse = blockingStub.getPublicKey(publicKeyRequest);

            publicKey = publicKeyResponse.getPublicKey();
            aesKeyEncrypted = publicKeyResponse.getAesKey();
        } catch (Exception e) {
            log.error("Public key not found: {}", e.getMessage());
        }

        boolean aesKeyExists = false;

        if (aesKeyEncrypted != null && !aesKeyEncrypted.isEmpty()) {
            log.info("AES Key created");
            aesKeyExists = true;
            aesKey = Base64.getDecoder().decode(aesKeyEncrypted);
        }

        if (!aesKeyExists) {
            if (publicKey == null) {
                log.debug("Public key not found");
                aesKey = null;
                throw new AesKeyException("Public key not found");
            }

            PublicKey serverPublicKey = RSAUtils.parsePublicKey(publicKey);
            log.info("Received Server Public Key");

            // Generate AES key
            aesKey = AESUtils.generateAESKey();
            log.info("Generated AES Key: " + Base64.getEncoder().encodeToString(aesKey));

            // Encrypt AES key with server's public key
            byte[] encryptedAESKey = RSAUtils.encryptWithPublicKey(aesKey, serverPublicKey);

            // Send encrypted AES key to the server
            String encryptedAESKeyBase64 = Base64.getEncoder().encodeToString(encryptedAESKey);
            log.info("Encrypted AES Key: " + encryptedAESKeyBase64);

            Random.ExchangePublicKeyRequest exchangeRequest = Random.ExchangePublicKeyRequest.newBuilder()
                    .setAesKeyEncrypted(encryptedAESKeyBase64)
                    .build();

            Random.ExchangePublicKeyResponse exchangeResponse = blockingStub.exchangePublicKey(exchangeRequest);
            log.info("Exchange Key Result: " + exchangeResponse.getSuccess());
        }
    }
}