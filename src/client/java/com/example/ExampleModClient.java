package com.example;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

public class ExampleModClient implements ClientModInitializer {
	private static final KeyMapping.Category VANISH_CATEGORY =
		KeyMapping.Category.register(Identifier.fromNamespaceAndPath("vanish", "general"));

	private static final KeyMapping T_MESSAGE_KEY = KeyMappingHelper.registerKeyMapping(new KeyMapping(
		"key.vanish.test_t",
		InputConstants.Type.KEYSYM,
		GLFW.GLFW_KEY_T,
		VANISH_CATEGORY
	));

	private static final KeyMapping DELETE_MESSAGE_KEY = KeyMappingHelper.registerKeyMapping(new KeyMapping(
		"key.vanish.test_delete",
		InputConstants.Type.KEYSYM,
		GLFW.GLFW_KEY_DELETE,
		VANISH_CATEGORY
	));

	private static boolean wasTKeyDown;
	private static boolean wasDeleteKeyDown;
	private static boolean inventoryPanelVisible;

	@Override
	public void onInitializeClient() {
		ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
			// Ensure UI work runs on the main client thread.
			client.execute(() -> sendJoinMessage(client));
		});

		ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
			if (screen instanceof InventoryScreen) {
				ScreenEvents.afterBackground(screen).register((currentScreen, extractor, mouseX, mouseY, tickDelta) -> {
					if (!inventoryPanelVisible) {
						return;
					}

					renderInventorySidePanel(extractor);
				});
			}
		});

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			boolean tKeyDown = T_MESSAGE_KEY.isDown();
			boolean deleteKeyDown = DELETE_MESSAGE_KEY.isDown();

			if (client.player != null) {
				if (client.screen instanceof InventoryScreen) {
					if (tKeyDown && !wasTKeyDown) {
						inventoryPanelVisible = !inventoryPanelVisible;
					}
				}

				if (tKeyDown && !wasTKeyDown) {
					// Consume the rising edge outside the inventory too, so key state stays in sync.
				}

				if (deleteKeyDown && !wasDeleteKeyDown) {
					sendHotkeyMessage(client, "[Vanish] Delete key pressed.");
				}
			}

			wasTKeyDown = tKeyDown;
			wasDeleteKeyDown = deleteKeyDown;
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

	private static void renderInventorySidePanel(net.minecraft.client.gui.GuiGraphicsExtractor extractor) {
		int inventoryWidth = 176;
		int inventoryHeight = 166;
		int leftPos = (extractor.guiWidth() - inventoryWidth) / 2;
		int topPos = (extractor.guiHeight() - inventoryHeight) / 2;

		int panelX1 = leftPos + inventoryWidth + 6;
		int panelY1 = topPos;
		int panelX2 = panelX1 + 54;
		int panelY2 = topPos + inventoryHeight;

		extractor.fill(panelX1, panelY1, panelX2, panelY2, 0xFFFFFFFF);
	}
}