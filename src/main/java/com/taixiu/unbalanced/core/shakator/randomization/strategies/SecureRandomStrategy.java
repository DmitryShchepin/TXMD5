package com.taixiu.unbalanced.core.shakator.randomization.strategies;

import com.taixiu.unbalanced.core.shakator.randomization.EntropySource;
import com.taixiu.unbalanced.core.shakator.randomization.RandomStrategy;

import java.security.SecureRandom;

public class SecureRandomStrategy implements RandomStrategy {

    private final SecureRandom secureRandom = new SecureRandom();

    @Override
    public int nextInt(int bound, EntropySource entropy) {
        return secureRandom.nextInt(bound);
    }

    @Override
    public void reseed(EntropySource entropy) {
        secureRandom.setSeed(entropy.getSeed());
    }
}