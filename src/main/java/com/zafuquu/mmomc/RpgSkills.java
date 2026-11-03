package com.zafuquu.mmomc;

import java.util.Comparator;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;

/** First playable skill set. Mana and cooldowns are stored in the player's RPG data. */
public final class RpgSkills {
    private RpgSkills() {}

    public static int list(ServerPlayer player) {
        player.sendSystemMessage(Component.literal("§6§lMMOMC Skills"));
        player.sendSystemMessage(Component.literal("§ePower Strike §7- 12 mana, 4s cooldown, STR-scaled close-range strike."));
        player.sendSystemMessage(Component.literal("§aHeal §7- 18 mana, 8s cooldown, restores health; Assist gets stronger healing."));
        player.sendSystemMessage(Component.literal("§bArcane Bolt §7- 15 mana, 3s cooldown, INT-scaled magic damage."));
        player.sendSystemMessage(Component.literal("§dClass skills: blade_fury, iron_guard, hawkeye, elemental_surge, blessing."));
        player.sendSystemMessage(Component.literal("§dSpecializations: blade_dance, berserker_rage, paladin_aegis, guardian_fortress, ranger_volley, sharpshooter_focus, elementor_meteor, psykeeper_blast, ringmaster_sanctuary, billposter_combo."));
        player.sendSystemMessage(Component.literal("§7Use /mmomc skill cast <skill_id>. Skills require the matching job or specialization."));
        return 1;
    }

    public static int cast(ServerPlayer player, String rawSkill) {
        String skill = rawSkill.toLowerCase(java.util.Locale.ROOT);
        return switch (skill) {
            case "power_strike" -> powerStrike(player);
            case "heal" -> heal(player);
            case "arcane_bolt" -> arcaneBolt(player);
            case "blade_fury" -> classBuff(player, skill, "swordsman", 20, 200, MobEffects.DAMAGE_BOOST, 1, "Blade Fury");
            case "iron_guard" -> classBuff(player, skill, "tank", 18, 240, MobEffects.DAMAGE_RESISTANCE, 1, "Iron Guard");
            case "hawkeye" -> classBuff(player, skill, "archer", 16, 240, MobEffects.MOVEMENT_SPEED, 1, "Hawkeye");
            case "elemental_surge" -> classBuff(player, skill, "magician", 24, 240, MobEffects.DAMAGE_BOOST, 1, "Elemental Surge");
            case "blessing" -> classBuff(player, skill, "assist", 22, 300, MobEffects.REGENERATION, 1, "Blessing");
            case "blade_dance" -> specializationAttack(player, skill, "blade_master", 24, 160, 5.0, 0.85, "Blade Dance");
            case "berserker_rage" -> specializationBuff(player, skill, "berserker", 22, 300, MobEffects.DAMAGE_BOOST, 1, "Berserker Rage");
            case "paladin_aegis" -> specializationBuff(player, skill, "paladin", 20, 300, MobEffects.DAMAGE_RESISTANCE, 1, "Paladin Aegis");
            case "guardian_fortress" -> specializationBuff(player, skill, "guardian", 24, 360, MobEffects.DAMAGE_RESISTANCE, 2, "Guardian Fortress");
            case "ranger_volley" -> specializationAttack(player, skill, "ranger", 20, 120, 12.0, 0.65, "Ranger Volley");
            case "sharpshooter_focus" -> specializationBuff(player, skill, "sharpshooter", 20, 300, MobEffects.MOVEMENT_SPEED, 1, "Sharpshooter Focus");
            case "elementor_meteor" -> specializationAttack(player, skill, "elementor", 30, 240, 16.0, 1.0, "Elementor Meteor");
            case "psykeeper_blast" -> specializationAttack(player, skill, "psykeeper", 28, 200, 14.0, 0.95, "Psykeeper Blast");
            case "ringmaster_sanctuary" -> specializationPartyHeal(player, skill);
            case "billposter_combo" -> specializationAttack(player, skill, "billposter", 22, 120, 4.0, 0.9, "Billposter Combo");
            default -> {
                player.sendSystemMessage(Component.literal("§cUnknown skill. Use /mmomc skill list."));
                yield 0;
            }
        };
    }

    private static int powerStrike(ServerPlayer player) {
        if (!ready(player, "power_strike", 12, 80)) return 0;
        LivingEntity target = nearestTarget(player, 4.0);
        if (target == null) {
            player.sendSystemMessage(Component.literal("§cNo hostile target within 4 blocks."));
            return 0;
        }
        int str = RpgData.stat(player, RpgData.Stat.STR);
        float damage = (float) (5.0 + Math.max(0, str - 15) * 0.35);
        target.hurt(player.damageSources().playerAttack(player), damage);
        spend(player, "power_strike", 12, 80);
        player.sendSystemMessage(Component.literal("§6Power Strike! §f" + damage + " damage."));
        return 1;
    }

    private static int arcaneBolt(ServerPlayer player) {
        if (!ready(player, "arcane_bolt", 15, 60)) return 0;
        LivingEntity target = nearestTarget(player, 12.0);
        if (target == null) {
            player.sendSystemMessage(Component.literal("§cNo hostile target within 12 blocks."));
            return 0;
        }
        int intelligence = RpgData.stat(player, RpgData.Stat.INT);
        float damage = (float) (4.0 + Math.max(0, intelligence - 15) * 0.6);
        target.hurt(player.damageSources().indirectMagic(player, player), damage);
        spend(player, "arcane_bolt", 15, 60);
        player.sendSystemMessage(Component.literal("§bArcane Bolt! §f" + damage + " magic damage."));
        return 1;
    }

    private static int heal(ServerPlayer player) {
        if (!ready(player, "heal", 18, 160)) return 0;
        ServerPlayer target = player.server.getPlayerList().getPlayers().stream()
                .filter(candidate -> candidate != player && RpgParty.canSupport(player, candidate))
                .filter(candidate -> candidate.getHealth() < candidate.getMaxHealth() - 0.01f)
                .filter(candidate -> player.distanceToSqr(candidate) <= 64.0)
                .filter(player::hasLineOfSight)
                .min(Comparator.comparingDouble(player::distanceToSqr))
                .orElse(null);
        if (target == null && player.getHealth() < player.getMaxHealth() - 0.01f) target = player;
        if (target == null) {
            player.sendSystemMessage(Component.literal("§eNo injured party member within 8 blocks."));
            return 0;
        }
        boolean assist = RpgData.job(player).equals("assist")
                || RpgData.specialization(player).equals("ringmaster")
                || RpgData.specialization(player).equals("billposter");
        float amount = (float) (assist ? 10 + Math.max(0, RpgData.stat(player, RpgData.Stat.INT) - 15) * 0.4
                : 6 + Math.max(0, RpgData.stat(player, RpgData.Stat.INT) - 15) * 0.2);
        float missing = target.getMaxHealth() - target.getHealth();
        target.heal(amount);
        spend(player, "heal", 18, 160);
        player.sendSystemMessage(Component.literal("§aHeal restored §f" + Math.min(amount, missing)
                + "§a health to " + target.getGameProfile().getName() + "."));
        if (target != player) target.sendSystemMessage(Component.literal("§a" + player.getGameProfile().getName() + " healed you."));
        return 1;
    }



    private static int specializationAttack(ServerPlayer player, String skill, String requiredSpec,
            int cost, int cooldownTicks, double range, double multiplier, String displayName) {
        if (!RpgData.specialization(player).equals(requiredSpec)) {
            player.sendSystemMessage(Component.literal("§cThis skill requires the " + RpgData.titleCase(requiredSpec) + " specialization."));
            return 0;
        }
        if (!ready(player, skill, cost, cooldownTicks)) return 0;
        LivingEntity target = nearestTarget(player, range);
        if (target == null) {
            player.sendSystemMessage(Component.literal("§cNo hostile target within " + (int) range + " blocks."));
            return 0;
        }
        double primary = requiredSpec.equals("elementor") || requiredSpec.equals("psykeeper")
                ? RpgData.stat(player, RpgData.Stat.INT) : RpgData.stat(player, RpgData.Stat.STR);
        float damage = (float) (6.0 + Math.max(0, primary - 15) * multiplier);
        target.hurt(player.damageSources().playerAttack(player), damage);
        spend(player, skill, cost, cooldownTicks);
        player.sendSystemMessage(Component.literal("§d" + displayName + "! §f" + damage + " damage."));
        return 1;
    }

    private static int specializationBuff(ServerPlayer player, String skill, String requiredSpec,
            int cost, int cooldownTicks, net.minecraft.core.Holder<net.minecraft.world.effect.MobEffect> effect,
            int amplifier, String displayName) {
        if (!RpgData.specialization(player).equals(requiredSpec)) {
            player.sendSystemMessage(Component.literal("§cThis skill requires the " + RpgData.titleCase(requiredSpec) + " specialization."));
            return 0;
        }
        if (!ready(player, skill, cost, cooldownTicks)) return 0;
        player.addEffect(new MobEffectInstance(effect, 20 * 20, amplifier));
        spend(player, skill, cost, cooldownTicks);
        player.sendSystemMessage(Component.literal("§d" + displayName + " activated for 20 seconds."));
        return 1;
    }

    private static int specializationPartyHeal(ServerPlayer player, String skill) {
        if (!RpgData.specialization(player).equals("ringmaster")) {
            player.sendSystemMessage(Component.literal("§cThis skill requires the Ringmaster specialization."));
            return 0;
        }
        if (!ready(player, skill, 30, 300)) return 0;
        int healed = 0;
        for (ServerPlayer ally : player.server.getPlayerList().getPlayers()) {
            if (!RpgParty.canSupport(player, ally) || player.distanceToSqr(ally) > 256.0) continue;
            if (ally.getHealth() >= ally.getMaxHealth() - 0.01f) continue;
            ally.heal(12.0f + Math.max(0, RpgData.stat(player, RpgData.Stat.INT) - 15) * 0.4f);
            healed++;
            if (ally != player) ally.sendSystemMessage(Component.literal("§aRingmaster Sanctuary restored your health."));
        }
        if (healed == 0) {
            player.sendSystemMessage(Component.literal("§eNo injured party members within 16 blocks."));
            return 0;
        }
        spend(player, skill, 30, 300);
        player.sendSystemMessage(Component.literal("§aSanctuary healed " + healed + " party member(s)."));
        return 1;
    }

    private static int classBuff(ServerPlayer player, String skill, String requiredJob,
            int cost, int cooldownTicks, net.minecraft.core.Holder<net.minecraft.world.effect.MobEffect> effect,
            int amplifier, String displayName) {
        if (!RpgData.job(player).equals(requiredJob)) {
            player.sendSystemMessage(Component.literal("§cThis skill requires the " + RpgData.titleCase(requiredJob) + " job."));
            return 0;
        }
        if (!ready(player, skill, cost, cooldownTicks)) return 0;
        if (skill.equals("blessing")) {
            int affected = 0;
            for (ServerPlayer ally : player.server.getPlayerList().getPlayers()) {
                if (RpgParty.canSupport(player, ally) && player.distanceToSqr(ally) <= 256.0) {
                    ally.addEffect(new MobEffectInstance(effect, 20 * 15, amplifier));
                    affected++;
                }
            }
            if (affected == 0) return 0;
            spend(player, skill, cost, cooldownTicks);
            player.sendSystemMessage(Component.literal("§dBlessing applied to " + affected + " party member(s) for 15 seconds."));
            return 1;
        }
        player.addEffect(new MobEffectInstance(effect, 20 * 15, amplifier));
        spend(player, skill, cost, cooldownTicks);
        player.sendSystemMessage(Component.literal("§d" + displayName + " activated for 15 seconds."));
        return 1;
    }

    private static boolean ready(ServerPlayer player, String skill, int cost, int cooldownTicks) {
        long now = player.level().getGameTime();
        long next = RpgData.data(player).getLong("skill_cooldown_" + skill);
        if (now < next) {
            player.sendSystemMessage(Component.literal("§cSkill cooldown: " + ((next - now + 19) / 20) + "s."));
            return false;
        }
        if (RpgData.mana(player) < cost) {
            player.sendSystemMessage(Component.literal("§cNot enough mana. Need " + cost + "."));
            return false;
        }
        return true;
    }

    private static void spend(ServerPlayer player, String skill, int cost, int cooldownTicks) {
        RpgData.setMana(player, RpgData.mana(player) - cost);
        RpgData.data(player).putLong("skill_cooldown_" + skill, player.level().getGameTime() + cooldownTicks);
    }

    private static LivingEntity nearestTarget(ServerPlayer player, double range) {
        AABB area = player.getBoundingBox().inflate(range);
        return player.level().getEntitiesOfClass(LivingEntity.class, area,
                        entity -> entity != player && entity.isAlive() && !entity.getType().getCategory().isFriendly()
                                && player.distanceToSqr(entity) <= range * range)
                .stream().filter(entity -> player.hasLineOfSight(entity))
                .min(Comparator.comparingDouble(player::distanceToSqr)).orElse(null);
    }
}
