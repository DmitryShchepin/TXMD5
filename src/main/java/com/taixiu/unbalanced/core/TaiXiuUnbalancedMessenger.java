package com.taixiu.unbalanced.core;

import com.taixiu.unbalanced.core.betting.BettingManager;
import com.taixiu.unbalanced.core.player.TaiXiuUnbalancedPlayer;
import com.taixiu.unbalanced.core.shakator.TaiXiuUnbalancedShakingResult;

import java.util.Map;
import java.util.Set;

public interface TaiXiuUnbalancedMessenger {
   void sendError(String ssid, TaiXiuUnbalancedError error);

   void bettingSuccess(TaiXiuUnbalancedPlayer player, BettingManager bettingManager);

   void endGame(BettingManager bm, long sessionId, TaiXiuUnbalancedShakingResult result, String resultText, String md5,
                TaiXiuUnbalancedPlayer player, Map<String, Object> extraData);

   void endGame(BettingManager bm, long sessionId, TaiXiuUnbalancedShakingResult result, String resultText, String md5,
                Set<TaiXiuUnbalancedPlayer> allPlayers, Map<String, Object> extraData);

   void startBetting(long sessionId, String resultMd5);

   void updateBetting(TaiXiuUnbalancedGameManager gm);
}
