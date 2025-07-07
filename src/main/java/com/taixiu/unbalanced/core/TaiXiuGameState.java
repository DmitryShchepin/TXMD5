package com.taixiu.unbalanced.core;

public enum TaiXiuGameState implements GameState {
   WAITING_FOR_START(1),
   BETTING(2),
   PAYOUT(3);

   private int state;

   private TaiXiuGameState(int state) {
      this.state = state;
   }

   public int getState() {
      return this.state;
   }

   public boolean isPlaying() {
      return this == BETTING || this == PAYOUT;
   }

   public boolean isPrepareToStart() {
      return this == WAITING_FOR_START;
   }
}
