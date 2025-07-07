package com.taixiu.unbalanced.core.shakator.randomization.strategies;

import com.taixiu.unbalanced.core.shakator.randomization.EntropySource;
import com.taixiu.unbalanced.core.shakator.randomization.RandomStrategy;

// Environmental Noise
public class EnvironmentalStrategy implements RandomStrategy {

    @Override
    public int nextInt(int bound, EntropySource entropy) {
        long seed = entropy.getCompositeSeed();
        return (int) (seed % bound);
    }

    @Override
    public void reseed(EntropySource entropy) {
        // Environmental strategy is stateless
    }
}