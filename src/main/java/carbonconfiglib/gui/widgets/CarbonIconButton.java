package carbonconfiglib.gui.widgets;

import java.util.function.Consumer;

import com.mojang.blaze3d.systems.RenderSystem;

import carbonconfiglib.gui.config.ConfigElement.GuiAlign;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

/**
 * Copyright 2023 Speiger, Meduris
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
public class CarbonIconButton extends AbstractButton
{
	Consumer<CarbonIconButton> listener;
	Icon icon;
	boolean iconOnly = false;
	int hash;
	
	public CarbonIconButton(int x, int y, int width, int height, Icon icon, Component name, Consumer<CarbonIconButton> listener) {
		super(x, y, width, height, name);
		this.listener = listener;
		this.icon = icon;
		this.hash = name.getString().hashCode();
	}
	
	public CarbonIconButton setIconOnly() {
		iconOnly = true;
		return this;
	}
	
	@Override
	public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float p_93679_) {
	    graphics.blitSprite(SPRITES.get(this.active, this.isHoveredOrFocused()), this.getX(), this.getY(), this.getWidth(), this.getHeight());
        if(iconOnly) {
    		int j = getFGColor();
            RenderSystem.setShaderColor(((j >> 16) & 0xFF) / 255F, ((j >> 8) & 0xFF) / 255F, (j & 0xFF) / 255F, 1F);
    		GuiUtils.drawTextureRegion(graphics, getX() + (width / 2) - 5.5F, getY()+height/2-5.5F, 11, 11, icon, 16, 16);
    		RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
        	return;
        }
        
		Minecraft minecraft = Minecraft.getInstance();
		Font font = minecraft.font;
		int width = font.width(getMessage()) + 21;
		float minX = getX() + 4 + (this.width / 2) - (width / 2);
		int j = getFGColor();
        RenderSystem.setShaderColor(((j >> 16) & 0xFF) / 255F, ((j >> 8) & 0xFF) / 255F, (j & 0xFF) / 255F, 1F);
		GuiUtils.drawTextureRegion(graphics, minX, getY()+(height-8)/2, 11, 11, icon, 16, 16);
		RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
		GuiUtils.drawScrollingShadowString(graphics, font, getMessage(), minX+15, getY(), width, height-2, GuiAlign.CENTER, getFGColor(), hash);
	}
	
	@Override
	public void onPress() {
		if(listener == null) return;
		listener.accept(this);
	}
	
	@Override
	public void updateWidgetNarration(NarrationElementOutput output) {
		this.defaultButtonNarrationText(output);
	}
}
