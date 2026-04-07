package com.example.mixin.client;

import com.example.ExampleModClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(InventoryScreen.class)
public class ExampleClientMixin {
	private static final int TRASH_SLOT_SIZE = 18;
	private static final int TRASH_SLOT_X_OFFSET = 2;
	private static final int TRASH_SLOT_Y_OFFSET = 130;
	private static final int INVENTORY_WIDTH = 176;
	private static final int INVENTORY_HEIGHT = 166;

	@Inject(method = "extractBackground", at = @At("TAIL"))
	private void vanish$drawInventorySidePanel(GuiGraphicsExtractor extractor, int mouseX, int mouseY, float tickDelta, CallbackInfo ci) {
		if (!ExampleModClient.isInventoryPanelVisible()) {
			return;
		}

		int leftPos = (extractor.guiWidth() - INVENTORY_WIDTH) / 2;
		int topPos = (extractor.guiHeight() - INVENTORY_HEIGHT) / 2;
		int slotX = leftPos + INVENTORY_WIDTH + TRASH_SLOT_X_OFFSET;
		int slotY = topPos + TRASH_SLOT_Y_OFFSET;

		drawTrashSlotFrame(extractor, leftPos, slotX, slotY);
		ItemStack stack = ExampleModClient.getTrashSlotStack();
		if (stack.isEmpty()) {
			drawTrashCanIcon(extractor, slotX, slotY);
		} else {
			extractor.fakeItem(stack, slotX + 1, slotY + 1);
			extractor.itemDecorations(Minecraft.getInstance().font, stack, slotX + 1, slotY + 1);
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

		Minecraft client = Minecraft.getInstance();
		int guiWidth = client.getWindow().getGuiScaledWidth();
		int guiHeight = client.getWindow().getGuiScaledHeight();
		int leftPos = (guiWidth - INVENTORY_WIDTH) / 2;
		int topPos = (guiHeight - INVENTORY_HEIGHT) / 2;
		int slotX = leftPos + INVENTORY_WIDTH + TRASH_SLOT_X_OFFSET;
		int slotY = topPos + TRASH_SLOT_Y_OFFSET;
		if (!isInsideSlot(event.x(), event.y(), slotX, slotY, TRASH_SLOT_SIZE)) {
			return;
		}

		InventoryScreen screen = (InventoryScreen) (Object) this;
		ItemStack carried = screen.getMenu().getCarried();
		ItemStack slotStack = ExampleModClient.getTrashSlotStack();

		if (!carried.isEmpty()) {
			if (slotStack.isEmpty()) {
				ExampleModClient.setTrashSlotStack(carried);
				screen.getMenu().setCarried(ItemStack.EMPTY);
			} else {
				ExampleModClient.setTrashSlotStack(carried);
				screen.getMenu().setCarried(slotStack.copy());
			}
			cir.setReturnValue(true);
			return;
		}

		if (!slotStack.isEmpty()) {
			screen.getMenu().setCarried(slotStack.copy());
			ExampleModClient.clearTrashSlotStack();
			cir.setReturnValue(true);
			return;
		}

		cir.setReturnValue(true);
	}

	private static boolean isInsideSlot(double mouseX, double mouseY, int slotX, int slotY, int slotSize) {
		return mouseX >= slotX && mouseX < slotX + slotSize && mouseY >= slotY && mouseY < slotY + slotSize;
	}

	private static void drawTrashSlotFrame(GuiGraphicsExtractor extractor, int leftPos, int slotX, int slotY) {
		int connectorX1 = leftPos + INVENTORY_WIDTH - 1;
		int connectorY1 = slotY - 2;
		int connectorX2 = slotX + TRASH_SLOT_SIZE;
		int connectorY2 = slotY + TRASH_SLOT_SIZE + 2;

		extractor.fill(connectorX1, connectorY1, connectorX2, connectorY2, 0xFFC6C6C6);
		extractor.horizontalLine(connectorX1, connectorX2 - 1, connectorY1, 0xFFFFFFFF);
		extractor.verticalLine(connectorX1, connectorY1, connectorY2 - 1, 0xFFFFFFFF);
		extractor.horizontalLine(connectorX1, connectorX2 - 1, connectorY2 - 1, 0xFF555555);
		extractor.verticalLine(connectorX2 - 1, connectorY1, connectorY2 - 1, 0xFF555555);

		extractor.fill(slotX, slotY, slotX + TRASH_SLOT_SIZE, slotY + TRASH_SLOT_SIZE, 0xFF8B8B8B);
		extractor.horizontalLine(slotX, slotX + TRASH_SLOT_SIZE - 1, slotY, 0xFFFFFFFF);
		extractor.verticalLine(slotX, slotY, slotY + TRASH_SLOT_SIZE - 1, 0xFFFFFFFF);
		extractor.horizontalLine(slotX, slotX + TRASH_SLOT_SIZE - 1, slotY + TRASH_SLOT_SIZE - 1, 0xFF373737);
		extractor.verticalLine(slotX + TRASH_SLOT_SIZE - 1, slotY, slotY + TRASH_SLOT_SIZE - 1, 0xFF373737);
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