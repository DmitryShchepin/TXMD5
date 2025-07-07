package com.taixiu.unbalanced.core.player;

import com.taixiu.unbalanced.core.betting.BettingEntry;
import lombok.Setter;
import lombok.ToString;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@ToString
public class TaiXiuUnbalancedPlayer extends AbstractPlayer {

	public static final String BETTING = "betting";
	public static final String BATCH_BETTING = "batchBetting";
	@Setter
    private String sessionId;
	@Setter
    private Map<BettingEntry, Long> bettings = new HashMap<>();
	private final Map<BettingEntry, Long> allBettingUpdatedTime = new HashMap<>();

	private long bettingUpdatedTime;

	@Setter
    private long winMoney;
	@Setter
    private String bettingId;
	@Setter
    private boolean sqe;
	@Setter
    private boolean allIn;

	public void addBetting(BettingEntry entry, long value) {
		if (!this.bettings.containsKey(entry)) {
			this.bettings.put(entry, 0L);
		}

		this.bettings.put(entry, this.bettings.get(entry) + value);
		this.bettingUpdatedTime = System.currentTimeMillis();
		this.allBettingUpdatedTime.put(entry, this.bettingUpdatedTime);
	}

	public long getBetting(BettingEntry entry) {
		if (!this.bettings.containsKey(entry)) {
			return 0;
		}
		return this.bettings.get(entry);
	}

	public void changeWinMoney(long payout) {
		this.winMoney += payout;
	}

	public void bet(long sessionId, int assetId, int entryId, long value) {
		PlayerEvent event = new PlayerEvent("betting", this);
		event.put("assetId", assetId);
		event.put("entryId", entryId);
		event.put("betting", value);
		event.put("sessionId", sessionId);

		dispatchEvent(event);
	}

	public long getTotalBet() {
		return bettings.values().stream().mapToLong(Long::valueOf).sum();
	}

	public String getSessionId() {
		return this.sessionId;
	}

	public Map<BettingEntry, Long> getBettings() {
		return this.bettings;
	}

	public Map<BettingEntry, Long> getAllBettingUpdatedTime() {
		return allBettingUpdatedTime;
	}

	public long getWinMoney() {
		return this.winMoney;
	}

	public String getBettingId() {
		return this.bettingId;
	}

    public String getBettingEntryNames() {
		return bettings.keySet().stream().map(BettingEntry::getName).collect(Collectors.joining(", "));
	}

	public long getBettingUpdatedTime() {
		return bettingUpdatedTime;
	}

	public boolean isSqe() {
		return sqe;
	}

    public boolean isAllIn() {
		return allIn;
	}
}
