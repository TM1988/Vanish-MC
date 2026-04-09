package com.example.mixin.client;

import com.example.ExampleModClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InventoryScreen.class)
public class ExampleClientMixin {
	private static final int TRASH_SLOT_SIZE = 18;
	private static final int TRASH_SLOT_X = 178;
	private static final int TRASH_SLOT_Y = 130;
	private static final int TRASH_SLOT_INDEX = 46;
	private static final int INVENTORY_WIDTH = 176;
	private static final int INVENTORY_HEIGHT = 166;

	@Inject(method = "extractBackground", at = @At("TAIL"))
	private void vanish$drawInventorySidePanel(GuiGraphicsExtractor extractor, int mouseX, int mouseY, float tickDelta, CallbackInfo ci) {
		if (!ExampleModClient.isInventoryPanelVisible()) {
			return;
		}

		int leftPos = (extractor.guiWidth() - INVENTORY_WIDTH) / 2;
		int topPos = (extractor.guiHeight() - INVENTORY_HEIGHT) / 2;
		int slotX = leftPos + TRASH_SLOT_X;
		int slotY = topPos + TRASH_SLOT_Y;

		drawConnectedPanel(extractor, leftPos, slotX, slotY);

		InventoryScreen screen = (InventoryScreen) (Object) this;
		if (!(screen.getMenu() instanceof InventoryMenu)) {
			return;
		}

		Slot trashSlot = screen.getMenu().getSlot(TRASH_SLOT_INDEX);
		ItemStack stack = trashSlot.getItem();
		if (stack.isEmpty()) {
			drawTrashCanIcon(extractor, slotX, slotY);
		}
	}

	private static void drawConnectedPanel(GuiGraphicsExtractor extractor, int leftPos, int slotX, int slotY) {
		int plateX1 = leftPos + INVENTORY_WIDTH - 1;
		int plateY1 = slotY - 2;
		int plateX2 = slotX + TRASH_SLOT_SIZE;
		int plateY2 = slotY + TRASH_SLOT_SIZE + 2;

		extractor.fill(plateX1, plateY1, plateX2, plateY2, 0xFFC6C6C6);
		extractor.outline(plateX1, plateY1, plateX2 - plateX1, plateY2 - plateY1, 0xFFF0F0F0);

		extractor.fill(slotX, slotY, slotX + TRASH_SLOT_SIZE, slotY + TRASH_SLOT_SIZE, 0xFF8B8B8B);
		extractor.outline(slotX, slotY, TRASH_SLOT_SIZE, TRASH_SLOT_SIZE, 0xFFF0F0F0);
		extractor.fill(slotX + 1, slotY + 1, slotX + TRASH_SLOT_SIZE - 1, slotY + TRASH_SLOT_SIZE - 1, 0xFF8B8B8B);
	}

	private static void drawTrashCanIcon(GuiGraphicsExtractor extractor, int slotX, int slotY) {
		int x = slotX + 5;
		int y = slotY + 4;

		extractor.fill(x, y, x + 8, y + 2, 0xFF232323);
		extractor.fill(x + 1, y + 2, x + 7, y + 3, 0xFF232323);
		extractor.fill(x + 2, y - 1, x + 6, y, 0xFF232323);

		extractor.fill(x + 1, y + 3, x + 7, y + 11, 0xFF232323);
		extractor.fill(x + 2, y + 4, x + 3, y + 10, 0xFFBDBDBD);
		extractor.fill(x + 4, y + 4, x + 5, y + 10, 0xFFBDBDBD);
		extractor.fill(x + 6, y + 4, x + 7, y + 10, 0xFFBDBDBD);
	}
}