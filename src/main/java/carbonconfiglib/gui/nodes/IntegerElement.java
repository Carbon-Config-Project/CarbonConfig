package carbonconfiglib.gui.nodes;

import com.mojang.blaze3d.vertex.PoseStack;

import carbonconfiglib.api.IRange;
import carbonconfiglib.api.IRange.IntegerRange;
import carbonconfiglib.gui.api.IValueNode;
import carbonconfiglib.gui.base.widgets.CarbonEditBox;
import carbonconfiglib.gui.base.widgets.CarbonSlider;
import carbonconfiglib.gui.base.widgets.CarbonSlider.SliderState;
import carbonconfiglib.gui.nodes.base.ValueElement;
import carbonconfiglib.utils.ParseResult;

public class IntegerElement extends ValueElement
{
	CarbonSlider slider = addChild(new CarbonSlider(0, 0, 0, 0, new SliderState(0, 0, 0).setListener(this::onSliderChanged)));
	CarbonEditBox text = addChild(new CarbonEditBox(getFont(), 0, 0, Integer.MAX_VALUE, 0));
	ParseResult<Boolean> result;
	
	public IntegerElement(IValueNode node) {
		super(node);
		text.getState().setValue(node.get());
		text.getState().setCallback(this::onTextChanged);
		generateSlider(node.getRange());
	}
	
	private void generateSlider(IRange range) {
		if(!(range instanceof IntegerRange)) return;
		IntegerRange ints = (IntegerRange)range;
		if(ints.length() > 99999) return; //We won't allow anything larger than 5 digits. We allow fine control within 1-1000
		slider.getState().setMinValue(ints.min()).setMaxValue(ints.max());
		if(!setSliderValue()) {
			slider.getState().setMinValue(0).setMaxValue(0);
		}
	}
	
	private boolean setSliderValue() {
		int range = slider.getState().getRange();
		if(range <= 0 || range > 99999) return false;
		try {
			slider.getState().setSilent(Integer.parseInt(node.get()));
			return true;
		}
		catch(Exception e) { return false; }
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
			node.set(value);
			setSliderValue();
		}
	}
	
	private void onSliderChanged() {
		text.setTextColor(0xE0E0E0);
		String value = Integer.toString(slider.get());
		result = node.isValid(value);
		if(!result.getValue()) {
			text.setTextColor(0xFF0000);
			return;
		}
		node.set(value);
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
}
