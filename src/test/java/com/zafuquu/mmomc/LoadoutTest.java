package com.zafuquu.mmomc;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class LoadoutTest {
    private static RpgEquipment.ItemDefinition item(String id, RpgEquipment.Slot slot, int level,
                                                     RpgEquipment.Stats stats) {
        return new RpgEquipment.ItemDefinition(id, slot, RpgEquipment.Rarity.COMMON, level, stats);
    }

    @Test
    void equipsOnlyWhenLevelRequirementIsMetAndPreservesOriginalLoadout() {
        var sword = item("starter_sword", RpgEquipment.Slot.WEAPON, 5,
                new RpgEquipment.Stats(1,0,0,0,4,0,0,0));
        var original = new RpgLoadout();
        var denied = original.equip(sword, 0, 4);
        assertEquals(RpgLoadout.Result.LEVEL_TOO_LOW, denied.result());
        assertEquals(0, original.size());

        var accepted = original.equip(sword, 0, 5);
        assertEquals(RpgLoadout.Result.EQUIPPED, accepted.result());
        assertEquals(1, accepted.loadout().size());
        assertEquals(0, original.size());
    }

    @Test
    void oneItemPerSlotAndReplacementIsReported() {
        var first = item("iron_sword", RpgEquipment.Slot.WEAPON, 1,
                new RpgEquipment.Stats(1,0,0,0,2,0,0,0));
        var second = item("steel_sword", RpgEquipment.Slot.WEAPON, 10,
                new RpgEquipment.Stats(2,0,0,0,5,0,0,0));
        var loadout = new RpgLoadout().equip(first, 0, 10).loadout();
        var result = loadout.equip(second, 5, 10);
        assertEquals(RpgLoadout.Result.EQUIPPED, result.result());
        assertEquals("iron_sword", result.replaced().definition().id());
        assertEquals("steel_sword", result.loadout().get(RpgEquipment.Slot.WEAPON).definition().id());
        assertEquals(1, result.loadout().size());
    }

    @Test
    void totalsIncludeRarityAndRefinementAndUnequipRemovesBonus() {
        var weapon = new RpgEquipment.ItemDefinition("rare_blade", RpgEquipment.Slot.WEAPON,
                RpgEquipment.Rarity.RARE, 1, new RpgEquipment.Stats(10,0,0,0,20,0,0,0));
        var ring = item("ring", RpgEquipment.Slot.RING, 1,
                new RpgEquipment.Stats(0,2,0,0,0,1,5,0));
        var loadout = new RpgLoadout().equip(weapon, 5, 10).loadout().equip(ring, 0, 10).loadout();
        assertEquals(new RpgEquipment.Stats(11,2,0,0,22,1,5,0), loadout.totalStats());
        assertEquals(1, loadout.unequip(RpgEquipment.Slot.WEAPON).size());
        assertEquals(new RpgEquipment.Stats(0,2,0,0,0,1,5,0),
                loadout.unequip(RpgEquipment.Slot.WEAPON).totalStats());
    }

    @Test
    void invalidEquipDoesNotMutateLoadout() {
        var loadout = new RpgLoadout();
        var sword = item("sword", RpgEquipment.Slot.WEAPON, 1,
                new RpgEquipment.Stats(1,0,0,0,0,0,0,0));
        assertEquals(RpgLoadout.Result.INVALID_ITEM, loadout.equip(sword, 21, 10).result());
        assertEquals(RpgLoadout.Result.INVALID_ITEM, loadout.equip(sword, 0, 121).result());
        assertEquals(0, loadout.size());
    }
}
