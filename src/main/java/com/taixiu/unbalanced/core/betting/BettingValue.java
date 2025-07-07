package com.taixiu.unbalanced.core.betting;

import com.taixiu.unbalanced.core.player.TaiXiuUnbalancedPlayer;

import java.util.HashSet;
import java.util.Set;

public class BettingValue implements Comparable<BettingValue> {
   private long value;
   private int count;
   private Set<TaiXiuUnbalancedPlayer> players = new HashSet();

   public void addBetting(TaiXiuUnbalancedPlayer player, long betting) {
      this.value += betting;
      ++this.count;
      this.players.add(player);
   }

   public int compareTo(BettingValue o) {
      if (this.getValue() != o.getValue()) {
         return this.getValue() > o.getValue() ? 1 : -1;
      } else {
         return 0;
      }
   }

   public void reset() {
      this.value = 0L;
      this.count = 0;
      this.players.clear();
   }

   public long getValue() {
      return this.value;
   }

   public int getCount() {
      return this.count;
   }

   public Set<TaiXiuUnbalancedPlayer> getPlayers() {
      return this.players;
   }

   public void setValue(long value) {
      this.value = value;
   }

   public void setCount(int count) {
      this.count = count;
   }

   public void setPlayers(Set<TaiXiuUnbalancedPlayer> players) {
      this.players = players;
   }
}
