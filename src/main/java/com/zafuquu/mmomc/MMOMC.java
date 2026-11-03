package com.zafuquu.mmomc;

import org.slf4j.Logger;
import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.common.NeoForge;

@Mod(MMOMC.MOD_ID)
public final class MMOMC {
    public static final String MOD_ID = "mmomc";
    public static final Logger LOGGER = LogUtils.getLogger();

    public MMOMC(IEventBus modEventBus, ModContainer modContainer) {
        RpgItems.register(modEventBus);
        NeoForge.EVENT_BUS.addListener(RpgEvents::onLivingDeath);
        NeoForge.EVENT_BUS.addListener(RpgEvents::onPlayerClone);
        NeoForge.EVENT_BUS.addListener(RpgCommands::onRegisterCommands);
        NeoForge.EVENT_BUS.addListener(RpgCombat::onPlayerTick);
        LOGGER.info("MMOMC initialized: RPG core event handlers registered.");
    }
}
