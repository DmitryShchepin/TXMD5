package com.taixiu.unbalanced.core;

import lombok.Data;

import java.util.UUID;

@Data
public class UAItem {

    private String accessToken;
    private String referenceId;
    private String userId;
    private String username;
    private long exchangeValue;
    private String changeAssetName;
    private String backupAssetName;
    private TransactionInfo transactionInfo;


    public UAItem(String accessToken, String userId, String username, long exchangeValue, String changeAssetName, String backupAssetName,
                  TransactionInfo transactionInfo) {
        this.accessToken = accessToken;
        this.userId = userId;
        this.exchangeValue = exchangeValue;
        this.username = username;
        this.referenceId = UUID.randomUUID().toString();
        this.setChangeAssetName(changeAssetName);
        this.setBackupAssetName(backupAssetName);

        String action = TransactionAction.WIN.name();
        transactionInfo.setTransactionAction(action);
        this.transactionInfo = transactionInfo;
    }

}
