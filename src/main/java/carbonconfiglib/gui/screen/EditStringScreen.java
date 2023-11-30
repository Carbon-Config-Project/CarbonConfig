package carbonconfiglib.gui.screen;

import carbonconfiglib.gui.api.BackgroundTexture;
import carbonconfiglib.gui.api.BackgroundTexture.BackgroundHolder;
import carbonconfiglib.gui.api.IConfigNode;
import carbonconfiglib.gui.api.IValueNode;
import carbonconfiglib.gui.config.ElementList;
import carbonconfiglib.gui.widgets.CarbonButton;
import carbonconfiglib.gui.widgets.CarbonEditBox;
import carbonconfiglib.gui.widgets.screen.CarbonScreen;
import carbonconfiglib.utils.ParseResult;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiYesNo;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;
import speiger.src.collections.objects.lists.ObjectArrayList;

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
public class EditStringScreen extends CarbonScreen
{
	IChatComponent title;
	GuiScreen parent;
	IConfigNode node;
	IValueNode value;
	CarbonEditBox textBox;
	boolean valid = true;
	BackgroundHolder texture;
	ParseResult<Boolean> result;

	public EditStringScreen(GuiScreen parent, IChatComponent name, IConfigNode node, IValueNode value, BackgroundHolder texture) {
		this.title = name;
		this.parent = parent;
		this.node = node;
		this.value = value;
		this.value.createTemp();
		this.texture = texture == null ? BackgroundTexture.DEFAULT.asHolder() : texture;
	}
	
	@Override
	public void initGui() {
		super.initGui();
		int x = width / 2 - 100;
		GuiButton apply = addWidget(new CarbonButton(x+10, 160, 85, 20, I18n.format("gui.carbonconfig.apply"), this::save));
		addWidget(new CarbonButton(x+105, 160, 85, 20, I18n.format("gui.carbonconfig.cancel"), this::cancel));
		textBox = new CarbonEditBox(fontRendererObj, x, 113, 200, 18);
		addWidget(textBox);
		textBox.setText(value.get());
		textBox.setListener(T -> {
			textBox.setTextColor(0xE0E0E0);
			valid = true;
			result = value.isValid(T);
			if(!result.getValue()) {
				textBox.setTextColor(0xFF0000);
				valid = false;
			}
			apply.enabled = valid;
			if(valid) value.set(textBox.getText());
		});
	}
	
	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		ElementList.renderBackground(0, width, 0, height, 0F, texture.getTexture());
		ElementList.renderListOverlay(0, width, 103, 142, width, height, texture.getTexture());
		super.drawScreen(mouseX, mouseY, partialTicks);
		String title = this.title.getFormattedText();
		fontRendererObj.drawString(title, (width/2)-(fontRendererObj.getStringWidth(title)/2), 85, -1);
		if(textBox.isMouseOver(mouseX, mouseY) && result != null && !result.getValue()) {
			drawHoveringText(new ObjectArrayList<>(fontRendererObj.listFormattedStringToWidth(result.getError().getMessage(), Integer.MAX_VALUE)), mouseX, mouseY);
		}
	}
	
	@Override
	public void onClose() {
		value.setPrevious();
		mc.displayGuiScreen(parent);
	}
	
	private void save(GuiButton button) {
		if(!valid) return;
		value.apply();
		mc.displayGuiScreen(parent);
	}
	
	private void cancel(GuiButton button) {
		if(value.isChanged()) {
			mc.displayGuiScreen(new GuiYesNo((T, k) -> {
				if(T) value.setPrevious();
				mc.displayGuiScreen(T ? parent : this);
			}, new ChatComponentTranslation("gui.carbonconfig.warn.changed").getFormattedText(), new ChatComponentTranslation("gui.carbonconfig.warn.changed.desc").setChatStyle(new ChatStyle().setColor(EnumChatFormatting.GRAY)).getFormattedText(), 0));
			return;
		}
		value.setPrevious();
		mc.displayGuiScreen(parent);
	}
}
