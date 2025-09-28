package carbonconfiglib.gui.nodes;

import com.mojang.blaze3d.vertex.PoseStack;

import carbonconfiglib.gui.api.INode;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.network.chat.Component;

public class TestElement extends ValueElement
{
	
	public TestElement(INode node) {
		super(node);
	}

	@Override
	protected void setRightComponentsVisible(boolean value) {}
	
	@Override
	public void renderRightPart(PoseStack stack, int left, int top, int width, int height, int mouseX, int mouseY, boolean selected, float partialTicks) {
		GuiComponent.fill(stack, left, top, left+width, top+height, 0xFFFF00FF);
		font.draw(stack, Component.empty().append("Value: ").append(node.getName()).append(", "+width+", "+left), left, top, -1);	
	}
	
	
}
