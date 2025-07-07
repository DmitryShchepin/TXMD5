package com.taixiu.unbalanced.core.shakator;


import lombok.Getter;

@Getter
public class TaiXiuUnbalancedShakingContext implements ShakingContext {
   private long sessionId;

    public void setSessionId(long sessionId) {
      this.sessionId = sessionId;
   }

   public TaiXiuUnbalancedShakingContext(long sessionId) {
      this.sessionId = sessionId;
   }
}
