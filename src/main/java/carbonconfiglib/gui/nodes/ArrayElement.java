package carbonconfiglib.gui.nodes;

import java.util.List;
import java.util.Random;

import carbonconfiglib.api.ISuggestionProvider.Suggestion;
import carbonconfiglib.gui.api.node.IArrayNode;
import carbonconfiglib.gui.base.helpers.Align;
import carbonconfiglib.gui.base.helpers.GuiUtils;
import carbonconfiglib.gui.base.helpers.Texts;
import carbonconfiglib.gui.base.widgets.CarbonBaseButton;
import carbonconfiglib.gui.base.widgets.CarbonButton;
import carbonconfiglib.gui.base.widgets.DropDownMenu;
import carbonconfiglib.gui.base.widgets.DropDownMenu.DropDownState;
import carbonconfiglib.gui.nodes.base.BaseElement;
import carbonconfiglib.gui.nodes.base.IFolderNode;
import carbonconfiglib.gui.nodes.base.ISortableNode;
import carbonconfiglib.gui.nodes.base.NodeElement;
import carbonconfiglib.gui.nodes.base.SuggestionEntry;
import carbonconfiglib.impl.ReloadMode;
import carbonconfiglib.utils.structure.IStructuredData.StructureType;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.ITextComponent;
import speiger.src.collections.objects.lists.ObjectArrayList;
import speiger.src.collections.objects.utils.ObjectLists;

/**
 * Copyright 2026 Speiger, Meduris
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
public class ArrayElement extends NodeElement implements IFolderNode, ISortableNode
{
	IArrayNode node;
	CarbonButton button;
	IFolderController listener;

	public ArrayElement(IArrayNode node) {
		super(node);
		this.node = node;
		button = addChild(new CarbonButton(0, 0, 0, 0, Texts.literal("▶"), this::onClick));
	}
	
	@Override
	protected void setRightComponentsVisible(boolean value) {}
	@Override
	protected boolean isValue() { return false; }
	@Override
	protected ReloadMode getReloadState() { return node.getReloadState(); }
	@Override
	public void setEditable(boolean value) {}
	@Override
	public void renderLeftPart(int left, int top, int width, int height, int mouseX, int mouseY, boolean selected, float partialTicks) {
		GuiUtils.drawScrollingShadowText(font, node.getName(), left, top, width-23, height, Align.START, -1, sinceFullyVisible);
		boolean active = context.isElementActive(this);
		button.setMessage(active ? "◀" : "▶");
		button.setSelected(active);
		button.x = left + width - 22;
		button.y = top;
		button.setWidth(20);
		button.setHeight(height);
		button.render(mouseX, mouseY, partialTicks);
	}
	
	@Override
	public void renderRightPart(int left, int top, int desiredWidth, int width, int height, int mouseX, int mouseY, boolean selected, float partialTicks) {
		GuiUtils.drawScrollingShadowText(font, Texts.translatable("gui.carbonconfig.elements", node.size()), left, top, desiredWidth-2, height, Align.END, -1, sinceFullyVisible);
	}
	
	@Override
	public List<BaseElement> getChildNodes() {
		List<BaseElement> result = new ObjectArrayList<>();
		for(int i = 0,m = node.size();i<m;i++) {
			BaseElement element = createNode(node.get(i));
			if(element == null) continue;
			element.setArray(node, this::reloadElements);
			result.add(element);
		}
		result.add(new AddElement(this));
		return result;
	}
	
	@Override
	public void onSwapped(int oldIndex, int newIndex) {
		node.swap(oldIndex, newIndex);
		onValueChanged();
	}
	
	@Override
	public void setCallbacks(IFolderController listener) {
		this.listener = listener;
	}
	
	@Override
	public String getNodeName() {
		return node.getNodeName();
	}
	
	protected void onClick(CarbonBaseButton button) {
		if(listener == null) return;
		listener.pushNode(this, layer, false);
	}
	
	private void reloadElements() {
		if(listener == null) return;
		listener.pushNode(this, layer, true);
	}
	
	private void addElement(String value) {
		node.createNode(value);
		if(node.getInnerType() == StructureType.SIMPLE) listener.pushNode(this, layer, true);
		else {
			listener.pushNode(this, layer, true);
			listener.pushChild(this, layer, 1, true);
		}
		onValueChanged();
	}
	
	public static class AddElement extends BaseElement {
		ArrayElement owner;
		DropDownMenu<Suggestion> selector = addChild(new DropDownMenu<>(0, 0, 120, 20, new DropDownState<Suggestion>(T -> Texts.literal(T.getName())).allowEmpty(true).withEmpty(Texts.translatable("gui.carbonconfig.array.default")).asSimpleButton(true).withListener(this::onElementSelected)));
		boolean skip;
		
		public AddElement(ArrayElement owner) {
			this.owner = owner;
			List<Suggestion> suggestions = owner.node.getSuggestions();
			skip = suggestions.isEmpty();
			selector.getState().setValues(suggestions)
			.allowEmpty(!owner.node.isForcedSuggestion());
			selector.setMessage(I18n.format("gui.carbonconfig.array.new"));
			if(!suggestions.isEmpty()) {
				selector.withTooltip(Texts.translatable("gui.carbonconfig.array.quick"));
				if(suggestions.get(0).getType() != null) {
					selector.getState().withCustomRenderer(SuggestionEntry::new).setElementHeight(22);
				}
			}
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
		protected ReloadMode getReloadState() {
			return null;
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
		protected void createTemp() {
		}
		
		@Override
		protected void deleteTempIfNeeded() {
		}
		
		@Override
		protected List<Suggestion> getSuggestions() {
			return ObjectLists.empty();
		}
		
		@Override
		public String getNodeName() {
			return null;
		}
		
		@Override
		public ITextComponent getName() {
			return Texts.translatable("gui.carbonconfig.array.new");
		}
		
		@Override
		public ITextComponent getTooltip() {
			return Texts.translatable("gui.carbonconfig.array.tooltip");
		}
		
		@Override
		protected void onArrayDelete() {}
		@Override
		public void setEditable(boolean value) {}
		@Override
		public void renderLeftPart(int left, int top, int width, int height, int mouseX, int mouseY, boolean selected, float partialTicks) {
			GuiUtils.drawScrollingShadowText(font, Texts.translatable("gui.carbonconfig.array.next"), left, top, width, height, Align.START, -1, 0);
		}

		@Override
		public void renderRightPart(int left, int top, int desiredWidth, int width, int height, int mouseX, int mouseY, boolean selected, float partialTicks) {
			selector.x = left + (int)(desiredWidth * 0.18F);
			selector.y = top;
			selector.render(mouseX, mouseY, partialTicks);
		}
		
		protected void onElementSelected(List<Suggestion> elements) {
			owner.addElement(elements.isEmpty() ? null : elements.get(0).getValue());
		}
		
		@Override
		public boolean mouseClick(double pMouseX, double pMouseY, int pButton) {
			if(pButton == 0 && (GuiScreen.isShiftKeyDown() || skip) && selector.isMouseOver(pMouseX, pMouseY)) {
				if(!owner.node.isForcedSuggestion()) {
					owner.addElement(null);					
					return true;
				}
				List<Suggestion> values = owner.node.getSuggestions();
				owner.addElement(values.isEmpty() ? null : values.get(new Random().nextInt(values.size())).getValue());
				return true;
			}
			return super.mouseClick(pMouseX, pMouseY, pButton);
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
