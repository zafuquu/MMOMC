package com.zafuquu.mmomc;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/**
 * Applies RPG-derived bonuses as stable transient modifiers.
 *
 * This deliberately leaves vanilla and other-mod base attribute values untouched.
 * Modifiers are replaced by their stable IDs, never stacked on each tick, and are
 * rebuilt after login/respawn from the authoritative RPG profile.
 */
public final class RpgCombat {
    private RpgCombat() {}

    private static final ResourceLocation HEALTH_ID = id("rpg_health");
    private static final ResourceLocation ATTACK_ID = id("rpg_attack");
    private static final ResourceLocation SPEED_ID = id("rpg_speed");
    private static final ResourceLocation ATTACK_SPEED_ID = id("rpg_attack_speed");
    private static final ResourceLocation ARMOR_ID = id("rpg_armor");

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MMOMC.MOD_ID, path);
    }

    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (player.tickCount % 20 != 0) return;
        applyAttributes(player);
    }

    public static void applyAttributes(ServerPlayer player) {
        RpgEquipment.Stats gear = RpgEquipmentData.load(player).totalStats();
        int str = RpgData.stat(player, RpgData.Stat.STR) + gear.strength();
        int sta = RpgData.stat(player, RpgData.Stat.STA) + gear.stamina();
        int dex = RpgData.stat(player, RpgData.Stat.DEX) + gear.dexterity();

        if (player.tickCount % 20 == 0 && RpgData.mana(player) < RpgData.maxMana(player)) {
            RpgData.setMana(player, RpgData.mana(player)
                    + 1 + Math.max(0, RpgData.stat(player, RpgData.Stat.INT) - 15) / 20);
        }

        // Preserve the vanilla base and every unrelated modifier.
        setBonus(player, Attributes.MAX_HEALTH, HEALTH_ID, Math.max(0, sta - 15) * 0.5 + gear.health());
        setBonus(player, Attributes.ATTACK_DAMAGE, ATTACK_ID, Math.max(0, str - 15) * 0.15 + gear.attack());
        setBonus(player, Attributes.MOVEMENT_SPEED, SPEED_ID,
                Math.min(100, Math.max(0, dex - 15)) * 0.0002);
        setBonus(player, Attributes.ATTACK_SPEED, ATTACK_SPEED_ID,
                Math.min(100, Math.max(0, dex - 15)) * 0.01);
        setBonus(player, Attributes.ARMOR, ARMOR_ID,
                Math.min(100, Math.max(0, sta - 15)) * 0.1 + gear.defense());
    }

    private static void setBonus(ServerPlayer player,
            net.minecraft.core.Holder<net.minecraft.world.entity.ai.attributes.Attribute> attribute,
            ResourceLocation id, double amount) {
        AttributeInstance instance = player.getAttribute(attribute);
        if (instance == null) return;
        if (amount <= 0.0) {
            instance.removeModifier(id);
        } else {
            instance.addOrUpdateTransientModifier(
                    new AttributeModifier(id, amount, AttributeModifier.Operation.ADD_VALUE));
        }
    }
}
