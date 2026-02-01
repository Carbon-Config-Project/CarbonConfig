package carbonconfiglib.gui.nodes.base;

import com.mojang.blaze3d.vertex.PoseStack;

import carbonconfiglib.api.ISuggestionProvider.Suggestion;
import carbonconfiglib.gui.api.IValueNode;
import carbonconfiglib.gui.base.helpers.GuiUtils;
import carbonconfiglib.gui.config.ConfigElement.GuiAlign;
import carbonconfiglib.gui.nodes.NodeElement;
import net.minecraft.network.chat.Component;

public abstract class ValueElement extends NodeElement
{
	protected IValueNode node;

	public ValueElement(IValueNode node) {
		super(node);
		this.node = node;
	}
	
	@Override
	protected boolean isValue() { return true; }
	@Override
	protected abstract void readValue();
	
	protected void setValue(String value) {
		node.set(value);
		onValueChanged();
	}
	
	@Override
	public void renderLeftPart(PoseStack stack, int left, int top, int width, int height, int mouseX, int mouseY, boolean selected, float partialTicks) {
		GuiUtils.drawScrollingShadowText(stack, font, shouldRenderIndex() ? Component.literal(index(node)+": ") : node.getName(), left, top, width, height, GuiAlign.LEFT, -1, node.hashCode());
	}
	
	@Override
	public void renderRightPart(PoseStack stack, int left, int top, int width, int height, int mouseX, int mouseY, boolean selected, float partialTicks) {
		
	}
	
	@Override
	protected void setSuggestion(Suggestion suggestion) {
		node.set(suggestion.getValue());
		readValue();
	}
	
	@Override
	protected void onValueChanged() {
		super.onValueChanged();
		String value = node.get();
		suggestionState.findSelected(T -> value.equals(T.getValue()));
	}
}
