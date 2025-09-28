package carbonconfiglib.gui.nodes;

import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;

import carbonconfiglib.gui.api.IConfigNode;
import carbonconfiglib.gui.api.INode;
import carbonconfiglib.gui.base.widgets.CarbonList.ListEntry;
import net.minecraft.network.chat.Component;
import speiger.src.collections.objects.utils.ObjectLists;

public class BaseElement extends ListEntry<BaseElement>
{
	protected IElementContext context;
	protected int layer;
	
	public BaseElement() {
	}
	
	public final void setContext(IElementContext context) {
		this.context = context;
	}
	
	public final void setLayer(int layer) {
		this.layer = layer;
	}
	
	@Override
	protected boolean containsSearch(String searchString) { return false; }
	
	@Override
	public void render(PoseStack poseStack, int x, int top, int left, int width, int height, int mouseX, int mouseY, boolean selected, float partialTicks) {
		font.draw(poseStack, Component.literal("top: "+top), left, top, -1);
	}
	
	public List<BaseElement> getChildNodes() {
		return ObjectLists.empty();
	}
	
	protected BaseElement createNode(IConfigNode node) {
		if(!node.isLeaf()) return new FolderElement(node);
		return new TestElement(node.asNode());
	}
	
	protected BaseElement createNode(INode node) {
		return new TestElement(node);
	}
}
