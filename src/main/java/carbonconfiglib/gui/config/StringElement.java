package carbonconfiglib.gui.config;

import carbonconfiglib.gui.api.IArrayNode;
import carbonconfiglib.gui.api.IConfigNode;
import carbonconfiglib.gui.api.IValueNode;
import carbonconfiglib.gui.screen.EditStringScreen;
import carbonconfiglib.gui.widgets.CarbonButton;
import carbonconfiglib.gui.widgets.CarbonEditBox;
import carbonconfiglib.utils.ParseResult;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;

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
public class StringElement extends ConfigElement
{
	EditBox edit;
	ParseResult<Boolean> result;
	
	public StringElement(IConfigNode node, IValueNode value) {
		super(node, value);
	}
	
	public StringElement(IConfigNode node, IArrayNode array, int index) {
		super(node, array, index);
	}
	
	@Override
	public void init()
	{
		super.init();
		if(this.isArray()) {
			edit = addChild(new CarbonEditBox(font, 0, 0, 150, 18), GuiAlign.CENTER, 0);
			edit.setValue(value.get());
			edit.setResponder(T -> {
				edit.setTextColor(0xE0E0E0);
				result = null;
				if(!T.isEmpty() && !(result = value.isValid(T)).getValue()) {
					edit.setTextColor(0xFF0000);
					return;
				}
				value.set(T);
			});
		}
		else {
			addChild(new CarbonButton(0, 0, 72, 18, new TranslatableComponent("gui.carbonconfig.edit"), this::onPress));
		}
	}
	
	@Override
	public void tick() {
		super.tick();
		if(edit != null) {
			edit.tick();
		}
	}
	
	@Override
	public void render(PoseStack poseStack, int x, int top, int left, int width, int height, int mouseX, int mouseY, boolean selected, float partialTicks) {
		super.render(poseStack, x, top, left, width, height, mouseX, mouseY, selected, partialTicks);
		if(edit != null && edit.isMouseOver(mouseX, mouseY) && result != null && !result.getValue()) {
			owner.addTooltips(new TextComponent(result.getError().getMessage()).withStyle(ChatFormatting.RED));
		}
	}
	
	@Override
	public void updateValues() {
		if(edit != null) {
			edit.setValue(value.get());
		}
	}
	
	private void onPress(Button button) {
		mc.setScreen(new EditStringScreen(mc.screen, name, node, value, owner.getCustomTexture()));
	}
}
