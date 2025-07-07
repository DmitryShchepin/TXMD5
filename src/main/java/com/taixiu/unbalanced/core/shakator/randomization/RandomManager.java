package com.taixiu.unbalanced.core.shakator.randomization;


import com.taixiu.unbalanced.core.shakator.randomization.strategies.EnvironmentalStrategy;
import com.taixiu.unbalanced.core.shakator.randomization.strategies.HmacDrbgRandomStrategy;
import com.taixiu.unbalanced.core.shakator.randomization.strategies.SecureRandomStrategy;
import com.taixiu.unbalanced.core.shakator.randomization.strategies.XorShiftStrategy;
import lombok.Getter;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

public class RandomManager extends Random {

    public static final Duration ROTATION_STRATEGY_THRESHOLD = Duration.ofMillis(500); // 500ms

    private final List<RandomStrategy> strategies;
    private final EntropySource entropy;
    private final AtomicLong lastSwitch;
    @Getter
    private volatile RandomStrategy currentStrategy;

    public RandomManager() {
        strategies = Arrays.asList(
                new SecureRandomStrategy(),
                new XorShiftStrategy(),
//                new PoolStrategy(1000),
                new HmacDrbgRandomStrategy(),
                new EnvironmentalStrategy()
        );
        entropy = createEntropySource();
        currentStrategy = strategies.get(0);
        lastSwitch = new AtomicLong(entropy.getNanoTime());
    }

    protected EntropySource createEntropySource() {
        return new EntropySource();
    }

    public int nextInt(int bound, boolean negativeAllowed) {
        if (shouldRotateStrategy()) {
            rotateStrategy();
        }
        int result = currentStrategy.nextInt(bound, entropy);
        if (!negativeAllowed) {
            if (result == Integer.MIN_VALUE) {
                result = 0;
            } else {
                result = Math.abs(result);
            }
        }
        return result;
    }

    public int nextInt(int bound) {
        if (shouldRotateStrategy()) {
            rotateStrategy();
        }
        return currentStrategy.nextInt(bound, entropy);
    }

    private boolean shouldRotateStrategy() {
        final long currentTime = entropy.getNanoTime();
        final long timeSinceLastSwitch = currentTime - lastSwitch.get();
        return timeSinceLastSwitch > ROTATION_STRATEGY_THRESHOLD.getNano();
    }

    private void rotateStrategy() {
        long seed = entropy.getCompositeSeed();
        if (seed == Long.MIN_VALUE) {
            seed = 0;
        }
        final int newIndex = (int) (Math.abs(seed) % strategies.size());
        currentStrategy = strategies.get(newIndex);
        currentStrategy.reseed(entropy);
        lastSwitch.set(entropy.getNanoTime());
    }
}
