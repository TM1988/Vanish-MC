package com.example.mixin;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractContainerMenu.class)
public class AbstractContainerMenuMixin {
	private static final int VANISH_TRASH_SLOT_INDEX = 46;

	@Inject(method = "clicked", at = @At("HEAD"), cancellable = true)
	private void vanish$handleTrashSlotClick(int slotId, int button, ContainerInput action, Player player, CallbackInfo ci) {
		Object self = this;
		if (!(self instanceof InventoryMenu)) {
			return;
		}
		InventoryMenu menu = (InventoryMenu) self;

		if (slotId != VANISH_TRASH_SLOT_INDEX || action != ContainerInput.PICKUP) {
			return;
		}

		Slot trashSlot = menu.getSlot(VANISH_TRASH_SLOT_INDEX);
		if (!menu.getCarried().isEmpty() && trashSlot.hasItem()) {
			// Overwrite behavior: delete old trash item before vanilla places new carried stack.
			trashSlot.setByPlayer(net.minecraft.world.item.ItemStack.EMPTY);
		}
	}
}
