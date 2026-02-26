package carbonconfiglib.gui.base.widgets;

import java.util.Optional;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import carbonconfiglib.gui.base.helpers.Align;
import carbonconfiglib.gui.base.helpers.GuiUtils;
import carbonconfiglib.gui.base.helpers.Icon;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;


/**
 * Copyright 2026 Speiger, Meduris
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
public class CarbonButton extends CarbonBaseButton {
	Optional<Icon> icon = Optional.empty();
	int hash;
	boolean selected = false;
	boolean highlighted = false;

	public CarbonButton(int pX, int pY, int pWidth, int pHeight, Component pMessage, OnPress listener) {
		super(pX, pY, pWidth, pHeight, pMessage, listener);
		this.hash = pMessage.getString().hashCode();
	}
	
	public CarbonButton withIcon(Optional<Icon> icon) {
		this.icon = icon;
		return this;
	}
	
	public CarbonButton setHighlighted(boolean value) {
		highlighted = value;
		return this;
	}
	
	public CarbonButton setSelected(boolean value) {
		selected = value;
		return this;
	}
	
	@Override
	public int getFGColor() {
		return super.getFGColor();
	}
	
	@Override
	public void updateNarration(NarrationElementOutput pNarrationElementOutput) {
		defaultButtonNarrationText(pNarrationElementOutput);
	}
	
	public void renderIcon(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
		int j = getFGColor();
		RenderSystem.setShaderColor(((j >> 16) & 0xFF) / 255F, ((j >> 8) & 0xFF) / 255F, (j & 0xFF) / 255F, 1F);
		GuiUtils.drawTextureRegion(pPoseStack, x + (width >> 1) - 6, y + (height >> 1) - 6, 11, 11, icon.get(), 16, 16);
		RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
	}
	
	@Override
	public void renderButton(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
		int k = this.getYImage(this.isHoveredOrFocused());
		if(selected) {
			RenderSystem.setShaderColor(0.5F, 0.5F, 0.5F, 1.0F);
			GuiUtils.blitWithBorder(pPoseStack, WIDGETS_LOCATION, x, y, 0, 46 + k * 20, this.width, this.height, 200, 20, 2, 3, 2, 2, this.getBlitOffset(), true);
			RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
		}
		else GuiUtils.blitWithBorder(pPoseStack, WIDGETS_LOCATION, x, y, 0, 46 + k * 20, this.width, this.height, 200, 20, 2, 3, 2, 2, this.getBlitOffset(), false);
		if(highlighted) {
			Screen.fill(pPoseStack, x+2, y+2, x+getWidth()-2, y+getHeight()-2, 0x33FFFFFF);
		}
		
		if (icon.isPresent()) {
			renderIcon(pPoseStack, pMouseX, pMouseY, pPartialTick);
		}
		GuiUtils.drawScrollingShadowText(pPoseStack, Minecraft.getInstance().font, getMessage(), x+2, y+2, width-4, height-4, Align.CENTER, this.active ? 16777215 : 10526880, hash);
	}
}
