package carbonconfiglib.gui.nodeElements;

import java.util.function.ObjIntConsumer;

import com.mojang.blaze3d.vertex.PoseStack;

import carbonconfiglib.gui.api.IConfigNode;

public class FolderElement extends BaseElement implements IFolderNode
{
	IConfigNode node;
	ObjIntConsumer<IConfigNode> listener;
	int index;

	public FolderElement(IConfigNode node) {
		this.node = node;
	}
	
	@Override
	public void setCallbacks(int index, ObjIntConsumer<IConfigNode> listener) {
		this.index = index;
		this.listener = listener;
	}	
	
	@Override
	public void render(PoseStack poseStack, int x, int top, int left, int width, int height, int mouseX, int mouseY, boolean selected, float partialTicks) {
		font.draw(poseStack, node.getName(), left, top, -1);
	}
	
	@Override
	public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
		if(listener != null) {
			listener.accept(node, index);
		}
		return true;
	}
}
