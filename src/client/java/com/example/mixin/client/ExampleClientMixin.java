package com.example.mixin.client;

import com.example.ExampleModClient;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.resources.Identifier;
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
	private static final int TRASH_SLOT_INDEX = 46;
	private static final int INVENTORY_WIDTH = 176;
	private static final int INVENTORY_HEIGHT = 166;
	private static final Identifier TRASH_SLOT_TEXTURE = Identifier.fromNamespaceAndPath("vanish", "textures/gui/trash_slot.png");

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
		extractor.blit(TRASH_SLOT_TEXTURE, slotX, slotY, TRASH_SLOT_SIZE, TRASH_SLOT_SIZE, 0.0F, 0.0F, 18.0F, 18.0F);

		InventoryScreen screen = (InventoryScreen) (Object) this;
		if (!(screen.getMenu() instanceof InventoryMenu)) {
			return;
		}
	}

	private static void drawConnectedPanel(GuiGraphicsExtractor extractor, int leftPos, int slotX, int slotY) {
		int plateX1 = leftPos + INVENTORY_WIDTH - 1;
		int plateY1 = slotY - 2;
		int plateX2 = slotX + TRASH_SLOT_SIZE;
		int plateY2 = slotY + TRASH_SLOT_SIZE + 2;

		extractor.fill(plateX1, plateY1, plateX2, plateY2, 0xFFC6C6C6);
		extractor.outline(plateX1, plateY1, plateX2 - plateX1, plateY2 - plateY1, 0xFFE0E0E0);
	}
}