package com.zafuquu.mmomc;

/** Exact, deterministic party EXP splitting with no reward creation or loss. */
public final class RpgPartyRewards {
    private RpgPartyRewards() {}

    /** The first remainder members receive one extra point. */
    public static long[] split(long totalReward, int memberCount) {
        if (totalReward < 0) throw new IllegalArgumentException("totalReward cannot be negative");
        if (memberCount < 1) throw new IllegalArgumentException("memberCount must be positive");
        long base = totalReward / memberCount;
        int remainder = (int) (totalReward % memberCount);
        long[] shares = new long[memberCount];
        for (int i = 0; i < memberCount; i++) shares[i] = base + (i < remainder ? 1 : 0);
        return shares;
    }
}
