package com.example;

import com.mojang.blaze3d.platform.InputConstants;
import com.example.mixin.client.AbstractContainerScreenAccessor;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import org.lwjgl.glfw.GLFW;

public class ExampleModClient implements ClientModInitializer {
	private static final int TRASH_SLOT_INDEX = 46;

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

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			boolean tKeyDown = isBoundKeyCurrentlyDown(client, T_MESSAGE_KEY);
			boolean deleteKeyDown = isBoundKeyCurrentlyDown(client, DELETE_MESSAGE_KEY);

			if (client.player != null) {
				if (client.screen instanceof InventoryScreen) {
					if (tKeyDown && !wasTKeyDown) {
						inventoryPanelVisible = !inventoryPanelVisible;
						sendHotkeyMessage(client, inventoryPanelVisible
							? "[Vanish] Trash slot shown."
							: "[Vanish] Trash slot hidden.");
					}
				}

				if (deleteKeyDown && !wasDeleteKeyDown) {
					if (!moveHoveredItemToTrash(client)) {
						sendHotkeyMessage(client, "[Vanish] No item to trash.");
					}
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

	public static boolean isInventoryPanelVisible() {
		return inventoryPanelVisible;
	}

	private static boolean moveHoveredItemToTrash(Minecraft client) {
		if (!(client.screen instanceof InventoryScreen screen) || client.player == null || client.gameMode == null) {
			return false;
		}

		Slot hoveredSlot = ((AbstractContainerScreenAccessor) (Object) screen).vanish$getHoveredSlot();
		if (hoveredSlot == null || !hoveredSlot.hasItem()) {
			return false;
		}

		if (hoveredSlot.index == TRASH_SLOT_INDEX) {
			sendHotkeyMessage(client, "[Vanish] Hover a normal inventory item, not the trash slot.");
			return true;
		}

		int containerId = screen.getMenu().containerId;
		client.gameMode.handleContainerInput(containerId, hoveredSlot.index, 0, ContainerInput.PICKUP, client.player);
		client.gameMode.handleContainerInput(containerId, TRASH_SLOT_INDEX, 0, ContainerInput.PICKUP, client.player);

		return true;
	}

	private static boolean isBoundKeyCurrentlyDown(Minecraft client, KeyMapping keyMapping) {
		var window = client.getWindow();
		InputConstants.Key key = KeyMappingHelper.getBoundKeyOf(keyMapping);

		return switch (key.getType()) {
			case KEYSYM, SCANCODE -> InputConstants.isKeyDown(window, key.getValue());
			case MOUSE -> GLFW.glfwGetMouseButton(window.handle(), key.getValue()) == GLFW.GLFW_PRESS;
			default -> false;
		};
	}
}