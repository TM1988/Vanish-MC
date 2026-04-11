package com.example.mixin.client;

import com.example.ExampleModClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.world.inventory.ContainerInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractContainerScreen.class)
public class AbstractContainerScreenMixin {
	private static final int TRASH_SLOT_SIZE = 18;
	private static final int TRASH_SLOT_X = 178;
	private static final int TRASH_SLOT_Y = 130;
	private static final int TRASH_SLOT_INDEX = 46;
	private boolean vanish$consumeNextRelease;

	@Inject(method = "hasClickedOutside", at = @At("HEAD"), cancellable = true)
	private void vanish$includeTrashSlotAsInside(double mouseX, double mouseY, int leftPos, int topPos, CallbackInfoReturnable<Boolean> cir) {
		if (!ExampleModClient.isInventoryPanelVisible()) {
			return;
		}

		if (!((Object) this instanceof InventoryScreen)) {
			return;
		}

		int slotX = leftPos + TRASH_SLOT_X;
		int slotY = topPos + TRASH_SLOT_Y;
		if (mouseX >= slotX && mouseX < slotX + TRASH_SLOT_SIZE && mouseY >= slotY && mouseY < slotY + TRASH_SLOT_SIZE) {
			cir.setReturnValue(false);
		}
	}

	@Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
	private void vanish$routeTrashSlotClick(MouseButtonEvent event, boolean dblClick, CallbackInfoReturnable<Boolean> cir) {
		if (!ExampleModClient.isInventoryPanelVisible()) {
			return;
		}

		if (!((Object) this instanceof InventoryScreen screen)) {
			return;
		}

		int leftPos = (screen.width - 176) / 2;
		int topPos = (screen.height - 166) / 2;
		int slotX = leftPos + TRASH_SLOT_X;
		int slotY = topPos + TRASH_SLOT_Y;
		boolean inside = event.x() >= slotX && event.x() < slotX + TRASH_SLOT_SIZE
			&& event.y() >= slotY && event.y() < slotY + TRASH_SLOT_SIZE;
		if (!inside) {
			return;
		}

		Minecraft client = Minecraft.getInstance();
		if (client.player == null || client.gameMode == null) {
			return;
		}

		int button = event.button() == 1 ? 1 : 0;
		client.gameMode.handleContainerInput(screen.getMenu().containerId, TRASH_SLOT_INDEX, button, ContainerInput.PICKUP, client.player);
		vanish$consumeNextRelease = true;
		cir.setReturnValue(true);
	}

	@Inject(method = "mouseReleased", at = @At("HEAD"), cancellable = true)
	private void vanish$consumeTrashSlotRelease(MouseButtonEvent event, CallbackInfoReturnable<Boolean> cir) {
		if (!vanish$consumeNextRelease) {
			return;
		}

		vanish$consumeNextRelease = false;
		cir.setReturnValue(true);
	}
}
