package com.tm1988.vanish;

import com.mojang.blaze3d.platform.InputConstants;
import com.tm1988.vanish.mixin.client.AbstractContainerScreenAccessor;
import java.util.Objects;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import org.lwjgl.glfw.GLFW;

public class VanishClient implements ClientModInitializer {
	private static final int TRASH_SLOT_INDEX = 46;

	private static final KeyMapping.Category VANISH_CATEGORY =
		Objects.requireNonNull(KeyMapping.Category.register(Identifier.fromNamespaceAndPath("vanish", "general")));

	private static final KeyMapping TOGGLE_TRASH_SLOT_KEY = KeyMappingHelper.registerKeyMapping(new KeyMapping(
		"key.vanish.toggle_trash_slot",
		InputConstants.Type.KEYSYM,
		GLFW.GLFW_KEY_T,
		VANISH_CATEGORY
	));

	private static final KeyMapping DELETE_HOVERED_ITEM_KEY = KeyMappingHelper.registerKeyMapping(new KeyMapping(
		"key.vanish.delete_hovered_item",
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
			boolean tKeyDown = isBoundKeyCurrentlyDown(client, TOGGLE_TRASH_SLOT_KEY);
			boolean deleteKeyDown = isBoundKeyCurrentlyDown(client, DELETE_HOVERED_ITEM_KEY);

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
		if (!(client.screen instanceof InventoryScreen screen)) {
			return false;
		}

		LocalPlayer player = client.player;
		MultiPlayerGameMode gameMode = client.gameMode;
		if (player == null || gameMode == null) {
			return false;
		}

		Slot hoveredSlot = ((AbstractContainerScreenAccessor) (Object) screen).vanish$getHoveredSlot();
		if (hoveredSlot == null || !hoveredSlot.hasItem()) {
			return false;
		}

		int containerId = screen.getMenu().containerId;
		if (hoveredSlot.index == TRASH_SLOT_INDEX) {
			gameMode.handleContainerInput(containerId, TRASH_SLOT_INDEX, 1, ContainerInput.PICKUP, player);
			return true;
		}

		gameMode.handleContainerInput(containerId, hoveredSlot.index, 0, ContainerInput.PICKUP, player);
		gameMode.handleContainerInput(containerId, TRASH_SLOT_INDEX, 0, ContainerInput.PICKUP, player);
		gameMode.handleContainerInput(containerId, AbstractContainerMenu.SLOT_CLICKED_OUTSIDE, 0, ContainerInput.PICKUP, player);

		return true;
	}

	private static boolean isBoundKeyCurrentlyDown(Minecraft client, KeyMapping keyMapping) {
		var window = client.getWindow();
		InputConstants.Key key = Objects.requireNonNull(KeyMappingHelper.getBoundKeyOf(keyMapping));

		return switch (key.getType()) {
			case KEYSYM, SCANCODE -> InputConstants.isKeyDown(window, key.getValue());
			case MOUSE -> GLFW.glfwGetMouseButton(window.handle(), key.getValue()) == GLFW.GLFW_PRESS;
			default -> false;
		};
	}
}