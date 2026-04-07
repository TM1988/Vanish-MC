package com.example.mixin.client;

import com.example.ExampleModClient;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(InventoryScreen.class)
public class ExampleClientMixin {
	@Shadow
	protected int leftPos;

	@Shadow
	protected int topPos;

	@Shadow
	protected int imageWidth;

	@Shadow
	protected int imageHeight;

	private static final int TRASH_SLOT_SIZE = 18;
	private static final int TRASH_SLOT_X_OFFSET = 4;
	private static final int TRASH_SLOT_Y_OFFSET = 130;

	@Inject(method = "extractBackground", at = @At("TAIL"))
	private void vanish$drawInventorySidePanel(GuiGraphicsExtractor extractor, int mouseX, int mouseY, float tickDelta, CallbackInfo ci) {
		if (!ExampleModClient.isInventoryPanelVisible()) {
			return;
		}

		int slotX = leftPos + imageWidth + TRASH_SLOT_X_OFFSET;
		int slotY = topPos + TRASH_SLOT_Y_OFFSET;

		drawTrashSlotBackground(extractor, slotX, slotY);
		ItemStack ghostStack = ExampleModClient.getGhostTrashStack();
		if (ghostStack.isEmpty()) {
			drawTrashCanIcon(extractor, slotX, slotY);
		} else {
			extractor.fakeItem(ghostStack, slotX + 1, slotY + 1);
		}
	}

	@Inject(method = "mouseReleased", at = @At("HEAD"), cancellable = true)
	private void vanish$handleTrashSlotDrop(MouseButtonEvent event, CallbackInfoReturnable<Boolean> cir) {
		if (!ExampleModClient.isInventoryPanelVisible()) {
			return;
		}

		if (event.button() != GLFW.GLFW_MOUSE_BUTTON_LEFT) {
			return;
		}

		int slotX = leftPos + imageWidth + TRASH_SLOT_X_OFFSET;
		int slotY = topPos + TRASH_SLOT_Y_OFFSET;
		if (!isInsideSlot(event.x(), event.y(), slotX, slotY, TRASH_SLOT_SIZE)) {
			return;
		}

		InventoryScreen screen = (InventoryScreen) (Object) this;
		ItemStack carried = screen.getMenu().getCarried();
		if (!carried.isEmpty()) {
			ExampleModClient.setGhostTrashStack(carried);
		}

		cir.setReturnValue(true);
	}

	private static boolean isInsideSlot(double mouseX, double mouseY, int slotX, int slotY, int slotSize) {
		return mouseX >= slotX && mouseX < slotX + slotSize && mouseY >= slotY && mouseY < slotY + slotSize;
	}

	private static void drawTrashSlotBackground(GuiGraphicsExtractor extractor, int x, int y) {
		extractor.fill(x, y, x + TRASH_SLOT_SIZE, y + TRASH_SLOT_SIZE, 0xFF8B8B8B);
		extractor.horizontalLine(x, x + TRASH_SLOT_SIZE - 1, y, 0xFFFFFFFF);
		extractor.verticalLine(x, y, y + TRASH_SLOT_SIZE - 1, 0xFFFFFFFF);
		extractor.horizontalLine(x, x + TRASH_SLOT_SIZE - 1, y + TRASH_SLOT_SIZE - 1, 0xFF373737);
		extractor.verticalLine(x + TRASH_SLOT_SIZE - 1, y, y + TRASH_SLOT_SIZE - 1, 0xFF373737);
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