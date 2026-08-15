package com.auco.tempered.registry;

import com.auco.tempered.Tempered;
import com.auco.tempered.aspect.TestAspectItem;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModItems {

    public static final DeferredRegister.Items ITEMS =
            DeferredRegister.createItems(Tempered.MODID);

    public static final DeferredItem<TestAspectItem> TEST_ASPECT =
            ITEMS.register(
                    "test_aspect",
                    () -> new TestAspectItem(
                            new Item.Properties()
                                    .stacksTo(1)
                                    .component(ModDataComponents.TEST_LEVEL.value(), 0)
                    )
            );

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }

    private ModItems() {
    }
}