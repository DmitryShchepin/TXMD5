package com.taixiu.unbalanced.core.betting;

import com.taixiu.unbalanced.core.player.TaiXiuUnbalancedPlayer;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class BettingManager {
    private Map<BettingEntry, BettingValue> bettings;
    private long gameBank;
    private final long minGameBank;
    private long maxGameBank;
    private float taxRate = 0.01F;

    private Set<TaiXiuUnbalancedPlayer> allBettedPlayers;

    public BettingManager(long gameBank, long minGameBank, long maxGameBank, float taxRate) {
        this.gameBank = gameBank;
        this.minGameBank = minGameBank;
        this.bettings = new ConcurrentHashMap<>();
        this.allBettedPlayers = new HashSet<>();
        BettingEntry[] entries = BettingEntry.values();

        for (BettingEntry entry : entries) {
            this.bettings.put(entry, new BettingValue());
        }

        this.taxRate = taxRate;
    }

    public void reset() {
        for (BettingEntry entry : BettingEntry.values()) {
            bettings.get(entry).reset();
        }
        allBettedPlayers.clear();
    }

    public synchronized void addBetting(TaiXiuUnbalancedPlayer player, BettingEntry entry, long value) {
        player.addBetting(entry, value);
        allBettedPlayers.add(player);
        bettings.get(entry).addBetting(player, value);
        gameBank += value;
    }

    public synchronized void addBetting(TaiXiuUnbalancedPlayer player, Map<BettingEntry, Long> bets) {
        bets.forEach((key, value) -> this.addBetting(player, key, value));
    }

    public boolean canPayout(BettingEntry result) {
        long totalPayout = this.getBettingEntry(result) * 2L;
        return this.gameBank - totalPayout + this.minGameBank > 0L;
    }

    public BettingEntry getEntryMinBetting() {
        List<BettingEntry> resultSortedKey = this.bettings.entrySet().stream()
                .sorted(Entry.comparingByValue()).map(Entry::getKey)
                .collect(Collectors.toList());
        return resultSortedKey.get(0);
    }

    public Set<TaiXiuUnbalancedPlayer> payout(BettingEntry result) {
        Set<TaiXiuUnbalancedPlayer> players = this.getPlayersBetEntry(result);

        for (TaiXiuUnbalancedPlayer player : players) {
            long betting = player.getBetting(result);
            if (betting > 0L) {
                long tax = this.computeTax(betting);
                long payout = betting + betting - tax;
                player.changeWinMoney(payout);
                player.changeTax(tax);
                this.gameBank -= payout;
            }
        }

        return players;
    }

    public long computeTax(long winMoney) {
        return Math.round((float) winMoney * this.taxRate);
    }

    public long getBettingEntry(BettingEntry entry) {
        return this.bettings.get(entry).getValue();
    }

    public int getBetCountEntry(BettingEntry entry) {
        return this.bettings.get(entry).getCount();
    }

    public Set<TaiXiuUnbalancedPlayer> getPlayersBetEntry(BettingEntry entry) {
        return this.bettings.get(entry).getPlayers();
    }

    public Map<BettingEntry, BettingValue> getBettings() {
        return this.bettings;
    }

    public long getGameBank() {
        return this.gameBank;
    }

    public long getMinGameBank() {
        return this.minGameBank;
    }

    public long getMaxGameBank() {
        return this.maxGameBank;
    }

    public float getTaxRate() {
        return this.taxRate;
    }

    public Set<TaiXiuUnbalancedPlayer> getAllBettedPlayers() {
        return allBettedPlayers;
    }

    public void payout(TaiXiuUnbalancedPlayer player, BettingEntry entryResult) {
        long betting = player.getBetting(entryResult);
        if (betting > 0L) {
            long tax = this.computeTax(betting);
            long payout = betting + betting - tax;
            player.changeWinMoney(payout);
            player.changeTax(tax);
            this.gameBank -= payout;
        }
    }
}
