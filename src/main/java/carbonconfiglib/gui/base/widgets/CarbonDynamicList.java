package carbonconfiglib.gui.base.widgets;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import carbonconfiglib.gui.base.helpers.GuiUtils;
import carbonconfiglib.gui.base.widgets.CarbonDynamicList.DynamicEntry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.util.Mth;


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
	
	protected boolean scrolling;
	protected boolean renderSelection = true;
	protected E hovered;
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
	
	protected void onElementsSwapped(int oldIndex, int newIndex) {
		
	}
	
	public E getHovered(double mouseX, double mouseY) {
		return getEntryAtPos(mouseX, mouseY);
	}
	
	protected E getEntryAtPos(double mouseX, double mouseY) {
		int centerWidth = this.getRowWidth() / 2;
		int centerX = this.getX() + this.width / 2;
		int minX = centerX - centerWidth;
		int maxX = centerX + centerWidth;
		int position = Mth.floor(mouseY - (double)this.getY()) - this.headerHeight + (int)this.getScrollAmount() - 4;
		int index = 0;
		while(index < children().size()) {
			int height = children().get(index).getItemHeight();
			if(height > position) break;
			position -= height;
			index++;
		}
		return (E)(mouseX < (double)this.getScrollbarPosition() && mouseX >= (double)minX && mouseX <= (double)maxX && index >= 0 && position >= 0 && index < this.getItemCount() ? this.children().get(index) : null);
	}
	
	@Override
	protected int getMaxPosition() {
		int max = 0;
		for(int i = 0,m = getItemCount();i < m;i++) max += children().get(i).getItemHeight();
		return max;
	}
		
	protected void scroll(int value) {
		setScrollAmount(getScrollAmount() + (double)value);
	}
	
	@Override
	protected void centerScrollOn(E element) {
		int index = children().indexOf(element);
		if(index <= 0) {
			setScrollAmount(0D);
			return;
		}
		int value = 0;
		for(int i = 0;i < index;i++) value += children().get(i).getItemHeight();
		value += children().get(index).getItemHeight() / 2;
		setScrollAmount(value - ((getHeight()) / 2));
	}
	
	protected void ensureVisible(E element) {
		int index = children().indexOf(element);
		int minY = getRowTop(index);
		int itemHeight = index < 0 ? this.itemHeight : children().get(index).getItemHeight();
		int maxY = minY - getY() - 4 - itemHeight;
		if(maxY < 0) scroll(maxY);
		
		int k = getBottom() - minY - itemHeight - itemHeight;
		if(k < 0) scroll(-k);
	}
	
	@Override
	protected int getRowTop(int p_93512_) {
		int max = getY() + 4 - (int)getScrollAmount();
		for(int i = 0,m = getItemCount();i < m;i++) { max += children().get(i).getItemHeight(); }
		return max + headerHeight;
	}
	
	@Override
	public boolean isMouseOver(double p_93479_, double p_93480_) {
		return enabled() && p_93480_ >= (double)this.getY() && p_93480_ <= (double)getBottom() && p_93479_ >= (double)this.getX() && p_93479_ <= (double)getScrollbarPosition()+6;
	}
	
	@Override
	protected void updateScrollingState(double mouseX, double mouseY, int button) {
		super.updateScrollingState(mouseX, mouseY, button);
		this.scrolling = button == 0 && mouseX >= (double)this.getScrollbarPosition() && mouseX < (double)(this.getScrollbarPosition() + 6);
	}
	
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		this.updateScrollingState(mouseX, mouseY, button);
		if(!this.isMouseOver(mouseX, mouseY)) return false;
		E element = this.getEntryAtPos(mouseX, mouseY);
		if(element != null) {
			E prev = getFocused();
			if(element.mouseClicked(mouseX, mouseY, button)) {
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
		else if(button == 0) {
			this.clickedHeader((int)(mouseX - (double)(this.getX() + this.width / 2 - this.getRowWidth() / 2)), (int)(mouseY - (double)this.getY()) + (int)this.getScrollAmount() - 4);
			return true;
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
	public boolean mouseDragged(double mouseX, double mouseY, int pButton, double pDragX, double pDragY) {
		if(dragging != null) draggingStarted = true;
		handleDragging(mouseX, mouseY);
		return super.mouseDragged(mouseX, mouseY, pButton, pDragX, pDragY);
	}
	
	protected void handleDragging(double mouseX, double mouseY) {
		if(draggingStarted) {
			E newElement = getEntryAtPos(mouseX, mouseY);
			if(newElement != null && newElement != dragging && newElement.isDraggable()) {
				int dragginIndex = children().indexOf(dragging);
				int newIndex = children().indexOf(newElement);
				if(dragginIndex == -1 || newIndex == -1) return;
				children().set(dragginIndex, children().set(newIndex, dragging));
				onElementsSwapped(dragginIndex, newIndex);
			}
		}
	}
	
	@Override
	public boolean mouseReleased(double mouseX, double mouseY, int button) {
		if(!enabled()) return false;
		dragging = null;
		draggingStarted = false;
		scrolling = false;
		return super.mouseReleased(mouseX, mouseY, button);
	}
	
	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
		if(!enabled()) return false;
		Optional<GuiEventListener> listener = getChildAt(mouseX, mouseY);
		if(!listener.isEmpty() && listener.get().mouseScrolled(mouseX, mouseY, scrollX, scrollY)) return true;
		
		scrolling = false;
		scroll(-(int)(scrollY * this.itemHeight * 2D));
		handleDragging(mouseX, mouseY);
		return true;
	}
	
	public boolean isScrolling() { return scrolling; }
	@Override
	public E getHovered() { return hovered; }
	protected boolean shouldRender() { return true; }
	protected boolean enabled() { return true; }
	
	@Override
	public void setFocused(GuiEventListener p_94024_) {
		ignoreSelection = true;
		super.setFocused(p_94024_);
		ignoreSelection = false;
	}
	
	@Override
	public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
		if(!shouldRender()) return;
		super.renderWidget(graphics, mouseX, mouseY, partialTicks);
	}
	
	@Override
	protected void renderListBackground(GuiGraphics graphics) {
		if(!drawBackground) return;
		super.renderListBackground(graphics);
	}
	
	@Override
	protected void renderListSeparators(GuiGraphics graphics) {
		if(!drawTopAndBottom) return;
		super.renderListSeparators(graphics);
	};
	
	@Override
	protected void renderListItems(GuiGraphics graphics, int mouseX, int mouseY, float particalTicks) {
		this.hovered = this.isMouseOver((double)mouseX, (double)mouseY) ? this.getEntryAtPos((double)mouseX, (double)mouseY) : null;
		int minX = this.getRowLeft();
		int width = this.getRowWidth();
		int size = this.getItemCount();
		int yOff = getY() + 4 - (int)getScrollAmount() + headerHeight;
		int yOffset = 0;
		boolean hasRendered = false;
		GuiUtils.pushScissors(minX, getY() + headerHeight, width, getHeight());
		minX+=1;
		for(int i = 0; i < size; ++i) {
			E entry = getEntry(i);
			int height = entry.getItemHeight();
			int minY = yOffset + yOff;
			int maxY = minY + height;
			if (maxY >= getY() && minY <= getBottom() && (entry != dragging || !draggingStarted)) {
				hasRendered = true;
				this.renderItem(graphics, mouseX, mouseY, particalTicks, i, minX, minY, width-2, height);
			}
			else if(hasRendered && (entry != dragging || !draggingStarted)) break;
			yOffset += height;
		}
		if(draggingStarted && dragging != null) {
			int ySize = dragging.getItemHeight();
			this.renderItem(graphics, mouseX, mouseY, particalTicks, children().indexOf(dragging), minX, mouseY - (ySize >> 1), width-2, ySize);
		}
		
		GuiUtils.popScissors();
	}
	
	protected void renderItem(GuiGraphics graphics, int mouseX, int mouseY, float particalTicks, int index, int left, int top, int width, int height) {
		E e = this.getEntry(index);
		if(this.renderSelection && this.isSelectedItem(index) && e.isRenderingSelection()) {
			int color = e.getSelectionColor(isFocused());
			this.renderSelection(graphics, left, top, width, height, color, e.getSelectionBackgroundColor());
		}
		e.location[0] = left;
		e.location[1] = top+2;
		e.location[2] = width;
		e.location[3] = height-4;
		e.render(graphics, index, top+2, left, width, height-4, mouseX, mouseY, Objects.equals(this.getHovered(), e), particalTicks);
	}
	
	public void renderSelection(GuiGraphics graphics, int left, int top, int width, int height, int frameColor, int backgroundColor) {
		graphics.fill(left, top, left + width, top + height, frameColor);
		graphics.fill(left + 1, top + 1, left + width - 1, top + height - 1, backgroundColor);
	}
	
	@Override
	public NarratableEntry.NarrationPriority narrationPriority() {
		if(this.isFocused()) {
			return NarratableEntry.NarrationPriority.FOCUSED;
		}
		else {
			return this.hovered != null ? NarratableEntry.NarrationPriority.HOVERED : NarratableEntry.NarrationPriority.NONE;
		}
	}
	
	@Override
	protected void replaceEntries(Collection<E> elements) {
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
		element.itemheight = itemHeight;
		element.owner = this;
	}
	
	public static abstract class DynamicEntry<E extends DynamicEntry<E>> extends ContainerObjectSelectionList.Entry<E> {
		int itemheight;
		protected CarbonDynamicList<E> owner;
		protected Font font = Minecraft.getInstance().font;
		protected int[] location = new int[4];

		protected boolean isVisible(int mouseX, int mouseY) {
			return owner.getY() <= mouseY && owner.getBottom() >= mouseY;
		}
		
		protected boolean isFullyVisible(int mouseX, int mouseY) {
			return mouseX >= location[0] && mouseX <= location[0] + location[2] && mouseY >= location[1] && mouseY <= location[1] + location[3];
		}
		
		public boolean isInFullView() {
			return (owner.getX() < location[0] && owner.getRight() >= location[0]) && owner.getY() < location[1] && owner.getBottom() >= location[1] + location[3];
		}
		
		public boolean isMouseOver(double pMouseX, double pMouseY) {
			return Objects.equals(owner.getEntryAtPos(pMouseX, pMouseY), this);
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
		
		public int getItemHeight() { return itemheight; }
		
		@Override
		public void render(GuiGraphics grahics, int x, int top, int left, int width, int height, int mouseX, int mouseY, boolean selected, float partialTicks) {
		}
	}
}
