package com.zafuquu.mmomc;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

/**
 * Immutable, runtime-independent equipment loadout rules.
 *
 * The server inventory adapter is responsible for moving ItemStacks transactionally;
 * this class decides whether a definition can occupy a slot and computes its bonuses.
 */
public final class RpgLoadout {
    public enum Result { EQUIPPED, LEVEL_TOO_LOW, INVALID_ITEM }

    private final Map<RpgEquipment.Slot, EquippedItem> items;

    public record EquippedItem(RpgEquipment.ItemDefinition definition, int refinementLevel) {
        public EquippedItem {
            Objects.requireNonNull(definition, "definition");
            RpgRefinement.successChance(refinementLevel, 0);
        }

        public RpgEquipment.Stats effectiveStats() {
            return definition.effectiveStats(refinementLevel);
        }
    }

    public record EquipResult(Result result, RpgLoadout loadout, EquippedItem replaced) {}

    public RpgLoadout() {
        this.items = Collections.unmodifiableMap(new EnumMap<>(RpgEquipment.Slot.class));
    }

    private RpgLoadout(Map<RpgEquipment.Slot, EquippedItem> source) {
        EnumMap<RpgEquipment.Slot, EquippedItem> copy = new EnumMap<>(RpgEquipment.Slot.class);
        copy.putAll(source);
        this.items = Collections.unmodifiableMap(copy);
    }

    public EquipResult equip(RpgEquipment.ItemDefinition item, int refinementLevel, int playerLevel) {
        if (item == null || refinementLevel < 0 || refinementLevel > 20 || playerLevel < 1 || playerLevel > 120) {
            return new EquipResult(Result.INVALID_ITEM, this, null);
        }
        if (!item.canEquipAtLevel(playerLevel)) {
            return new EquipResult(Result.LEVEL_TOO_LOW, this, null);
        }
        EquippedItem equipped = new EquippedItem(item, refinementLevel);
        EnumMap<RpgEquipment.Slot, EquippedItem> next = new EnumMap<>(RpgEquipment.Slot.class);
        next.putAll(items);
        EquippedItem replaced = next.put(item.slot(), equipped);
        return new EquipResult(Result.EQUIPPED, new RpgLoadout(next), replaced);
    }

    public RpgLoadout unequip(RpgEquipment.Slot slot) {
        if (slot == null || !items.containsKey(slot)) return this;
        EnumMap<RpgEquipment.Slot, EquippedItem> next = new EnumMap<>(RpgEquipment.Slot.class);
        next.putAll(items);
        next.remove(slot);
        return new RpgLoadout(next);
    }

    public EquippedItem get(RpgEquipment.Slot slot) {
        return items.get(slot);
    }

    public Map<RpgEquipment.Slot, EquippedItem> items() {
        return items;
    }

    public RpgEquipment.Stats totalStats() {
        RpgEquipment.Stats total = new RpgEquipment.Stats(0, 0, 0, 0, 0, 0, 0, 0);
        for (EquippedItem item : items.values()) total = total.plus(item.effectiveStats());
        return total;
    }

    public int size() {
        return items.size();
    }
}
