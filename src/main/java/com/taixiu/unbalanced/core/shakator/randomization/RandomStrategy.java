package com.taixiu.unbalanced.core.shakator.randomization;

public interface RandomStrategy {

    int nextInt(int bound, EntropySource entropy);

    void reseed(EntropySource entropy);
}
