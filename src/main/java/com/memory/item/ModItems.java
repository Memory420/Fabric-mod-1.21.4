package com.memory.item;

import com.memory.TutorialMod;
import com.memory.item.custom.RakeItem;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

import java.util.function.Function;

public class ModItems {

    public static final Item IRON_RAKE = register("iron_rake", RakeItem::new, new Item.Settings().maxDamage(128));

    public static Item register(String name, Function<Item.Settings, Item> itemFactory, Item.Settings settings) {
        RegistryKey<Item> itemKey = RegistryKey.of(
                RegistryKeys.ITEM,
                Identifier.of(TutorialMod.MOD_ID, name)
        );

        Item item = itemFactory.apply(settings.registryKey(itemKey));

        Registry.register(Registries.ITEM, itemKey, item);

        return item;
    }

    public static void initialize() {
        TutorialMod.LOGGER.info("Registering Mod Items (via official Fabric approach) ...");

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS).register(entries -> {
            entries.add(IRON_RAKE);
        });
    }
}
