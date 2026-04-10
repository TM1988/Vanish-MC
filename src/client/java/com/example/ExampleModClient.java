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
import net.minecraft.resources.Identifier;
import net.minecraft.world.inventory.AbstractContainerMenu;
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
			// World-scoped state: reset on new world connection.
			setInventoryPanelVisible(false);
			wasTKeyDown = false;
			wasDeleteKeyDown = false;
		});

		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
			setInventoryPanelVisible(false);
			wasTKeyDown = false;
			wasDeleteKeyDown = false;
		});

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			boolean tKeyDown = isBoundKeyCurrentlyDown(client, T_MESSAGE_KEY);
			boolean deleteKeyDown = isBoundKeyCurrentlyDown(client, DELETE_MESSAGE_KEY);

			if (client.player != null) {
				if (client.screen instanceof InventoryScreen) {
					if (tKeyDown && !wasTKeyDown) {
						setInventoryPanelVisible(!inventoryPanelVisible);
					}
				}

				if (deleteKeyDown && !wasDeleteKeyDown) {
					moveHoveredItemToTrash(client);
				}
			}

			wasTKeyDown = tKeyDown;
			wasDeleteKeyDown = deleteKeyDown;
		});
	}

	public static boolean isInventoryPanelVisible() {
		return inventoryPanelVisible;
	}

	private static void setInventoryPanelVisible(boolean visible) {
		inventoryPanelVisible = visible;
		VanishUiState.setInventoryPanelVisible(visible);
	}

	private static boolean moveHoveredItemToTrash(Minecraft client) {
		if (!(client.screen instanceof InventoryScreen screen) || client.player == null || client.gameMode == null) {
			return false;
		}

		Slot hoveredSlot = ((AbstractContainerScreenAccessor) (Object) screen).vanish$getHoveredSlot();
		if (hoveredSlot == null || !hoveredSlot.hasItem()) {
			return false;
		}

		int containerId = screen.getMenu().containerId;
		if (hoveredSlot.index == TRASH_SLOT_INDEX) {
			// If hovering trash slot itself, send special clear action (right-pickup on empty cursor).
			client.gameMode.handleContainerInput(containerId, TRASH_SLOT_INDEX, 1, ContainerInput.PICKUP, client.player);
			return true;
		}

		// Pick up hovered item.
		client.gameMode.handleContainerInput(containerId, hoveredSlot.index, 0, ContainerInput.PICKUP, client.player);
		// Place into trash slot. If trash already had an item, overwrite leaves old one on cursor.
		client.gameMode.handleContainerInput(containerId, TRASH_SLOT_INDEX, 0, ContainerInput.PICKUP, client.player);
		// Delete anything left on cursor (the overwritten old trash item).
		client.gameMode.handleContainerInput(containerId, AbstractContainerMenu.SLOT_CLICKED_OUTSIDE, 0, ContainerInput.PICKUP, client.player);

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