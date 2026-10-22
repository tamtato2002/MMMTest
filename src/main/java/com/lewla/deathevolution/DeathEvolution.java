package com.lewla.deathevolution;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

@Mod(
    modid = DeathEvolution.MODID,
    name = DeathEvolution.NAME,
    version = DeathEvolution.VERSION,
    acceptedMinecraftVersions = "[1.12.2]"
)
public class DeathEvolution {
    public static final String MODID = "deathevolution";
    public static final String NAME = "Death Evolution";
    public static final String VERSION = "1.0.0";

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        // Forge's @Config annotation loads config/deathevolution.cfg automatically.
        event.getModLog().info("Death Evolution is loading.");
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        DeathEvolutionEvents events = new DeathEvolutionEvents();
        MinecraftForge.EVENT_BUS.register(events);
        FMLCommonHandler.instance().bus().register(events);
    }
}
