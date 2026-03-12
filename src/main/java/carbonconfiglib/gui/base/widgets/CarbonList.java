package carbonconfiglib.gui.base.widgets;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.IntSupplier;
import java.util.function.Predicate;

import com.mojang.blaze3d.vertex.PoseStack;

import carbonconfiglib.gui.base.helpers.ITooltipProvider;
import carbonconfiglib.gui.base.helpers.SmoothDouble;
import carbonconfiglib.gui.base.widgets.CarbonList.ListEntry;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectLists;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import speiger.src.collections.ints.functions.consumer.IntIntConsumer;
import speiger.src.collections.objects.sets.ObjectLinkedOpenHashSet;


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
public class CarbonList<T extends ListEntry<T>> extends CarbonDynamicList<T> implements ITooltipProvider {
	ListState<T> state;
	
	public CarbonList(Screen owner, int width, int height, int startY, int endY, ListState<T> state) {
		super(owner.getMinecraft(), width, height, startY, endY, state.getItemHeight());
		this.state = state;
		state.setOwner(this);
		applySearch(state.getSearch());
		if(state.isFramed() || state.isSelectable()) setRenderSelection(true);
	}
	
	public CarbonList(Screen owner, ListState<T> state) {
		this(owner, owner.width, owner.height, state.getTopPadding(), owner.height-state.getBottomPadding(), state);
	}
	
	@Override
	public void provideTooltips(int mouseX, int mouseY, Consumer<Component> tooltips) {
		for(T entry : children()) {
			if(entry instanceof ITooltipProvider) {
				((ITooltipProvider)entry).provideTooltips(mouseX, mouseY, tooltips);
			}
		}
	}
	
	@Override
	protected boolean areChildrenDraggable() {
		return state.draggable;
	}
	
	@Override
	protected void onElementsSwapped(int oldIndex, int newIndex) {
		if(state.draggingListener != null) {
			state.draggingListener.accept(oldIndex, newIndex);
		}
	}
	
	@Override
	protected boolean shouldRender() {
		return state.visible;
	}
	
	@Override
	protected boolean enabled() {
		return state.enabled;
	}
	
	@Override
	public int getRowWidth() {
		return state.getRowWidth() - 4; 
	}
	
	@Override
	public int getRowLeft() {
		return x0 + 2;
	}
	
	@Override
	protected boolean isSelectedItem(int index) {
		return state.isSelected(index);
	}
	
	@Override
	public T getSelected() { return state.getSelected(); }
	@Override
	public void setSelected(T element) {
		if(!state.isSelectable()) return;
		state.setSelected(element);
	}
	
	@Override
	protected int getScrollbarPosition() {
		return getLeft() + this.width / 2 + (state.getRowWidth() / 2) + state.scrollOffset; 
	}
	
	@Override
	protected void scroll(int value) {
		setScrollAmount(state.scrollAmount.getTarget() + value, isScrolling());
	}
	
	@Override
	public void setScrollAmount(double value) {
		setScrollAmount(value, isScrolling());
	}
	
	public void setScrollAmount(double value, boolean force) {
		float actualValue = (float)Mth.clamp(value, 0, getMaxScroll());
		state.setScrollAmount(actualValue);
		if(force) state.scrollAmount.forceFinish();
	}
	
	@Override
	public double getScrollAmount() {
		return state.getScrollAmount(); 
	}
	
	@Override
	public void render(PoseStack stack, int mouseX, int mouseY, float partialTicks) {
		boolean finished = state.scrollAmount.isDone();
		state.scrollAmount.update(partialTicks);
		super.setScrollAmount(state.scrollAmount.getValue());
		if(!finished) {
			handleDragging(mouseX, mouseY);
		}
		super.render(stack, mouseX, mouseY, partialTicks);
	}
	
	public void applySearch(String search) {
		state.updateSearch(search);
		if(search == null || search.isEmpty()) {
			replaceEntries(state.getNodes());
			setScrollAmount(getScrollAmount());
			return;
		}
		String actualSearch = search.toLowerCase(Locale.ROOT);
		List<T> nodes = new ObjectArrayList<>();
		for(T entry : state.getNodes()) {
			if(entry.containsSearch(actualSearch)) {
				nodes.add(entry);
			}
		}
		replaceEntries(nodes);
		setScrollAmount(getScrollAmount());
	}
		
	@Override
	public boolean mouseReleased(double p_93491_, double p_93492_, int p_93493_) {
		if(scrolling) state.scrollAmount.forceFinish();
		return super.mouseReleased(p_93491_, p_93492_, p_93493_);
	}
	
	public static abstract class ListEntry<T extends ListEntry<T>> extends CarbonDynamicList.DynamicEntry<T> implements ITooltipProvider {
		List<GuiEventListener> children = new ObjectArrayList<>();
		protected Font font = Minecraft.getInstance().font;

		protected <E extends GuiEventListener> E addChild(E child) {
			children.add(child);
			return child;
		}
		
		@Override
		public void provideTooltips(int mouseX, int mouseY, Consumer<Component> tooltips) {
			if(!isMouseOver(mouseX, mouseY)) return;
			for(GuiEventListener listener : children) {
				if(listener instanceof ITooltipProvider) {
					((ITooltipProvider)listener).provideTooltips(mouseX, mouseY, tooltips);
				}
			}
		}
		
		@Override
		public List<? extends GuiEventListener> children() {
			return children;
		}
		
		@Override
		public List<? extends NarratableEntry> narratables() {
			return ObjectLists.emptyList();
		}
		
		protected abstract boolean containsSearch(String searchString);
		@Override
		public abstract void render(PoseStack poseStack, int x, int top, int left, int width, int height, int mouseX, int mouseY, boolean selected, float partialTicks);
	}
	
	public static class ListState<T extends ListEntry<T>> {
		List<T> nodes = new ObjectArrayList<>();
		T selectedElement = null;
		String search = "";
		int top = 50;
		int bottom = 36;
		int itemHeight = 24;
		IntSupplier rowWidth = () -> 220;
		int scrollOffset = 14;
		SmoothDouble scrollAmount = new SmoothDouble(0.8F);
		boolean frame;
		boolean allowSelection;
		boolean enabled = true;
		boolean visible = true;
		CarbonList<T> owner;
		Runnable changeListener;
		boolean draggable;
		IntIntConsumer draggingListener;
				
		@SafeVarargs
		public ListState(T... nodes) {
			this.nodes.addAll(Arrays.asList(nodes));
		}
		
		public ListState(List<T> nodes) {
			this.nodes.addAll(nodes);
		}
		
		@SafeVarargs
		public ListState(int height, T... nodes) {
			this.itemHeight = height;
			this.nodes.addAll(Arrays.asList(nodes));
		}
		
		public ListState(int height, List<T> nodes) {
			this.nodes.addAll(nodes);
			this.itemHeight = height;
		}
		
		public ListState<T> add(T node) {
			this.nodes.add(node);
			return this;
		}
		
		@SuppressWarnings("unchecked")
		public ListState<T> add(T... nodes) {
			this.nodes.addAll(Arrays.asList(nodes));
			return this;
		}
		
		public ListState<T> add(List<T> nodes) {
			this.nodes.addAll(nodes);
			return this;
		}
		
		public ListState<T> clear() {
			nodes.clear();
			return this;
		}
		
		public ListState<T> replace(List<T> nodes) {
			this.nodes.clear();
			this.nodes.addAll(nodes);
			return search(search);
		}
		
		public ListState<T> remove(T entry) {
			nodes.remove(entry);
			if(selectedElement == entry) {
				selectedElement = null;
				changeListener.run();
			}
			return this;
		}
		
		public ListState<T> selectRandomElement() {
			if(nodes.isEmpty()) return this;
			int index = RandomSource.create().nextInt(nodes.size());
			setSelected(nodes.get(index));
			return this;
		}
		
		public ListState<T> findDefault(Predicate<T> value) {
			setSelected(findEntry(value));
			return this;
		}
		
		public T findEntry(Predicate<T> value) {
			for(T entry : nodes) {
				if(value.test(entry)) return entry;
			}
			return null;
		}
		
		public void forEach(Consumer<T> consumer) {
			nodes.forEach(consumer);
		}
		
		public boolean isEmpty() {
			return nodes.isEmpty();
		}
		
		public void sort(Comparator<T> sorter) {
			nodes.sort(sorter);
			if(owner != null) owner.children().sort(sorter);
		}
		
		public ListState<T> setItemHeight(int newHeight) {
			this.itemHeight = newHeight;
			return this;
		}
		
		public ListState<T> setRowWidth(int rowWidth) {
			this.rowWidth = () -> rowWidth;
			return this;
		}
		
		public ListState<T> setDynamicRowWidth(IntSupplier provider) {
			this.rowWidth = provider;
			return this;
		}
		
		public ListState<T> setParentRowWidth() {
			rowWidth = () -> owner != null ? owner.width : 0;
			return this;
		}
		
		public ListState<T> setScrollOffset(int offset) {
			this.scrollOffset = offset;
			return this;
		}
		
		public ListState<T> setFrame(boolean frame) {
			this.frame = frame;
			return this;
		}
		
		public ListState<T> setChangeListener(Runnable run) {
			this.changeListener = run;
			return this;
		}
		
		public ListState<T> setSelectable(boolean value) {
			allowSelection = value;
			return this;
		}
		
		public ListState<T> setSelected(T value) {
			selectedElement = value;
			if(changeListener != null) changeListener.run();
			return this;
		}
		
		public ListState<T> setVisible(boolean visible) {
			this.visible = visible; 
			return this;
		}
		
		public ListState<T> setEnabled(boolean enabled) {
			this.enabled = enabled; 
			return this;
		}
		
		public ListState<T> setTopPadding(int value) {
			top = value;
			return this;
		}
		
		public ListState<T> setBottomPadding(int value) {
			bottom = value;
			return this;
		}
		
		public ListState<T> setScrollAmount(double value) {
			scrollAmount.setTarget(value);
			return this;
		}
		
		public ListState<T> setDragListener(IntIntConsumer listener) {
			this.draggingListener = listener;
			return this;
		}
		
		public ListState<T> setDraggable(boolean value) {
			draggable = value;
			return this;
		}
		
		public double getScrollAmount() {
			return owner != null && owner.isScrolling() ? scrollAmount.getTarget() : scrollAmount.getValue();
		}
		
		public int getTopPadding() {
			return top; 
		}
		
		public int getBottomPadding() {
			return bottom;
		}
		
		public int getRowWidth() {
			return rowWidth.getAsInt();
		}
		
		public int getItemHeight() {
			return itemHeight; 
		}
		
		public boolean isSelectable() {
			return allowSelection;
		}
		
		public T getSelected() {
			return selectedElement;
		}
		
		public boolean isFramed() { 
			return frame; 
		}
		
		public boolean isSelected(int index) {
			return isFramed() || (isSelectable() && owner.getEntry(index) == getSelected());
		}
		
		public boolean isEnabled() {
			return enabled;
		}
		
		public boolean isVisible() {
			return visible; 
		}
		
		void setOwner(CarbonList<T> owner) {
			this.owner = owner; 
		}
		
		public CarbonList<T> getOwner() {
			return owner;
		}
		
		public boolean isScrollbarVisible() {
			return owner != null && owner.getMaxScroll() > 0;
		}
		
		public boolean isSearching() {
			return search != null && !search.isEmpty();
		}
		
		public ListState<T> search(String search) {
			this.search = search;
			if(owner != null) owner.applySearch(search);
			return this;
		}
		
		public ListState<T> updateSearch() {
			if(owner != null) owner.applySearch(search);
			return this;
		}
		
		void updateSearch(String search) {
			this.search = search;
		}
		
		public String getSearch() {
			return search; 
		}
		
		public List<T> getNodes() {
			return nodes;
		}
		public List<T> getSelectedItems() {
			return selectedElement == null ? ObjectLists.emptyList() : ObjectLists.singleton(selectedElement);
		}
	}
	
	public static class ListMultiState<T extends ListEntry<T>> extends ListState<T> {
		Set<T> selected = new ObjectLinkedOpenHashSet<>();
		
		@SafeVarargs
		public ListMultiState(T... nodes) {
			super(nodes);
		}
		
		public ListMultiState(List<T> nodes) {
			super(nodes);
		}
		
		@SafeVarargs
		public ListMultiState(int height, T... nodes) {
			super(height, nodes);
		}
		
		public ListMultiState(int height, List<T> nodes) {
			super(height, nodes);
		}
		
		@Override
		public boolean isSelected(int index) {
			return isFramed() || isSelectable() && selected.contains(owner.getEntry(index));
		}
		
		@Override
		public ListState<T> setSelected(T value) {
			if(selected.contains(value)) selected.remove(value);
			else selected.add(value);
			if(changeListener != null) changeListener.run();
			return this;
		}
		
		@Override
		public List<T> getSelectedItems() { return selected.isEmpty() ? ObjectLists.emptyList() : new ObjectArrayList<>(selected); }
	}
}
