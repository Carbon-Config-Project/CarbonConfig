	package carbonconfiglib.gui.nodes;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import carbonconfiglib.api.ISuggestionProvider.Suggestion;
import carbonconfiglib.gui.api.node.IConfigFolderNode;
import carbonconfiglib.gui.api.node.IConfigNode;
import carbonconfiglib.gui.base.helpers.Align;
import carbonconfiglib.gui.base.helpers.GuiUtils;
import carbonconfiglib.gui.base.helpers.Texts;
import carbonconfiglib.gui.base.widgets.CarbonBaseButton;
import carbonconfiglib.gui.base.widgets.CarbonButton;
import carbonconfiglib.gui.nodes.base.BaseElement;
import carbonconfiglib.gui.nodes.base.IFolderNode;
import carbonconfiglib.impl.ReloadMode;
import net.minecraft.util.text.ITextComponent;
import speiger.src.collections.objects.lists.ObjectArrayList;
import speiger.src.collections.objects.utils.ObjectLists;
import speiger.src.collections.utils.Stack;

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
public class FolderElement extends BaseElement implements IFolderNode
{
	CarbonButton button;
	IConfigNode node;
	IFolderController listener;

	public FolderElement(IConfigNode node) {
		this.node = node;
		button = addChild(new CarbonButton(0, 0, 0, 0, Texts.literal("▶"), this::onClick));
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
	
	public void renderLeftPart(int left, int top, int width, int height, int mouseX, int mouseY, boolean selected, float partialTicks) {
		GuiUtils.drawScrollingShadowText( font, node.getName(), left, top, width-22, height, Align.START, -1, sinceFullyVisible);
		boolean active = context.isElementActive(this);
		button.setMessage(active ? "◀" : "▶");
		button.setSelected(active);
		button.xPosition = left + width - 22;
		button.yPosition = top;
		button.setWidth(20);
		button.setHeight(height);
		button.setActive(node.getChildren().size() > 0);
		button.render(mouseX, mouseY, partialTicks);
	}
	
	public void renderRightPart(int left, int top, int desiredWidth, int width, int height, int mouseX, int mouseY, boolean selected, float partialTicks) {
		GuiUtils.drawScrollingShadowText(font, Texts.translatable("gui.carbonconfig.elements", node.getChildren().size()), left, top, desiredWidth-2, height, Align.END, -1, sinceFullyVisible);
	}
	
	protected void onClick(CarbonBaseButton button) {
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
	public ITextComponent getName() {
		return node.getName();
	}
	
	@Override
	public ITextComponent getTooltip() {
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
	
	public boolean save(Consumer<ReloadMode> notification) {
		if(node.isUnsaved()) {
			findUnsavedState().ifPresent(notification);
			node.save();
			return true;
		}
		return false;
	}
	
	private Optional<ReloadMode> findUnsavedState() {
		ReloadMode result = null;
		for(IConfigNode node : getUnsaved()) {
			result = ReloadMode.or(result, node.getReloadState());
		}
		return Optional.ofNullable(result);
	}
	
	private List<IConfigNode> getUnsaved() {
		List<IConfigNode> allNodes = new ObjectArrayList<>();
		Stack<IConfigNode> toScan = new ObjectArrayList<>(node);
		while(!toScan.isEmpty()) {
			IConfigNode node = toScan.pop();
			if(node instanceof IConfigFolderNode) {
				node.getChildren().forEach(toScan::push);
				continue;
			}
			if(node.isUnsaved()) {
				allNodes.add(node);
			}
		}
		return allNodes;
	}
}
