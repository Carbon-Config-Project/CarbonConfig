package carbonconfiglib.gui.base.widgets;

import java.util.Optional;

import com.mojang.blaze3d.systems.RenderSystem;

import carbonconfiglib.gui.base.helpers.Align;
import carbonconfiglib.gui.base.helpers.GuiUtils;
import carbonconfiglib.gui.base.helpers.Icon;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;


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
	public static final WidgetSprites SPRITES = new WidgetSprites(ResourceLocation.tryParse("widget/button"), ResourceLocation.tryParse("widget/button_disabled"), ResourceLocation.tryParse("widget/button_highlighted"));
	Optional<Icon> icon = Optional.empty();
	int hash;
	int padding = 3;
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
	
	public CarbonButton setPadding(int value) {
		this.padding = value;
		return this;
	}
	
	@Override
	public void updateWidgetNarration(NarrationElementOutput pNarrationElementOutput) {
		defaultButtonNarrationText(pNarrationElementOutput);
	}
	
	public void renderIcon(GuiGraphics graphics, int pMouseX, int pMouseY, float pPartialTick) {
		int j = getFGColor();
		RenderSystem.setShaderColor(((j >> 16) & 0xFF) / 255F, ((j >> 8) & 0xFF) / 255F, (j & 0xFF) / 255F, 1F);
		GuiUtils.drawTextureRegion(graphics, getX() + padding, getY() + padding, width-padding*2, height-padding*2, icon.get(), 16, 16);
		RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
	}
	
	@Override
	public void renderWidget(GuiGraphics graphics, int pMouseX, int pMouseY, float pPartialTick) {
		if(selected) {
			RenderSystem.setShaderColor(0.5F, 0.5F, 0.5F, 1.0F);
			graphics.blitSprite(SPRITES.get(active, isHovered()), getX(), getY(), width, height);
			RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
		}
		else graphics.blitSprite(SPRITES.get(active, isHovered()), getX(), getY(), width, height);
		if(highlighted) {
			graphics.fill(getX()+2, getY()+2, getX()+getWidth()-2, getY()+getHeight()-2, 0x33FFFFFF);
		}
		
		if (icon.isPresent()) {
			renderIcon(graphics, pMouseX, pMouseY, pPartialTick);
		}
		GuiUtils.drawScrollingShadowText(graphics, Minecraft.getInstance().font, getMessage(), getX()+2, getY()+2, width-4, height-4, Align.CENTER, this.active ? 16777215 : 10526880, hash);
	}
}
