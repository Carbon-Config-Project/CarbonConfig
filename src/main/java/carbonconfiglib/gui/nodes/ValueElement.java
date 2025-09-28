package carbonconfiglib.gui.nodes;

import java.util.function.Consumer;

import com.mojang.blaze3d.vertex.PoseStack;

import carbonconfiglib.gui.api.INode;
import carbonconfiglib.gui.base.helpers.GuiUtils;
import carbonconfiglib.gui.config.ConfigElement.GuiAlign;
import net.minecraft.network.chat.Component;

public abstract class ValueElement extends BaseElement
{
	protected INode node;
	protected boolean right;

	public ValueElement(INode node) {
		this.node = node;
	}
	
	@Override
	public final void render(PoseStack poseStack, int x, int top, int left, int width, int height, int mouseX, int mouseY, boolean selected, float partialTicks) {
		int leftWidth = context.calculateSegmentWidth();
		renderLeftPart(poseStack, left, top, leftWidth-4, height, mouseX, mouseY, selected, partialTicks);
		if(context.isAtTop(layer)) {
			if(!right) {
				right = true;
				setRightComponentsVisible(true);
			}
			renderRightPart(poseStack, left+leftWidth+6, top, width-leftWidth, height, mouseX, mouseY, selected, partialTicks);
		}
		else if(right) {
			right = false;
			setRightComponentsVisible(false);
		}
	}
	
	protected abstract void setRightComponentsVisible(boolean value);
	
	@Override
	public final void provideTooltips(int mouseX, int mouseY, Consumer<Component> tooltips) {
		super.provideTooltips(mouseX, mouseY, tooltips);
		
	}
	
	public void renderLeftPart(PoseStack stack, int left, int top, int width, int height, int mouseX, int mouseY, boolean selected, float partialTicks) {
		GuiUtils.drawScrollingShadowText(stack, font, node.getName(), left, top, width, height, GuiAlign.LEFT, -1, node.hashCode());
	}
	
	public void renderRightPart(PoseStack stack, int left, int top, int width, int height, int mouseX, int mouseY, boolean selected, float partialTicks) {
		
	}
}
