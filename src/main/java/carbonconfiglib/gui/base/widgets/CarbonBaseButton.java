package carbonconfiglib.gui.base.widgets;

import java.util.function.Consumer;
import java.util.function.Function;

import carbonconfiglib.gui.base.helpers.Align;
import carbonconfiglib.gui.base.helpers.GuiUtils;
import carbonconfiglib.gui.base.helpers.ITooltipProvider;
import carbonconfiglib.gui.base.interaction.IWidget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.IChatComponent;


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
public class CarbonBaseButton extends GuiButton implements ITooltipProvider, IWidget
{
	protected Function<CarbonBaseButton, IChatComponent> tooltip;
	Consumer<CarbonBaseButton> handler;
	
	public CarbonBaseButton(int x, int y, int width, int height, IChatComponent message, Consumer<CarbonBaseButton> handler) {
		super(0, x, y, width, height, message.getFormattedText());
		this.handler = handler;
	}
	
	protected void onPress() {
		if(handler != null) handler.accept(this);		
	}
	
	@Override
	public boolean mouseClick(double mouseX, double mouseY, int button) {
		if(isMouseOver(mouseX, mouseY)) {
			playPressSound(Minecraft.getMinecraft().getSoundHandler());
			onPress();
			return true;
		}
		return false;
	}
	
	@Override
	public boolean isMouseOver(double mouseX, double mouseY) {
		return this.enabled && this.visible && mouseX >= this.xPosition && mouseY >= this.yPosition && mouseX < this.xPosition + this.width && mouseY < this.yPosition + this.height;
	}
	
	public void setHeight(int height) {
		this.height = height;
	}
	
	public void setMessage(String text) {
		this.displayString = text;
	}
	
	@Override
	public void setActive(boolean value) {
		this.enabled = value;
	}

	@Override
	public boolean isActive() {
		return enabled;
	}

	@SuppressWarnings("unchecked")
	public <T extends CarbonBaseButton> T withTooltip(IChatComponent tooltip) {
		this.tooltip = T -> tooltip;
		return (T)this;
	}
	
	@SuppressWarnings("unchecked")
	public <T extends CarbonBaseButton> T withTooltip(Function<T, IChatComponent> tooltip) {
		this.tooltip = (Function<CarbonBaseButton, IChatComponent>)tooltip;
		return (T)this;
	} 
	
	protected boolean canShowTooltip(double mouseX, double mouseY) {
		return this.visible && mouseX >= (double)this.xPosition && mouseY >= (double)this.yPosition && mouseX < (double)(this.xPosition + this.width) && mouseY < (double)(this.yPosition + this.height);
	}
	
	@Override
	public void provideTooltips(int mouseX, int mouseY, Consumer<IChatComponent> tooltips) {
		if(tooltip != null && canShowTooltip(mouseX, mouseY)) {
			hovered = false;
			IChatComponent result = tooltip.apply(this);
			if(result == null) return;
			tooltips.accept(result);
		}
	}

	@Override
	public void render(int mouseX, int mouseY, float partialTicks) {
		if(!visible) return;
		Minecraft mc = Minecraft.getMinecraft();
		this.hovered = mousePressed(mc, mouseX, mouseY);
		int k = this.getHoverState(this.hovered);
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1F);
		GuiUtils.blitWithBorder(buttonTextures, this.xPosition, this.yPosition, 0, 46 + k * 20, this.width, this.height, 200, 20, 2, 3, 2, 2, 0, false);
		GuiUtils.drawScrollingShadowText(mc.fontRendererObj, displayString, xPosition, yPosition, width, height-2, Align.CENTER, this.enabled ? 16777215 : 10526880, 0);
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
	public int getWidth() { return width; }
	@Override
	public int getHeight() { return height; }
	@Override
	public boolean isHovered() { return hovered; }
}
