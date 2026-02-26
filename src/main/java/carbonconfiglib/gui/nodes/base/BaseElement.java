package carbonconfiglib.gui.nodes.base;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.function.BooleanSupplier;

import com.mojang.blaze3d.vertex.PoseStack;

import carbonconfiglib.api.ISuggestionProvider.Suggestion;
import carbonconfiglib.gui.api.node.IArrayNode;
import carbonconfiglib.gui.api.node.ICompoundNode;
import carbonconfiglib.gui.api.node.IConfigNode;
import carbonconfiglib.gui.api.node.INode;
import carbonconfiglib.gui.api.node.IValueNode;
import carbonconfiglib.gui.api.types.CompoundType;
import carbonconfiglib.gui.api.types.DataType;
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
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.network.chat.Component;
import speiger.src.collections.objects.utils.ObjectLists;

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
	
	public BaseElement() {
		if(showControls()) {
			revert = addChild(new CarbonButton(0, 0, 18, 18, Component.empty(), T -> onRevert()).withIcon(Optional.of(Icon.REVERT)));
			reset = addChild(new CarbonButton(0, 0, 18, 18, Component.empty(), T -> onReset()).withIcon(Optional.of(Icon.SET_DEFAULT)));
			if(isValue()) {
				if(allowSuggestions()) suggestion = addChild(new DropDownMenu<Suggestion>(0, 0, 18, 18, suggestionState));
				edit = addChild(new CarbonCheckBox(0, 0, 18, 18, new CheckBoxState(false, Icon.NOT_DEFAULT).setCallback(T -> onEditButtonPressed(T.getValue(), false))));
			}
		}
	}
	
	public final void setArray(IArrayNode array, Runnable reloader) {
		this.array = array;
		this.reloader = reloader;
		delete = addChild(new CarbonButton(0, 0, 18, 18, Component.empty(), T -> onArrayDelete()).withIcon(Optional.of(Icon.DELETE)));
	}
	
	public final void setContext(IElementContext context) {
		this.context = context;
		if(array == null && reload == null) {
			ReloadMode mode = getReloadState();
			if(mode != null) reload = addChild(new CarbonLabel(0, 0, 18, 18, mode == ReloadMode.GAME ? Icon.RESTART : Icon.RELOAD).withTooltip(Component.literal(mode == ReloadMode.GAME ? "Requires Game Restart" : "Requires World Reload")));
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
	public final void render(PoseStack poseStack, int x, int top, int left, int width, int height, int mouseX, int mouseY, boolean selected, float partialTicks) {
		int leftWidth = context.calculateSegmentWidth()-4;
		renderLeftPart(poseStack, left, top, leftWidth, height, mouseX, mouseY, selected, partialTicks);
		if(context.isAtTop(layer)) {
			if(!right) {
				right = true;
				setRightComponentsVisible(true);
			}
			if(!showControls()) {
				renderRightPart(poseStack, left+leftWidth+8, top, width-leftWidth-10, height, mouseX, mouseY, selected, partialTicks);
				return;
			}
			int controlWidth = 60 + (delete != null ? 20 : 0) + (reload != null ? 20 : 0);
			int newWidth = width-leftWidth-10-controlWidth;
			int renderWidth = Math.min((newWidth + controlWidth) >> 1, newWidth);
			controlWidth-=2;
			renderControls(poseStack, left+width-controlWidth, top, controlWidth, height, mouseX, mouseY, selected, partialTicks);
			renderRightPart(poseStack, left+leftWidth+8, top, renderWidth, height, mouseX, mouseY, selected, partialTicks);
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
	public abstract Component getName();
	public abstract Component getTooltip();
	
	public abstract void renderLeftPart(PoseStack stack, int left, int top, int width, int height, int mouseX, int mouseY, boolean selected, float partialTicks);
	
	public abstract void renderRightPart(PoseStack stack, int left, int top, int width, int height, int mouseX, int mouseY, boolean selected, float partialTicks);
	
	public void renderControls(PoseStack stack, int left, int top, int width, int height, int mouseX, int mouseY, boolean selected, float partialTicks) {
		left-=1;
		left+= 40 + (reload != null || delete != null ? 20 : 0);
		if(!render(delete, left, top, null, stack, mouseX, mouseY, partialTicks)) {
			render(reload, left, top, null, stack, mouseX, mouseY, partialTicks);
		}
		
		render(revert, left-20, top, this::isChanged, stack, mouseX, mouseY, partialTicks);
		render(reset, left-40, top, this::isNotDefault, stack, mouseX, mouseY, partialTicks);
		render(edit, left-60, top, null, stack, mouseX, mouseY, partialTicks);
		if(allowSuggestions() && !getSuggestions().isEmpty()) {
			render(suggestion, left-80, top, null, stack, mouseX, mouseY, partialTicks);
		}
	}
	
	private boolean render(AbstractWidget widget, int x, int y, BooleanSupplier active, PoseStack stack, int mouseX, int mouseY, float partialTicks) {
		if(widget == null) return false;
		widget.x = x;
		widget.y = y;
		if(active != null) widget.active = active.getAsBoolean();
		widget.render(stack, mouseX, mouseY, partialTicks);
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
