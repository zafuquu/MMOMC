package com.zafuquu.mmomc;

import java.util.UUID;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

/** Persistent player party IDs and expiring leader invitations. */
public final class RpgParty {
    private RpgParty() {}
    private static final String ID = "mmomc_party_id";
    private static final String LEADER = "mmomc_party_leader";
    private static final String INVITER = "mmomc_party_inviter";
    private static final String INVITED_ID = "mmomc_party_invited_id";
    private static final String EXPIRES = "mmomc_party_invite_expires";
    private static final int LIMIT = 5;
    private static final long TTL = 6000L;

    private static String id(ServerPlayer p) { return p.getPersistentData().getString(ID); }
    private static String leader(ServerPlayer p) { return p.getPersistentData().getString(LEADER); }
    private static boolean joined(ServerPlayer p) { return !id(p).isBlank(); }

    public static boolean canSupport(ServerPlayer source, ServerPlayer target) {
        if (source == target) return true;
        String sourceParty = id(source);
        return !sourceParty.isBlank() && sourceParty.equals(id(target));
    }

    public static int create(ServerPlayer p) {
        if (joined(p)) { p.sendSystemMessage(Component.literal("§cLeave your current party first.")); return 0; }
        p.getPersistentData().putString(ID, UUID.randomUUID().toString());
        p.getPersistentData().putString(LEADER, p.getUUID().toString());
        p.sendSystemMessage(Component.literal("§aParty created. You are the leader."));
        return 1;
    }

    public static int invite(ServerPlayer p, ServerPlayer target) {
        if (!joined(p) || !p.getUUID().toString().equals(leader(p))) {
            p.sendSystemMessage(Component.literal("§cOnly the party leader can invite."));
            return 0;
        }
        if (target == p || joined(target)) { p.sendSystemMessage(Component.literal("§cThat player cannot be invited.")); return 0; }
        if (count(p.server, id(p)) >= LIMIT) { p.sendSystemMessage(Component.literal("§cParty is full (5 players).")); return 0; }
        var data = target.getPersistentData();
        data.putString(INVITER, p.getUUID().toString());
        data.putString(INVITED_ID, id(p));
        data.putLong(EXPIRES, p.serverLevel().getGameTime() + TTL);
        target.sendSystemMessage(Component.literal("§d" + p.getGameProfile().getName() + " invited you. Use /mmomc party join."));
        p.sendSystemMessage(Component.literal("§aInvitation sent."));
        return 1;
    }

    public static int join(ServerPlayer p) {
        var data = p.getPersistentData();
        String inviterId = data.getString(INVITER);
        String partyId = data.getString(INVITED_ID);
        if (joined(p)) { p.sendSystemMessage(Component.literal("§cLeave your current party first.")); return 0; }
        if (inviterId.isBlank() || partyId.isBlank() || p.serverLevel().getGameTime() > data.getLong(EXPIRES)) {
            clearInvite(p); p.sendSystemMessage(Component.literal("§cNo valid party invitation.")); return 0;
        }
        ServerPlayer inviter;
        try { inviter = p.server.getPlayerList().getPlayer(UUID.fromString(inviterId)); }
        catch (IllegalArgumentException ex) { inviter = null; }
        if (inviter == null || !id(inviter).equals(partyId) || !inviter.getUUID().toString().equals(leader(inviter))) {
            clearInvite(p); p.sendSystemMessage(Component.literal("§cThat party no longer exists.")); return 0;
        }
        if (count(p.server, partyId) >= LIMIT) {
            clearInvite(p); p.sendSystemMessage(Component.literal("§cThat party is full.")); return 0;
        }
        data.putString(ID, partyId);
        data.putString(LEADER, inviterId);
        clearInvite(p);
        broadcast(p.server, partyId, "§a" + p.getGameProfile().getName() + " joined the party.");
        return 1;
    }

    public static int leave(ServerPlayer p) {
        if (!joined(p)) { p.sendSystemMessage(Component.literal("§cYou are not in a party.")); return 0; }
        String oldId = id(p);
        p.getPersistentData().remove(ID);
        p.getPersistentData().remove(LEADER);
        broadcast(p.server, oldId, "§e" + p.getGameProfile().getName() + " left the party.");
        p.sendSystemMessage(Component.literal("§eYou left the party."));
        return 1;
    }

    public static int list(ServerPlayer p) {
        if (!joined(p)) { p.sendSystemMessage(Component.literal("§7You are not in a party.")); return 1; }
        p.sendSystemMessage(Component.literal("§6Party members:"));
        for (ServerPlayer member : p.server.getPlayerList().getPlayers()) {
            if (id(member).equals(id(p))) {
                String role = member.getUUID().toString().equals(leader(member)) ? " §e(Leader)" : "";
                p.sendSystemMessage(Component.literal("§f- " + member.getGameProfile().getName() + role));
            }
        }
        return 1;
    }

    public static void distributeExperience(ServerPlayer killer, long totalReward) {
        java.util.List<ServerPlayer> eligible = killer.server.getPlayerList().getPlayers().stream()
                .filter(member -> canSupport(killer, member))
                .filter(member -> killer.distanceToSqr(member) <= 1024.0)
                .toList();
        if (eligible.isEmpty()) eligible = java.util.List.of(killer);
        long share = Math.max(1L, totalReward / eligible.size());
        for (ServerPlayer member : eligible) {
            boolean leveled = RpgData.addExperience(member, share);
            String message = "+" + share + " EXP (party share)";
            if (leveled) message += " — Level up! You are now level " + RpgData.level(member) + ".";
            member.sendSystemMessage(Component.literal("§a" + message));
        }
    }

    private static int count(MinecraftServer server, String partyId) {
        int n = 0;
        for (ServerPlayer p : server.getPlayerList().getPlayers()) if (id(p).equals(partyId)) n++;
        return n;
    }
    private static void broadcast(MinecraftServer server, String partyId, String message) {
        for (ServerPlayer p : server.getPlayerList().getPlayers()) if (id(p).equals(partyId)) p.sendSystemMessage(Component.literal(message));
    }
    private static void clearInvite(ServerPlayer p) {
        var data = p.getPersistentData();
        data.remove(INVITER); data.remove(INVITED_ID); data.remove(EXPIRES);
    }
}