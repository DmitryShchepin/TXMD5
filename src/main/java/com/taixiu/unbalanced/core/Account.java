package com.taixiu.unbalanced.core;

public class Account {
	public static final String THREE_CARD_BANKING_ID = "26c8d537-a7f1-4fd9-bf45-1c176978ce16";
	public static final String THREE_CARD_BANKING_NAME = "three_card_banking";

	private String userId;
	private long mainBalance;
	private long guarranteedBalance;
	private String changeAssetName;

	private String transactionId;
	
	public Account(String userId, long mainBalanace, long guarranteedBalance) {
		this.userId = userId;
		this.mainBalance = mainBalanace;
		this.guarranteedBalance = guarranteedBalance;
	}

	public Account(String userId, long mainBalanace, long guarranteedBalance, String changeAssetName) {
		this.userId = userId;
		this.mainBalance = mainBalanace;
		this.guarranteedBalance = guarranteedBalance;
		this.changeAssetName = changeAssetName;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public long getGuarranteedBalance() {
		return guarranteedBalance;
	}

	public void setGuarranteedBalance(long guarranteedBalance) {
		this.guarranteedBalance = guarranteedBalance;
	}

	public long getMainBalance() {
		return mainBalance;
	}

	public void setMainBalance(long mainBalance) {
		this.mainBalance = mainBalance;
	}

	public long getTotalBalance() {
		return this.mainBalance + this.guarranteedBalance;
	}

	public String getChangeAssetName() {
		return changeAssetName;
	}

	public void setChangeAssetName(String changeAssetName) {
		this.changeAssetName = changeAssetName;
	}

	public String getTransactionId() {
		return transactionId;
	}

	public void setTransactionId(String transactionId) {
		this.transactionId = transactionId;
	}
	
}
