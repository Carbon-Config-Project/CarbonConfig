package carbonconfiglib.gui.nodes;

import com.mojang.blaze3d.vertex.PoseStack;

import carbonconfiglib.gui.api.IValueNode;
import carbonconfiglib.gui.base.widgets.CarbonCheckBox;
import carbonconfiglib.gui.base.widgets.CarbonCheckBox.CheckBoxState;
import carbonconfiglib.gui.nodes.base.ValueElement;
import carbonconfiglib.gui.widgets.Icon;

public class BooleanElement extends ValueElement
{
	CarbonCheckBox box = addChild(new CarbonCheckBox(0, 0, 14, 14, new CheckBoxState(Icon.SELECTED).setCallback(this::onCallback)));
	
	public BooleanElement(IValueNode node) {
		super(node);
		box.getState().setValue(Boolean.parseBoolean(node.get()));
	}

	@Override
	protected void setRightComponentsVisible(boolean value) {
		box.visible = value;
	}
	
	private void onCallback(CheckBoxState state) {
		node.set(Boolean.toString(state.getValue()));
	}
	
	@Override
	public void renderRightPart(PoseStack stack, int left, int top, int width, int height, int mouseX, int mouseY, boolean selected, float partialTicks) {
		box.x = left;
		box.y = top;
		box.render(stack, mouseX, mouseY, partialTicks);
	}
}
