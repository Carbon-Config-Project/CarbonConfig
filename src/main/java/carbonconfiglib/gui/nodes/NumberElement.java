package carbonconfiglib.gui.nodes;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.function.Consumer;

import com.mojang.blaze3d.matrix.MatrixStack;

import carbonconfiglib.api.IRange;
import carbonconfiglib.api.IRange.DoubleRange;
import carbonconfiglib.api.IRange.FloatRange;
import carbonconfiglib.api.IRange.IntegerRange;
import carbonconfiglib.api.IRange.LongRange;
import carbonconfiglib.gui.api.Texts;
import carbonconfiglib.gui.api.node.IValueNode;
import carbonconfiglib.gui.api.types.EntrySettingTypes.FloatingSlider;
import carbonconfiglib.gui.base.helpers.Align;
import carbonconfiglib.gui.base.helpers.Icon;
import carbonconfiglib.gui.base.widgets.CarbonCheckBox;
import carbonconfiglib.gui.base.widgets.CarbonCheckBox.CheckBoxState;
import carbonconfiglib.gui.base.widgets.CarbonEditBox;
import carbonconfiglib.gui.base.widgets.CarbonSlider;
import carbonconfiglib.gui.base.widgets.CarbonSlider.SliderState;
import carbonconfiglib.gui.nodes.base.ValueElement;
import carbonconfiglib.utils.ParseResult;
import net.minecraft.util.text.ITextComponent;

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
public abstract class NumberElement extends ValueElement
{
	private static final DecimalFormat FLOATING_SLIDER_VALUE = new DecimalFormat("0.0#####", DecimalFormatSymbols.getInstance(Locale.ROOT));
	CarbonSlider slider = addChild(new CarbonSlider(0, 0, 0, 0, new SliderState(0, 0, 0)));
	CarbonEditBox text = addChild(new CarbonEditBox(getFont(), 0, 0, Integer.MAX_VALUE, 0));
	CarbonCheckBox subMode = addChild(new CarbonCheckBox(0, 0, 18, 18, new CheckBoxState(Icon.SUB_MODE).setCallback(T -> updateState()).withTooltip(T -> Texts.translatable("gui.carbonconfig.mode."+(T.selected() ? "slider" : "text")))));
	ParseResult<Boolean> result;
	
	public NumberElement(IValueNode node) {
		super(node);
		readValue();
		text.getState().setCallback(this::onTextChanged);
		generateSlider(node.getRange(), slider.getState());
		slider.getState().setListener(this::onSliderChanged);
		if(slider.getState().getRange() > 0) {
			subMode.getState().setValue(true);
		}
	}
	
	@Override
	protected void readValue() {
		text.getState().setValue(node.get());		
	}
	
	@Override
	public void setEditable(boolean value) {
		slider.active = value;
		text.active = value;
		if(!value) text.setFocus(false);
	}
	
	private void updateState() {
		if(!isRightSideEnabled()) return;
		if(slider.getState().getRange() <= 0) return;
		slider.visible = subMode.selected();
		text.visible = !slider.visible;
	}
		
	protected boolean setSliderValue() {
		if(slider.getState().getRange() <= 0) return false;
		try {
			slider.getState().setSilent(parseValue(node.get()));
			return true;
		}
		catch(Exception e) { return false; }
	}
	
	
	protected abstract void generateSlider(IRange range, SliderState state);
	protected abstract long parseValue(String input) throws NumberFormatException;
	protected abstract String toString(long value);
	
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
			setSliderValue();
		}
	}
	
	private void onSliderChanged() {
		text.setTextColor(0xE0E0E0);
		String value = toString(slider.get());
		result = node.isValid(value);
		if(!result.getValue()) {
			text.setTextColor(0xFF0000);
			return;
		}
		setValue(value);
		text.getState().setSilentValue(value);
	}
	
	@Override
	protected void setRightComponentsVisible(boolean value) {
		slider.visible = value && slider.getState().getRange() != 0 && subMode.selected();
		text.visible = value && !slider.visible;
		subMode.visible = value && slider.getState().getRange() != 0;
	}
	
	@Override
	public void renderRightPart(MatrixStack stack, int left, int top, int desiredWidth, int width, int height, int mouseX, int mouseY, boolean selected, float partialTicks) {
		slider.x = left;
		slider.y = top;
		slider.setWidth(desiredWidth);
		slider.setHeight(height);
		slider.render(stack, mouseX, mouseY, partialTicks);
		
		text.x = left;
		text.y = top;
		text.setWidth(desiredWidth);
		text.setHeight(height);
		text.render(stack, mouseX, mouseY, partialTicks);
		if(slider.getState().getRange() > 0 && width - (desiredWidth+2) >= height) {
			subMode.x = left + desiredWidth+2;
			subMode.y = Align.CENTER.alignStart(top, height, subMode.getHeight());
			subMode.setWidth(height);
			subMode.setHeight(height);
			subMode.render(stack, mouseX, mouseY, partialTicks);
		}
	}
	
	@Override
	public void provideTooltips(int mouseX, int mouseY, Consumer<ITextComponent> tooltips) {
		if(result != null && !result.getValue() && isMouseOver(mouseX, mouseY)) {
			tooltips.accept(Texts.literal(result.getError().getMessage()));
		}
		super.provideTooltips(mouseX, mouseY, tooltips);
	}
	
	public static class FloatElement extends NumberElement {
		FloatingSlider slider;
		public FloatElement(IValueNode node) {
			super(node);
		}

		@Override
		protected void generateSlider(IRange range, SliderState state) {
			slider = node.getSetting(FloatingSlider.class);
			if(slider == null) return;
			if(!(range instanceof FloatRange)) return;
			FloatRange floats = (FloatRange)range;
			if((floats.length() / slider.stepSize()) > 99999 && !isForcingAlternative()) return; //We won't allow anything larger than 5 digits. We allow fine control within 1-10000
			state.setMaxValue((long)(floats.max() / slider.stepSize())).setMinValue((long)(floats.min() / slider.stepSize()));
			state.setDisplayFunction(T -> Texts.literal(FLOATING_SLIDER_VALUE.format(T * slider.stepSize())));
			if(!setSliderValue()) {
				state.setMinValue(0).setMaxValue(0);
			}
		}

		@Override
		protected long parseValue(String input) throws NumberFormatException {
			return (long)(Float.parseFloat(input) / slider.stepSize());
		}

		@Override
		protected String toString(long value) {
			return FLOATING_SLIDER_VALUE.format(value * slider.stepSize());
		}
	}
	
	public static class DoubleElement extends NumberElement {
		FloatingSlider slider;
		public DoubleElement(IValueNode node) {
			super(node);
		}

		@Override
		protected void generateSlider(IRange range, SliderState state) {
			slider = node.getSetting(FloatingSlider.class);
			if(slider == null) return;
			if(!(range instanceof DoubleRange)) return;
			DoubleRange doubles = (DoubleRange)range;
			if((doubles.length() / slider.stepSize()) > 99999 && !isForcingAlternative()) return; //We won't allow anything larger than 5 digits. We allow fine control within 1-10000
			state.setMaxValue((long)(doubles.max() / slider.stepSize())).setMinValue((long)(doubles.min() / slider.stepSize()));
			state.setDisplayFunction(T -> Texts.literal(FLOATING_SLIDER_VALUE.format(T * slider.stepSize())));
			if(!setSliderValue()) {
				state.setMinValue(0).setMaxValue(0);
			}
		}

		@Override
		protected long parseValue(String input) throws NumberFormatException {
			return (long)(Double.parseDouble(input) / slider.stepSize());
		}

		@Override
		protected String toString(long value) {
			return FLOATING_SLIDER_VALUE.format(value * slider.stepSize());
		}
	}
	
	public static class IntegerElement extends NumberElement {
		public IntegerElement(IValueNode node) {
			super(node);
		}

		@Override
		protected void generateSlider(IRange range, SliderState state) {			
			if(!(range instanceof IntegerRange)) return;
			IntegerRange ints = (IntegerRange)range;
			if(ints.length() > 99999 && !isForcingAlternative()) return; //We won't allow anything larger than 5 digits. We allow fine control within 1-10000
			state.setMaxValue(ints.max()).setMinValue(ints.min());
			if(!setSliderValue()) {
				state.setMinValue(0).setMaxValue(0);
			}
		}

		@Override
		protected long parseValue(String input) throws NumberFormatException {
			return Integer.parseInt(input);
		}

		@Override
		protected String toString(long value) {
			return Integer.toString((int)(value & 0xFFFFFFFF));
		}
		
	}
	
	public static class LongElement extends NumberElement {
		public LongElement(IValueNode node) {
			super(node);
		}

		@Override
		protected void generateSlider(IRange range, SliderState state) {			
			if(!(range instanceof LongRange)) return;
			LongRange longs = (LongRange)range;
			if(longs.length() > 99999 && !isForcingAlternative()) return; //We won't allow anything larger than 5 digits. We allow fine control within 1-10000
			state.setMaxValue(longs.max()).setMinValue(longs.min());
			if(!setSliderValue()) {
				state.setMinValue(0).setMaxValue(0);
			}
		}

		@Override
		protected long parseValue(String input) throws NumberFormatException {
			return Long.parseLong(input);
		}

		@Override
		protected String toString(long value) {
			return Long.toString(value);
		}
		
	}
}
