package com.zafuquu.mmomc;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Server-authoritative allowlist for the first equipment set.
 * Item stats are defined in code, never trusted from player-supplied NBT.
 */
public final class RpgEquipmentCatalog {
    private static final Map<String, RpgEquipment.ItemDefinition> ITEMS = createItems();

    private RpgEquipmentCatalog() {}

    private static Map<String, RpgEquipment.ItemDefinition> createItems() {
        Map<String, RpgEquipment.ItemDefinition> items = new LinkedHashMap<>();
        add(items, "novice_sword", RpgEquipment.Slot.WEAPON, RpgEquipment.Rarity.COMMON, 1,
                stats(1, 0, 0, 0, 3, 0, 0, 0));
        add(items, "novice_bow", RpgEquipment.Slot.WEAPON, RpgEquipment.Rarity.COMMON, 1,
                stats(0, 0, 2, 0, 2, 0, 0, 0));
        add(items, "novice_staff", RpgEquipment.Slot.WEAPON, RpgEquipment.Rarity.COMMON, 1,
                stats(0, 0, 0, 2, 1, 0, 0, 4));
        add(items, "novice_helmet", RpgEquipment.Slot.HELMET, RpgEquipment.Rarity.COMMON, 1,
                stats(0, 1, 0, 0, 0, 1, 2, 0));
        add(items, "novice_chest", RpgEquipment.Slot.CHEST, RpgEquipment.Rarity.COMMON, 1,
                stats(0, 2, 0, 0, 0, 2, 5, 0));
        add(items, "novice_legs", RpgEquipment.Slot.LEGS, RpgEquipment.Rarity.COMMON, 1,
                stats(0, 1, 0, 0, 0, 1, 3, 0));
        add(items, "novice_boots", RpgEquipment.Slot.BOOTS, RpgEquipment.Rarity.COMMON, 1,
                stats(0, 0, 1, 0, 0, 0, 1, 0));
        add(items, "novice_gloves", RpgEquipment.Slot.GLOVES, RpgEquipment.Rarity.COMMON, 1,
                stats(0, 0, 1, 0, 1, 0, 0, 0));
        add(items, "novice_ring", RpgEquipment.Slot.RING, RpgEquipment.Rarity.UNCOMMON, 5,
                stats(0, 0, 1, 1, 0, 0, 0, 2));
        add(items, "novice_necklace", RpgEquipment.Slot.NECKLACE, RpgEquipment.Rarity.UNCOMMON, 5,
                stats(0, 1, 0, 1, 0, 1, 2, 2));
        return Map.copyOf(items);
    }

    private static RpgEquipment.Stats stats(int str, int sta, int dex, int intelligence,
            int attack, int defense, int health, int mana) {
        return new RpgEquipment.Stats(str, sta, dex, intelligence, attack, defense, health, mana);
    }

    private static void add(Map<String, RpgEquipment.ItemDefinition> target, String id,
            RpgEquipment.Slot slot, RpgEquipment.Rarity rarity, int requiredLevel,
            RpgEquipment.Stats stats) {
        RpgEquipment.ItemDefinition definition =
                new RpgEquipment.ItemDefinition(id, slot, rarity, requiredLevel, stats);
        if (target.putIfAbsent(id, definition) != null) {
            throw new IllegalStateException("Duplicate equipment id: " + id);
        }
    }

    public static RpgEquipment.ItemDefinition get(String id) {
        return id == null ? null : ITEMS.get(id);
    }

    public static Collection<RpgEquipment.ItemDefinition> all() {
        return ITEMS.values();
    }
}
