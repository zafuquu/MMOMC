package com.zafuquu.mmomc;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;

/** Persistent adapter between the pure loadout model and server-owned player NBT. */
public final class RpgEquipmentData {
    private static final String KEY = "equipment";

    private RpgEquipmentData() {}

    public static RpgLoadout load(ServerPlayer player) {
        CompoundTag root = RpgData.data(player);
        ListTag entries = root.getList(KEY, Tag.TAG_COMPOUND);
        RpgLoadout loadout = new RpgLoadout();
        for (int i = 0; i < entries.size(); i++) {
            CompoundTag entry = entries.getCompound(i);
            RpgEquipment.ItemDefinition definition = RpgEquipmentCatalog.get(entry.getString("id"));
            if (definition == null) continue;
            int refinement = entry.getInt("refinement");
            if (refinement < RpgRefinement.MIN_LEVEL || refinement > RpgRefinement.MAX_LEVEL) continue;
            RpgLoadout.EquipResult result = loadout.equip(definition, refinement, RpgData.maxLevel());
            if (result.result() == RpgLoadout.Result.EQUIPPED) loadout = result.loadout();
        }
        return loadout;
    }

    public static void save(ServerPlayer player, RpgLoadout loadout) {
        ListTag entries = new ListTag();
        for (RpgEquipment.Slot slot : RpgEquipment.Slot.values()) {
            RpgLoadout.EquippedItem equipped = loadout.get(slot);
            if (equipped == null) continue;
            CompoundTag entry = new CompoundTag();
            entry.putString("id", equipped.definition().id());
            entry.putInt("refinement", equipped.refinementLevel());
            entries.add(entry);
        }
        RpgData.data(player).put(KEY, entries);
    }

    public static RpgLoadout.Result equip(ServerPlayer player, String itemId) {
        RpgEquipment.ItemDefinition definition = RpgEquipmentCatalog.get(itemId);
        if (definition == null) return RpgLoadout.Result.INVALID_ITEM;
        RpgLoadout current = load(player);
        RpgLoadout.EquipResult result = current.equip(definition, 0, RpgData.level(player));
        if (result.result() == RpgLoadout.Result.EQUIPPED) {
            save(player, result.loadout());
        }
        return result.result();
    }

    public static boolean unequip(ServerPlayer player, RpgEquipment.Slot slot) {
        if (slot == null) return false;
        RpgLoadout current = load(player);
        if (current.get(slot) == null) return false;
        save(player, current.unequip(slot));
        return true;
    }
}
