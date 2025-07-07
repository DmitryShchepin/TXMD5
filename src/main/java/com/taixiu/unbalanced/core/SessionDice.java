package com.taixiu.unbalanced.core;

import com.taixiu.unbalanced.core.shakator.Dice;

public class SessionDice {
   private long sessionId;
   private Dice dice;

   public long getSessionId() {
      return this.sessionId;
   }

   public Dice getDice() {
      return this.dice;
   }

   public void setSessionId(long sessionId) {
      this.sessionId = sessionId;
   }

   public void setDice(Dice dice) {
      this.dice = dice;
   }

   public SessionDice(long sessionId, Dice dice) {
      this.sessionId = sessionId;
      this.dice = dice;
   }
}
