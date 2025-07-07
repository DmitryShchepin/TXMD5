package com.taixiu.unbalanced.core;


import com.taixiu.unbalanced.core.exception.TransactionNotStartedException;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class AssetTransactionManager {

	private String roundId;
	
	private final Map<String, List<String>> userIdTicketIdMap = new HashMap<>();
	
	public String getRoundId() {
		if (this.roundId == null) {
//			throw new TransactionNotStartedException();
			this.roundId = generateNewId();
		}
		
		return this.roundId;
	}
	
	public String getOrCreateFirstTicketIdByUserId(String userId) {
		if (this.roundId == null) {
			throw new TransactionNotStartedException();
		}
		
		if (!userIdTicketIdMap.containsKey(userId)) {
			List<String> listTicket = new LinkedList<>();
			listTicket.add(generateNewId());
			userIdTicketIdMap.put(userId, listTicket);
		}
		return ((LinkedList<String>) userIdTicketIdMap.get(userId)).getFirst();
	}

//	public void startGame() {
//		this.roundId = generateNewId();
//		this.userIdTicketIdMap.clear();
//	}
	
	public void endGame() {
		this.roundId = null;
		this.userIdTicketIdMap.clear();
	}
	
	private static String generateNewId() {
		return UUID.randomUUID().toString();
	}

	public boolean isStarted() {
		return this.roundId != null;
	}
}
