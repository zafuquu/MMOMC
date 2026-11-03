package com.zafuquu.mmomc;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

/**
 * Server-owned RPG data stored in the player's persistent entity data.
 * Keep all mutations on the logical server; client UI will synchronize read-only snapshots later.
 */
public final class RpgData {
    private RpgData() {}

    private static final String ROOT = "mmomc";
    private static final int MAX_LEVEL = 120;
    private static final int STAT_POINTS_PER_LEVEL = 3;

    public enum Stat {
        STR("str", "STR"),
        STA("sta", "STA"),
        DEX("dex", "DEX"),
        INT("int", "INT");

        public final String key;
        public final String label;

        Stat(String key, String label) {
            this.key = key;
            this.label = label;
        }

        public static Stat parse(String input) {
            if (input == null) return null;
            for (Stat stat : values()) {
                if (stat.key.equalsIgnoreCase(input) || stat.name().equalsIgnoreCase(input)) {
                    return stat;
                }
            }
            return null;
        }
    }

    public static CompoundTag data(ServerPlayer player) {
        CompoundTag persistent = player.getPersistentData();
        if (!persistent.contains(ROOT, Tag.TAG_COMPOUND)) {
            CompoundTag fresh = new CompoundTag();
            fresh.putInt("level", 1);
            fresh.putInt("stat_str", 15);
            fresh.putInt("stat_sta", 15);
            fresh.putInt("stat_dex", 15);
            fresh.putInt("stat_int", 15);
            fresh.putInt("stat_points", 0);
            fresh.putLong("experience", 0L);
            fresh.putInt("mana", 50);
            fresh.putLong("skill_cooldown_power_strike", 0L);
            fresh.putLong("skill_cooldown_heal", 0L);
            fresh.putLong("skill_cooldown_arcane_bolt", 0L);
            fresh.putString("job", "beginner");
            fresh.putString("specialization", "");
            persistent.put(ROOT, fresh);
        }

        // Safe defaults also migrate player data created by earlier development builds.
        CompoundTag tag = persistent.getCompound(ROOT);
        if (!tag.contains("level")) tag.putInt("level", 1);
        if (!tag.contains("stat_str")) tag.putInt("stat_str", 15);
        if (!tag.contains("stat_sta")) tag.putInt("stat_sta", 15);
        if (!tag.contains("stat_dex")) tag.putInt("stat_dex", 15);
        if (!tag.contains("stat_int")) tag.putInt("stat_int", 15);
        if (!tag.contains("stat_points")) tag.putInt("stat_points", 0);
        if (!tag.contains("experience")) tag.putLong("experience", 0L);
        if (!tag.contains("mana")) tag.putInt("mana", 50 + Math.max(0, tag.getInt("stat_int") - 15) * 3);
        if (!tag.contains("job")) tag.putString("job", "beginner");
        if (!tag.contains("specialization")) tag.putString("specialization", "");
        return tag;
    }

    public static int level(ServerPlayer player) {
        return Math.max(1, data(player).getInt("level") == 0 ? 1 : data(player).getInt("level"));
    }

    public static long experience(ServerPlayer player) {
        return Math.max(0L, data(player).getLong("experience"));
    }

    public static int stat(ServerPlayer player, Stat stat) {
        return Math.max(0, data(player).getInt("stat_" + stat.key));
    }

    public static int maxMana(ServerPlayer player) { return 50 + Math.max(0, stat(player, Stat.INT) - 15) * 3; }

    public static int mana(ServerPlayer player) { return Math.max(0, Math.min(maxMana(player), data(player).getInt("mana"))); }

    public static void setMana(ServerPlayer player, int value) { data(player).putInt("mana", Math.max(0, Math.min(maxMana(player), value))); }

    public static int availableStatPoints(ServerPlayer player) {
        return Math.max(0, data(player).getInt("stat_points"));
    }

    public static String job(ServerPlayer player) {
        String value = data(player).getString("job");
        return value.isBlank() ? "beginner" : value;
    }

    public static String specialization(ServerPlayer player) {
        return data(player).getString("specialization");
    }

    public static long experienceToNextLevel(ServerPlayer player) {
        int level = level(player);
        if (level >= MAX_LEVEL) return 0;
        return 100L + (level * 50L) + (level * (long) level * 5L);
    }

    public static boolean addExperience(ServerPlayer player, long amount) {
        if (amount <= 0 || level(player) >= MAX_LEVEL) return false;

        CompoundTag tag = data(player);
        long total = Math.max(0L, tag.getLong("experience")) + amount;
        int currentLevel = level(player);
        boolean leveledUp = false;

        while (currentLevel < MAX_LEVEL) {
            long required = experienceToNextLevelAt(currentLevel);
            if (total < required) break;
            total -= required;
            currentLevel++;
            tag.putInt("stat_points", tag.getInt("stat_points") + STAT_POINTS_PER_LEVEL);
            leveledUp = true;
        }

        tag.putLong("experience", total);
        tag.putInt("level", currentLevel);
        return leveledUp;
    }

    private static long experienceToNextLevelAt(int level) {
        return 100L + (level * 50L) + (level * (long) level * 5L);
    }

    public static boolean allocate(ServerPlayer player, Stat stat, int points) {
        if (points <= 0 || availableStatPoints(player) < points) return false;
        CompoundTag tag = data(player);
        tag.putInt("stat_" + stat.key, stat(player, stat) + points);
        tag.putInt("stat_points", availableStatPoints(player) - points);
        return true;
    }

    public static boolean chooseJob(ServerPlayer player, String jobId) {
        if (level(player) < 10 || !job(player).equals("beginner")) return false;
        if (!Job.isFirstJob(jobId)) return false;
        data(player).putString("job", jobId);
        return true;
    }

    public static boolean chooseSpecialization(ServerPlayer player, String specializationId) {
        if (level(player) < 50 || job(player).equals("beginner") || !specialization(player).isBlank()) return false;
        if (!Job.specializationBelongsTo(job(player), specializationId)) return false;
        data(player).putString("specialization", specializationId);
        return true;
    }

    public static String displayJob(ServerPlayer player) {
        String spec = specialization(player);
        return titleCase(spec.isBlank() ? job(player) : spec);
    }

    public static String titleCase(String id) {
        if (id == null || id.isBlank()) return "Beginner";
        String[] words = id.replace('_', ' ').split(" ");
        StringBuilder result = new StringBuilder();
        for (String word : words) {
            if (word.isBlank()) continue;
            if (!result.isEmpty()) result.append(' ');
            result.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
        }
        return result.toString();
    }

    public static void copyProgression(Entity original, Entity replacement) {
        CompoundTag source = original.getPersistentData();
        if (source.contains(ROOT, Tag.TAG_COMPOUND)) {
            replacement.getPersistentData().put(ROOT, source.getCompound(ROOT).copy());
        }
    }

    public static int maxLevel() {
        return MAX_LEVEL;
    }
}
