package com.zafuquu.mmomc;

/**
 * Pure equipment progression model. Runtime item components and inventory transactions
 * consume this model; keeping the rules independent makes them safe to test offline.
 */
public final class RpgEquipment {
    public enum Slot {
        WEAPON, HELMET, CHEST, LEGS, BOOTS, GLOVES, RING, NECKLACE
    }

    public enum Rarity {
        COMMON(0, 0), UNCOMMON(1, 2), RARE(2, 4), EPIC(3, 7), LEGENDARY(4, 11), MYTHIC(5, 16);

        private final int tier;
        private final int bonusPercent;

        Rarity(int tier, int bonusPercent) {
            this.tier = tier;
            this.bonusPercent = bonusPercent;
        }

        public int tier() { return tier; }
        public int bonusPercent() { return bonusPercent; }
    }

    public record Stats(int strength, int stamina, int dexterity, int intelligence,
                        int attack, int defense, int health, int mana) {
        public Stats {
            if (strength < 0 || stamina < 0 || dexterity < 0 || intelligence < 0
                    || attack < 0 || defense < 0 || health < 0 || mana < 0) {
                throw new IllegalArgumentException("Equipment stats cannot be negative");
            }
        }

        public Stats plus(Stats other) {
            return new Stats(strength + other.strength, stamina + other.stamina,
                    dexterity + other.dexterity, intelligence + other.intelligence,
                    attack + other.attack, defense + other.defense,
                    health + other.health, mana + other.mana);
        }

        public Stats scalePercent(int percent) {
            if (percent < 0) throw new IllegalArgumentException("percent cannot be negative");
            return new Stats(percentOf(strength, percent), percentOf(stamina, percent),
                    percentOf(dexterity, percent), percentOf(intelligence, percent),
                    percentOf(attack, percent), percentOf(defense, percent),
                    percentOf(health, percent), percentOf(mana, percent));
        }

        private static int percentOf(int value, int percent) {
            return (int) (((long) value * percent) / 100L);
        }
    }

    public record ItemDefinition(String id, Slot slot, Rarity rarity, int requiredLevel, Stats baseStats) {
        public ItemDefinition {
            if (id == null || !id.matches("[a-z0-9_]+")) throw new IllegalArgumentException("Invalid item id");
            if (slot == null || rarity == null || baseStats == null) throw new IllegalArgumentException("Missing item metadata");
            if (requiredLevel < 1 || requiredLevel > 120) {
                throw new IllegalArgumentException("requiredLevel must be between 1 and 120");
            }
        }

        public Stats effectiveStats(int refinementLevel) {
            int rarityBonus = rarity.bonusPercent();
            int refineBonus = RpgRefinement.statBonusPercent(refinementLevel);
            return baseStats.plus(baseStats.scalePercent(rarityBonus + refineBonus));
        }

        public boolean canEquipAtLevel(int playerLevel) {
            return playerLevel >= requiredLevel;
        }
    }

    private RpgEquipment() {}
}
