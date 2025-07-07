package com.taixiu.unbalanced.core.shakator;

import com.taixiu.unbalanced.core.shakator.randomization.RandomManager;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RandomStrategyTaiXiuShakator implements Shakator {

    private final RandomManager randomManager;

    public RandomStrategyTaiXiuShakator() {
        this.randomManager = new RandomManager();
    }

    public ShakingResult doShake(ShakingContext context) {
        Dice dice = new Dice(randomManager.nextInt(6, false) + 1,
                randomManager.nextInt(6, false) + 1,
                randomManager.nextInt(6, false) + 1);
        return new TaiXiuUnbalancedShakingResult(dice);
    }
}
