package carbonconfiglib.gui.config;

import com.mojang.blaze3d.vertex.PoseStack;

import carbonconfiglib.gui.api.IArrayNode;
import carbonconfiglib.gui.api.IConfigNode;
import carbonconfiglib.gui.api.IValueNode;
import carbonconfiglib.gui.screen.EditStringScreen;
import carbonconfiglib.gui.screen.ListSelectionScreen;
import carbonconfiglib.gui.widgets.CarbonButton;
import carbonconfiglib.gui.widgets.GuiUtils;
import carbonconfiglib.utils.ParseResult;
import net.minecraft.client.gui.components.Button;
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
public class EnumElement extends ConfigElement
{
	ParseResult<Boolean> result;
	
	public EnumElement(IConfigNode node, IValueNode value) {
		super(node, value);
	}
	
	public EnumElement(IConfigNode node, IArrayNode array, int index) {
		super(node, array, index);
	}
	
	@Override
	public void init() {
		super.init();
		if(!hasSuggestions() || isArray()) {
			if(this.isArray()) {
				addChild(new CarbonButton(0, 0, 40, 18, Component.translatable("gui.chunk_pregen.config.edit"), this::onSelect), -12);				
			}
			else {
				addChild(new CarbonButton(0, 0, 72, 18, Component.translatable("gui.chunk_pregen.config.edit"), this::onPress));
			}
		}
	}
	
	@Override
	protected int getMaxX(int prevMaxX) {
		return super.getMaxX(prevMaxX) - 140;
	}
	
	@Override
	public void render(PoseStack poseStack, int x, int top, int left, int width, int height, int mouseX, int mouseY, boolean selected, float partialTicks) {
		super.render(poseStack, x, top, left, width, height, mouseX, mouseY, selected, partialTicks);
		String value = this.value.get();
		GuiUtils.drawScrollingString(poseStack, font, Component.literal(value), left + width - 235, top, 135, height - 2.75F, GuiAlign.LEFT, -1, 0);
	}
	
	private void onSelect(Button button) {
		mc.setScreen(ListSelectionScreen.ofValue(mc.screen, node, value, owner.getCustomTexture()));
	}
	
	private void onPress(Button button) {
		mc.setScreen(new EditStringScreen(mc.screen, name, node, value, owner.getCustomTexture()));
	}
}
