package carbonconfiglib.gui.nodes.base;

import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;

import carbonconfiglib.gui.api.DataType;
import carbonconfiglib.gui.api.IConfigNode;
import carbonconfiglib.gui.api.INode;
import carbonconfiglib.gui.api.IValueNode;
import carbonconfiglib.gui.base.widgets.CarbonList.ListEntry;
import carbonconfiglib.gui.nodes.BooleanElement;
import carbonconfiglib.gui.nodes.DoubleElement;
import carbonconfiglib.gui.nodes.EnumElement;
import carbonconfiglib.gui.nodes.FolderElement;
import carbonconfiglib.gui.nodes.NumberElement.IntegerElement;
import carbonconfiglib.gui.nodes.NumberElement.LongElement;
import carbonconfiglib.gui.nodes.StringElement;
import carbonconfiglib.gui.nodes.TestElement;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import speiger.src.collections.objects.utils.ObjectLists;

public abstract class BaseElement extends ListEntry<BaseElement>
{
	protected IElementContext context;
	private boolean right;
	protected int layer;
	
	public BaseElement() {
	}
	
	public final void setContext(IElementContext context) {
		this.context = context;
		setRightComponentsVisible(false);
	}
	
	public final void setLayer(int layer) {
		this.layer = layer;
	}
	
	@Override
	protected boolean containsSearch(String searchString) { return false; }
	
	@Override
	public final void render(PoseStack poseStack, int x, int top, int left, int width, int height, int mouseX, int mouseY, boolean selected, float partialTicks) {
		int leftWidth = context.calculateSegmentWidth()-4;
		renderLeftPart(poseStack, left, top, leftWidth, height, mouseX, mouseY, selected, partialTicks);
		if(context.isAtTop(layer)) {
			if(!right) {
				right = true;
				setRightComponentsVisible(true);
			}
			renderRightPart(poseStack, left+leftWidth+8, top, width-leftWidth-10, height, mouseX, mouseY, selected, partialTicks);
		}
		else if(right) {
			right = false;
			setRightComponentsVisible(false);
		}
	}
	
	protected abstract void setRightComponentsVisible(boolean value);
	
	public abstract void renderLeftPart(PoseStack stack, int left, int top, int width, int height, int mouseX, int mouseY, boolean selected, float partialTicks);
	
	public abstract void renderRightPart(PoseStack stack, int left, int top, int width, int height, int mouseX, int mouseY, boolean selected, float partialTicks);

	
	public List<BaseElement> getChildNodes() {
		return ObjectLists.empty();
	}
	
	protected BaseElement createNode(IConfigNode node) {
		if(!node.isLeaf()) return new FolderElement(node);
		return createNode(node.asNode());
	}
	
	protected BaseElement createNode(INode node) {
		return switch(node.getNodeType()) {
			case COMPOUND -> null;
			case LIST -> null;
			case SIMPLE -> createFromType(node.asValue(), node.asValue().getDataType());
			default -> throw new IllegalStateException("Unknown Node Type");
		};
	}
	
	protected BaseElement createFromType(IValueNode node, DataType type) {
		if(type == DataType.BOOLEAN) return new BooleanElement(node);
		if(type == DataType.INTEGER) return new IntegerElement(node);
		if(type == DataType.LONG) return new LongElement(node);
		if(type == DataType.DOUBLE || type == DataType.FLOAT) return new DoubleElement(node);
		if(type == DataType.STRING) return new StringElement(node);
		if(type == DataType.ENUM) return new EnumElement(node);
		return new TestElement(node);
	}
	
	protected final Font getFont() {
		return Minecraft.getInstance().font;
	}
}
