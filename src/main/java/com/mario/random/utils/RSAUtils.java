package com.mario.random.utils;

import lombok.extern.slf4j.Slf4j;

import javax.crypto.Cipher;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Slf4j
public class RSAUtils {

    private static final String PKCS1PrivateHeader = "-----BEGIN RSA PRIVATE KEY-----";
    private static final String PKCS1PrivateFooter = "-----END RSA PRIVATE KEY-----";
    private static final String PKCS1PublicHeader = "-----BEGIN RSA PUBLIC KEY-----";
    private static final String PKCS1PublicFooter = "-----END RSA PUBLIC KEY-----";

    private static final String PKCS8PrivateHeader = "-----BEGIN PRIVATE KEY-----";
    private static final String PKCS8PrivateFooter = "-----END PRIVATE KEY-----";
    private static final String PKCS8PublicHeader = "-----BEGIN PUBLIC KEY-----";
    private static final String PKCS8PublicFooter = "-----END PUBLIC KEY-----";

    private static byte[] bytes(int... elements) {
        byte[] result = new byte[elements.length];
        for (int i = 0; i < elements.length; i++) {
            result[i] = (byte) elements[i];
        }
        return result;
    }

    public static byte[] encryptWithPublicKey(byte[] data, PublicKey publicKey) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        return cipher.doFinal(data);
    }

    private static byte[] joinBytes(byte[] a, byte[] b) {
        byte[] bytes = new byte[a.length + b.length];
        System.arraycopy(a, 0, bytes, 0, a.length);
        System.arraycopy(b, 0, bytes, a.length, b.length);
        return bytes;
    }

    // PKCS#1 To PKCS#8 Helpers
    // =========================================================================

    private static PrivateKey parsePkcs8PrivateKey(byte[] pkcs8Bytes) throws GeneralSecurityException {
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(pkcs8Bytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePrivate(keySpec);
    }

    private static PublicKey parsePkcs8PublicKey(byte[] pkcs8Bytes) throws GeneralSecurityException {
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(pkcs8Bytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePublic(keySpec);
    }

    private static PrivateKey parsePkcs1PrivateKey(byte[] pkcs1Bytes) throws GeneralSecurityException {
        int pkcs1Length = pkcs1Bytes.length;
        int totalLength = pkcs1Length + 22;
        byte[] pkcs8Header = bytes(
                // Sequence + total length
                0x30, 0x82, ((totalLength >> 8) & 0xff), (totalLength & 0xff),
                // Integer (0) (Version)
                0x2, 0x1, 0x0,
                // Sequence: 1.2.840.113549.1.1.1, NULL
                0x30, 0xD, 0x6, 0x9, 0x2A, 0x86, 0x48, 0x86, 0xF7, 0xD, 0x1, 0x1, 0x1, 0x5, 0x0,
                // Octet string + length
                0x4, 0x82, ((pkcs1Length >> 8) & 0xff), (pkcs1Length & 0xff)
        );
        byte[] pkcs8bytes = joinBytes(pkcs8Header, pkcs1Bytes);
        return parsePkcs8PrivateKey(pkcs8bytes);
    }

    private static PublicKey parsePkcs1PublicKey(byte[] pkcs1Bytes) throws GeneralSecurityException {
        int pkcs1Length = pkcs1Bytes.length + 1;
        int totalLength = pkcs1Length + 19;
        byte[] pkcs8Header = bytes(
                // Sequence + total length
                0x30, 0x82, ((totalLength >> 8) & 0xff), (totalLength & 0xff),
                // Sequence: 1.2.840.113549.1.1.1, NULL
                0x30, 0xD, 0x6, 0x9, 0x2A, 0x86, 0x48, 0x86, 0xF7, 0xD, 0x1, 0x1, 0x1, 0x5, 0x0,
                // Bit string + length
                0x3, 0x82, ((pkcs1Length >> 8) & 0xff), (pkcs1Length & 0xff),
                // Pad bits
                0x00
        );
        byte[] pkcs8bytes = joinBytes(pkcs8Header, pkcs1Bytes);
        return parsePkcs8PublicKey(pkcs8bytes);
    }

    public static PrivateKey parsePrivateKey(final String key) throws GeneralSecurityException {
        String string = key.replaceAll("\\n", "");
        if (string.startsWith(PKCS8PrivateHeader) && string.endsWith(PKCS8PrivateFooter)) {
            string = string.replace(PKCS8PrivateHeader, "").replace(PKCS8PrivateFooter, "");
            byte[] pkcs8Bytes = Base64.getDecoder().decode(string);
            return parsePkcs8PrivateKey(pkcs8Bytes);
        }
        if (string.startsWith(PKCS1PrivateHeader) && string.endsWith(PKCS1PrivateFooter)) {
            string = string.replace(PKCS1PrivateHeader, "").replace(PKCS1PrivateFooter, "");
            byte[] pkcs1Bytes = Base64.getDecoder().decode(string);
            return parsePkcs1PrivateKey(pkcs1Bytes);
        }
        throw new InvalidKeyException();
    }

    public static PublicKey parsePublicKey(String key) throws GeneralSecurityException {
        String string = key.replaceAll("\\n", "");
        if (string.startsWith(PKCS8PublicHeader) && string.endsWith(PKCS8PublicFooter)) {
            string = string.replace(PKCS8PublicHeader, "").replace(PKCS8PublicFooter, "");
            byte[] pkcs8Bytes = Base64.getDecoder().decode(string);
            return parsePkcs8PublicKey(pkcs8Bytes);
        }
        if (string.startsWith(PKCS1PublicHeader) && string.endsWith(PKCS1PublicFooter)) {
            string = string.replace(PKCS1PublicHeader, "").replace(PKCS1PublicFooter, "");
            byte[] pkcs1Bytes = Base64.getDecoder().decode(string);
            return parsePkcs1PublicKey(pkcs1Bytes);
        }
        throw new InvalidKeyException();
    }
}
