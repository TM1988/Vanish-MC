package com.example;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

public class ExampleModClient implements ClientModInitializer {
	private static final KeyMapping.Category VANISH_CATEGORY =
		KeyMapping.Category.register(Identifier.fromNamespaceAndPath("vanish", "general"));

	private static final KeyMapping T_MESSAGE_KEY = new KeyMapping(
		"key.vanish.test_t",
		InputConstants.Type.KEYSYM,
		GLFW.GLFW_KEY_T,
		VANISH_CATEGORY
	);

	private static final KeyMapping DELETE_MESSAGE_KEY = new KeyMapping(
		"key.vanish.test_delete",
		InputConstants.Type.KEYSYM,
		GLFW.GLFW_KEY_DELETE,
		VANISH_CATEGORY
	);

	@Override
	public void onInitializeClient() {
		ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
			// Ensure UI work runs on the main client thread.
			client.execute(() -> sendJoinMessage(client));
		});

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			while (T_MESSAGE_KEY.consumeClick()) {
				sendHotkeyMessage(client, "[Vanish] T key pressed.");
			}

			while (DELETE_MESSAGE_KEY.consumeClick()) {
				sendHotkeyMessage(client, "[Vanish] Delete key pressed.");
			}
		});
	}

	private static void sendJoinMessage(Minecraft client) {
		if (client.player == null) {
			return;
		}

		client.player.sendSystemMessage(Component.literal("[Vanish] Test message: world loaded."));
	}

	private static void sendHotkeyMessage(Minecraft client, String message) {
		if (client.player == null) {
			return;
		}

		client.player.sendSystemMessage(Component.literal(message));
	}
}