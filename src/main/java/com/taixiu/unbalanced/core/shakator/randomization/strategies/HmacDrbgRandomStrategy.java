package com.taixiu.unbalanced.core.shakator.randomization.strategies;

import com.taixiu.unbalanced.core.shakator.randomization.EntropySource;
import com.taixiu.unbalanced.core.shakator.randomization.RandomStrategy;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.crypto.Digest;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.macs.HMac;
import org.bouncycastle.crypto.prng.EntropySourceProvider;
import org.bouncycastle.crypto.prng.drbg.HMacSP800DRBG;
import org.bouncycastle.crypto.prng.drbg.SP80090DRBG;

import java.security.SecureRandom;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class HmacDrbgRandomStrategy implements RandomStrategy {

    private SP80090DRBG drbg;
    private final Digest digest = new SHA256Digest();
    private final AtomicInteger reseedCounter = new AtomicInteger(0);
    private final Object lock = new Object();

    @Override
    public int nextInt(int bound, EntropySource entropy) {
        if (bound <= 0) {
            throw new IllegalArgumentException("Bound must be positive");
        }

        byte[] output = new byte[4];
        synchronized (lock) {
            if (drbg == null) {
                log.debug("DRBG is null. Reseeding before generating random number.");
                reseed(entropy);
            }

            int val;
            // Upper limit for unbiased modulo reduction
            int limit = Integer.MAX_VALUE - (Integer.MAX_VALUE % bound);

            do {
                drbg.generate(output, null, false);
                val = ((output[0] & 0xFF) << 24) |
                        ((output[1] & 0xFF) << 16) |
                        ((output[2] & 0xFF) << 8) |
                        (output[3] & 0xFF);
                val = val == Integer.MIN_VALUE ? 0 : Math.abs(val);

            } while (val >= limit);

            return val % bound;
        }
    }

    @Override
    public void reseed(EntropySource entropySource) {
        log.debug("Starting reseed with entropy source: {}", entropySource.getClass().getSimpleName());
        byte[] seed = createSeed(entropySource);
        synchronized (lock) {
            drbg = new HMacSP800DRBG(
                    new HMac(digest),
                    digest.getDigestSize() * 8,
                    new CustomEntropyProvider().get(256),
                    seed,
                    null
            );
            int count = reseedCounter.incrementAndGet();
            log.debug("DRBG reseeded. New reseed count: {}", count);
        }
    }

    public int getReseedCount() {
        int count = reseedCounter.get();
        log.debug("Reseed count queried: {}", count);
        return count;
    }

    private byte[] createSeed(EntropySource entropy) {
        long e1 = entropy.getCompositeSeed();
        long e2 = entropy.getSeed();
        long e3 = entropy.getNanoTime();
        long e4 = System.currentTimeMillis();

        log.debug("Creating seed using entropy values: e1={}, e2={}, e3={}, time={}", e1, e2, e3, e4);

        byte[] seed = new byte[32];
        for (int i = 0; i < 8; i++) {
            seed[i] = (byte) ((e1 >> (i * 8)) & 0xFF);
            seed[8 + i] = (byte) ((e2 >> (i * 8)) & 0xFF);
            seed[16 + i] = (byte) ((e3 >> (i * 8)) & 0xFF);
        }

        seed[24] = (byte) entropy.getThreadCount();
        seed[25] = (byte) entropy.getCpuCores();
        seed[26] = (byte) (e4 & 0xFF);
        seed[27] = (byte) ((e4 >> 8) & 0xFF);

        byte[] temp = new byte[4];
        new SecureRandom().nextBytes(temp);
        System.arraycopy(temp, 0, seed, 28, 4);

        SHA256Digest digest = new SHA256Digest();
        digest.update(seed, 0, seed.length);
        byte[] finalSeed = new byte[digest.getDigestSize()];
        digest.doFinal(finalSeed, 0);

        log.debug("Final seed (SHA-256 hash of input): {}", Arrays.toString(finalSeed));

        return finalSeed;
    }

    static class CustomEntropyProvider implements EntropySourceProvider {
        private static final SecureRandom SECURE_RANDOM = new SecureRandom();

        @Override
        public org.bouncycastle.crypto.prng.EntropySource get(final int bitsRequired) {

            return new org.bouncycastle.crypto.prng.EntropySource() {
                @Override
                public boolean isPredictionResistant() {
                    return true;
                }

                @Override
                public byte[] getEntropy() {
                    byte[] seed = new byte[bitsRequired / 8];
                    SECURE_RANDOM.nextBytes(seed);
                    log.debug("Generated {} bits of secure entropy", bitsRequired);
                    return seed;
                }

                @Override
                public int entropySize() {
                    return bitsRequired;
                }
            };
        }
    }
}