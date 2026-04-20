package carbonconfiglib.gui.base.widgets;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.jspecify.annotations.Nullable;

import carbonconfiglib.gui.base.widgets.CarbonDynamicList.DynamicEntry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.input.MouseButtonEvent;


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
public class CarbonDynamicList<E extends DynamicEntry<E>> extends ContainerObjectSelectionList<E> {
	
	protected boolean renderSelection = true;
	protected boolean draggingStarted = false;
	protected boolean ignoreSelection = false;
	protected E dragging;
	protected boolean drawBackground = true;
	protected boolean drawTopAndBottom = true;
	
	public CarbonDynamicList(Minecraft minecraft, int width, int height, int y, int itemHeight) {
		super(minecraft, width, height, y, itemHeight);
	}
	
	protected boolean areChildrenDraggable() {
		return false;
	}
	
	public void setRenderTopAndBottom(boolean value) {
		this.drawTopAndBottom = value;
	}
	
	public void setRenderBackground(boolean value) {
		this.drawBackground = value;
	}
	
	protected boolean entriesCanBeSelected() {
		return renderSelection;
	}
	
	protected boolean isSelected(E entry) {
		return getSelected() == entry;
	}
	
	protected void onElementsSwapped(int oldIndex, int newIndex) {
		
	}
	
	public E getHovered(double mouseX, double mouseY) {
		return getEntryAtPosition(mouseX, mouseY);
	}
	
	private E getHoveredWithoutDragging(double mouseX, double mouseY) {
        for (E child : this.children) {
            if (child != dragging && child.isMouseOver(mouseX, mouseY)) {
                return child;
            }
        }

        return null;
	}
	
	protected void scroll(int value) {
		setScrollAmount(scrollAmount() + (double)value);
	}
	
	@Override
	protected void centerScrollOn(E element) {
		int index = children.indexOf(element);
		if(index <= 0) {
			setScrollAmount(0D);
			return;
		}
		int value = 0;
		for(int i = 0;i < index;i++) value += children().get(i).getHeight();
		value += children().get(index).getHeight() / 2;
		setScrollAmount(value - ((getHeight()) / 2));
	}
		
	@Override
	public boolean isMouseOver(double p_93479_, double p_93480_) {
		return enabled() && p_93480_ >= (double)this.getY() && p_93480_ <= (double)getBottom() && p_93479_ >= (double)this.getX() && p_93479_ <= (double)scrollBarX()+scrollbarWidth();
	}
	
	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
		updateScrolling(event);
		if(!this.isMouseOver(event.x(), event.y())) return false;
		E element = this.getEntryAtPosition(event.x(), event.y());
		if(element != null) {
			E prev = getFocused();
			if(element.mouseClicked(event, doubleClick)) {
				this.setFocused(element);
				this.setDragging(true);
				if(prev != null && prev != element) {
					prev.clearFocus();
				}
				return true;
			}
			if(prev != null && prev != element) {
				prev.clearFocus();
				if(getFocused() == prev) setFocused(null);
			}
			if(areChildrenDraggable() && element.isDraggable()) {
				dragging = element;
				return true;
			}
		}
		return this.scrolling;
	}
	
	@Override
	public void mouseMoved(double mouseX, double mouseY) {
		for(E element : children()) {
			if(element.isInFullView()) element.mouseMoved(mouseX, mouseY);
		}
	}
	
	@Override
	public boolean mouseDragged(MouseButtonEvent event, double dx, double dy) {
		if(dragging != null) draggingStarted = true;
		handleDragging(event.x(), event.y());
		return super.mouseDragged(event, dx, dy);
	}
	
	protected void handleDragging(double mouseX, double mouseY) {
		if(draggingStarted) {
			E newElement = getHoveredWithoutDragging(mouseX, mouseY);
			if(newElement != null && newElement != dragging && newElement.isDraggable()) {
				int dragginIndex = children().indexOf(dragging);
				int newIndex = children().indexOf(newElement);
				if(dragginIndex == -1 || newIndex == -1) return;
				children.set(dragginIndex, children.set(newIndex, dragging));
				onElementsSwapped(dragginIndex, newIndex);
				repositionEntries();
			}
		}
	}
	
	@Override
	public boolean mouseReleased(MouseButtonEvent event) {
		if(!enabled()) return false;
		dragging = null;
		draggingStarted = false;
		scrolling = false;
		return super.mouseReleased(event);
	}
		
	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
		if(!enabled()) return false;
		Optional<GuiEventListener> listener = getChildAt(mouseX, mouseY);
		if(!listener.isEmpty() && listener.get().mouseScrolled(mouseX, mouseY, scrollX, scrollY)) return true;
		
		scrolling = false;
		scroll(-(int)(scrollY * this.defaultEntryHeight * 2D));
		handleDragging(mouseX, mouseY);
		return true;
	}
	
	public E getHovered() { return super.getHovered(); }
	public boolean isScrolling() { return scrolling; }
	protected boolean shouldRender() { return true; }
	protected boolean enabled() { return true; }
	
	@Override
	public void setFocused(GuiEventListener p_94024_) {
		ignoreSelection = true;
		super.setFocused(p_94024_);
		ignoreSelection = false;
	}
	
	@Override
	public void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
		if(!shouldRender()) return;
		super.extractWidgetRenderState(graphics, mouseX, mouseY, partialTicks);
	}
	
	@Override
	protected void extractListBackground(GuiGraphicsExtractor graphics) {
		if(!drawBackground) return;
		super.extractListBackground(graphics);
	}
	
	@Override
	protected void extractListSeparators(GuiGraphicsExtractor graphics) {
		if(!drawTopAndBottom) return;
		super.extractListSeparators(graphics);		
	}
	
	@Override
	protected void extractListItems(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float particalTicks) {
		boolean hasRendered = false;
        for (E child : this.children()) {
            if (child.getY() + child.getHeight() >= this.getY() && child.getY() <= this.getBottom() && (child != dragging || !draggingStarted)) {
                hasRendered = true;
                this.extractItem(graphics, mouseX, mouseY, particalTicks, child);
            }
            else if(hasRendered && (child != dragging || !draggingStarted)) break;
        }
		if(draggingStarted && dragging != null) {
			dragging.setY(mouseY - (dragging.getHeight() >> 1));
            this.extractItem(graphics, mouseX, mouseY, particalTicks, dragging);
		}
	}
	
	@Override
	@SuppressWarnings("unlikely-arg-type")
	protected void extractItem(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float particalTicks, E entry) {
        if (this.entriesCanBeSelected() && isSelected(entry)) {
			int color = entry.getSelectionColor(isFocused());
            this.extractSelection(graphics, entry, color, entry.getSelectionBackgroundColor());
        }
        entry.extractContent(graphics, mouseX, mouseY, Objects.equals(isHovered(), entry), particalTicks);
	}
	
	protected void extractSelection(GuiGraphicsExtractor graphics, E entry, int frameColor, int backgroundColor) {
        int left = entry.getX();
        int top = entry.getY();
        int right = left + entry.getWidth();
        int bottom = top + entry.getHeight();
		graphics.fill(left, top, right, bottom, frameColor);
		graphics.fill(left + 1, top + 1, right - 1, bottom - 1, backgroundColor);
	}
		
	@Override
	public NarratableEntry.NarrationPriority narrationPriority() {
		if(this.isFocused()) {
			return NarratableEntry.NarrationPriority.FOCUSED;
		}
		else {
			return this.getHovered() != null ? NarratableEntry.NarrationPriority.HOVERED : NarratableEntry.NarrationPriority.NONE;
		}
	}
	
	@Override
	public void replaceEntries(Collection<E> elements) {
		elements.forEach(this::bindToSelf);
		super.replaceEntries(elements);
	}
	
	protected void addEntries(List<E> elements) {
		elements.forEach(this::addEntry);
	}
	
	protected void addEntries(int index, List<E> elements) {
		elements.forEach(this::bindToSelf);
		children().addAll(index, elements);
	}
	
	protected void addEntry(int index, E element) {
		bindToSelf(element);
		children().add(index, element);;
	}
	
	@Override
	protected int addEntry(E element) {
		bindToSelf(element);
		return super.addEntry(element);
	}
	
	@Override
	protected void addEntryToTop(E element) {
		bindToSelf(element);
		super.addEntryToTop(element);
	}
	
	protected void bindToSelf(E element) {
		element.itemheight = defaultEntryHeight;
		element.owner = this;
	}
	
	public static abstract class DynamicEntry<E extends DynamicEntry<E>> extends ContainerObjectSelectionList.Entry<E> {
		int itemheight;
		protected CarbonDynamicList<E> owner;
		protected Font font = Minecraft.getInstance().font;

		protected boolean isVisible(int mouseX, int mouseY) {
			return owner.getY() <= mouseY && owner.getBottom() >= mouseY;
		}
		
		protected boolean isFullyVisible(int mouseX, int mouseY) {
			return mouseX >= getX() && mouseX <= getX() + getWidth() && mouseY >= getY() && mouseY <= getY() + getHeight();
		}
		
		public boolean isInFullView() {
			return (owner.getX() < getX() && owner.getRight() >= getX()) && owner.getY() < getY() && owner.getBottom() >= getY() + getHeight();
		}
		
		public void clearFocus() {
			children().forEach(this::clearFocus);
			setFocused(null);
		}
		
		private void clearFocus(GuiEventListener listener) {
			if(listener instanceof ContainerEventHandler) {
				ContainerEventHandler container = ((ContainerEventHandler)listener);
				container.children().forEach(this::clearFocus);
				container.setFocused(null);
			}
			if(listener.isFocused()) listener.setFocused(false);
		}
		
		@Override
		public void setFocused(@Nullable GuiEventListener focused) {
		}
		
		@SuppressWarnings("unchecked")
		protected <T extends DynamicEntry<T>> void setOwner(CarbonDynamicList<T> list) {
			this.owner = (CarbonDynamicList<E>)list;
		}
		
		public boolean isDraggable() {
			return true;
		}
		
		protected boolean isRenderingSelection() {
			return true;
		}
		
		public int getSelectionColor(boolean focused) {
			return focused ? -1 : -8355712;
		}
		
		public int getSelectionBackgroundColor() {
			return -16777216;
		}
		
		public int getHeight() { return itemheight; }
		
		@Override
		public void extractContent(GuiGraphicsExtractor grahics, int mouseX, int mouseY, boolean selected, float partialTicks) {
		}
	}
}
