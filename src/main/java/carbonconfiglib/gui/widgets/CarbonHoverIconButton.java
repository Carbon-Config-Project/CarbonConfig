package carbonconfiglib.gui.widgets;

import java.util.function.Consumer;

import com.mojang.blaze3d.systems.RenderSystem;

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
public class CarbonHoverIconButton extends AbstractButton
{
	Consumer<CarbonHoverIconButton> listener;
	Icon[] icons;
	IconInfo info;
	
	public CarbonHoverIconButton(int x, int y, int width, int height, IconInfo info, Icon basicIcon, Icon hoverIcon, Consumer<CarbonHoverIconButton> listener) {
		super(x, y, width, height, Component.empty());
		this.listener = listener;
		this.icons = new Icon[2];
		icons[0] = basicIcon;
		icons[1] = hoverIcon;
		this.info = info;
	}
	
	public CarbonHoverIconButton setIconOnly() {
		return this;
	}
	
	@Override
	public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float p_93679_) {
		int j = getFGColor();
        RenderSystem.setShaderColor(((j >> 16) & 0xFF) / 255F, ((j >> 8) & 0xFF) / 255F, (j & 0xFF) / 255F, 1F);
		GuiUtils.drawTextureRegion(graphics, getX() + info.xOff, getY() + info.yOff, info.width, info.height, icons[isHoveredOrFocused() ? 1 : 0], 16, 16);
		RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
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
	
	public static class IconInfo {
		int xOff;
		int yOff;
		int width;
		int height;
		
		public IconInfo(int xOff, int yOff, int width, int height) {
			this.xOff = xOff;
			this.yOff = yOff;
			this.width = width;
			this.height = height;
		}
	}
}
