package com.zafuquu.mmomc;

/** Pure progression formulas kept separate from Minecraft runtime classes for unit testing. */
public final class RpgFormulas {
    private RpgFormulas() {}

    public static int maxManaForInt(int intelligence) {
        return 50 + Math.max(0, intelligence - 15) * 3;
    }
}
