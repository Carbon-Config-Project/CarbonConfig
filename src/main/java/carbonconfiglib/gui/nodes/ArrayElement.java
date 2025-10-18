package carbonconfiglib.gui.nodes;

import java.util.List;
import java.util.function.ObjIntConsumer;

import com.mojang.blaze3d.vertex.PoseStack;

import carbonconfiglib.api.ISuggestionProvider.Suggestion;
import carbonconfiglib.gui.api.IArrayNode;
import carbonconfiglib.gui.base.helpers.Align;
import carbonconfiglib.gui.base.helpers.GuiUtils;
import carbonconfiglib.gui.base.widgets.CarbonButton;
import carbonconfiglib.gui.base.widgets.DropDownMenu;
import carbonconfiglib.gui.base.widgets.DropDownMenu.DropDownState;
import carbonconfiglib.gui.config.ConfigElement.GuiAlign;
import carbonconfiglib.gui.nodes.base.BaseElement;
import carbonconfiglib.gui.nodes.base.IFolderNode;
import carbonconfiglib.gui.nodes.base.ISortableNode;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import speiger.src.collections.objects.lists.ObjectArrayList;
import speiger.src.collections.objects.utils.ObjectLists;

public class ArrayElement extends NodeElement implements IFolderNode, ISortableNode
{
	IArrayNode node;
	CarbonButton button;
	ObjIntConsumer<BaseElement> listener;

	public ArrayElement(IArrayNode node) {
		super(node);
		this.node = node;
		button = addChild(new CarbonButton(0, 0, 0, 0, node.getName(), this::onClick));
	}
	
	@Override
	protected void setRightComponentsVisible(boolean value) {}
	@Override
	protected boolean isValue() { return false; }
	@Override
	public void setEditable(boolean value) {}
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
		GuiUtils.drawScrollingShadowText(stack, font, Component.literal(node.size()+" Elements"), left, top, width-2, height, GuiAlign.RIGHT, -1, 32);
	}
	
	@Override
	public List<BaseElement> getChildNodes() {
		List<BaseElement> result = new ObjectArrayList<>();
		for(int i = 0,m = node.size();i<m;i++) {
			BaseElement element = createNode(node.get(i));
			if(element == null) continue;
			element.setArray(node);
			result.add(element);
		}
		result.add(new AddElement(this));
		return result;
	}
	
	@Override
	public void onSwapped(int oldIndex, int newIndex) {
		node.swap(oldIndex, newIndex);
	}
	
	@Override
	public void setCallbacks(ObjIntConsumer<BaseElement> listener) {
		this.listener = listener;
	}
	
	protected void onClick(Button button) {
		if(listener == null) return;
		listener.accept(this, layer);
	}
	
	private void addElement(String value) {
		node.createNode(value);
		listener.accept(this, layer);
	}
	
	public static class AddElement extends BaseElement {
		ArrayElement owner;
		DropDownMenu<Suggestion> selector = addChild(new DropDownMenu<>(0, 0, 120, 20, new DropDownState<Suggestion>(T -> Component.literal(T.getName())).allowEmpty(true).withEmpty(Component.literal("Default")).asSimpleButton(true).withListener(this::onElementSelected)));
		
		public AddElement(ArrayElement owner) {
			this.owner = owner;
			selector.getState().setValues(owner.node.getSuggestions());
		}

		@Override
		protected void setRightComponentsVisible(boolean value) {
			selector.visible = value;
		}

		@Override
		protected boolean isValue() {
			return false;
		}
		
		@Override
		protected boolean showControls() {
			return false;
		}
		
		@Override
		public boolean isDraggable() {
			return false;
		}
		
		@Override
		protected List<Suggestion> getSuggestions() {
			return ObjectLists.empty();
		}
		
		@Override
		public void setEditable(boolean value) {}
		
		@Override
		public void renderLeftPart(PoseStack stack, int left, int top, int width, int height, int mouseX, int mouseY, boolean selected, float partialTicks) {}

		@Override
		public void renderRightPart(PoseStack stack, int left, int top, int width, int height, int mouseX, int mouseY, boolean selected, float partialTicks) {
			selector.x = left + (int)(width * 0.18F);
			selector.y = top;
			selector.render(stack, mouseX, mouseY, partialTicks);
		}
		
		protected void onElementSelected(List<Suggestion> elements) {
			owner.addElement(elements.isEmpty() ? null : elements.get(0).getValue());
		}
		
		@Override
		protected void onRevert() {}
		@Override
		protected void onReset() {}
		@Override
		protected boolean isChanged() { return false; }
		@Override
		protected boolean isNotDefault() { return false; }
	}
}
