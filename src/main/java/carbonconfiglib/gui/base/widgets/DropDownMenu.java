package carbonconfiglib.gui.base.widgets;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.OptionalInt;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import carbonconfiglib.gui.base.helpers.Align;
import carbonconfiglib.gui.base.helpers.GuiUtils;
import carbonconfiglib.gui.base.helpers.Icon;
import carbonconfiglib.gui.base.screen.BaseCarbonScreen;
import carbonconfiglib.gui.base.widgets.CarbonEditBox.TextState;
import carbonconfiglib.gui.base.widgets.CarbonList.ListEntry;
import carbonconfiglib.gui.base.widgets.CarbonList.ListMultiState;
import carbonconfiglib.gui.base.widgets.CarbonList.ListState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.client.ClientHooks;
import speiger.src.collections.objects.lists.ObjectArrayList;


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
	public void onPress(InputWithModifiers input) {
		BaseCarbonScreen.pushExternalScreen(new DropDownScreen<>(this));
	}
	
	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
		if(event.input() == 2 && this.isMouseOver(event.x(), event.y())) {
			state.reset();
			return true;
		}
		return super.mouseClicked(event, doubleClick);
	}
	
	public boolean shouldTakeFocusAfterInteraction() {
		return false;
	}
	
	@Override
	@SuppressWarnings("rawtypes")
	protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
		if(state.isSimpleButton()) {
			Icon icon = state.getIcon();
			if(icon != null) {
				graphics.blitSprite(RenderPipelines.GUI_TEXTURED, SPRITES.get(active, isHoveredOrFocused()), getX(), getY(), width, height);
				icon.drawIcon(graphics, getX()+2, getY()+2, width-4, height-4, state.iconWidth, state.iconHeight, -1);
				return;
			}
			super.extractContents(graphics, mouseX, mouseY, partialTick);
			return;
		}
		Minecraft mc = Minecraft.getInstance();
		boolean open = mc.screen instanceof DropDownScreen drop && drop.owner == this;
		boolean up = open && ((DropDownScreen)mc.screen).isUp();
		graphics.blitSprite(RenderPipelines.GUI_TEXTURED, SPRITES.get(active, isHoveredOrFocused() || open), getX(), getY(), width-14, height);
		graphics.blitSprite(RenderPipelines.GUI_TEXTURED, SPRITES.get(active, isHoveredOrFocused() || open), getX()+width-15, getY(), 15, height);
		GuiUtils.drawScrollingShadowText(graphics, mc.font, getMessage(), getX()+2, getY(), width-18, height, Align.CENTER, getFGColor(), hash);
		GuiUtils.drawScrollingShadowText(graphics, mc.font, Component.literal(open ? (up ? "▲" : "▼") : "◀"), getX()+width-13, getY(), 11, height, Align.CENTER, getFGColor(), hash);
	}
	
	public static class DropDownScreen<T> extends BaseCarbonScreen {
		boolean reOpen = false;
		ListState<DropDownEntry<T>> selections;
		TextState state = new TextState();
		DropDownMenu<T> owner;
		DropDownState<T> ownerState;
		
		public DropDownScreen(DropDownMenu<T> owner) {
			this.owner = owner;
			this.ownerState = owner.getState();
			generateElements();
			renderBackground = false;
		}
		
		private void generateElements() {
			selections = (ownerState.isMultiSelection() ? new ListMultiState<DropDownEntry<T>>() : new ListState<DropDownEntry<T>>());
			selections.setSelectable(true).setParentRowWidth();
			Function<T, Component> text = ownerState.displayFunction;
			Function<T, CarbonList.ListEntry<?>> render = ownerState.renderFunction != null ? ownerState.renderFunction : _ -> null;
			
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
				if(!ownerState.isMultiSelection()) ClientHooks.popGuiLayer(getMinecraft());
				if(selections.getSelectedItems().isEmpty() && !ownerState.isMultiSelection()) ownerState.reset();
				else ownerState.replaceSelection(selections.getSelectedItems().stream().map(DropDownEntry::getEntry).toList());
			});
			state.setCallback(selections::search);
		}
		
		public boolean isUp() {
			int maxHeight = ownerState.getElementHeight() * ownerState.getDisplayedElements() + (ownerState.searchable ? 20 : 0);
			int yDiff = this.height - (owner.getY() + owner.getHeight() + maxHeight);
			return yDiff < 0 && yDiff < owner.getY() - maxHeight;
		}
		
		public void resize(Minecraft pMinecraft, int pWidth, int pHeight) {
			reOpen = true;
		}
		
		@Override
		protected void init() {
			int x = owner.getX() + ownerState.xOffset;
			int y = owner.getY() + owner.getHeight();
			int width = ownerState.customWidth.orElse(owner.getWidth());
			int height = ownerState.getElementHeight() * ownerState.getDisplayedElements();
			
			int maxHeight = height + (ownerState.searchable ? 20 : 0); 
			if(isUp()) {
				y = owner.getY() - maxHeight - 1;
			}
			if(ownerState.searchable) text(x+1, y+1, width-2, 18, state);
			boolean wasOwned = selections.getOwner() != null;
			CarbonList<DropDownEntry<T>> list = listArea(x, y+(ownerState.searchable ? 20 : 0), width, height, selections);
			if(!wasOwned && selections.getSelected() != null) {
				list.centerScrollOn(selections.getSelected());
			}
			selections.setScrollOffset(1);
		}
		
		@Override
		public void drawBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
			if(reOpen) {
				reOpen = false;
				Minecraft.getInstance().popGuiLayer();
				owner.onPress(null);
			}
			int x = owner.getX() + ownerState.xOffset;
			int y = owner.getY() + owner.getHeight();
			int width = ownerState.customWidth.orElse(owner.getWidth());
			int height = ownerState.getElementHeight() * ownerState.getDisplayedElements()+(ownerState.searchable ? 20 : 0);
			if(isUp()) {
				y = owner.getY() - height - 1;
			}
			renderSelection(graphics, x, y, width, height, 0xFFA0A0A0, 0xFF000000);
		}
		
		public void renderSelection(GuiGraphicsExtractor graphics, int left, int top, int width, int height, int frameColor, int backgroundColor) {
			graphics.fill(left, top, left + width, top + height, frameColor);
			graphics.fill(left + 1, top + 1, left + width - 1, top + height - 1, backgroundColor);
		}
		
		@Override
		public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
			if(super.mouseClicked(event, doubleClick)) return true;
			ClientHooks.popGuiLayer(getMinecraft());
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
			return text.getString().toLowerCase(Locale.ROOT).contains(searchString);
		}
		
		@Override
		public void extractContent(GuiGraphicsExtractor graphics, int mouseX, int mouseY, boolean selected, float partialTicks) {
			if(owner.getHovered() == this) {
				owner.extractSelection(graphics, this, GuiUtils.brighter(getSelectionColor(false), 0.8F), getSelectionBackgroundColor());
			}
			if(renderer != null) {
				renderer.setOwner(owner);
				renderer.setX(getX()+1);
				renderer.setY(getY()+1);
				renderer.setWidth(getWidth()-2);
				renderer.setHeight(getHeight()-2);
				renderer.extractContent(graphics, mouseX, mouseY, selected, partialTicks);
				return;
			}
			GuiUtils.drawScrollingShadowText(graphics, font, text, getContentX(), getContentY(), getContentWidth(), getContentHeight(), Align.CENTER, -1, Objects.hashCode(data));
		}
		
		public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
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
		Component prefix = Component.translatable("gui.carbonconfig.dropdown.selected");
		Component empty = Component.translatable("gui.carbonconfig.dropdown.none");
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
			return (valueOnly ? Component.empty() : prefix.copy().append(": ")).append(selected.size() == 0 ? empty : (selected.size() == 1 ? displayFunction.apply(selected.get(0)) : Component.translatable("gui.carbonconfig.dropdown.selected_elements", selected.size())));
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
		
		public boolean isAllowingEmpty() {
			return allowEmpty;
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
		
		public T getSelectedElement() {
			return selected.isEmpty() ? null : selected.get(0);
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
