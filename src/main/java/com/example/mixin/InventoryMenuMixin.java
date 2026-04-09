package com.example.mixin;

import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InventoryMenu.class)
public class InventoryMenuMixin {
	@Unique
	private static final int VANISH_TRASH_SLOT_X = 178;

	@Unique
	private static final int VANISH_TRASH_SLOT_Y = 130;

	@Unique
	private static final int VANISH_TRASH_SLOT_INDEX = 46;

	@Unique
	private final SimpleContainer vanish$trashContainer = new SimpleContainer(1);

	@Inject(method = "<init>", at = @At("TAIL"))
	private void vanish$addTrashSlot(Inventory inventory, boolean active, Player owner, CallbackInfo ci) {
		((AbstractContainerMenuAccessor) (Object) this).vanish$invokeAddSlot(new Slot(vanish$trashContainer, 0, VANISH_TRASH_SLOT_X, VANISH_TRASH_SLOT_Y) {
			@Override
			public ItemStack safeInsert(ItemStack incoming, int maxCount) {
				if (incoming.isEmpty() || !this.mayPlace(incoming)) {
					return incoming;
				}

				// Overwrite behavior: existing trash slot content is deleted.
				if (this.hasItem()) {
					this.set(ItemStack.EMPTY);
				}

				int toPlace = Math.min(Math.min(maxCount, incoming.getCount()), this.getMaxStackSize(incoming));
				ItemStack placed = incoming.copy();
				placed.setCount(toPlace);
				this.set(placed);
				incoming.shrink(toPlace);
				return incoming;
			}

			@Override
			public void setByPlayer(ItemStack stack, ItemStack previousStack) {
				if (!stack.isEmpty() && this.hasItem()) {
					this.set(ItemStack.EMPTY);
				}
				super.setByPlayer(stack, previousStack);
			}
		});
	}

	@Inject(method = "removed", at = @At("TAIL"))
	private void vanish$returnTrashSlotItem(Player player, CallbackInfo ci) {
		if (player.level().isClientSide()) {
			return;
		}

		ItemStack stack = vanish$trashContainer.removeItemNoUpdate(0);
		if (!stack.isEmpty()) {
			player.getInventory().placeItemBackInInventory(stack);
		}
	}
}
