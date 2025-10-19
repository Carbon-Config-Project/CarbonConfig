package carbonconfiglib.gui.base.widgets;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.OptionalInt;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import carbonconfiglib.gui.base.helpers.GuiUtils;
import carbonconfiglib.gui.base.screen.BaseCarbonScreen;
import carbonconfiglib.gui.base.widgets.CarbonEditBox.TextState;
import carbonconfiglib.gui.base.widgets.CarbonList.ListEntry;
import carbonconfiglib.gui.base.widgets.CarbonList.ListMultiState;
import carbonconfiglib.gui.base.widgets.CarbonList.ListState;
import carbonconfiglib.gui.config.ConfigElement.GuiAlign;
import carbonconfiglib.gui.widgets.Icon;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.gui.ScreenUtils;

public class DropDownMenu<T> extends CarbonButton {
	
	DropDownState<T> state;
	
	public DropDownMenu(int xPos, int yPos, int width, int height, DropDownState<T> state) {
		super(xPos, yPos, width, height, state.generateText(), null);
		this.state = state;
		state.owner = this;
	}
	
	public DropDownState<T> getState() {
		return state;
	}
	
	@Override
	public void onPress() {
		BaseCarbonScreen.pushExternalScreen(new DropDownScreen<>(this));
	}
	
	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if(button == 2 && clicked(mouseX, mouseY)) {
			state.reset();
			return true;
		}
		return super.mouseClicked(mouseX, mouseY, button);
	}
	
	@Override
	@SuppressWarnings("rawtypes")
	public void renderButton(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
		if(state.isSimpleButton()) {
			Icon icon = state.getIcon();
			if(icon != null) {
				int k = this.getYImage(this.isHovered);
				ScreenUtils.blitWithBorder(poseStack, WIDGETS_LOCATION, this.x, this.y, 0, 46 + k * 20, this.width, this.height, 200, 20, 2, 3, 2, 2, this.getBlitOffset());
				RenderSystem.enableDepthTest();
				RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, this.alpha);
				RenderSystem.enableBlend();
				RenderSystem.defaultBlendFunc();
				RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
		    	GuiUtils.drawTextureRegion(poseStack, x+2, y+2, width-4, height-4, state.getIcon(), state.iconWidth, state.iconHeight);
		    	return;
			}
			super.renderButton(poseStack, mouseX, mouseY, partialTick);
			return;
		}
		Minecraft mc = Minecraft.getInstance();
		boolean open = mc.screen instanceof DropDownScreen && ((DropDownScreen)mc.screen).owner == this;
		boolean up = open && ((DropDownScreen)mc.screen).isUp();
		int k = this.getYImage(this.isHovered || open);
		ScreenUtils.blitWithBorder(poseStack, WIDGETS_LOCATION, this.x, this.y, 0, 46 + k * 20, this.width-14, this.height, 200, 20, 2, 3, 2, 2, this.getBlitOffset());
		ScreenUtils.blitWithBorder(poseStack, WIDGETS_LOCATION, this.x+width-15, this.y, 0, 46 + k * 20, 15, this.height, 200, 20, 2, 3, 2, 2, this.getBlitOffset());
		GuiUtils.drawScrollingShadowText(poseStack, mc.font, getMessage(), x+2, y, width-18, height, GuiAlign.CENTER, getFGColor(), hash);
		GuiUtils.drawScrollingShadowText(poseStack, mc.font, Component.literal(open ? (up ? "▲" : "▼") : "◀"), x+width-15, y, 11, height, GuiAlign.CENTER, getFGColor(), hash);
	}
	
	public static class DropDownScreen<T> extends BaseCarbonScreen {
		ListState<DropDownEntry<T>> selections;
		TextState state = new TextState();
		DropDownMenu<T> owner;
		DropDownState<T> ownerState;
		
		public DropDownScreen(DropDownMenu<T> owner) {
			this.owner = owner;
			this.ownerState = owner.getState();
			generateElements();
		}
		
		private void generateElements() {
			selections = (ownerState.isMultiSelection() ? new ListMultiState<DropDownEntry<T>>() : new ListState<DropDownEntry<T>>());
			selections.setSelectable(true).setParentRowWidth();
			Function<T, Component> text = ownerState.displayFunction;
			Function<T, CarbonList.ListEntry<?>> render = ownerState.renderFunction != null ? ownerState.renderFunction : T -> null;
			
			if(ownerState.allowEmpty && !ownerState.isMultiSelection()) {
				DropDownEntry<T> value = new DropDownEntry<T>(null, ownerState.empty, null);
				selections.add(value);
				if(ownerState.getSelected().isEmpty()) selections.setSelected(value);
			}
			for(T entry : ownerState.getValues()) {
				DropDownEntry<T> value = new DropDownEntry<>(entry, text.apply(entry), render.apply(entry));
				selections.add(value);
				if(ownerState.isSelected(entry)) selections.setSelected(value);
			}
			selections.setItemHeight(ownerState.getElementHeight()).setChangeListener(() -> {
				if(selections.getSelectedItems().isEmpty() && !ownerState.isMultiSelection()) ownerState.reset();
				else ownerState.replaceSelection(selections.getSelectedItems().stream().map(DropDownEntry::getEntry).toList());
				if(!ownerState.isMultiSelection()) ForgeHooksClient.popGuiLayer(getMinecraft());
			});
			state.setCallback(selections::search);
		}
		
		public boolean isUp() {
			int maxHeight = ownerState.getElementHeight() * ownerState.getDisplayedElements() + (ownerState.searchable ? 20 : 0);
			int yDiff = this.height - (owner.y + owner.getHeight() + maxHeight);
			return yDiff < 0 && yDiff < owner.y - maxHeight;
		}
		
		@Override
		protected void init() {
			int x = owner.x + ownerState.xOffset;
			int y = owner.y + owner.getHeight();
			int width = ownerState.customWidth.orElse(owner.getWidth());
			int height = ownerState.getElementHeight() * ownerState.getDisplayedElements();
			
			int maxHeight = height + (ownerState.searchable ? 20 : 0); 
			if(isUp()) {
				y = owner.y - maxHeight - 1;
			}
			if(ownerState.searchable) text(x+1, y+1, width-2, 18, state);
			listArea(x, y+(ownerState.searchable ? 20 : 0), width, height, selections);
			selections.setScrollOffset(1);
		}
		
		@Override
		public void renderBackground(PoseStack matrix, int mouseX, int mouseY, float partialTicks) {
			int x = owner.x + ownerState.xOffset;
			int y = owner.y + owner.getHeight();
			int width = ownerState.customWidth.orElse(owner.getWidth());
			int height = ownerState.getElementHeight() * ownerState.getDisplayedElements()+(ownerState.searchable ? 20 : 0);
			if(isUp()) {
				y = owner.y - height - 1;
			}
			renderSelection(matrix, x, y, width, height, 0xFFA0A0A0, 0xFF000000);
		}
		
		public void renderSelection(PoseStack matrix, int left, int top, int width, int height, int frameColor, int backgroundColor) {
			fill(matrix, left, top, left + width, top + height, frameColor);
			fill(matrix, left + 1, top + 1, left + width - 1, top + height - 1, backgroundColor);
		}
		
		@Override
		public boolean mouseClicked(double mouseX, double mouseY, int button) {
			if(super.mouseClicked(mouseX, mouseY, button)) return true;
			ForgeHooksClient.popGuiLayer(getMinecraft());
			return true;
		}
	}
	
	public static class DropDownEntry<T> extends ListEntry<DropDownEntry<T>> {
		T data;
		ListEntry<?> renderer;
		Component text;
		
		public DropDownEntry(T data, Component text, ListEntry<?> renderer) {
			this.data = data;
			this.renderer = renderer;
			this.text = text;
		}

		public T getEntry() {
			return data;
		}
		
		@Override
		protected boolean containsSearch(String searchString) {
			return text.getString().contains(searchString);
		}
		
		@Override
		public void render(PoseStack poseStack, int x, int top, int left, int width, int height, int mouseX, int mouseY, boolean selected, float partialTicks) {
			if(owner.getHovered() == this) {
				owner.renderSelection(poseStack, left, top, width, height, GuiUtils.brighter(getSelectionColor(false), 0.8F), getSelectionBackgroundColor());
			}
			if(renderer != null) {
				renderer.render(poseStack, x, top+1, left+1, width-2, height-2, mouseX, mouseY, selected, partialTicks);
				return;
			}
			GuiUtils.drawScrollingShadowText(poseStack, font, text, left, top, width, height, GuiAlign.CENTER, -1, Objects.hashCode(data));
		}
		
		@Override
		public boolean mouseClicked(double mouseX, double mouseY, int button) {
			owner.setSelected(data == null ? null : this);
			return true;
		}
	}
	
	public static class DropDownState<T> {
		Function<T, Component> displayFunction;
		Function<T, ListEntry<?>> renderFunction;
		Supplier<List<T>> dynamicValues;
		List<T> values = new ObjectArrayList<>();
		List<T> selected = new ObjectArrayList<>();
		List<T> defaultSelected = new ObjectArrayList<>();
		DropDownMenu<T> owner;
		int displayedElements = 5;
		int elementHeight = 14;
		boolean multiSelection;
		boolean searchable = true;
		boolean allowEmpty = true;
		boolean valueOnly;
		boolean simpleButtonOnly;
		Icon icon;
		int iconWidth = 16;
		int iconHeight = 16;
		Component prefix = Component.literal("Selected");
		Component empty = Component.literal("None");
		Consumer<List<T>> listener;
		int xOffset = 0;
		OptionalInt customWidth = OptionalInt.empty();
		
		@SafeVarargs
		public DropDownState(Function<T, Component> displayFunction, T... values) {
			this(displayFunction, Arrays.asList(values));
		}
		
		public DropDownState(Function<T, Component> displayFunction, List<T> values) {
			this.displayFunction = displayFunction;
			this.values.addAll(values);
			this.multiSelection = false;
		}
		
		@SafeVarargs
		public DropDownState(T selected, Function<T, Component> displayFunction, T... values) {
			this(selected, displayFunction, Arrays.asList(values));
		}
		
		public DropDownState(T selected, Function<T, Component> displayFunction, List<T> values) {
			this.displayFunction = displayFunction;
			this.values.addAll(values);
			this.selected.add(selected);
			this.defaultSelected.add(selected);
			this.multiSelection = false;
		}
		
		@SafeVarargs
		public DropDownState(List<T> selection, Function<T, Component> displayFunction, T... values) {
			this(selection, displayFunction, Arrays.asList(values));
		}
		
		public DropDownState(List<T> selection, Function<T, Component> displayFunction, List<T> values) {
			this.displayFunction = displayFunction;
			this.values.addAll(values);
			this.selected.addAll(selection);
			this.defaultSelected.addAll(selection);
			this.multiSelection = true;
		}
		
		public DropDownState<T> withPrefix(Component prefix) {
			this.prefix = Objects.requireNonNull(prefix);
			updateText();
			return this;
		}
		
		public DropDownState<T> withEmpty(Component empty) {
			this.empty = Objects.requireNonNull(empty);
			updateText();
			return this;
		}
		
		public DropDownState<T> setSearchable(boolean value) {
			this.searchable = value;
			return this;
		}
		
		public DropDownState<T> asSimpleButton(boolean value) {
			this.simpleButtonOnly = value;
			return this;
		}
		
		public boolean isSimpleButton() {
			return simpleButtonOnly;
		}
		
		public DropDownState<T> withDynamicValues(Supplier<List<T>> dynamicValues) {
			this.dynamicValues = dynamicValues;
			return this;
		}
		
		protected List<T> getValues() {
			return dynamicValues != null ? dynamicValues.get() : values;
		}
		
		public DropDownState<T> withIcon(Icon icon) {
			this.icon = icon;
			return this;
		}
		
		public DropDownState<T> withIconBounds(int width, int height) {
			this.iconWidth = width;
			this.iconHeight = height;
			return this;
		}
		
		public Icon getIcon() {
			return icon;
		}
		
		public void updateText() {
			if(owner == null) return;
			owner.setMessage(generateText());
		}
		
		protected Component generateText() {
			return (valueOnly ? Component.empty() : prefix.copy().append(": ")).append(selected.size() == 0 ? empty : (selected.size() == 1 ? displayFunction.apply(selected.get(0)) : Component.literal(selected.size()+" Selected")));
		}
		
		public DropDownState<T> withListener(Consumer<List<T>> listener) {
			this.listener = listener;
			return this;
		}
		
		public DropDownState<T> withCustomRenderer(Function<T, ListEntry<?>> function) {
			this.renderFunction = function;
			return this;
		}
		
		public DropDownState<T> withCustomWidth(OptionalInt width) {
			customWidth = width;
			return this;
		}
		
		public DropDownState<T> withXOffset(int offset) {
			xOffset = offset;
			return this;
		}
		
		public DropDownState<T> allowEmpty(boolean value) {
			allowEmpty = value;
			return this;
		}
		
		public DropDownState<T> valueOnly(boolean value) {
			this.valueOnly = value;
			return this;
		}
		
		public boolean isValueOnly() {
			return valueOnly;
		}
		
		public DropDownState<T> setDisplayedElements(int newValue) {
			this.displayedElements = newValue;
			return this;
		}
		
		public int getDisplayedElements() {
			return displayedElements;
		}
		
		public DropDownState<T> setElementHeight(int newValue) {
			this.elementHeight = newValue;
			return this;
		}
		
		public int getElementHeight() {
			return elementHeight;
		}
		
		public DropDownState<T> setMultiSelection(boolean multiSelection) {
			this.multiSelection = multiSelection;
			if(!multiSelection) {
				ensureSingle(selected);
				ensureSingle(defaultSelected);
			}
			return this;
		}
		
		public boolean isMultiSelection() {
			return multiSelection;
		}
		
		public DropDownState<T> findDefaultSelected(Predicate<T> filter) {
			defaultSelected.clear();
			for(int i = 0,m=this.values.size();i<m;i++) {
				T value = values.get(i);
				if(filter.test(value)) defaultSelected.add(value);
			}
			selected.addAll(defaultSelected);
			if(!multiSelection) {
				ensureSingle(defaultSelected);
				ensureSingle(selected);
			}
			updateText();
			return this;
		}
		
		public DropDownState<T> findSelected(Predicate<T> filter) {
			selected.clear();
			List<T> values = getValues();
			for(int i = 0,m=values.size();i<m;i++) {
				T value = values.get(i);
				if(filter.test(value)) selected.add(value);
			}
			if(!multiSelection) ensureSingle(selected);
			updateText();
			return this;
		}
		
		public DropDownState<T> replaceSelection(List<T> values) {
			selected.clear();
			selected.addAll(values);
			if(!multiSelection) ensureSingle(selected);
			if(listener != null) listener.accept(new ObjectArrayList<>(selected));
			updateText();
			return this;
		}
		
		public DropDownState<T> reset() {
			selected.clear();
			selected.addAll(defaultSelected);
			if(!multiSelection) ensureSingle(selected);
			if(listener != null) listener.accept(new ObjectArrayList<>(selected));
			updateText();
			return this;
		}
		
		public DropDownState<T> setValues(List<T> values) {
			this.values.clear();
			this.values.addAll(values);
			return this;
		}
		
		public DropDownState<T> setDefaultValues(List<T> values) {
			this.defaultSelected.clear();
			this.defaultSelected.addAll(values);
			this.selected.clear();
			this.selected.addAll(values);
			if(!multiSelection) {
				ensureSingle(defaultSelected);
				ensureSingle(selected);
			}
			return this;
		}
		
		public DropDownState<T> setSelected(T value, boolean set) {
			if(set) {
				if(!multiSelection) selected.clear();
				selected.add(value);
				if(listener != null) listener.accept(new ObjectArrayList<>(selected));
				updateText();
				return this;
			}
			selected.remove(value);
			if(listener != null) listener.accept(new ObjectArrayList<>(selected));
			updateText();
			return this;
		}
		
		public void clearSelection() {
			selected.clear();
			updateText();
		}
		
		public boolean isSelected(T value) {
			return selected.contains(value);
		}
		
		public List<T> getSelected() {
			return new ObjectArrayList<>(selected);
		}
		
		private void ensureSingle(List<T> values) {
			if(values.size() <= 1) return;
			T value = values.get(0);
			values.clear();
			values.add(value);
		}
	}
}
