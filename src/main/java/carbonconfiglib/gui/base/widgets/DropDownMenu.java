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
import java.util.stream.Collectors;

import org.lwjgl.opengl.GL11;

import carbonconfiglib.gui.base.helpers.Align;
import carbonconfiglib.gui.base.helpers.GuiUtils;
import carbonconfiglib.gui.base.helpers.Icon;
import carbonconfiglib.gui.base.helpers.Texts;
import carbonconfiglib.gui.base.screen.BaseCarbonScreen;
import carbonconfiglib.gui.base.screen.LayeredScreen;
import carbonconfiglib.gui.base.widgets.CarbonEditBox.TextState;
import carbonconfiglib.gui.base.widgets.CarbonList.ListEntry;
import carbonconfiglib.gui.base.widgets.CarbonList.ListMultiState;
import carbonconfiglib.gui.base.widgets.CarbonList.ListState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.util.IChatComponent;
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
	public void onPress() {
		BaseCarbonScreen.pushExternalScreen(new DropDownScreen<>(this));
	}
	
	@Override
	public boolean mouseClick(double mouseX, double mouseY, int button) {
		if(button == 2 && clicked(mouseX, mouseY)) {
			state.reset();
			return true;
		}
		return super.mouseClick(mouseX, mouseY, button);
	}
	
	protected boolean clicked(double pMouseX, double pMouseY) {
		return this.enabled && this.visible && pMouseX >= (double)this.xPosition && pMouseY >= (double)this.yPosition && pMouseX < (double)(this.xPosition + this.width) && pMouseY < (double)(this.yPosition + this.height);
	}
	
	@Override
	@SuppressWarnings({"rawtypes"})
	public void render(int mouseX, int mouseY, float partialTick) {
		if(state.isSimpleButton()) {
			Icon icon = state.getIcon();
			if(icon != null) {
				int k = this.getHoverState(this.field_146123_n);
				GuiUtils.blitWithBorder(buttonTextures, this.xPosition, this.yPosition, 0, 46 + k * 20, this.width, this.height, 200, 20, 2, 3, 2, 2, zLevel, false);
		        GL11.glEnable(GL11.GL_DEPTH_TEST);
				GL11.glColor4f(1.0F, 1.0F, 1.0F, 1F);
		        GL11.glEnable(GL11.GL_BLEND);
		        OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
		    	GuiUtils.drawTextureRegion(xPosition+2, yPosition+2, width-4, height-4, state.getIcon(), state.iconWidth, state.iconHeight);
		    	return;
			}
			super.render(mouseX, mouseY, partialTick);
			return;
		}
		Minecraft mc = Minecraft.getMinecraft();
		boolean open = mc.currentScreen instanceof DropDownScreen && ((DropDownScreen)mc.currentScreen).owner == this;
		boolean up = open && ((DropDownScreen)mc.currentScreen).isUp();
		int k = this.getHoverState(this.field_146123_n || open);
		int j = this.enabled ? 16777215 : 10526880;
		GuiUtils.blitWithBorder(buttonTextures, this.xPosition, this.yPosition, 0, 46 + k * 20, this.width-14, this.height, 200, 20, 2, 3, 2, 2, zLevel, false);
		GuiUtils.blitWithBorder(buttonTextures, this.xPosition+width-15, this.yPosition, 0, 46 + k * 20, 15, this.height, 200, 20, 2, 3, 2, 2, zLevel, false);
		GuiUtils.drawScrollingShadowText(mc.fontRenderer, displayString, xPosition+2, yPosition, width-18, height, Align.CENTER, j, 0);
		GuiUtils.drawScrollingShadowText(mc.fontRenderer, Texts.literal(open ? (up ? "▲" : "▼") : "◀"), xPosition+width-13, yPosition, 11, height, Align.CENTER, j, 0);
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
		}
		
		private void generateElements() {
			selections = (ownerState.isMultiSelection() ? new ListMultiState<DropDownEntry<T>>() : new ListState<DropDownEntry<T>>());
			selections.setSelectable(true).setParentRowWidth();
			Function<T, IChatComponent> text = ownerState.displayFunction;
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
				else ownerState.replaceSelection(selections.getSelectedItems().stream().map(DropDownEntry::getEntry).collect(Collectors.toList()));
				if(!ownerState.isMultiSelection()) LayeredScreen.popGuiLayer();
			});
			state.setCallback(selections::search);
		}
		
		public boolean isUp() {
			int maxHeight = ownerState.getElementHeight() * ownerState.getDisplayedElements() + (ownerState.searchable ? 20 : 0);
			int yDiff = this.height - (owner.yPosition + owner.getHeight() + maxHeight);
			return yDiff < 0 && yDiff < owner.yPosition - maxHeight;
		}
		
		public void resize(Minecraft pMinecraft, int pWidth, int pHeight) {
			reOpen = true;
		}
		
		@Override
		public void initGui() {
			int x = owner.xPosition + ownerState.xOffset;
			int y = owner.yPosition + owner.getHeight();
			int width = ownerState.customWidth.orElse(owner.getWidth());
			int height = ownerState.getElementHeight() * ownerState.getDisplayedElements();
			
			int maxHeight = height + (ownerState.searchable ? 20 : 0); 
			if(isUp()) {
				y = owner.yPosition - maxHeight - 1;
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
		public void renderBackground(int mouseX, int mouseY, float partialTicks) {
			if(reOpen) {
				reOpen = false;
				LayeredScreen.popGuiLayer();
				owner.onPress();
			}
			int x = owner.xPosition + ownerState.xOffset;
			int y = owner.yPosition + owner.getHeight();
			int width = ownerState.customWidth.orElse(owner.getWidth());
			int height = ownerState.getElementHeight() * ownerState.getDisplayedElements()+(ownerState.searchable ? 20 : 0);
			if(isUp()) {
				y = owner.yPosition - height - 1;
			}
			renderSelection(x, y, width, height, 0xFFA0A0A0, 0xFF000000);
		}
		
		public void renderSelection(int left, int top, int width, int height, int frameColor, int backgroundColor) {
			Gui.drawRect(left, top, left + width, top + height, frameColor);
			Gui.drawRect(left + 1, top + 1, left + width - 1, top + height - 1, backgroundColor);
		}
		
		@Override
		public boolean mouseClick(double mouseX, double mouseY, int button) {
			if(super.mouseClick(mouseX, mouseY, button)) return true;
			LayeredScreen.popGuiLayer();
			return true;
		}
	}
	
	public static class DropDownEntry<T> extends ListEntry<DropDownEntry<T>> {
		T data;
		ListEntry<?> renderer;
		IChatComponent text;
		
		public DropDownEntry(T data, IChatComponent text, ListEntry<?> renderer) {
			this.data = data;
			this.renderer = renderer;
			this.text = text;
		}

		public T getEntry() {
			return data;
		}
		
		@Override
		protected boolean containsSearch(String searchString) {
			return text.getUnformattedText().toLowerCase(Locale.ROOT).contains(searchString);
		}
		
		@Override
		public void render(int x, int top, int left, int width, int height, int mouseX, int mouseY, boolean selected, float partialTicks) {
			if(owner.getHovered() == this) {
				owner.renderSelection(left, top, width, height, GuiUtils.brighter(getSelectionColor(false), 0.8F), getSelectionBackgroundColor());
			}
			if(renderer != null) {
				renderer.render(x, top+1, left+1, width-2, height-2, mouseX, mouseY, selected, partialTicks);
				return;
			}
			GuiUtils.drawScrollingShadowText(font, text, left, top, width, height, Align.CENTER, -1, Objects.hashCode(data));
		}
		
		@Override
		public boolean mouseClick(double mouseX, double mouseY, int button) {
			owner.setSelected(data == null ? null : this);
			return true;
		}
	}
	
	public static class DropDownState<T> {
		Function<T, IChatComponent> displayFunction;
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
		IChatComponent prefix = Texts.translatable("gui.carbonconfig.dropdown.selected");
		IChatComponent empty = Texts.translatable("gui.carbonconfig.dropdown.none");
		Consumer<List<T>> listener;
		int xOffset = 0;
		OptionalInt customWidth = OptionalInt.empty();
		
		@SafeVarargs
		public DropDownState(Function<T, IChatComponent> displayFunction, T... values) {
			this(displayFunction, Arrays.asList(values));
		}
		
		public DropDownState(Function<T, IChatComponent> displayFunction, List<T> values) {
			this.displayFunction = displayFunction;
			this.values.addAll(values);
			this.multiSelection = false;
		}
		
		@SafeVarargs
		public DropDownState(T selected, Function<T, IChatComponent> displayFunction, T... values) {
			this(selected, displayFunction, Arrays.asList(values));
		}
		
		public DropDownState(T selected, Function<T, IChatComponent> displayFunction, List<T> values) {
			this.displayFunction = displayFunction;
			this.values.addAll(values);
			this.selected.add(selected);
			this.defaultSelected.add(selected);
			this.multiSelection = false;
		}
		
		@SafeVarargs
		public DropDownState(List<T> selection, Function<T, IChatComponent> displayFunction, T... values) {
			this(selection, displayFunction, Arrays.asList(values));
		}
		
		public DropDownState(List<T> selection, Function<T, IChatComponent> displayFunction, List<T> values) {
			this.displayFunction = displayFunction;
			this.values.addAll(values);
			this.selected.addAll(selection);
			this.defaultSelected.addAll(selection);
			this.multiSelection = true;
		}
		
		public DropDownState<T> withPrefix(IChatComponent prefix) {
			this.prefix = Objects.requireNonNull(prefix);
			updateText();
			return this;
		}
		
		public DropDownState<T> withEmpty(IChatComponent empty) {
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
			owner.displayString = generateText().getFormattedText();
		}
		
		protected IChatComponent generateText() {
			return (valueOnly ? Texts.empty() : prefix.createCopy().appendText(": ")).appendSibling(selected.size() == 0 ? empty : (selected.size() == 1 ? displayFunction.apply(selected.get(0)) : Texts.translatable("gui.carbonconfig.dropdown.selected_elements", selected.size())));
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
