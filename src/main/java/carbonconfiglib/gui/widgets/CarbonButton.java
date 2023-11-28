package carbonconfiglib.gui.widgets;

import com.mojang.blaze3d.platform.GlStateManager;

import carbonconfiglib.gui.config.ConfigElement.GuiAlign;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.button.Button;

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
public class CarbonButton extends Button
{
	int hash;
	
	public CarbonButton(int xPos, int yPos, int width, int height, String displayString, IPressable handler) {
		super(xPos, yPos, width, height, displayString, handler);
		hash = displayString.hashCode();
	}
	
	@Override
	public void renderButton(int mouseX, int mouseY, float partialTick) {
		Minecraft mc = Minecraft.getInstance();
		int k = this.getYImage(this.isHovered);
		GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1F);
		GuiUtils.drawTextureWithBorder(WIDGETS_LOCATION, this.x, this.y, 0, 46 + k * 20, this.width, this.height, 200, 20, 2, 3, 2, 2, blitOffset);
		this.renderBg(mc, mouseX, mouseY);
		GuiUtils.drawScrollingShadowString(mc.fontRenderer, getMessage(), x, y, width, height-2, GuiAlign.CENTER, getFGColor(), hash);
	}
}
