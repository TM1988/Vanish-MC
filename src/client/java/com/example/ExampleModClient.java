package com.example;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

public class ExampleModClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
			// Ensure UI work runs on the main client thread.
			client.execute(() -> sendJoinMessage(client));
		});
	}

	private static void sendJoinMessage(Minecraft client) {
		if (client.player == null) {
			return;
		}

		client.player.sendSystemMessage(Component.literal("[Void Pocket] Test message: world loaded."));
	}
}