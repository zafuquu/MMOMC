package com.zafuquu.mmomc;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/**
 * Converts the four allocated RPG attributes into server-authoritative combat stats.
 *
 * Values are intentionally conservative so the RPG layer complements vanilla combat
 * rather than replacing its progression. All calculations are deterministic and
 * are recalculated on the logical server.
 */
public final class RpgCombat {
    private RpgCombat() {}

    private static final double BASE_HEALTH = 20.0;
    private static final double BASE_ATTACK_DAMAGE = 1.0;
    private static final double BASE_MOVEMENT_SPEED = 0.1;
    private static final double BASE_ATTACK_SPEED = 4.0;
    private static final double BASE_ARMOR = 0.0;

    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (player.tickCount % 20 != 0) return;
        applyAttributes(player);
    }

    public static void applyAttributes(ServerPlayer player) {
        int str = RpgData.stat(player, RpgData.Stat.STR);
        int sta = RpgData.stat(player, RpgData.Stat.STA);
        int dex = RpgData.stat(player, RpgData.Stat.DEX);\n\n        if (player.tickCount % 20 == 0 && RpgData.mana(player) < RpgData.maxMana(player)) {\n            RpgData.setMana(player, RpgData.mana(player) + 1 + Math.max(0, RpgData.stat(player, RpgData.Stat.INT) - 15) / 20);\n        }

        // Each point above the starting value contributes a small, readable benefit.
        double maxHealth = BASE_HEALTH + Math.max(0, sta - 15) * 0.5;
        double attackDamage = BASE_ATTACK_DAMAGE + Math.max(0, str - 15) * 0.15;
        double movementSpeed = BASE_MOVEMENT_SPEED + Math.min(100, Math.max(0, dex - 15)) * 0.0002;
        double attackSpeed = BASE_ATTACK_SPEED + Math.min(100, Math.max(0, dex - 15)) * 0.01;
        double armor = BASE_ARMOR + Math.min(100, Math.max(0, sta - 15)) * 0.1;

        setBaseValue(player, Attributes.MAX_HEALTH, maxHealth);
        setBaseValue(player, Attributes.ATTACK_DAMAGE, attackDamage);
        setBaseValue(player, Attributes.MOVEMENT_SPEED, movementSpeed);
        setBaseValue(player, Attributes.ATTACK_SPEED, attackSpeed);
        setBaseValue(player, Attributes.ARMOR, armor);
    }

    private static void setBaseValue(ServerPlayer player,
            net.minecraft.core.Holder<net.minecraft.world.entity.ai.attributes.Attribute> attribute,
            double value) {
        AttributeInstance instance = player.getAttribute(attribute);
        if (instance == null) return;
        if (Math.abs(instance.getBaseValue() - value) > 0.000001) {
            instance.setBaseValue(value);
        }
    }
}
