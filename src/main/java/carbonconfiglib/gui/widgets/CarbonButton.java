package carbonconfiglib.gui.widgets;

import java.util.function.Consumer;

import carbonconfiglib.gui.config.ConfigElement.GuiAlign;
import carbonconfiglib.gui.widgets.screen.IWidget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;

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
public class CarbonButton extends GuiButton implements IWidget
{
	int hash;
	Consumer<GuiButton> handler;
	
	public CarbonButton(int xPos, int yPos, int width, int height, String displayString, Consumer<GuiButton> handler) {
		super(0, xPos, yPos, width, height, displayString);
		hash = displayString.hashCode();
		this.handler = handler;
	}
	
	@Override
	public boolean mouseClick(double mouseX, double mouseY, int button) {
		if(this.enabled && this.visible && mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height) {
			playPressSound(Minecraft.getMinecraft().getSoundHandler());
			if(handler != null) handler.accept(this);
			return true;
		}
		return false;
	}
	
	@Override
	public void render(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
		if(!visible) return;
		this.hovered = mousePressed(mc, mouseX, mouseY);
		int k = this.getHoverState(this.hovered);
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1F);
		GuiUtils.drawTextureWithBorder(BUTTON_TEXTURES, this.x, this.y, 0, 46 + k * 20, this.width, this.height, 200, 20, 2, 3, 2, 2, 0);
		GuiUtils.drawScrollingShadowString(mc.fontRenderer, displayString, x, y, width, height-2, GuiAlign.CENTER, this.enabled ? 16777215 : 10526880, hash);
	}

	@Override
	public void setX(int x) { this.x = x; }
	@Override
	public void setY(int y) { this.y = y; }
	@Override
	public int getX() { return x; }
	@Override
	public int getY() { return y; }
	@Override
	public int getWidgetWidth() { return width; }
	@Override
	public int getWidgetHeight() { return height; }
	@Override
	public boolean isHovered() { return hovered; }
	
}
