package carbonconfiglib.gui.screen;

import com.mojang.blaze3d.matrix.MatrixStack;

import carbonconfiglib.gui.api.BackgroundTexture;
import carbonconfiglib.gui.api.BackgroundTexture.BackgroundHolder;
import carbonconfiglib.gui.api.IConfigNode;
import carbonconfiglib.gui.api.IValueNode;
import carbonconfiglib.gui.config.ElementList;
import carbonconfiglib.utils.ParseResult;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

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
	Screen parent;
	IConfigNode node;
	IValueNode value;
	TextFieldWidget textBox;
	boolean valid = true;
	BackgroundHolder texture;
	ParseResult<Boolean> result;

	public EditStringScreen(Screen parent, ITextComponent name, IConfigNode node, IValueNode value, BackgroundHolder texture) {
		super(name);
		this.parent = parent;
		this.node = node;
		this.value = value;
		this.value.createTemp();
		this.texture = texture == null ? BackgroundTexture.DEFAULT.asHolder() : texture;
	}
	
	@Override
	protected void init() {
		super.init();
		int x = width / 2 - 100;
		Button apply = addButton(new Button(x+10, 160, 85, 20, new TranslationTextComponent("gui.carbonconfig.apply"), this::save));
		addButton(new Button(x+105, 160, 85, 20, new TranslationTextComponent("gui.carbonconfig.cancel"), this::cancel));
		textBox = new TextFieldWidget(font, x, 113, 200, 18, new StringTextComponent(""));
		addButton(textBox);
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
	public void render(MatrixStack stack, int mouseX, int mouseY, float partialTicks) {
		ElementList.renderBackground(0, width, 0, height, 0F, texture.getTexture());
		ElementList.renderListOverlay(0, width, 103, 142, width, height, texture.getTexture());
		super.render(stack, mouseX, mouseY, partialTicks);
		font.draw(stack, title, (width/2)-(font.width(title)/2), 85, -1);
		if(textBox.isMouseOver(mouseX, mouseY) && result != null && !result.getValue()) {
			renderToolTip(stack, new ObjectArrayList<>(font.split(new StringTextComponent(result.getError().getMessage()), Integer.MAX_VALUE)), mouseX, mouseY, font);
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
			}, new TranslationTextComponent("gui.carbonconfig.warn.changed"), new TranslationTextComponent("gui.carbonconfig.warn.changed.desc").withStyle(TextFormatting.GRAY)));
			return;
		}
		value.setPrevious();
		minecraft.setScreen(parent);
	}
}
