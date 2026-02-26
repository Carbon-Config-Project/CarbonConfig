package carbonconfiglib.gui.nodes;

import com.mojang.blaze3d.vertex.PoseStack;

import carbonconfiglib.gui.api.node.IValueNode;
import carbonconfiglib.gui.api.types.EntrySettingTypes.ColorType;
import carbonconfiglib.gui.base.screen.BaseCarbonScreen;
import carbonconfiglib.gui.base.widgets.CarbonSlider;
import carbonconfiglib.gui.base.widgets.CarbonSlider.SliderState;
import carbonconfiglib.gui.nodes.base.ValueElement;
import carbonconfiglib.impl.entries.ColorValue.ColorWrapper;
import net.minecraft.network.chat.Component;

/**
 * Copyright 2026 Speiger, Meduris
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
public class ColorElement extends ValueElement
{
	CarbonSlider red = addChild(new CarbonSlider(0, 0, 0, 0, new SliderState(0, 0, 255).setPrefix(Component.literal("Red: ")).setListener(this::updateSliders)));
	CarbonSlider green = addChild(new CarbonSlider(0, 0, 0, 0, new SliderState(0, 0, 255).setPrefix(Component.literal("Green: ")).setListener(this::updateSliders)));
	CarbonSlider blue = addChild(new CarbonSlider(0, 0, 0, 0, new SliderState(0, 0, 255).setPrefix(Component.literal("Blue: ")).setListener(this::updateSliders)));
	CarbonSlider alpha = addChild(new CarbonSlider(0, 0, 0, 0, new SliderState(0, 0, 255).setPrefix(Component.literal("Alpha: ")).setListener(this::updateSliders)));

	public ColorElement(IValueNode node) {
		super(node);
	}
	
	@Override
	public int getItemHeight() {
		return hasAlpha() ? 60 : 48;
	}

	@Override
	protected void readValue() {
		int value = getColor();
		red.getState().set((value >> 16) & 0xFF);
		green.getState().set((value >> 8) & 0xFF);
		blue.getState().set(value & 0xFF);
		alpha.getState().set((value >> 24) & 0xFF);
	}
	
	@Override
	protected void setEditable(boolean value) {
		red.active = value;
		green.active = value;
		blue.active = value;
		alpha.active = value;
	}
	
	@Override
	protected void setRightComponentsVisible(boolean value) {
		red.visible = value;
		green.visible = value;
		blue.visible = value;
		alpha.visible = value && hasAlpha();
	}
	
	protected void updateSliders() {
		setValue(hasAlpha() ? ColorWrapper.serializeRGB(generateColor()) : ColorWrapper.serialize(generateColor()));
		
	}
	
	protected boolean hasAlpha() {
		ColorType type = node.getSetting(ColorType.class);
		return type != null && type.hasAlpha();
	}
	
	private int getColor() {
		return ColorWrapper.parseInt(node.get()).getValue();
	}
	
	private int generateColor() {
		return (int)((hasAlpha() ? (alpha.get() & 0xFF) << 24 : 0xFF000000) | (red.get() & 0xFF) << 16 | (green.get() & 0xFF) << 8 | (blue.get() & 0xFF));
	}
	
	@Override
	public void renderRightPart(PoseStack stack, int left, int top, int width, int height, int mouseX, int mouseY, boolean selected, float partialTicks) {
		int color = generateColor();
		BaseCarbonScreen.fill(stack, left, top, left+20, top+height-(hasAlpha() ? 1 : 0), color);
		
		int realWidth = width - 40;
		int realHeight = hasAlpha() ? (height >> 2) - 1 : height / 3;
		int xOff = width - realWidth;
		red.x = left + xOff;
		red.y = top;
		red.setWidth(realWidth);
		red.setHeight(realHeight);
		red.render(stack, mouseX, mouseY, partialTicks);
		BaseCarbonScreen.fill(stack, left+21, top, red.x-1, top+red.getHeight(), (int)(0xFF000000 | (red.get() & 0xFF) << 16));
		top += red.getHeight()+1;
		
		green.x = left + xOff;
		green.y = top;
		green.setWidth(realWidth);
		green.setHeight(realHeight);
		green.render(stack, mouseX, mouseY, partialTicks);
		BaseCarbonScreen.fill(stack, left+21, top, green.x-1, top+green.getHeight(), (int)(0xFF000000 | (green.get() & 0xFF) << 8));
		top += green.getHeight()+1;
		
		blue.x = left + xOff;
		blue.y = top;
		blue.setWidth(realWidth);
		blue.setHeight(realHeight);
		blue.render(stack, mouseX, mouseY, partialTicks);
		BaseCarbonScreen.fill(stack, left+21, top, blue.x-1, top+blue.getHeight(), (int)(0xFF000000 | (blue.get() & 0xFF)));
		top += blue.getHeight()+1;
		
		if(hasAlpha()) {
			alpha.x = left + xOff;
			alpha.y = top;
			alpha.setWidth(realWidth);
			alpha.setHeight(realHeight);
			alpha.render(stack, mouseX, mouseY, partialTicks);
			float value = alpha.get() / 255F;
			int r = (int)((red.get() * value)) & 0xFF;
			int g = (int)((green.get() * value)) & 0xFF;
			int b = (int)((blue.get() * value)) & 0xFF;
			BaseCarbonScreen.fill(stack, left+21, top, alpha.x-1, top+alpha.getHeight(), (int)(0xFF000000 | r << 16 | g << 8 | b));
		}
	}
	
}
