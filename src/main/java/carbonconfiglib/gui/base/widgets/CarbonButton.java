package carbonconfiglib.gui.base.widgets;

import java.util.Optional;

import carbonconfiglib.gui.base.helpers.Align;
import carbonconfiglib.gui.base.helpers.GuiUtils;
import carbonconfiglib.gui.base.helpers.Icon;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ARGB;


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
	
	@Override
	protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
		if(selected) graphics.blitSprite(RenderPipelines.GUI_TEXTURED, SPRITES.get(active, isHovered()), getX(), getY(), width, height, ARGB.colorFromFloat(1F, 0.5F, 0.5F, 0.5F));
		else graphics.blitSprite(RenderPipelines.GUI_TEXTURED, SPRITES.get(active, isHovered()), getX(), getY(), width, height);
		
		if(highlighted) graphics.fill(getX()+2, getY()+2, getX()+getWidth()-2, getY()+getHeight()-2, 0x33FFFFFF);
		
		if(icon.isPresent()) renderIcon(graphics, mouseX, mouseY, partialTick);
		GuiUtils.drawScrollingShadowText(graphics, Minecraft.getInstance().font, getMessage(), getX()+2, getY()+2, width-3, height-3, Align.CENTER, this.active ? -1 : 0xFFA0A0A0, hash);

	}
	
	public void renderIcon(GuiGraphicsExtractor graphics, int pMouseX, int pMouseY, float pPartialTick) {
		icon.get().drawIcon(graphics, getX() + padding, getY() + padding, width-padding*2, height-padding*2, 16, 16, getFGColor());
	}
}
