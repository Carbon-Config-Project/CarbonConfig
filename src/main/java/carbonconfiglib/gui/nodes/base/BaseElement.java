package carbonconfiglib.gui.nodes.base;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.function.BooleanSupplier;

import carbonconfiglib.api.ISuggestionProvider.Suggestion;
import carbonconfiglib.gui.api.node.IArrayNode;
import carbonconfiglib.gui.api.node.ICompoundNode;
import carbonconfiglib.gui.api.node.IConfigNode;
import carbonconfiglib.gui.api.node.INode;
import carbonconfiglib.gui.api.node.IValueNode;
import carbonconfiglib.gui.api.types.CompoundType;
import carbonconfiglib.gui.api.types.DataType;
import carbonconfiglib.gui.base.helpers.Align;
import carbonconfiglib.gui.base.helpers.GuiUtils;
import carbonconfiglib.gui.base.helpers.Icon;
import carbonconfiglib.gui.base.widgets.CarbonButton;
import carbonconfiglib.gui.base.widgets.CarbonCheckBox;
import carbonconfiglib.gui.base.widgets.CarbonCheckBox.CheckBoxState;
import carbonconfiglib.gui.base.widgets.CarbonLabel;
import carbonconfiglib.gui.base.widgets.CarbonList.ListEntry;
import carbonconfiglib.gui.base.widgets.DropDownMenu;
import carbonconfiglib.gui.base.widgets.DropDownMenu.DropDownState;
import carbonconfiglib.gui.nodes.ArrayElement;
import carbonconfiglib.gui.nodes.CompoundElement;
import carbonconfiglib.gui.nodes.FolderElement;
import carbonconfiglib.gui.nodes.SelectionElement;
import carbonconfiglib.impl.ReloadMode;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.network.chat.Component;
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
public abstract class BaseElement extends ListEntry<BaseElement>
{
	protected IElementContext context;
	private IArrayNode array;
	private Runnable reloader;
	private boolean right;
	protected int layer;
	private CarbonLabel reload;
	private CarbonButton delete;
	private CarbonButton revert;
	private CarbonButton reset;
	protected DropDownState<Suggestion> suggestionState = new DropDownState<Suggestion>(T -> Component.literal(T.getName()))
			.allowEmpty(false)
			.withDynamicValues(this::getSuggestions)
			.withCustomWidth(OptionalInt.of(200))
			.withXOffset(-180)
			.withIcon(Icon.SUGGESTIONS)
			.asSimpleButton(true)
			.withListener(this::onSuggestionPicked);
	private DropDownMenu<Suggestion> suggestion;
	private CarbonCheckBox edit;
	protected long sinceFullyVisible;
	
	public BaseElement() {
		if(showControls()) {
			revert = addChild(new CarbonButton(0, 0, 18, 18, Component.empty(), T -> onRevert()).withIcon(Optional.of(Icon.REVERT)).withTooltip(Component.translatable("gui.carbonconfig.revert")));
			reset = addChild(new CarbonButton(0, 0, 18, 18, Component.empty(), T -> onReset()).withIcon(Optional.of(Icon.SET_DEFAULT)).withTooltip(Component.translatable("gui.carbonconfig.default")));
			if(isValue()) {
				if(allowSuggestions()) suggestion = addChild(new DropDownMenu<Suggestion>(0, 0, 18, 18, suggestionState).withTooltip(Component.translatable("gui.carbonconfig.suggestions")));
				edit = addChild(new CarbonCheckBox(0, 0, 18, 18, new CheckBoxState(false, Icon.NOT_DEFAULT).setTooltip(Component.translatable("gui.carbonconfig.edit")).setCallback(T -> onEditButtonPressed(T.getValue(), false))));
			}
		}
	}
	
	public final void setArray(IArrayNode array, Runnable reloader) {
		this.array = array;
		this.reloader = reloader;
		delete = addChild(new CarbonButton(0, 0, 18, 18, Component.empty(), T -> onArrayDelete()).withIcon(Optional.of(Icon.DELETE)).withTooltip(Component.translatable("gui.carbonconfig.delete")));
	}
	
	public final void setContext(IElementContext context) {
		this.context = context;
		if(array == null && reload == null) {
			ReloadMode mode = getReloadState();
			if(mode != null) reload = addChild(new CarbonLabel(0, 0, 18, 18, mode == ReloadMode.GAME ? Icon.RESTART : Icon.RELOAD).withTooltip(mode.getState()));
		}
		setRightComponentsVisible(false);
		setEditable(false);
	}
	
	public final void setLayer(int layer) {
		this.layer = layer;
	}
	
	@Override
	protected boolean containsSearch(String searchString) { return getName().getString().toLowerCase(Locale.ROOT).contains(searchString); }
	
	@Override
	public final void render(GuiGraphics graphics, int x, int top, int left, int width, int height, int mouseX, int mouseY, boolean selected, float partialTicks) {
		if(!isInFullView() || sinceFullyVisible == 0L) sinceFullyVisible = GuiUtils.currentMillseconds();
		int leftWidth = context.calculateSegmentWidth(layer)-4;
		renderLeftPart(graphics, left, top, leftWidth, height, mouseX, mouseY, selected, partialTicks);
		if(context.isAtTop(layer)) {
			if(!right) {
				right = true;
				setRightComponentsVisible(true);
			}
			if(!showControls()) {
				int totalWidth = width-leftWidth-10;
				int desiredWidth = Math.min(totalWidth >> 1, totalWidth);
				renderRightPart(graphics, left+leftWidth+8, top, desiredWidth, totalWidth, height, mouseX, mouseY, selected, partialTicks);
				return;
			}
			int controlWidth = 80 + (allowSuggestions() && !getSuggestions().isEmpty() ? 20 : 0);
			int totalWidth = width-leftWidth-10-controlWidth;
			int desiredWidth = Math.min((totalWidth + controlWidth) >> 1, totalWidth);
			renderControls(graphics, left+width-controlWidth, top, controlWidth, height, mouseX, mouseY, selected, partialTicks);
			renderRightPart(graphics, left+leftWidth+8, top, desiredWidth, totalWidth, height, mouseX, mouseY, selected, partialTicks);
		}
		else if(right) {
			right = false;
			setRightComponentsVisible(false);
		}
	}
	
	public void setBulkEdit(boolean state) {
		if(edit == null || edit.getState().getValue() == state) return;
		edit.getState().setValue(state);
		onEditButtonPressed(state, true);
	}
	private void onEditButtonPressed(boolean state, boolean bulk) {
		setEditable(state);
		if(state) createTemp();
		else deleteTempIfNeeded();
		if(context != null && !bulk) context.setTooltipFocused(state ? this : null);
	}
	
	protected abstract void createTemp();
	protected abstract void deleteTempIfNeeded();
	protected abstract void setEditable(boolean value);
	protected abstract void setRightComponentsVisible(boolean value);
	protected abstract boolean isValue();
	protected abstract ReloadMode getReloadState();
	protected boolean showControls() { return true; }
	protected boolean shouldRenderIndex() { return array != null; }
	protected boolean allowSuggestions() { return true; }
	protected int index(INode node) { return array == null ? -1 : array.indexOf(node); }
	protected boolean deleteNode(INode node) {
		if(array == null) return false;
		int index = array.indexOf(node);
		if(index == -1) return false;
		array.removeNode(index);
		reloader.run();
		return true;
	}
	public abstract String getNodeName();
	public abstract Component getName();
	public abstract Component getTooltip();
	
	public abstract void renderLeftPart(GuiGraphics graphics, int left, int top, int width, int height, int mouseX, int mouseY, boolean selected, float partialTicks);
	
	public abstract void renderRightPart(GuiGraphics graphics, int left, int top, int desiredWidth, int width, int height, int mouseX, int mouseY, boolean selected, float partialTicks);
	
	public void renderControls(GuiGraphics graphics, int left, int top, int width, int height, int mouseX, int mouseY, boolean selected, float partialTicks) {
		int right = left + width - 19;
		if(!render(delete, right, top, height, null, graphics, mouseX, mouseY, partialTicks)) {
			render(reload, right, top, height, null, graphics, mouseX, mouseY, partialTicks);
		}
		
		render(revert, right-20, top, height, this::isChanged, graphics, mouseX, mouseY, partialTicks);
		render(reset, right-40, top, height, this::isNotDefault, graphics, mouseX, mouseY, partialTicks);
		render(edit, right-60, top, height, null, graphics, mouseX, mouseY, partialTicks);
		if(allowSuggestions() && !getSuggestions().isEmpty()) {
			render(suggestion, right-80, top, height, null, graphics, mouseX, mouseY, partialTicks);
		}
	}
	
	private boolean render(AbstractWidget widget, int x, int y, int height, BooleanSupplier active, GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
		if(widget == null) return false;
		widget.setX(x);
		widget.setY((int)Align.CENTER.alignStart(y, height, widget.getHeight()));
		if(active != null) widget.active = active.getAsBoolean();
		widget.render(graphics, mouseX, mouseY, partialTicks);
		return true;
	}
	
	private void onSuggestionPicked(List<Suggestion> result) {
		if(result.isEmpty()) return;
		setSuggestion(result.get(0));
	}
	
	protected void setSuggestion(Suggestion suggestion) {}
	protected abstract List<Suggestion> getSuggestions();
	protected abstract boolean isChanged();
	protected abstract boolean isNotDefault();
	protected abstract void onRevert();
	protected abstract void onReset();
	protected abstract void onArrayDelete();
	protected final boolean isRightSideEnabled() {
		return right;
	}
	
	public List<BaseElement> getChildNodes() {
		return ObjectLists.empty();
	}
	
	protected BaseElement createNode(IConfigNode node) {
		if(!node.isLeaf()) return new FolderElement(node);
		return createNode(node.asNode());
	}
	
	protected BaseElement createNode(INode node) {
		switch(node.getNodeType()) {
			case COMPOUND: return createCompound(node.asCompound());
			case LIST: return new ArrayElement(node.asArray());
			case SIMPLE: return createFromType(node.asValue(), node.asValue().getDataType());
			default: throw new IllegalStateException("Unknown Node Type");
		}
	}
	
	protected BaseElement createCompound(ICompoundNode node) {
		CompoundType override = CompoundType.by(node);
		return override != null ? override.create(node) : new CompoundElement(node);
	}
	
	protected BaseElement createFromType(IValueNode node, DataType type) {
		if(node.isForcingSuggestions()) return new SelectionElement(node);
		return type.createElement(node);
	}
	
	protected final Font getFont() {
		return Minecraft.getInstance().font;
	}

}
