package com.zafuquu.mmomc;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

public final class RpgEvents {
    private RpgEvents() {}

    public static void onLivingDeath(LivingDeathEvent event) {
        if (!(event.getSource().getEntity() instanceof ServerPlayer player)) return;
        if (!(event.getEntity() instanceof LivingEntity victim)) return;
        if (victim == player || victim.getType().getCategory().isFriendly()) return;

        // Keep early-game EXP modest; elite/boss multipliers will be introduced with the loot system.
        long reward = Math.max(10L, Math.round(victim.getMaxHealth() * 2.0));
        RpgParty.distributeExperience(player, reward);
    }

    public static void onPlayerClone(PlayerEvent.Clone event) {
        // NeoForge persistent entity data is not automatically copied when a player respawns after death.
        if (!event.isWasDeath()) return;
        RpgData.copyProgression(event.getOriginal(), event.getEntity());
    }
}
