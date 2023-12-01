package carbonconfiglib.gui.widgets;

import java.util.function.Consumer;

import org.lwjgl.opengl.GL11;

import carbonconfiglib.gui.widgets.screen.IWidget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;

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
public class CarbonHoverIconButton extends GuiButton implements IWidget
{
	Consumer<CarbonHoverIconButton> listener;
	Icon[] icons;
	IconInfo info;
	
	public CarbonHoverIconButton(int x, int y, int width, int height, IconInfo info, Icon basicIcon, Icon hoverIcon, Consumer<CarbonHoverIconButton> listener) {
		super(0, x, y, width, height, "");	
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
	public void render(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
		if(!visible) return;
		this.field_146123_n = mousePressed(mc, mouseX, mouseY);
		int j = this.enabled ? 16777215 : 10526880;
        GL11.glColor4f(((j >> 16) & 0xFF) / 255F, ((j >> 8) & 0xFF) / 255F, (j & 0xFF) / 255F, 1F);
		GuiUtils.drawTextureRegion(xPosition + info.xOff, yPosition + info.yOff, info.width, info.height, icons[field_146123_n ? 1 : 0], 16, 16);
		GL11.glColor4f(1F, 1F, 1F, 1F);
	}
	
	@Override
	public boolean mouseClick(double mouseX, double mouseY, int button) {
		if(this.enabled && this.visible && mouseX >= this.xPosition && mouseY >= this.yPosition && mouseX < this.xPosition + this.width && mouseY < this.yPosition + this.height) {
			func_146113_a(Minecraft.getMinecraft().getSoundHandler());
			if(listener != null) listener.accept(this);
			return true;
		}
		return false;
	}
	
	@Override
	public void setX(int x) { this.xPosition = x; }
	@Override
	public void setY(int y) { this.yPosition = y; }
	@Override
	public int getX() { return xPosition; }
	@Override
	public int getY() { return yPosition; }
	@Override
	public int getWidgetWidth() { return width; }
	@Override
	public int getWidgetHeight() { return height; }
	@Override
	public boolean isHovered() { return field_146123_n; }
	
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
