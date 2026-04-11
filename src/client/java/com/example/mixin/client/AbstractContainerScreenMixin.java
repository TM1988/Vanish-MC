package com.example.mixin.client;

import com.example.ExampleModClient;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractContainerScreen.class)
public class AbstractContainerScreenMixin {
	private static final int TRASH_SLOT_SIZE = 18;
	private static final int TRASH_SLOT_X = 178;
	private static final int TRASH_SLOT_Y = 130;

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
}
