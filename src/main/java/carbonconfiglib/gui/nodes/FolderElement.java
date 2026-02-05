package carbonconfiglib.gui.nodes;

import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;

import carbonconfiglib.api.ISuggestionProvider.Suggestion;
import carbonconfiglib.gui.api.IConfigNode;
import carbonconfiglib.gui.base.helpers.Align;
import carbonconfiglib.gui.base.helpers.GuiUtils;
import carbonconfiglib.gui.base.widgets.CarbonButton;
import carbonconfiglib.gui.config.ConfigElement.GuiAlign;
import carbonconfiglib.gui.nodes.base.BaseElement;
import carbonconfiglib.gui.nodes.base.IFolderNode;
import carbonconfiglib.impl.ReloadMode;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import speiger.src.collections.objects.lists.ObjectArrayList;
import speiger.src.collections.objects.utils.ObjectLists;

public class FolderElement extends BaseElement implements IFolderNode
{
	CarbonButton button;
	IConfigNode node;
	IFolderController listener;

	public FolderElement(IConfigNode node) {
		this.node = node;
		button = addChild(new CarbonButton(0, 0, 0, 0, node.getName(), this::onClick));
	}
	
	@Override
	public void setCallbacks(IFolderController listener) {
		this.listener = listener;
	}
	
	@Override
	public String getNodeName() {
		return node.getNodeName();
	}
	
	@Override
	public void setEditable(boolean value) {}
	@Override
	protected void setRightComponentsVisible(boolean value) {}
	@Override
	protected boolean isValue() { return false; }
	@Override
	protected ReloadMode getReloadState() { return null; }
	
	public void renderLeftPart(PoseStack stack, int left, int top, int width, int height, int mouseX, int mouseY, boolean selected, float partialTicks) {
		button.x = left;
		button.y = top;
		boolean active = context.isElementActive(this);
		button.setWidth(width-2 - (active ? 8 : 0));
		button.setHeight(height);
		button.active = node.getChildren().size() > 0;
		button.render(stack, mouseX, mouseY, partialTicks);
		if(active) GuiUtils.drawText(stack, font, Component.literal("▶"), left + width-2, top + (height >> 1) - (font.lineHeight >> 1), Align.END, -1); 
	}
	
	public void renderRightPart(PoseStack stack, int left, int top, int width, int height, int mouseX, int mouseY, boolean selected, float partialTicks) {
		GuiUtils.drawScrollingShadowText(stack, font, Component.literal(node.getChildren().size()+" Elements"), left, top, width-2, height, GuiAlign.RIGHT, -1, 32);
	}
	
	protected void onClick(Button button) {
		if(listener == null) return;
		listener.pushNode(this, layer, false);
	}
	
	@Override
	public List<BaseElement> getChildNodes() {
		List<BaseElement> result = new ObjectArrayList<>();
		for(IConfigNode entry : node.getChildren()) {
			BaseElement element = createNode(entry);
			if(element == null) continue;
			result.add(element);
		}
		return result;
	}
	
	@Override
	public Component getName() {
		return node.getName();
	}
	
	@Override
	public Component getTooltip() {
		return node.getTooltip();
	}
	
	@Override
	protected List<Suggestion> getSuggestions() {
		return ObjectLists.empty();
	}
	
	@Override
	protected void createTemp() {
	}
	
	@Override
	protected void deleteTempIfNeeded() {
	}

	@Override
	protected void onArrayDelete() {
	}

	@Override
	protected boolean isChanged() {
		return node.isChanged();
	}
	
	@Override
	protected boolean isNotDefault() {
		return !node.isDefault();
	}
	
	@Override
	protected void onRevert() {
		node.setPrevious();
	}
	
	@Override
	protected void onReset() {
		node.setDefault();
	}
	
	public boolean needsSaving() {
		return node.isUnsaved();
	}
	
	public boolean save() {
		if(node.isUnsaved()) {
			node.save();
			return true;
		}
		return false;
	}
}
