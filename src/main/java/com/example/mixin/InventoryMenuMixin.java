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
	private static final int VANISH_TRASH_SLOT_X = 176;

	@Unique
	private static final int VANISH_TRASH_SLOT_Y = 130;

	@Unique
	private static final int VANISH_TRASH_SLOT_INDEX = 46;

	@Unique
	private final SimpleContainer vanish$trashContainer = new SimpleContainer(1);

	@Inject(method = "<init>", at = @At("TAIL"))
	private void vanish$addTrashSlot(Inventory inventory, boolean active, Player owner, CallbackInfo ci) {
		((AbstractContainerMenuAccessor) (Object) this).vanish$invokeAddSlot(new Slot(vanish$trashContainer, 0, VANISH_TRASH_SLOT_X, VANISH_TRASH_SLOT_Y));
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
