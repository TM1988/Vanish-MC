package com.tm1988.vanish;

import net.fabricmc.api.ModInitializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VanishMod implements ModInitializer {
	public static final String MOD_ID = "vanish";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("Vanish initialized.");
	}
}