package com.taixiu.unbalanced.core;

import java.util.List;

public interface DatalayerAdapter {

	Account changeMoney(UserAndExchangeValue userAsset, GameActionInfo gameActionInfo);
	
	boolean changeMoney(List<UserAndExchangeValue> userAssets, GameActionInfo transactionInfo);
	
	long getNewUserAsset(String userId, String accessToken, String assetType);

	String createLogId();

	void appendLog(String logId, String content, int type);

	void appendLog(String logId, String content);


	void logTax(long tax, String logId);

	List<Account> changeGuarraneteeBalanceBySplittedPacks(int packSize, List<UserAndExchangeValue> userAssets, boolean isNotGuarrantee,
														  GameActionInfo gameActionInfo, String gameStatus);

	List<Account> changeGuarraneteeBalance(List<UserAndExchangeValue> userAssets, boolean isNotGuarrantee,
										   GameActionInfo gameActionInfo, String gameStatus);

	Account updateGuarranteedAccount(String userId, long money, GameActionInfo gameActionInfo, TransactionInfo transactionInfo, String gameStatus);

	Account transfer(String userId, long minMoney, long expectedMoney, GameActionInfo gameActionInfo, TransactionInfo transactionInfo,
					 String gameStatus);

	Account rechargeMainAccount(String userId, long value, GameActionInfo gameActionInfo, TransactionInfo transactionInfo, String gameStatus);

}
