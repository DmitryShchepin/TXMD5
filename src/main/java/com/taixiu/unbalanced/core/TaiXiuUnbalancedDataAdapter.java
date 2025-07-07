package com.taixiu.unbalanced.core;

import com.taixiu.unbalanced.core.player.TaiXiuUnbalancedPlayer;
import com.taixiu.unbalanced.core.randomizer.RandomServiceManager;
import com.taixiu.unbalanced.core.security.ResultProcessor;
import com.taixiu.unbalanced.core.shakator.Dice;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface TaiXiuUnbalancedDataAdapter {


    void updatePlayerBetting(TaiXiuUnbalancedPlayer player, long sid);

    List<Account> changeMoney(long sessionId, List<UAItem> items, String gameStatus);

    void updateBettings(Set<TaiXiuUnbalancedPlayer> players, long ssid);

    void updateSession(Session session, String roundId, Dice resultDice, ResultProcessor resultProcessor,
                       RandomServiceManager randomServiceManager);

    void updateSession(Session session, Map<String, Object> updates);

    Session createSession(String roundId);

}
