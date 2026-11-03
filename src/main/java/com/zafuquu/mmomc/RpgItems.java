package com.zafuquu.mmomc;

import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Registered physical items backing the server-authoritative RPG equipment catalog. */
public final class RpgItems {
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MMOMC.MOD_ID);
    private static final Map<String, DeferredItem<Item>> EQUIPMENT = registerEquipment();
    private RpgItems() {}

    private static Map<String, DeferredItem<Item>> registerEquipment() {
        Map<String, DeferredItem<Item>> items = new LinkedHashMap<>();
        for (RpgEquipment.ItemDefinition definition : RpgEquipmentCatalog.all()) {
            items.put(definition.id(), ITEMS.registerSimpleItem(definition.id()));
        }
        return Map.copyOf(items);
    }

    public static void register(IEventBus modEventBus) { ITEMS.register(modEventBus); }

    public static Item getEquipmentItem(String id) {
        DeferredItem<Item> item = EQUIPMENT.get(id);
        return item == null ? null : item.get();
    }
}
