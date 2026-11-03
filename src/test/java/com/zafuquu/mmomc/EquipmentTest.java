package com.zafuquu.mmomc;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class EquipmentTest {
    private static final RpgEquipment.Stats BASE =
            new RpgEquipment.Stats(10, 5, 4, 3, 20, 8, 30, 12);

    @Test
    void rarityAndRefinementBonusesAreAddedToBaseStats() {
        var item = new RpgEquipment.ItemDefinition("iron_sword", RpgEquipment.Slot.WEAPON,
                RpgEquipment.Rarity.RARE, 12, BASE);
        // Rare +4%, refinement +10% at +5 = +14%; integer rounding is deterministic.
        assertEquals(new RpgEquipment.Stats(11, 5, 4, 3, 22, 9, 34, 13),
                item.effectiveStats(5));
    }

    @Test
    void equipmentLevelRequirementIsEnforcedByModel() {
        var item = new RpgEquipment.ItemDefinition("novice_ring", RpgEquipment.Slot.RING,
                RpgEquipment.Rarity.COMMON, 10, BASE);
        assertFalse(item.canEquipAtLevel(9));
        assertTrue(item.canEquipAtLevel(10));
        assertTrue(item.canEquipAtLevel(120));
    }

    @Test
    void statsAggregateWithoutMutatingOriginal() {
        var other = new RpgEquipment.Stats(1, 2, 3, 4, 5, 6, 7, 8);
        assertEquals(new RpgEquipment.Stats(11, 7, 7, 7, 25, 14, 37, 20), BASE.plus(other));
        assertEquals(10, BASE.strength());
    }

    @Test
    void invalidDefinitionsAndNegativeStatsAreRejected() {
        assertThrows(IllegalArgumentException.class, () -> new RpgEquipment.Stats(-1,0,0,0,0,0,0,0));
        assertThrows(IllegalArgumentException.class, () -> new RpgEquipment.ItemDefinition(
                "bad id", RpgEquipment.Slot.WEAPON, RpgEquipment.Rarity.COMMON, 1, BASE));
        assertThrows(IllegalArgumentException.class, () -> new RpgEquipment.ItemDefinition(
                "too_high", RpgEquipment.Slot.WEAPON, RpgEquipment.Rarity.COMMON, 121, BASE));
    }
}
