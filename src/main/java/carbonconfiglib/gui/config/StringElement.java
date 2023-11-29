package carbonconfiglib.gui.config;

import carbonconfiglib.gui.api.IArrayNode;
import carbonconfiglib.gui.api.IConfigNode;
import carbonconfiglib.gui.api.IValueNode;
import carbonconfiglib.gui.screen.EditStringScreen;
import carbonconfiglib.gui.widgets.CarbonButton;
import carbonconfiglib.gui.widgets.CarbonEditBox;
import carbonconfiglib.utils.ParseResult;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;

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
	CarbonEditBox edit;
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
			edit.setText(value.get());
			edit.setListener(T -> {
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
			addChild(new CarbonButton(0, 0, 72, 18, I18n.format("gui.carbonconfig.edit"), this::onPress));
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
	public void render(int x, int top, int left, int width, int height, int mouseX, int mouseY, boolean selected, float partialTicks) {
		super.render(x, top, left, width, height, mouseX, mouseY, selected, partialTicks);
		if(edit != null && edit.isMouseOver(mouseX, mouseY) && result != null && !result.getValue()) {
			owner.addTooltips(new TextComponentString(result.getError().getMessage()).setStyle(new Style().setColor(TextFormatting.RED)));			
		}
	}
	
	@Override
	public void updateValues() {
		if(edit != null) {
			edit.setText(value.get());
		}
	}
	
	private void onPress(GuiButton button) {
		mc.displayGuiScreen(new EditStringScreen(mc.currentScreen, name, node, value, owner.getCustomTexture()));
	}
}
