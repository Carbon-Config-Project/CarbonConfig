package carbonconfiglib.gui.widgets;

import com.mojang.blaze3d.vertex.PoseStack;

import carbonconfiglib.gui.config.ConfigElement.GuiAlign;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraftforge.client.gui.ScreenUtils;

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
	
	public CarbonButton(int xPos, int yPos, int width, int height, Component displayString, OnPress handler) {
		super(xPos, yPos, width, height, displayString, handler);
		hash = displayString.getString().hashCode();
	}
	
	@Override
	public void renderButton(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
		Minecraft mc = Minecraft.getInstance();
		int k = this.getYImage(this.isHovered);
		ScreenUtils.blitWithBorder(poseStack, WIDGETS_LOCATION, this.x, this.y, 0, 46 + k * 20, this.width, this.height, 200, 20, 2, 3, 2, 2, this.getBlitOffset());
		this.renderBg(poseStack, mc, mouseX, mouseY);
		GuiUtils.drawScrollingShadowString(poseStack, mc.font, getMessage(), x, y, width, height-2, GuiAlign.CENTER, this.active ? 16777215 : 10526880, hash);
	}
}
