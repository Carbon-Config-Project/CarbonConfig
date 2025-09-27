package carbonconfiglib.gui.nodeElements;

import com.mojang.blaze3d.vertex.PoseStack;

import carbonconfiglib.gui.api.IConfigNode;
import net.minecraft.network.chat.Component;

public class TestElement extends BaseElement
{
	IConfigNode node;

	public TestElement(IConfigNode node) {
		this.node = node;
	}
	
	@Override
	public void render(PoseStack poseStack, int x, int top, int left, int width, int height, int mouseX, int mouseY, boolean selected, float partialTicks) {
		font.draw(poseStack, Component.empty().append("Value: ").append(node.getName()), left, top, -1);
	}
}
