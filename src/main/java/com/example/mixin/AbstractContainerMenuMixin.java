package com.example.mixin;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
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
		ItemStack carried = menu.getCarried();
		ItemStack trashStack = trashSlot.getItem();

		if (!carried.isEmpty()) {
			if (!trashStack.isEmpty()) {
				trashSlot.set(ItemStack.EMPTY);
			}

			int placedCount = Math.min(carried.getCount(), trashSlot.getMaxStackSize(carried));
			ItemStack placed = carried.copy();
			placed.setCount(placedCount);
			trashSlot.set(placed);
			carried.shrink(placedCount);
			menu.setCarried(carried);
			ci.cancel();
			return;
		}

		if (!trashStack.isEmpty()) {
			menu.setCarried(trashStack.copy());
			trashSlot.set(ItemStack.EMPTY);
			ci.cancel();
		}
	}
}
