package carbonconfiglib.gui.nodes;

import com.mojang.blaze3d.vertex.PoseStack;

import carbonconfiglib.gui.api.IValueNode;
import carbonconfiglib.gui.base.helpers.GuiUtils;
import carbonconfiglib.gui.config.ConfigElement.GuiAlign;

public abstract class ValueElement extends BaseElement
{
	protected IValueNode node;
	protected boolean right;

	public ValueElement(IValueNode node) {
		this.node = node;
	}
	
	@Override
	public void renderLeftPart(PoseStack stack, int left, int top, int width, int height, int mouseX, int mouseY, boolean selected, float partialTicks) {
		GuiUtils.drawScrollingShadowText(stack, font, node.getName(), left, top, width, height, GuiAlign.LEFT, -1, node.hashCode());
	}
	
	@Override
	public void renderRightPart(PoseStack stack, int left, int top, int width, int height, int mouseX, int mouseY, boolean selected, float partialTicks) {
		
	}
}
