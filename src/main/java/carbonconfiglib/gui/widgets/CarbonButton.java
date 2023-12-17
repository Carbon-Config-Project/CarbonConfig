package carbonconfiglib.gui.widgets;

import com.mojang.blaze3d.vertex.PoseStack;

import carbonconfiglib.gui.config.ConfigElement.GuiAlign;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

import java.util.function.Supplier;

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

	public CarbonButton(int i, int j, int k, int l, Component component, OnPress onPress, CreateNarration createNarration) {
		super(i, j, k, l, component, onPress, createNarration);
		hash = component.getString().hashCode();
	}

	public CarbonButton(int i, int j, int k, int l, Component component, OnPress onPress) {
		this(i, j, k, l, component, onPress, Supplier::get);
	}

	@Override
	public void renderWidget(GuiGraphics poseStack, int mouseX, int mouseY, float partialTick) {
		Minecraft mc = Minecraft.getInstance();
		int k = this.getYImage(this.isHovered);
		GuiUtils.drawTextureWithBorder(poseStack, WIDGETS_LOCATION, this.getX(), this.getY(), 0, 46 + k * 20, this.width, this.height, 200, 20, 2, 3, 2, 2);
		GuiUtils.drawScrollingShadowString(poseStack, mc.font, getMessage(), getX(), getY(), width, height-2, GuiAlign.CENTER, this.active ? 16777215 : 10526880, hash);
	}

	protected int getYImage(boolean isHovered) {
		int i = 1;
		if (!this.active) {
			i = 0;
		} else if (isHovered) {
			i = 2;
		}

		return i;
	}
}
