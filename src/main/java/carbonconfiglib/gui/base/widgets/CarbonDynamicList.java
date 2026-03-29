package carbonconfiglib.gui.base.widgets;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

import carbonconfiglib.gui.api.background.BackgroundTexture;
import carbonconfiglib.gui.base.helpers.GuiUtils;
import carbonconfiglib.gui.base.interaction.IInteractable;
import carbonconfiglib.gui.base.widgets.CarbonDynamicList.DynamicEntry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.MathHelper;


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
public class CarbonDynamicList<E extends DynamicEntry<E>> extends AbstractScrollList<E> {
	private static final BackgroundTexture TEXTURE = BackgroundTexture.of(Gui.optionsBackground).withBrightness(32).build();
	protected boolean scrolling;
	protected boolean renderSelection = true;
	protected boolean renderBackground = true;
	protected boolean renderTopAndBottom = true;
	protected E hovered;
	protected boolean draggingStarted = false;
	protected E dragging;
	
	public CarbonDynamicList(int width, int height, int startY, int endY, int itemHeight) {
		super(width, height, startY, endY, itemHeight);
	}
	
	protected boolean areChildrenDraggable() {
		return false;
	}
	
	public void setRenderBackground(boolean value) {
		renderBackground = value;
	}
	
	public void setRenderTopAndBottom(boolean value) {
		renderTopAndBottom = value;
	}
	
	protected void onElementsSwapped(int oldIndex, int newIndex) {
		
	}
	
	public E getHovered(double mouseX, double mouseY) {
		return getEntryAtPos(mouseX, mouseY);
	}
	
	protected E getEntryAtPos(double mouseX, double mouseY) {
		int centerWidth = this.getRowWidth() / 2;
		int centerX = this.x0 + this.width / 2;
		int minX = centerX - centerWidth;
		int maxX = centerX + centerWidth;
		int position = (int)Math.floor(mouseY - (double)this.y0) - this.headerHeight + (int)this.getScrollAmount() - 4;
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
	
	public void setWidth(int width) {
		x1 = x0 + width;
		this.width = width;
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
		setScrollAmount(value - ((this.y1 - this.y0) / 2));
	}
	
	protected void ensureVisible(E element) {
		int index = children().indexOf(element);
		int minY = getRowTop(index);
		int itemHeight = index < 0 ? this.itemHeight : children().get(index).getItemHeight();
		int maxY = minY - y0 - 4 - itemHeight;
		if(maxY < 0) scroll(maxY);
		
		int k = y1 - minY - itemHeight - itemHeight;
		if(k < 0) scroll(-k);
	}
	
	@Override
	protected int getRowTop(int p_93512_) {
		int max = y0 + 4 - (int)getScrollAmount();
		for(int i = 0,m = getItemCount();i < m;i++) { max += children().get(i).getItemHeight(); }
		return max + headerHeight;
	}
	
	@Override
	public boolean isMouseOver(double p_93479_, double p_93480_) {
		return enabled() && p_93480_ >= (double)this.y0 && p_93480_ <= (double)this.y1 && p_93479_ >= (double)this.x0 && p_93479_ <= (double)getScrollbarPosition()+6;
	}
	
	@Override
	protected void updateScrollingState(double mouseX, double mouseY, int button) {
		super.updateScrollingState(mouseX, mouseY, button);
		this.scrolling = button == 0 && mouseX >= (double)this.getScrollbarPosition() && mouseX < (double)(this.getScrollbarPosition() + 6);
	}
	
	@SuppressWarnings("unchecked")
	public boolean mouseClick(double mouseX, double mouseY, int button) {
		this.updateScrollingState(mouseX, mouseY, button);
		if(!this.isMouseOver(mouseX, mouseY)) return false;
		E element = this.getEntryAtPos(mouseX, mouseY);
		if(element != null) {
			E prev = (E)getFocused();
			if(element.mouseClick(mouseX, mouseY, button)) {
				this.setFocused(element);
				this.setDragging(true);
				return true;
			}
			if(prev != null && prev != element) {
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
	public boolean mouseDrag(double mouseX, double mouseY, int pButton, double pDragX, double pDragY) {
		if(dragging != null) draggingStarted = true;
		handleDragging(mouseX, mouseY);
		return super.mouseDrag(mouseX, mouseY, pButton, pDragX, pDragY);
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
	public boolean mouseRelease(double mouseX, double mouseY, int button) {
		if(!enabled()) return false;
		dragging = null;
		draggingStarted = false;
		scrolling = false;
		return super.mouseRelease(mouseX, mouseY, button);
	}
	
	@Override
	public boolean mouseScroll(double mouseX, double mouseY, double scroll) {
		if(!enabled()) return false;
		IInteractable listener = getChildAt(mouseX, mouseY);
		if(listener != null && listener.mouseScroll(mouseX, mouseY, scroll)) return true;
		
		scrolling = false;
		scroll(-(int)(scroll * this.itemHeight * 2D));
		handleDragging(mouseX, mouseY);
		return true;
	}
	
	public boolean isScrolling() { return scrolling; }
	public E getHovered() { return hovered; }
	protected boolean shouldRender() { return true; }
	protected boolean enabled() { return true; }
	
	public int getMaxScroll() {
		return Math.max(0, this.getMaxPosition() - (this.y1 - this.y0 - 4));
	}
	
	@Override
	public void render(int mouseX, int mouseY, float partialTicks) {
		if(!shouldRender()) return;
		if(renderBackground) renderBackground();
		renderList(mouseX, mouseY, partialTicks);
		if(renderTopAndBottom) renderTopAndBottom();
		renderSlider();
	}
	
	protected void renderSlider() {
		int j1 = this.getMaxScroll();
		if (j1 > 0) {
			int k1 = (int)((float)((this.y1 - this.y0) * (this.y1 - this.y0)) / (float)this.getMaxPosition());
			k1 = MathHelper.clamp_int(k1, 32, this.y1 - this.y0 - 8);
			int l1 = (int)this.getScrollAmount() * (this.y1 - this.y0 - k1) / j1 + this.y0;
			if (l1 < this.y0) {
				l1 = this.y0;
			}
			int i = this.getScrollbarPosition();
			int j = i + 6;
			GlStateManager.disableTexture2D();
		      	
			Tessellator tes = Tessellator.getInstance();
			WorldRenderer builder = tes.getWorldRenderer();
			builder.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
			builder.pos((double)i, (double)this.y1, 0.0D).tex(0.0F, 1.0F).color(0, 0, 0, 255).endVertex();
			builder.pos((double)j, (double)this.y1, 0.0D).tex(1.0F, 1.0F).color(0, 0, 0, 255).endVertex();
			builder.pos((double)j, (double)this.y0, 0.0D).tex(1.0F, 0.0F).color(0, 0, 0, 255).endVertex();
			builder.pos((double)i, (double)this.y0, 0.0D).tex(0.0F, 0.0F).color(0, 0, 0, 255).endVertex();
			tes.draw();
			builder.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
			builder.pos((double)i, (double)(l1 + k1), 0.0D).tex(0.0F, 1.0F).color(128, 128, 128, 255).endVertex();
			builder.pos((double)j, (double)(l1 + k1), 0.0D).tex(1.0F, 1.0F).color(128, 128, 128, 255).endVertex();
			builder.pos((double)j, (double)l1, 0.0D).tex(1.0F, 0.0F).color(128, 128, 128, 255).endVertex();
			builder.pos((double)i, (double)l1, 0.0D).tex(0.0F, 0.0F).color(128, 128, 128, 255).endVertex();
			tes.draw();
			builder.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
			builder.pos((double)i, (double)(l1 + k1 - 1), 0.0D).tex(0.0F, 1.0F).color(192, 192, 192, 255).endVertex();
			builder.pos((double)(j - 1), (double)(l1 + k1 - 1), 0.0D).tex(1.0F, 1.0F).color(192, 192, 192, 255).endVertex();
			builder.pos((double)(j - 1), (double)l1, 0.0D).tex(1.0F, 0.0F).color(192, 192, 192, 255).endVertex();
			builder.pos((double)i, (double)l1, 0.0D).tex(0.0F, 0.0F).color(192, 192, 192, 255).endVertex();
			tes.draw();
			GlStateManager.enableTexture2D();
		}
	}
	
	protected void renderTopAndBottom() {
		GuiUtils.renderListOverlay(x0, x1, y0, y1, width, height, TEXTURE);
		GuiUtils.renderListShadow(x0, x1, y0, y1, width, height);
	}
	
	protected void renderBackground() {
		GuiUtils.renderBackground(x0, x1, y0, y1, (int)getScrollAmount(), TEXTURE);
	}
	
	
	@Override
	protected void renderList(int mouseX, int mouseY, float particalTicks) {
		this.hovered = this.isMouseOver(mouseX, mouseY) ? this.getEntryAtPos(mouseX, mouseY) : null;
		int minX = this.getRowLeft();
		int width = this.getRowWidth();
		int size = this.getItemCount();
		int yOff = y0 + 4 - (int)getScrollAmount() + headerHeight;
		int yOffset = 0;
		boolean hasRendered = false;
		GuiUtils.pushScissors(minX, y0 + headerHeight, width, y1-y0);
		minX+=1;
		for(int i = 0; i < size; ++i) {
			E entry = getEntry(i);
			int height = entry.getItemHeight();
			int minY = yOffset + yOff;
			int maxY = minY + height;
			if (maxY >= y0 && minY <= y1 && (entry != dragging || !draggingStarted)) {
				hasRendered = true;
				this.renderItem(mouseX, mouseY, particalTicks, i, minX, minY, width-2, height);
			}
			else if(hasRendered && (entry != dragging || !draggingStarted)) break;
			yOffset += height;
		}
		if(draggingStarted && dragging != null) {
			int ySize = dragging.getItemHeight();
			this.renderItem(mouseX, mouseY, particalTicks, children().indexOf(dragging), minX, mouseY - (ySize >> 1), width-2, ySize);
		}
		
		GuiUtils.popScissors();
	}
	
	protected void renderItem(int mouseX, int mouseY, float particalTicks, int index, int left, int top, int width, int height) {
		E e = this.getEntry(index);
		if(this.renderSelection && this.isSelectedItem(index) && e.isRenderingSelection()) {
			int color = e.getSelectionColor(isFocused());
			this.renderSelection(left, top, width, height, color, e.getSelectionBackgroundColor());
		}
		e.location[0] = left;
		e.location[1] = top+2;
		e.location[2] = width;
		e.location[3] = height-4;
		e.render(index, top+2, left, width, height-4, mouseX, mouseY, Objects.equals(this.getHovered(), e), particalTicks);
	}
	
	public void renderSelection(int left, int top, int width, int height, int frameColor, int backgroundColor) {
		Gui.drawRect(left, top, left + width, top + height, frameColor);
		Gui.drawRect(left + 1, top + 1, left + width - 1, top + height - 1, backgroundColor);
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
	
	protected void bindToSelf(E element) {
		element.itemheight = itemHeight;
		element.owner = this;
	}
	
	public static abstract class DynamicEntry<E extends DynamicEntry<E>> extends AbstractScrollList.Entry<E> {
		int itemheight;
		protected CarbonDynamicList<E> owner;
		protected FontRenderer font = Minecraft.getMinecraft().fontRendererObj;
		protected int[] location = new int[4];

		protected boolean isVisible(int mouseX, int mouseY) {
			return owner.y0 <= mouseY && owner.y1 >= mouseY;
		}
		
		protected boolean isFullyVisible(int mouseX, int mouseY) {
			return mouseX >= location[0] && mouseX <= location[0] + location[2] && mouseY >= location[1] && mouseY <= location[1] + location[3];
		}
		
		public boolean isInFullView() {
			return (owner.x0 < location[0] && owner.x1 >= location[0]) && owner.y0 < location[1] && owner.y1 >= location[1] + location[3];
		}
		
		public boolean isMouseOver(double pMouseX, double pMouseY) {
			return Objects.equals(owner.getEntryAtPos(pMouseX, pMouseY), this);
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
		public void render(int x, int top, int left, int width, int height, int mouseX, int mouseY, boolean selected, float partialTicks) {
		}
	}
}
