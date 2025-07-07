package com.taixiu.unbalanced.core;

import java.util.UUID;

public class UserAndExchangeValue {

    private String referenceId;
    private String userId;
    private String username;
    private long exchangeValue;
    private String assetName;

    private final TransactionInfo transactionInfo;

    public UserAndExchangeValue(String userId, String username, long exchangeValue, String refereceId, TransactionInfo transactionInfo) {
        this.userId = userId;
        this.exchangeValue = exchangeValue;
        this.username = username;
        this.referenceId = refereceId;
        this.transactionInfo = transactionInfo;
    }

    public UserAndExchangeValue(String userId, String username, String assetName, long exchangeValue, TransactionInfo transactionInfo) {
        this.userId = userId;
        this.exchangeValue = exchangeValue;
        this.username = username;
        this.referenceId = UUID.randomUUID().toString();
        this.assetName = assetName;
        this.transactionInfo = transactionInfo;
    }

    public UserAndExchangeValue(String userId, String username, long exchangeValue, TransactionInfo transactionInfo) {
        this.userId = userId;
        this.exchangeValue = exchangeValue;
        this.username = username;
        this.referenceId = UUID.randomUUID().toString();
        this.transactionInfo = transactionInfo;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public long getExchangeValue() {
        return exchangeValue;
    }

    public void setExchangeValue(long exchangeValue) {
        this.exchangeValue = exchangeValue;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(String referenceId) {
        this.referenceId = referenceId;
    }

    public String getAssetName() {
        return assetName;
    }

    public void setAssetName(String assetName) {
        this.assetName = assetName;
    }

    public TransactionInfo getTransactionInfo() {
        return transactionInfo;
    }
}
