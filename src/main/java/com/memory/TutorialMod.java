package com.memory;

import com.memory.effect.ModEffects;
import com.memory.item.ModItems;
import net.fabricmc.api.ModInitializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TutorialMod implements ModInitializer {
	public static final String MOD_ID = "tutorialmod";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		ModEffects.registerEffects(); //!!!
		ModItems.initialize();
		LOGGER.info("Registering Mod Items 👍");
	}
}