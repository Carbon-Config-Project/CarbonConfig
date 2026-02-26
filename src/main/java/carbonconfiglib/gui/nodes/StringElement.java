package carbonconfiglib.gui.nodes;

import com.mojang.blaze3d.vertex.PoseStack;

import carbonconfiglib.gui.api.node.IValueNode;
import carbonconfiglib.gui.base.widgets.CarbonEditBox;
import carbonconfiglib.gui.base.widgets.CarbonEditBox.TextState;
import carbonconfiglib.gui.nodes.base.ValueElement;
import carbonconfiglib.utils.ParseResult;

public class StringElement extends ValueElement
{
	CarbonEditBox text = addChild(new CarbonEditBox(getFont(), 0, 0, Integer.MAX_VALUE, 0, new TextState().setMaxLength(Integer.MAX_VALUE)));
	ParseResult<Boolean> result;

	public StringElement(IValueNode node) {
		super(node);
		readValue();
		text.moveCursorToStart();
		text.getState().setCallback(this::onTextChanged);
	}
	
	@Override
	protected void readValue() {
		text.getState().setValue(node.get());
	}
	
	@Override
	public void setEditable(boolean value) {
		text.active = value;
		if(!value) text.setFocus(false);
	}
	
	@Override
	protected void setRightComponentsVisible(boolean value) {
		text.visible = value;
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
		}
	}
	
	@Override
	public void renderRightPart(PoseStack stack, int left, int top, int width, int height, int mouseX, int mouseY, boolean selected, float partialTicks) {
		text.x = left;
		text.y = top;
		text.setWidth(width);
		text.setHeight(height);
		text.render(stack, mouseX, mouseY, partialTicks);
	}
}
