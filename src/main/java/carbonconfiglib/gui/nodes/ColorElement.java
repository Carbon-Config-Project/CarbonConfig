package carbonconfiglib.gui.nodes;

import carbonconfiglib.gui.api.node.IValueNode;
import carbonconfiglib.gui.api.types.EntrySettingTypes.ColorType;
import carbonconfiglib.gui.base.helpers.GuiUtils;
import carbonconfiglib.gui.base.helpers.Texts;
import carbonconfiglib.gui.base.screen.BaseCarbonScreen;
import carbonconfiglib.gui.base.widgets.CarbonEditBox;
import carbonconfiglib.gui.base.widgets.CarbonSlider;
import carbonconfiglib.gui.base.widgets.CarbonSlider.SliderState;
import carbonconfiglib.gui.nodes.base.ValueElement;
import carbonconfiglib.impl.entries.ColorValue.ColorWrapper;
import carbonconfiglib.utils.ParseResult;

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
	CarbonSlider red = addChild(new CarbonSlider(0, 0, 0, 0, new SliderState(0, 0, 255).setPrefix(Texts.literal("Red: ")).setListener(this::updateSliders)));
	CarbonSlider green = addChild(new CarbonSlider(0, 0, 0, 0, new SliderState(0, 0, 255).setPrefix(Texts.literal("Green: ")).setListener(this::updateSliders)));
	CarbonSlider blue = addChild(new CarbonSlider(0, 0, 0, 0, new SliderState(0, 0, 255).setPrefix(Texts.literal("Blue: ")).setListener(this::updateSliders)));
	CarbonSlider alpha = addChild(new CarbonSlider(0, 0, 0, 0, new SliderState(0, 0, 255).setPrefix(Texts.literal("Alpha: ")).setListener(this::updateSliders)));
	CarbonEditBox text = addChild(new CarbonEditBox(getFont(), 0, 0, Integer.MAX_VALUE, 0));
	ParseResult<Boolean> result;
	boolean hasAlpha;
	FormatType type;
	
	public ColorElement(IValueNode node) {
		super(node);
		type = node.isCarbon() ? FormatType.HEX_NUMBER : FormatType.guessType(node.get());
		hasAlpha = hasAlpha() || (!node.isCarbon() && FormatType.hasAlpha(node.getDefault(), type));
		readValue();
		text.getState().setCallback(this::onTextChanged);
	}
	
	@Override
	public int getItemHeight() {
		if(isForcingText()) return super.getItemHeight();
		return (hasAlpha ? 50 : 38) + 20;
	}

	@Override
	protected void readValue() {
		text.getState().setSilentValue(node.get());
		readSliders();
		text.setTextColor(0xE0E0E0);
		result = null;
	}
	
	private void readSliders() {
		int value = getColor();
		red.getState().setSilent((value >> 16) & 0xFF);
		green.getState().setSilent((value >> 8) & 0xFF);
		blue.getState().setSilent(value & 0xFF);
		alpha.getState().setSilent((value >> 24) & 0xFF);
	}
	
	@Override
	protected void setEditable(boolean value) {
		red.enabled = value;
		green.enabled = value;
		blue.enabled = value;
		alpha.enabled = value;
		text.setActive(value);
		if(!value) {
			text.setFocused(false);
		}
	}
	
	@Override
	protected void setRightComponentsVisible(boolean value) {
		boolean notText = !isForcingText();
		red.visible = value && notText;
		green.visible = value && notText;
		blue.visible = value && notText;
		alpha.visible = value && hasAlpha && notText;
		text.setVisible(value);
	}
	
	private void onTextChanged(String value) {
		text.setTextColor(0xE0E0E0);
		result = null;
		if(!value.isEmpty()) {
			result = node.isValid(value);
			if(!result.getValue()) {
				text.setTextColor(0xFF0000);
				return;
			}
			setValue(value);
			readSliders();
		}
	}
	
	protected void updateSliders() {
		setValue(type.serialize(generateColor(), hasAlpha));
		text.getState().setSilentValue(node.get());
	}
	
	protected boolean hasAlpha() {
		ColorType type = node.getSetting(ColorType.class);
		return type != null && type.hasAlpha();
	}
	
	private int getColor() {
		return type.parse(node.get());
	}
	
	private int generateColor() {
		return (int)((hasAlpha ? (alpha.get() & 0xFF) << 24 : 0xFF000000) | (red.get() & 0xFF) << 16 | (green.get() & 0xFF) << 8 | (blue.get() & 0xFF));
	}
	
	@Override
	public void renderRightPart(int left, int top, int desiredWidth, int width, int height, int mouseX, int mouseY, boolean selected, float partialTicks) {
		int color = generateColor();
		BaseCarbonScreen.drawRect(left, top, left+20, top+height-(hasAlpha ? 1 : 0), color);
		GuiUtils.drawFrame(left, top, left+20, top+height-(hasAlpha ? 2 : 1), 0xFF848484, 1F);
		
		if(isForcingText()) {
			int realWidth = desiredWidth - 23;
			int xOff = desiredWidth - realWidth;
			realWidth = Math.min(97, realWidth) - 1;
			text.x = left + xOff;
			text.y = top + 1;
			text.setWidth(realWidth);
			text.setHeight(height - 2);
			text.render(mouseX, mouseY, partialTicks);
			return;
		}
		int realWidth = desiredWidth - 40;
		int realHeight = hasAlpha ? (height / 5) - 1 : height >> 2;
		int xOff = desiredWidth - realWidth;
		realWidth = Math.min(80, realWidth);
		
		red.x = left + xOff;
		red.y = top;
		red.setWidth(realWidth);
		red.setHeight(realHeight);
		red.render(mouseX, mouseY, partialTicks);
		BaseCarbonScreen.drawRect(left+23, top+1, red.x-2, top+red.getHeight()-1, (int)(0xFF000000 | (red.get() & 0xFF) << 16));
		GuiUtils.drawFrame(left+22, top, red.x-2, top+red.getHeight()-1, 0xFF848484, 1F);
		top += red.getHeight()+1;
		
		green.x = left + xOff;
		green.y = top;
		green.setWidth(realWidth);
		green.setHeight(realHeight);
		green.render(mouseX, mouseY, partialTicks);
		BaseCarbonScreen.drawRect(left+23, top+1, green.x-2, top+green.getHeight()-1, (int)(0xFF000000 | (green.get() & 0xFF) << 8));
		GuiUtils.drawFrame(left+22, top, green.x-2, top+green.getHeight()-1, 0xFF848484, 1F);
		top += green.getHeight()+1;
		
		blue.x = left + xOff;
		blue.y = top;
		blue.setWidth(realWidth);
		blue.setHeight(realHeight);
		blue.render(mouseX, mouseY, partialTicks);
		BaseCarbonScreen.drawRect(left+23, top+1, blue.x-2, top+blue.getHeight()-1, (int)(0xFF000000 | (blue.get() & 0xFF)));
		GuiUtils.drawFrame(left+22, top, blue.x-2, top+blue.getHeight()-1, 0xFF848484, 1F);
		top += blue.getHeight()+1;
		
		if(hasAlpha) {
			alpha.x = left + xOff;
			alpha.y = top;
			alpha.setWidth(realWidth);
			alpha.setHeight(realHeight);
			alpha.render(mouseX, mouseY, partialTicks);
			float value = alpha.get() / 255F;
			int r = (int)((red.get() * value)) & 0xFF;
			int g = (int)((green.get() * value)) & 0xFF;
			int b = (int)((blue.get() * value)) & 0xFF;
			BaseCarbonScreen.drawRect(left+23, top+1, alpha.x-2, top+alpha.getHeight()-1, (int)(0xFF000000 | r << 16 | g << 8 | b));
			GuiUtils.drawFrame(left+22, top, alpha.x-2, top+alpha.getHeight()-1, 0xFF848484, 1F);
			top += alpha.getHeight()+1;
		}
		
		text.x = left + 23;
		text.y = top+1;
		text.setWidth(realWidth+xOff-24);
		text.setHeight(realHeight-1);
		text.render(mouseX, mouseY, partialTicks);
	}
	
	public static enum FormatType {
		NUMBER {
			@Override
			public boolean isValid(String input) {
				try {
					Long.parseLong(input, 16);
					return true; 
				}
				catch(Exception e) { return false; }
			}
			
			@Override
			public int parse(String input) {
				try { return (int)Long.parseLong(input, 16); }
				catch(Exception e) { e.printStackTrace(); }
				return 0;
			}
			
			@Override
			public String serialize(int input, boolean hasAlpha) {
				return hasAlpha ? ColorWrapper.serializeNumber(input) : ColorWrapper.serializeNumberRGB(input);
			}
		},
		HEX_NUMBER {
			@Override
			public boolean isValid(String input) {
				return ColorWrapper.parseInt(input).isValid();
			}
			
			@Override
			public int parse(String input) {
				ParseResult<Integer> result = ColorWrapper.parseInt(input);
				return !result.isValid() ? -1 : result.getValue();
			}
			
			@Override
			public String serialize(int input, boolean hasAlpha) {
				return hasAlpha ? ColorWrapper.serialize(input) : ColorWrapper.serializeRGB(input);
			}
		},
		HTML_NUMBER {
			@Override
			public boolean isValid(String input) {
				return ColorWrapper.parseInt(input).isValid();
			}
			
			@Override
			public int parse(String input) {
				ParseResult<Integer> result = ColorWrapper.parseInt(input);
				return !result.isValid() ? -1 : result.getValue();
			}
			
			@Override
			public String serialize(int input, boolean hasAlpha) {
				return hasAlpha ? ColorWrapper.serializeHTML(input) : ColorWrapper.serializeHTMLRGB(input);
			}
		};
		
		public abstract boolean isValid(String input);
		public abstract int parse(String input);
		public abstract String serialize(int input, boolean hasAlpha);
		
		public static FormatType guessType(String value) {
			for(FormatType type : values()) {
				if(type.isValid(value)) return type;
			}
			return null;
		}
		
		public static boolean hasAlpha(String input, FormatType type) {
			return (type.parse(input) & 0xFF000000) != 0;
		}
	}
}
