package me.seyfu_t.util;

public class MT19937Random {
    private static final int N = 624;
    private static final int M = 397;
    private static final long MATRIX_A = 0xb5026f5aa96619e9L;
    private static final long UPPER_MASK = 0xffffffff80000000L;
    private static final long LOWER_MASK = 0x7fffffffL;

    private long[] mt = new long[N];
    private int mti = N + 1;

    public MT19937Random(long seed) {
            mt[0] = seed;
            for (mti = 1; mti < N; mti++) {
                mt[mti] = (6364136223846793005L * (mt[mti-1] ^ (mt[mti-1] >>> 62)) + mti);
            }
        }

    private void twist() {
        for (int i = 0; i < N; i++) {
            long x = (mt[i] & UPPER_MASK) | (mt[(i + 1) % N] & LOWER_MASK);
            long xA = x >>> 1;
            if ((x & 1) != 0) {
                xA ^= MATRIX_A;
            }
            mt[i] = mt[(i + M) % N] ^ xA;
        }
        mti = 0;
    }

    public long nextLong() {
        if (mti >= N) {
            twist();
        }

        long y = mt[mti++];

        // Tempering
        y ^= (y >>> 29) & 0x5555555555555555L;
        y ^= (y << 17) & 0x71d67fffeda60000L;
        y ^= (y << 37) & 0xfff7eee000000000L;
        y ^= (y >>> 43);

        return y;
    }

    public int nextInt() {
        return (int) (nextLong() >>> 1);
    }

    public int nextInt(int bound) {
        return (int) ((nextLong() >>> 1) % bound);
    }
}
