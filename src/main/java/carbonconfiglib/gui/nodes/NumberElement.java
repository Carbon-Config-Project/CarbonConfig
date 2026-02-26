package carbonconfiglib.gui.nodes;

import com.mojang.blaze3d.vertex.PoseStack;

import carbonconfiglib.api.IRange;
import carbonconfiglib.api.IRange.IntegerRange;
import carbonconfiglib.api.IRange.LongRange;
import carbonconfiglib.gui.api.node.IValueNode;
import carbonconfiglib.gui.base.widgets.CarbonEditBox;
import carbonconfiglib.gui.base.widgets.CarbonSlider;
import carbonconfiglib.gui.base.widgets.CarbonSlider.SliderState;
import carbonconfiglib.gui.nodes.base.ValueElement;
import carbonconfiglib.utils.ParseResult;

public abstract class NumberElement extends ValueElement
{
	CarbonSlider slider = addChild(new CarbonSlider(0, 0, 0, 0, new SliderState(0, 0, 0).setListener(this::onSliderChanged)));
	CarbonEditBox text = addChild(new CarbonEditBox(getFont(), 0, 0, Integer.MAX_VALUE, 0));
	ParseResult<Boolean> result;
	
	public NumberElement(IValueNode node) {
		super(node);
		readValue();
		text.getState().setCallback(this::onTextChanged);
		generateSlider(node.getRange(), slider.getState());
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
		slider.visible = value && slider.getState().getRange() != 0;
		text.visible = value && !slider.visible;
	}
	
	@Override
	public void renderRightPart(PoseStack stack, int left, int top, int width, int height, int mouseX, int mouseY, boolean selected, float partialTicks) {
		slider.x = left;
		slider.y = top;
		slider.setWidth(width);
		slider.setHeight(height);
		slider.render(stack, mouseX, mouseY, partialTicks);
		
		text.x = left;
		text.y = top;
		text.setWidth(width);
		text.setHeight(height);
		text.render(stack, mouseX, mouseY, partialTicks);
	}
	
	public static class IntegerElement extends NumberElement {
		public IntegerElement(IValueNode node) {
			super(node);
		}

		@Override
		protected void generateSlider(IRange range, SliderState state) {			
			if(!(range instanceof IntegerRange)) return;
			IntegerRange ints = (IntegerRange)range;
			if(ints.length() > 99999) return; //We won't allow anything larger than 5 digits. We allow fine control within 1-1000
			state.setMinValue(ints.min()).setMaxValue(ints.max());
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
			if(longs.length() > 99999) return; //We won't allow anything larger than 5 digits. We allow fine control within 1-1000
			state.setMinValue(longs.min()).setMaxValue(longs.max());
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
