package carbonconfiglib.gui.config;

import carbonconfiglib.gui.api.IArrayNode;
import carbonconfiglib.gui.api.IConfigNode;
import carbonconfiglib.gui.api.IValueNode;
import carbonconfiglib.gui.widgets.CarbonEditBox;
import carbonconfiglib.utils.ParseResult;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.network.chat.TextComponent;

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
public class ColorElement extends ConfigElement
{
	CarbonEditBox textBox;
	ParseResult<Boolean> result;
	
	public ColorElement(IConfigNode node, IValueNode value) {
		super(node, value);
	}
	
	public ColorElement(IConfigNode node, IArrayNode array, int index) {
		super(node, array, index);
	}
	
	@Override
	public void init() {
		super.init();
		textBox = addChild(new CarbonEditBox(font, 0, 0, isArray() ? 130 : 52, 18).setInnerDiff(4), isArray() ? GuiAlign.CENTER : GuiAlign.RIGHT, 1);
		textBox.setValue(value.get());
		textBox.setResponder(T -> {
			textBox.setTextColor(0xE0E0E0);
			result = null;
			if(!T.isEmpty()) {
				result = value.isValid(T);
				if(!result.getValue()) {
					textBox.setTextColor(0xFF0000);
					return;
				}
				value.set(T);
			}
		});
	}
	
	@Override
	public void render(PoseStack poseStack, int x, int top, int left, int width, int height, int mouseX, int mouseY, boolean selected, float partialTicks)
	{
		super.render(poseStack, x, top, left, width, height, mouseX, mouseY, selected, partialTicks);
		if(isArray()) {
			GuiComponent.fill(poseStack, left+186, top-1, left+203, top+19, 0xFFA0A0A0);
			GuiComponent.fill(poseStack, left+187, top, left+202, top+18, Integer.decode(value.get()) | 0xFF000000);
		}
		else {
			int xOff = isCompound() ? 106 : 186;
			GuiComponent.fill(poseStack, left+xOff, top-1, left+xOff+17, top+19, 0xFFA0A0A0);
			GuiComponent.fill(poseStack, left+xOff+1, top, left+xOff+16, top+18, Integer.decode(value.get()) | 0xFF000000);
		}
		if(textBox.isMouseOver(mouseX, mouseY) && result != null && !result.getValue()) {
			owner.addTooltips(new TextComponent(result.getError().getMessage()).withStyle(ChatFormatting.RED));
		}
	}
	
	@Override
	public void updateValues() {
		textBox.setValue(value.get());
	}
}
