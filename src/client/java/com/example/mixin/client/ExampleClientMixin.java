package com.example.mixin.client;

import com.example.ExampleModClient;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.world.inventory.InventoryMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InventoryScreen.class)
public class ExampleClientMixin {
	private static final int TRASH_SLOT_SIZE = 18;
	private static final int TRASH_SLOT_X = 176;
	private static final int TRASH_SLOT_Y = 130;
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
		drawSlotBase(extractor, slotX, slotY);
		drawTrashGlyph(extractor, slotX, slotY);

		InventoryScreen screen = (InventoryScreen) (Object) this;
		if (!(screen.getMenu() instanceof InventoryMenu)) {
			return;
		}
	}

	private static void drawConnectedPanel(GuiGraphicsExtractor extractor, int leftPos, int slotX, int slotY) {
		int panelX1 = leftPos + INVENTORY_WIDTH - 1;
		int panelY1 = slotY - 2;
		int panelX2 = slotX + TRASH_SLOT_SIZE;
		int panelY2 = slotY + TRASH_SLOT_SIZE + 2;

		extractor.fill(panelX1, panelY1, panelX2, panelY2, 0xFFC6C6C6);
		extractor.outline(panelX1, panelY1, panelX2 - panelX1, panelY2 - panelY1, 0xFFE0E0E0);
	}

	private static void drawSlotBase(GuiGraphicsExtractor extractor, int slotX, int slotY) {
		extractor.fill(slotX, slotY, slotX + TRASH_SLOT_SIZE, slotY + TRASH_SLOT_SIZE, 0xFF8B8B8B);
		extractor.fill(slotX + 1, slotY + 1, slotX + TRASH_SLOT_SIZE - 1, slotY + TRASH_SLOT_SIZE - 1, 0xFF3F3F3F);
		extractor.outline(slotX, slotY, TRASH_SLOT_SIZE, TRASH_SLOT_SIZE, 0xFFD8D8D8);
	}

	private static void drawTrashGlyph(GuiGraphicsExtractor extractor, int slotX, int slotY) {
		int cx = slotX + 9;
		int cy = slotY + 9;

		// Lid.
		extractor.fill(cx - 4, cy - 5, cx + 4, cy - 4, 0xFFBEBEBE);
		// Rim.
		extractor.fill(cx - 3, cy - 4, cx + 3, cy - 3, 0xFF9C9C9C);
		// Can body.
		extractor.fill(cx - 3, cy - 3, cx + 3, cy + 4, 0xFFB0B0B0);
		// Vertical grooves.
		extractor.fill(cx - 2, cy - 2, cx - 1, cy + 3, 0xFF7A7A7A);
		extractor.fill(cx, cy - 2, cx + 1, cy + 3, 0xFF7A7A7A);
		extractor.fill(cx + 2, cy - 2, cx + 3, cy + 3, 0xFF7A7A7A);
	}

}