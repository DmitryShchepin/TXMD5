package com.taixiu.unbalanced.core.shakator.randomization.strategies;

import com.taixiu.unbalanced.core.shakator.randomization.EntropySource;
import com.taixiu.unbalanced.core.shakator.randomization.RandomStrategy;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PoolStrategy implements RandomStrategy {
    private final List<Integer> pool = new ArrayList<>();
    private int pointer;

    public PoolStrategy(int size) {
        for (int i = 0; i < size; i++) {
            pool.add(i);
        }
    }

    @Override
    public int nextInt(int bound, EntropySource entropy) {
        if (pointer >= pool.size()) {
            chaosReshuffle(entropy);
            pointer = 0;
        }
        return pool.get(pointer++) % bound;
    }

    private void chaosReshuffle(EntropySource entropy) {
        SecureRandom random = new SecureRandom();
        random.setSeed(entropy.getSeed());
        Collections.shuffle(pool, random);
        for (int i = 0; i < 3; i++) {
            Collections.swap(pool,
                    entropy.getThreadCount() % pool.size(),
                    entropy.getCpuCores() % pool.size());
        }
    }

    @Override
    public void reseed(EntropySource entropy) {
        chaosReshuffle(entropy);
    }
}