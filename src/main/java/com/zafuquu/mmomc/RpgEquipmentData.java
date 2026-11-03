package com.zafuquu.mmomc;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/** Persistent adapter between the pure loadout model and server-owned player NBT/inventory. */
public final class RpgEquipmentData {
    private static final String KEY = "equipment";
    private RpgEquipmentData() {}

    public static RpgLoadout load(ServerPlayer player) {
        ListTag entries = RpgData.data(player).getList(KEY, Tag.TAG_COMPOUND);
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

    /** Validates first, consumes one matching stack, then commits the new loadout. */
    public static RpgLoadout.Result equipFromInventory(ServerPlayer player, String itemId) {
        RpgEquipment.ItemDefinition definition = RpgEquipmentCatalog.get(itemId);
        if (definition == null) return RpgLoadout.Result.INVALID_ITEM;
        RpgLoadout current = load(player);
        RpgLoadout.EquipResult result = current.equip(definition, 0, RpgData.level(player));
        if (result.result() != RpgLoadout.Result.EQUIPPED) return result.result();

        Item item = RpgItems.getEquipmentItem(itemId);
        if (item == null) return RpgLoadout.Result.INVALID_ITEM;
        Inventory inventory = player.getInventory();
        int sourceSlot = findInventorySlot(inventory, item);
        if (sourceSlot < 0) return RpgLoadout.Result.INVALID_ITEM;
        ItemStack consumed = inventory.removeItem(sourceSlot, 1);
        if (consumed.isEmpty() || !consumed.is(item)) return RpgLoadout.Result.INVALID_ITEM;

        save(player, result.loadout());
        if (result.replaced() != null) {
            Item oldItem = RpgItems.getEquipmentItem(result.replaced().definition().id());
            if (oldItem != null) returnOrDrop(player, new ItemStack(oldItem));
        }
        return RpgLoadout.Result.EQUIPPED;
    }

    public static boolean unequipToInventory(ServerPlayer player, RpgEquipment.Slot slot) {
        if (slot == null) return false;
        RpgLoadout current = load(player);
        RpgLoadout.EquippedItem equipped = current.get(slot);
        if (equipped == null) return false;
        Item item = RpgItems.getEquipmentItem(equipped.definition().id());
        if (item == null) return false;
        save(player, current.unequip(slot));
        returnOrDrop(player, new ItemStack(item));
        return true;
    }

    private static int findInventorySlot(Inventory inventory, Item item) {
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            if (inventory.getItem(slot).is(item)) return slot;
        }
        return -1;
    }

    private static void returnOrDrop(ServerPlayer player, ItemStack stack) {
        player.getInventory().add(stack);
        if (!stack.isEmpty()) player.drop(stack, false);
    }
}
