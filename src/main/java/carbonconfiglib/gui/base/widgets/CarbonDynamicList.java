package carbonconfiglib.gui.base.widgets;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

import com.mojang.blaze3d.vertex.PoseStack;

import carbonconfiglib.gui.base.helpers.GuiUtils;
import carbonconfiglib.gui.base.widgets.CarbonDynamicList.DynamicEntry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.util.Mth;

public class CarbonDynamicList<E extends DynamicEntry<E>> extends ContainerObjectSelectionList<E> {
	
	protected boolean scrolling;
	protected boolean renderSelection = true;
	protected E hovered;
	
	public CarbonDynamicList(Minecraft minecraft, int width, int height, int startY, int endY, int itemHeight) {
		super(minecraft, width, height, startY, endY, itemHeight);
	}
	
	public E getHovered(double mouseX, double mouseY) {
		return getEntryAtPos(mouseX, mouseY);
	}
	
	protected E getEntryAtPos(double mouseX, double mouseY) {
		int centerWidth = this.getRowWidth() / 2;
		int centerX = this.x0 + this.width / 2;
		int minX = centerX - centerWidth;
		int maxX = centerX + centerWidth;
		int position = Mth.floor(mouseY - (double)this.y0) - this.headerHeight + (int)this.getScrollAmount() - 4;
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
	
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		this.updateScrollingState(mouseX, mouseY, button);
		if(!this.isMouseOver(mouseX, mouseY)) return false;
		E element = this.getEntryAtPos(mouseX, mouseY);
		if(element != null) {
			if(element.mouseClicked(mouseX, mouseY, button)) {
				this.setFocused(element);
				this.setDragging(true);
				return true;
			}
		}
		else if(button == 0) {
			this.clickedHeader((int)(mouseX - (double)(this.x0 + this.width / 2 - this.getRowWidth() / 2)), (int)(mouseY - (double)this.y0) + (int)this.getScrollAmount() - 4);
			return true;
		}
		return this.scrolling;
	}
	
	@Override
	public void mouseMoved(double mouseX, double mouseY) {
		for(E element : children()) {
			if(element.isInView()) element.mouseMoved(mouseX, mouseY);
		}
	}
	
	@Override
	public boolean mouseReleased(double p_93491_, double p_93492_, int p_93493_) {
		if(!enabled()) return false;
		scrolling = false;
		return super.mouseReleased(p_93491_, p_93492_, p_93493_);
	}
	
	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double scroll) {
		if(!enabled()) return false;
		scrolling = false;
		scroll(-(int)(scroll * this.itemHeight * 2D));
		return true;
	}
	
	public boolean isScrolling() { return scrolling; }
	@Override
	public E getHovered() { return hovered; }
	protected boolean shouldRender() { return true; }
	protected boolean enabled() { return true; }
	
	@Override
	public void render(PoseStack p_93447_, int p_93448_, int p_93449_, float p_93450_) {
		if(!shouldRender()) return;
		super.render(p_93447_, p_93448_, p_93449_, p_93450_);
	}
	
	@Override
	protected void renderList(PoseStack matrix, int mouseX, int mouseY, float particalTicks) {
		this.hovered = this.isMouseOver((double)mouseX, (double)mouseY) ? this.getEntryAtPos((double)mouseX, (double)mouseY) : null;
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
			if (maxY >= y0 && minY <= y1) {
				hasRendered = true;
				this.renderItem(matrix, mouseX, mouseY, particalTicks, i, minX, minY, width, height-4);
			}
			else if(hasRendered) break;
			yOffset += height;
		}
		GuiUtils.popScissors();
	}
	
	protected void renderItem(PoseStack matrix, int mouseX, int mouseY, float particalTicks, int index, int left, int top, int width, int height) {
		E e = this.getEntry(index);
		if(this.renderSelection && this.isSelectedItem(index) && e.isRenderingSelection()) {
			int color = e.getSelectionColor(isFocused());
			this.renderSelection(matrix, left, top, width, height, color, e.getSelectionBackgroundColor());
		}
		e.location[0] = left;
		e.location[1] = top;
		e.location[2] = width;
		e.location[3] = height;
		e.render(matrix, index, top, left, width, height, mouseX, mouseY, Objects.equals(this.getHovered(), e), particalTicks);
	}
	
	public void renderSelection(PoseStack matrix, int left, int top, int width, int height, int frameColor, int backgroundColor) {
		fill(matrix, left, top, left + width, top + height, frameColor);
		fill(matrix, left + 1, top + 1, left + width - 1, top + height - 1, backgroundColor);
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
			return owner.y0 <= mouseY && owner.y1 >= mouseY;
		}
		
		protected boolean isFullyVisible(int mouseX, int mouseY) {
			return mouseX >= location[0] && mouseX <= location[0] + location[2] && mouseY >= location[1] && mouseY <= location[1] + location[3];
		}
		
		public boolean isInView() {
			return (owner.x0 < location[0] && owner.x1 >= location[0]) && owner.y0 < location[1] && owner.y1 >= location[1] + location[3];
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
		public void render(PoseStack poseStack, int x, int top, int left, int width, int height, int mouseX, int mouseY, boolean selected, float partialTicks) {
		}
	}
}
