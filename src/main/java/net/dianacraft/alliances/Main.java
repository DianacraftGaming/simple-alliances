package net.dianacraft.alliances;

import net.dianacraft.alliances.command.AllianceCommand;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main implements ModInitializer {
	public static final String MOD_ID = "alliances";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.

        LOGGER.info("Initialising Alliances..");
        CommandRegistrationCallback.EVENT.register((dispatcher, context, selection) -> {
            AllianceCommand.register(dispatcher);
        });

	}
}