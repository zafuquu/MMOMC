package com.zafuquu.mmomc;

/** Pure RPG formulas kept separate from Minecraft runtime classes for deterministic unit testing. */
public final class RpgFormulas {
    private RpgFormulas() {}

    public static int maxManaForInt(int intelligence) {
        return 50 + Math.max(0, intelligence - 15) * 3;
    }

    /** EXP required to advance from the supplied level, matching the live progression curve. */
    public static long experienceToNextLevel(int level) {
        if (level < 1) throw new IllegalArgumentException("level must be at least 1");
        return 100L + (level * 50L) + (level * (long) level * 5L);
    }
}
