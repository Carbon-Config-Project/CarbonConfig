package carbonconfiglib.gui.screen;

import com.mojang.blaze3d.vertex.PoseStack;

import carbonconfiglib.gui.api.BackgroundTexture;
import carbonconfiglib.gui.api.IConfigNode;
import carbonconfiglib.gui.api.IValueNode;
import carbonconfiglib.gui.config.ElementList;
import carbonconfiglib.utils.ParseResult;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.client.gui.widget.ExtendedButton;

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
public class EditStringScreen extends Screen
{
	private static final BackgroundTexture DEFAULT = BackgroundTexture.of(BACKGROUND_LOCATION).build();
	
	Screen parent;
	IConfigNode node;
	IValueNode value;
	EditBox textBox;
	boolean valid = true;
	BackgroundTexture texture;
	ParseResult<Boolean> result;

	public EditStringScreen(Screen parent, Component name, IConfigNode node, IValueNode value, BackgroundTexture texture) {
		super(name);
		this.parent = parent;
		this.node = node;
		this.value = value;
		this.value.createTemp();
		this.texture = texture == null ? DEFAULT : texture;
	}
	
	@Override
	protected void init() {
		super.init();
		int x = width / 2 - 100;
		Button apply = addRenderableWidget(new ExtendedButton(x+10, 160, 85, 20, Component.translatable("gui.carbonconfig.apply"), this::save));
		addRenderableWidget(new ExtendedButton(x+105, 160, 85, 20, Component.translatable("gui.carbonconfig.cancel"), this::cancel));
		textBox = new EditBox(font, x, 113, 200, 18, Component.empty());
		addRenderableWidget(textBox);
		textBox.setValue(value.get());
		textBox.setResponder(T -> {
			textBox.setTextColor(0xE0E0E0);
			valid = true;
			result = value.isValid(T);
			if(!result.getValue()) {
				textBox.setTextColor(0xFF0000);
				valid = false;
			}
			apply.active = valid;
			if(valid) value.set(textBox.getValue());
		});
	}
	
	@Override
	public void render(PoseStack stack, int mouseX, int mouseY, float partialTicks) {
		ElementList.renderBackground(0, width, 0, height, 0F, texture);
		ElementList.renderListOverlay(0, width, 103, 142, width, height, texture);
		super.render(stack, mouseX, mouseY, partialTicks);
		font.draw(stack, title, (width/2)-(font.width(title)/2), 85, -1);
		if(textBox.isMouseOver(mouseX, mouseY) && result != null && !result.getValue()) {
			renderTooltip(stack, Component.literal(result.getError().getMessage()), mouseX, mouseY);
		}
	}
	
	@Override
	public void onClose() {
		value.setPrevious();
		minecraft.setScreen(parent);
	}
	
	private void save(Button button) {
		if(!valid) return;
		value.apply();
		minecraft.setScreen(parent);
	}
	
	private void cancel(Button button) {
		if(value.isChanged()) {
			minecraft.setScreen(new ConfirmScreen(T -> {
				if(T) value.setPrevious();
				minecraft.setScreen(T ? parent : this);
			}, Component.translatable("gui.carbonconfig.warn.changed"), Component.translatable("gui.carbonconfig.warn.changed.desc").withStyle(ChatFormatting.GRAY)));
			return;
		}
		value.setPrevious();
		minecraft.setScreen(parent);
	}
}
