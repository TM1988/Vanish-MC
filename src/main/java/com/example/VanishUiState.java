package com.example;

public final class VanishUiState {
	private static volatile boolean inventoryPanelVisible;

	private VanishUiState() {
	}

	public static boolean isInventoryPanelVisible() {
		return inventoryPanelVisible;
	}

	public static void setInventoryPanelVisible(boolean visible) {
		inventoryPanelVisible = visible;
	}
}
