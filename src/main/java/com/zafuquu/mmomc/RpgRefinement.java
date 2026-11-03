package com.zafuquu.mmomc;

/**
 * Deterministic rules for the planned +1..+20 equipment refinement system.
 *
 * This class intentionally has no Minecraft dependencies. Item persistence, material
 * consumption, and the actual upgrade transaction are integrated separately so these
 * rules can be tested without launching a game.
 */
public final class RpgRefinement {
    public static final int MIN_LEVEL = 0;
    public static final int MAX_LEVEL = 20;
    public static final int PITY_FAILURE_LIMIT = 10;
    private static final int[] BASE_SUCCESS_PERCENT = {
            100, 100, 100, 100, 90, 90, 75, 75, 75, 55,
            55, 35, 35, 35, 20, 20, 10, 10, 10, 5
    };

    private RpgRefinement() {}

    /** Chance to reach targetLevel; target level is 1..20. */
    public static int successChancePercent(int targetLevel, int consecutiveFailures) {
        validateTarget(targetLevel);
        if (consecutiveFailures < 0) throw new IllegalArgumentException("consecutiveFailures cannot be negative");
        if (consecutiveFailures >= PITY_FAILURE_LIMIT) return 100;
        int base = BASE_SUCCESS_PERCENT[targetLevel - 1];
        return Math.min(100, base + consecutiveFailures * 5);
    }

    /** Aura tier is granted at +5, +10, +15, and +20. */
    public static int auraTier(int refinementLevel) {
        validateLevel(refinementLevel);
        return refinementLevel / 5;
    }

    /** Percentage bonus applied to the refined item's eligible base stats. */
    public static int statBonusPercent(int refinementLevel) {
        validateLevel(refinementLevel);
        return refinementLevel * 2;
    }

    public static boolean canRefine(int refinementLevel) {
        return refinementLevel >= MIN_LEVEL && refinementLevel < MAX_LEVEL;
    }

    private static void validateTarget(int targetLevel) {
        if (targetLevel < 1 || targetLevel > MAX_LEVEL) {
            throw new IllegalArgumentException("targetLevel must be between 1 and " + MAX_LEVEL);
        }
    }

    private static void validateLevel(int level) {
        if (level < MIN_LEVEL || level > MAX_LEVEL) {
            throw new IllegalArgumentException("refinementLevel must be between 0 and " + MAX_LEVEL);
        }
    }
}
