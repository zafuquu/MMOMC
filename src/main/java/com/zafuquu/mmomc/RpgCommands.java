package com.zafuquu.mmomc;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.arguments.EntityArgument;

import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

public final class RpgCommands {
    private RpgCommands() {}

    public static void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("mmomc")
                .then(Commands.literal("skill")
                        .then(Commands.literal("list").executes(context -> RpgSkills.list(context.getSource().getPlayerOrException())))
                        .then(Commands.literal("cast")
                                .then(Commands.argument("skill", StringArgumentType.word())
                                        .executes(context -> RpgSkills.cast(context.getSource().getPlayerOrException(), StringArgumentType.getString(context, "skill"))))))
                .then(Commands.literal("party")
                        .then(Commands.literal("create").executes(context -> RpgParty.create(context.getSource().getPlayerOrException())))
                        .then(Commands.literal("invite").then(Commands.argument("player", EntityArgument.player())
                                .executes(context -> RpgParty.invite(context.getSource().getPlayerOrException(), EntityArgument.getPlayer(context, "player")))))
                        .then(Commands.literal("join").executes(context -> RpgParty.join(context.getSource().getPlayerOrException())))
                        .then(Commands.literal("leave").executes(context -> RpgParty.leave(context.getSource().getPlayerOrException())))
                        .then(Commands.literal("list").executes(context -> RpgParty.list(context.getSource().getPlayerOrException()))))
                .then(Commands.literal("profile")
                        .executes(context -> showProfile(context.getSource().getPlayerOrException())))
                .then(Commands.literal("allocate")
                        .then(Commands.argument("stat", StringArgumentType.word())
                                .then(Commands.argument("points", IntegerArgumentType.integer(1, 1000))
                                        .executes(context -> allocate(
                                                context.getSource().getPlayerOrException(),
                                                StringArgumentType.getString(context, "stat"),
                                                IntegerArgumentType.getInteger(context, "points")
                                        )))))
                .then(Commands.literal("job")
                        .then(Commands.argument("job", StringArgumentType.word())
                                .executes(context -> chooseJob(
                                        context.getSource().getPlayerOrException(),
                                        StringArgumentType.getString(context, "job")
                                ))))
                .then(Commands.literal("specialize")
                        .then(Commands.argument("specialization", StringArgumentType.word())
                                .executes(context -> specialize(
                                        context.getSource().getPlayerOrException(),
                                        StringArgumentType.getString(context, "specialization")
                                ))))
        );
    }

    private static int showProfile(ServerPlayer player) {
        String spec = RpgData.specialization(player);
        player.sendSystemMessage(Component.literal("§6§lMMOMC Character"));
        player.sendSystemMessage(Component.literal("§eLevel: §f" + RpgData.level(player)
                + " §7| EXP: §f" + RpgData.experience(player) + "/" + RpgData.experienceToNextLevel(player)));
        player.sendSystemMessage(Component.literal("§eJob: §f" + RpgData.displayJob(player)
                + (spec.isBlank() ? "" : " §7(specialized)")));
        player.sendSystemMessage(Component.literal("§eSTR: §f" + RpgData.stat(player, RpgData.Stat.STR)
                + " §eSTA: §f" + RpgData.stat(player, RpgData.Stat.STA)
                + " §eDEX: §f" + RpgData.stat(player, RpgData.Stat.DEX)
                + " §eINT: §f" + RpgData.stat(player, RpgData.Stat.INT)));
        player.sendSystemMessage(Component.literal("§eUnspent stat points: §f" + RpgData.availableStatPoints(player)));
        player.sendSystemMessage(Component.literal("§bMana: §f" + RpgData.mana(player) + "/" + RpgData.maxMana(player)));
        if (RpgData.level(player) < 10 && RpgData.job(player).equals("beginner")) {
            player.sendSystemMessage(Component.literal("§7Choose your first job at level 10 with /mmomc job <job>."));
        } else if (RpgData.level(player) < 50 && !RpgData.job(player).equals("beginner")) {
            player.sendSystemMessage(Component.literal("§7Second-job specialization unlocks at level 50."));
        }
        return 1;
    }

    private static int allocate(ServerPlayer player, String rawStat, int points) {
        RpgData.Stat stat = RpgData.Stat.parse(rawStat);
        if (stat == null) {
            player.sendSystemMessage(Component.literal("§cUnknown stat. Use str, sta, dex, or int."));
            return 0;
        }
        if (!RpgData.allocate(player, stat, points)) {
            player.sendSystemMessage(Component.literal("§cNot enough unspent stat points."));
            return 0;
        }
        player.sendSystemMessage(Component.literal("§aAllocated " + points + " point(s) to " + stat.label
                + ". Remaining points: " + RpgData.availableStatPoints(player) + "."));
        return 1;
    }

    private static int chooseJob(ServerPlayer player, String rawJob) {
        String job = rawJob.toLowerCase(java.util.Locale.ROOT);
        if (!RpgData.chooseJob(player, job)) {
            player.sendSystemMessage(Component.literal("§cYou need level 10, must still be a Beginner, and must choose one of: swordsman, tank, archer, magician, assist."));
            return 0;
        }
        player.sendSystemMessage(Component.literal("§aFirst job selected: " + RpgData.titleCase(job) + "!"));
        return 1;
    }

    private static int specialize(ServerPlayer player, String rawSpecialization) {
        String specialization = rawSpecialization.toLowerCase(java.util.Locale.ROOT);
        if (!RpgData.chooseSpecialization(player, specialization)) {
            player.sendSystemMessage(Component.literal("§cYou need level 50, a first job, and a specialization that belongs to it. Available: blade_master, berserker, paladin, guardian, ranger, sharpshooter, elementor, psykeeper, ringmaster, billposter."));
            return 0;
        }
        player.sendSystemMessage(Component.literal("§dSecond job unlocked: " + RpgData.titleCase(specialization) + "!"));
        return 1;
    }
}
