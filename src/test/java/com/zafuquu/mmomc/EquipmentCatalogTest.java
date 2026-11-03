package com.zafuquu.mmomc;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class EquipmentCatalogTest {
    @Test
    void starterCatalogContainsWeaponArmorAndAccessories() {
        assertNotNull(RpgEquipmentCatalog.get("novice_sword"));
        assertNotNull(RpgEquipmentCatalog.get("novice_bow"));
        assertNotNull(RpgEquipmentCatalog.get("novice_staff"));
        assertNotNull(RpgEquipmentCatalog.get("novice_chest"));
        assertNotNull(RpgEquipmentCatalog.get("novice_ring"));
        assertNotNull(RpgEquipmentCatalog.get("novice_necklace"));
        assertEquals(10, RpgEquipmentCatalog.all().size());
    }

    @Test
    void catalogIsAnAllowlistAndUnknownIdsCannotResolve() {
        assertNull(RpgEquipmentCatalog.get("admin_created_mythic_item"));
        assertNull(RpgEquipmentCatalog.get(null));
    }

    @Test
    void everyCatalogEntryHasConsistentIdentityAndValidLevel() {
        for (var item : RpgEquipmentCatalog.all()) {
            assertEquals(item.id(), RpgEquipmentCatalog.get(item.id()).id());
            assertTrue(item.requiredLevel() >= 1 && item.requiredLevel() <= RpgData.maxLevel());
            assertNotNull(item.slot());
            assertNotNull(item.rarity());
        }
    }
}
