package carbonconfiglib.gui.nodes.base;

import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.function.BooleanSupplier;

import com.mojang.blaze3d.vertex.PoseStack;

import carbonconfiglib.api.ISuggestionProvider.Suggestion;
import carbonconfiglib.gui.api.DataType;
import carbonconfiglib.gui.api.IArrayNode;
import carbonconfiglib.gui.api.IConfigNode;
import carbonconfiglib.gui.api.INode;
import carbonconfiglib.gui.api.IValueNode;
import carbonconfiglib.gui.base.widgets.CarbonButton;
import carbonconfiglib.gui.base.widgets.CarbonCheckBox;
import carbonconfiglib.gui.base.widgets.CarbonCheckBox.CheckBoxState;
import carbonconfiglib.gui.base.widgets.CarbonList.ListEntry;
import carbonconfiglib.gui.base.widgets.DropDownMenu;
import carbonconfiglib.gui.base.widgets.DropDownMenu.DropDownState;
import carbonconfiglib.gui.nodes.ArrayElement;
import carbonconfiglib.gui.nodes.BooleanElement;
import carbonconfiglib.gui.nodes.CompoundElement;
import carbonconfiglib.gui.nodes.DoubleElement;
import carbonconfiglib.gui.nodes.FolderElement;
import carbonconfiglib.gui.nodes.NumberElement.IntegerElement;
import carbonconfiglib.gui.nodes.NumberElement.LongElement;
import carbonconfiglib.gui.nodes.SelectionElement;
import carbonconfiglib.gui.nodes.StringElement;
import carbonconfiglib.gui.widgets.Icon;
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
				suggestion = addChild(new DropDownMenu<Suggestion>(0, 0, 18, 18, suggestionState));
				edit = addChild(new CarbonCheckBox(0, 0, 18, 18, new CheckBoxState(false, Icon.NOT_DEFAULT).setCallback(T -> setEditable(T.getValue()))));
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
		setRightComponentsVisible(false);
		setEditable(false);
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
			if(!showControls()) {
				renderRightPart(poseStack, left+leftWidth+8, top, width-leftWidth-10, height, mouseX, mouseY, selected, partialTicks);
				return;
			}
			int controlWidth = delete != null ? 100 : 80;
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
	
	public abstract void setEditable(boolean value);
	protected abstract void setRightComponentsVisible(boolean value);
	protected abstract boolean isValue();
	protected boolean showControls() { return true; }
	protected boolean shouldRenderIndex() { return array != null; }
	protected int index(INode node) { return array == null ? -1 : array.indexOf(node); }
	protected boolean deleteNode(INode node) {
		if(array == null) return false;
		int index = array.indexOf(node);
		if(index == -1) return false;
		array.removeNode(index);
		reloader.run();
		return true;
	}
	
	public abstract void renderLeftPart(PoseStack stack, int left, int top, int width, int height, int mouseX, int mouseY, boolean selected, float partialTicks);
	
	public abstract void renderRightPart(PoseStack stack, int left, int top, int width, int height, int mouseX, int mouseY, boolean selected, float partialTicks);
	
	public void renderControls(PoseStack stack, int left, int top, int width, int height, int mouseX, int mouseY, boolean selected, float partialTicks) {
		left-=1;
		left+=60;
		if(delete != null) {
			left+=20;
			render(delete, left, top, null, stack, mouseX, mouseY, partialTicks);			
			left-=20;
		}
		render(revert, left, top, this::isChanged, stack, mouseX, mouseY, partialTicks);
		left -=20;
		render(reset, left, top, this::isNotDefault, stack, mouseX, mouseY, partialTicks);
		if(!getSuggestions().isEmpty()) {
			left -=20;
			render(suggestion, left, top, null, stack, mouseX, mouseY, partialTicks);
		}
		left -=20;
		render(edit, left, top, null, stack, mouseX, mouseY, partialTicks);
	}
	
	private void render(AbstractWidget widget, int x, int y, BooleanSupplier active, PoseStack stack, int mouseX, int mouseY, float partialTicks) {
		if(widget == null) return;
		widget.x = x;
		widget.y = y;
		if(active != null) widget.active = active.getAsBoolean();
		widget.render(stack, mouseX, mouseY, partialTicks);
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
			case COMPOUND: return new CompoundElement(node.asCompound());
			case LIST: return new ArrayElement(node.asArray());
			case SIMPLE: return createFromType(node.asValue(), node.asValue().getDataType());
			default: throw new IllegalStateException("Unknown Node Type");
		}
	}
	
	protected BaseElement createFromType(IValueNode node, DataType type) {
		if(type == DataType.ENUM || node.isForcingSuggestions()) return new SelectionElement(node);
		if(type == DataType.BOOLEAN) return new BooleanElement(node);
		if(type == DataType.INTEGER) return new IntegerElement(node);
		if(type == DataType.LONG) return new LongElement(node);
		if(type == DataType.DOUBLE || type == DataType.FLOAT) return new DoubleElement(node);
		if(type == DataType.STRING) return new StringElement(node);
		return null;
	}
	
	protected final Font getFont() {
		return Minecraft.getInstance().font;
	}
}
