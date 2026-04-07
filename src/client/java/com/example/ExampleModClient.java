package com.example;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
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
	private static ItemStack trashSlotStack = ItemStack.EMPTY;

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
						if (!inventoryPanelVisible) {
							trashSlotStack = ItemStack.EMPTY;
						}
						sendHotkeyMessage(client, inventoryPanelVisible
							? "[Vanish] Trash slot shown."
							: "[Vanish] Trash slot hidden.");
					}
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

	public static boolean isInventoryPanelVisible() {
		return inventoryPanelVisible;
	}

	public static ItemStack getTrashSlotStack() {
		return trashSlotStack;
	}

	public static void setTrashSlotStack(ItemStack stack) {
		trashSlotStack = stack.copy();
	}

	public static void clearTrashSlotStack() {
		trashSlotStack = ItemStack.EMPTY;
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