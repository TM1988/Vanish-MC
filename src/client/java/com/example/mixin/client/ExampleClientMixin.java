package com.example.mixin.client;

import com.example.ExampleModClient;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InventoryScreen.class)
public class ExampleClientMixin {
	@Inject(method = "extractBackground", at = @At("TAIL"))
	private void vanish$drawInventorySidePanel(GuiGraphicsExtractor extractor, int mouseX, int mouseY, float tickDelta, CallbackInfo ci) {
		if (!ExampleModClient.isInventoryPanelVisible()) {
			return;
		}

		int inventoryWidth = 176;
		int inventoryHeight = 166;
		int leftPos = (extractor.guiWidth() - inventoryWidth) / 2;
		int topPos = (extractor.guiHeight() - inventoryHeight) / 2;

		int panelX1 = leftPos + inventoryWidth + 5;
		int panelY1 = topPos;
		int panelX2 = panelX1 + 56;
		int panelY2 = panelY1 + inventoryHeight;

		extractor.fill(panelX1, panelY1, panelX2, panelY2, 0xFFFFFFFF);
		extractor.outline(panelX1, panelY1, panelX2 - panelX1, panelY2 - panelY1, 0xFF000000);
	}
}