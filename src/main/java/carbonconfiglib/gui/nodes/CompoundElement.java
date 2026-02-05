package carbonconfiglib.gui.nodes;

import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;

import carbonconfiglib.gui.api.ICompoundNode;
import carbonconfiglib.gui.api.INode;
import carbonconfiglib.gui.base.helpers.Align;
import carbonconfiglib.gui.base.helpers.GuiUtils;
import carbonconfiglib.gui.base.widgets.CarbonButton;
import carbonconfiglib.gui.config.ConfigElement.GuiAlign;
import carbonconfiglib.gui.nodes.base.BaseElement;
import carbonconfiglib.gui.nodes.base.IFolderNode;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import speiger.src.collections.objects.lists.ObjectArrayList;

public class CompoundElement extends NodeElement implements IFolderNode
{
	ICompoundNode node;
	CarbonButton button;
	IFolderController listener;
	
	public CompoundElement(ICompoundNode node) {
		super(node);
		this.node = node;
		button = addChild(new CarbonButton(0, 0, 0, 0, node.getName(), this::onClick));
	}
	
	@Override
	protected void setRightComponentsVisible(boolean value) {}
	@Override
	public void setEditable(boolean value) {}
	@Override
	protected boolean isValue() { return false; }
	
	@Override
	public void setCallbacks(IFolderController listener) {
		this.listener = listener;
	}
	
	@Override
	public String getNodeName() {
		return node.getNodeName();
	}

	@Override
	public void renderLeftPart(PoseStack stack, int left, int top, int width, int height, int mouseX, int mouseY, boolean selected, float partialTicks) {
		button.x = left;
		button.y = top;
		boolean active = context.isElementActive(this);
		button.setWidth(width-2 - (active ? 8 : 0));
		button.setHeight(height);
		button.render(stack, mouseX, mouseY, partialTicks);
		if(active) GuiUtils.drawText(stack, font, Component.literal("▶"), left + width-2, top + (height >> 1) - (font.lineHeight >> 1), Align.END, -1); 
	}
	
	@Override
	public void renderRightPart(PoseStack stack, int left, int top, int width, int height, int mouseX, int mouseY, boolean selected, float partialTicks) {
		GuiUtils.drawScrollingShadowText(stack, font, Component.literal(node.getValues().size()+" Elements"), left, top, width-2, height, GuiAlign.RIGHT, -1, 32);
	}
	
	protected void onClick(Button button) {
		if(listener == null) return;
		listener.pushNode(this, layer, false);
	}
	
	@Override
	public List<BaseElement> getChildNodes() {
		List<BaseElement> result = new ObjectArrayList<>();
		for(INode entry : node.getValues()) {
			BaseElement element = createNode(entry);
			if(element == null) continue;
			result.add(element);
		}
		return result;
	}
}
