package com.zafuquu.mmomc;

/** Pure combat-skill scaling formulas for deterministic balance tests. */
public final class RpgSkillFormulas {
    private RpgSkillFormulas() {}

    public static double powerStrikeDamage(int strength) {
        requireStat(strength);
        return 5.0 + Math.max(0, strength - 15) * 0.35;
    }

    public static double arcaneBoltDamage(int intelligence) {
        requireStat(intelligence);
        return 4.0 + Math.max(0, intelligence - 15) * 0.6;
    }

    public static double specializationDamage(int primaryStat, double baseDamage, double multiplier) {
        requireStat(primaryStat);
        if (!Double.isFinite(baseDamage) || baseDamage < 0
                || !Double.isFinite(multiplier) || multiplier < 0) {
            throw new IllegalArgumentException("Damage and multiplier must be finite and non-negative");
        }
        return baseDamage + Math.max(0, primaryStat - 15) * multiplier;
    }

    public static double healAmount(int intelligence, boolean assistRole) {
        requireStat(intelligence);
        return (assistRole ? 10.0 : 6.0)
                + Math.max(0, intelligence - 15) * (assistRole ? 0.4 : 0.2);
    }

    private static void requireStat(int value) {
        if (value < 0) throw new IllegalArgumentException("Stat cannot be negative");
    }
}
