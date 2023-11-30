package carbonconfiglib.gui.widgets.screen;

import java.util.AbstractList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

import javax.annotation.Nullable;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import com.google.common.collect.Lists;

import carbonconfiglib.gui.api.BackgroundTexture;
import carbonconfiglib.gui.api.BackgroundTexture.BackgroundHolder;
import carbonconfiglib.gui.screen.SmoothFloat;
import carbonconfiglib.gui.widgets.screen.AbstractScrollList.Entry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.MathHelper;

public class AbstractScrollList<E extends Entry<E>> implements IInteractableContainer, IWidget
{
	BackgroundHolder texture = BackgroundTexture.DEFAULT.asHolder();
	protected final int itemHeight;
	@SuppressWarnings({"unchecked", "rawtypes"})
	private final List<E> children = new AbstractScrollList.TrackedList();
	protected int width;
	protected int height;
	protected int y0;
	protected int y1;
	protected int x1;
	protected int x0;
	protected boolean centerListVertically = true;
	private boolean renderSelection = true;
	protected int headerHeight;
	private boolean scrolling;
	@Nullable
	private E selected;
	private boolean renderBackground = true;
	private boolean renderTopAndBottom = true;
	@Nullable
	private E hovered;
	   
	private IInteractable focused;
	private boolean isDragging;
	private int lastTick = 0;
	SmoothFloat scrollAmount = new SmoothFloat(0.5F);
	
	public AbstractScrollList(int width, int height, int screenY, int listY, int itemHeight) {
		this.width = width;
		this.height = height;
		this.y0 = screenY;
		this.y1 = listY;
		this.itemHeight = itemHeight;
		this.x0 = 0;
		this.x1 = width;
	}
	
	@Override
	public void setX(int x) {}
	@Override
	public void setY(int y) {}
	@Override
	public int getX() { return 0; }
	@Override
	public int getY() { return 0; }
	public boolean isHovered() { return hovered != null; }
	
	public void setRenderSelection(boolean render) {
		this.renderSelection = render;
	}
	
	public void setCustomBackground(BackgroundHolder customBackground) {
		this.texture = customBackground;
	}
	
	public int getRowWidth() {
		return 220;
	}
	
	public int getLastTick() {
		return lastTick;
	}
	
	@Nullable
	public E getSelected() {
		return this.selected;
	}
	
	public void setSelected(E selected) {
		this.selected = selected;
	}
	
	public void setRenderBackground(boolean render) {
		this.renderBackground = render;
	}
	
	public void setRenderTopAndBottom(boolean render) {
		this.renderTopAndBottom = render;
	}
	
	protected final void clearEntries() {
		this.children.clear();
	}
	
	protected void replaceEntries(Collection<E> entries) {
		this.children.clear();
		this.children.addAll(entries);
	}
	
	protected E getEntry(int index) {
		return this.children().get(index);
	}
	
	protected int addEntry(E entry) {
		this.children.add(entry);
		return this.children.size() - 1;
	}
	
	protected void addEntryToTop(E entry) {
		double d0 = (double)this.getMaxScroll() - this.getScrollAmount();
		this.children.add(0, entry);
		this.setScrollAmount((double)this.getMaxScroll() - d0);
	}
	
	protected boolean removeEntryFromTop(E entry) {
		double d0 = (double)this.getMaxScroll() - this.getScrollAmount();
		boolean flag = this.removeEntry(entry);
		this.setScrollAmount((double)this.getMaxScroll() - d0);
		return flag;
	}
	
	protected int getItemCount() {
		return this.children().size();
	}
	
	protected boolean isSelectedItem(int index) {
		return Objects.equals(this.getSelected(), this.children().get(index));
	}
	
	@Nullable
	protected final E getEntryAtPosition(double mouseX, double mouseY) {
		int i = this.getRowWidth() / 2;
		int j = this.x0 + this.width / 2;
		int k = j - i;
		int l = j + i;
		int i1 = MathHelper.floor_double(mouseY - (double)this.y0) - this.headerHeight + (int)this.getScrollAmount() - 4;
		int j1 = i1 / this.itemHeight;
		return (E)(mouseX < (double)this.getScrollbarPosition() && mouseX >= (double)k && mouseX <= (double)l && j1 >= 0 && i1 >= 0 && j1 < this.getItemCount() ? this.children().get(j1) : null);
	}
	
	public void updateSize(int width, int height, int screenY, int listY) {
		this.width = width;
		this.height = height;
		this.y0 = screenY;
		this.y1 = listY;
		this.x0 = 0;
		this.x1 = width;
	}
	
	public void setLeftPos(int left) {
		this.x0 = left;
		this.x1 = left + this.width;
	}
	
	protected int getMaxPosition() {
		return this.getItemCount() * this.itemHeight + this.headerHeight;
	}
	
	public void tick() {
		lastTick++;
		int max = this.getItemCount();
		for(int i = 0;i < max;++i)
		{
			int j1 = this.getRowTop(i);
			if(j1+itemHeight >= this.y0 && j1 <= this.y1) {
				getEntry(i).tick();
			}
		}
	}
	
	protected void renderBackground() {
	}

	@Override
	public void render(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
		scrollAmount.update(partialTicks);
		this.renderBackground();
		Tessellator tes = Tessellator.getInstance();
		VertexBuffer builder = tes.getBuffer();
		this.hovered = this.isMouseOver((double)mouseX, (double)mouseY) ? this.getEntryAtPosition((double)mouseX, (double)mouseY) : null;
		if (this.renderBackground) {
			GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
			BackgroundTexture texture = this.texture.getTexture();
			Minecraft.getMinecraft().getTextureManager().bindTexture(texture.getBackgroundTexture());
			int color = texture.getBackgroundBrightness();
			float scroll = (float)getScrollAmount();
			builder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
			builder.pos(x0, y1, 0D).tex(x0 / 32F, (y1 + scroll) / 32F).color(color, color, color, 255).endVertex();
			builder.pos(x1, y1, 0D).tex(x1 / 32F, (y1 + scroll) / 32F).color(color, color, color, 255).endVertex();
			builder.pos(x1, y0, 0D).tex(x1 / 32F, (y0 + scroll) / 32F).color(color, color, color, 255).endVertex();
			builder.pos(x0, y0, 0D).tex(x0 / 32F, (y0 + scroll) / 32F).color(color, color, color, 255).endVertex();
			tes.draw();
		}
		this.renderList(mouseX, mouseY, partialTicks);
		if (this.renderTopAndBottom) {
			BackgroundTexture texture = this.texture.getTexture();
			Minecraft.getMinecraft().getTextureManager().bindTexture(texture.getForegroundTexture());
			GlStateManager.enableTexture2D();
			GlStateManager.enableDepth();
			GlStateManager.depthFunc(519);
			int color = texture.getForegroundBrightness();
			builder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
			builder.pos(x0, y0, -100D).tex(0, y0 / 32F).color(color, color, color, 255).endVertex();
			builder.pos(x0 + width, y0, -100D).tex(width / 32F, y0 / 32F).color(color, color, color, 255).endVertex();
			builder.pos(x0 + width, 0D, -100D).tex(width / 32F, 0F).color(color, color, color, 255).endVertex();
			builder.pos(x0, 0D, -100D).tex(0F, 0F).color(color, color, color, 255).endVertex();
			builder.pos(x0, height, -100D).tex(0F, height / 32F).color(color, color, color, 255).endVertex();
			builder.pos(x0 + width, height, -100D).tex(width / 32F, height / 32F).color(color, color, color, 255).endVertex();
			builder.pos(x0 + width, y1, -100D).tex(width / 32F, y1 / 32F).color(color, color, color, 255).endVertex();
			builder.pos(x0, y1, -100D).tex(0F, y1 / 32F).color(color, color, color, 255).endVertex();
			tes.draw();
			GlStateManager.depthFunc(515);
			GlStateManager.disableDepth();
	        GlStateManager.enableBlend();
	        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ZERO, GlStateManager.DestFactor.ONE);
	        GlStateManager.disableAlpha();
	        GlStateManager.shadeModel(7425);
			GlStateManager.disableTexture2D();
			builder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
			builder.pos(x0, y0 + 4, 0D).color(0, 0, 0, 0).endVertex();
			builder.pos(x1, y0 + 4, 0D).color(0, 0, 0, 0).endVertex();
			builder.pos(x1, y0, 0D).color(0, 0, 0, 255).endVertex();
			builder.pos(x0, y0, 0D).color(0, 0, 0, 255).endVertex();
			builder.pos(x0, y1, 0D).color(0, 0, 0, 255).endVertex();
			builder.pos(x1, y1, 0D).color(0, 0, 0, 255).endVertex();
			builder.pos(x1, y1 - 4, 0D).color(0, 0, 0, 0).endVertex();
			builder.pos(x0, y1 - 4, 0D).color(0, 0, 0, 0).endVertex();
			tes.draw();
			GlStateManager.enableAlpha();
			GlStateManager.disableBlend();
			GlStateManager.enableTexture2D();
		}
		
		int maxScroll = this.getMaxScroll();
		if (maxScroll > 0) {
			int maxPosition = (int)((float)((this.y1 - this.y0) * (this.y1 - this.y0)) / (float)this.getMaxPosition());
			maxPosition = MathHelper.clamp_int(maxPosition, 32, this.y1 - this.y0 - 8);
			int scrollValue = (int)this.getScrollAmount() * (this.y1 - this.y0 - maxPosition) / maxScroll + this.y0;
			if (scrollValue < this.y0) {
				scrollValue = this.y0;
			}
			int minX = this.getScrollbarPosition();
			int maxX = minX + 6;
			GlStateManager.enableBlend();
			GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ZERO, GlStateManager.DestFactor.ONE);
			GlStateManager.disableAlpha();
			GlStateManager.shadeModel(7425);
			GlStateManager.disableTexture2D();
			builder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
			builder.pos((double)minX, (double)this.y1, 0.0D).tex(0.0F, 1.0F).color(0, 0, 0, 255).endVertex();
			builder.pos((double)maxX, (double)this.y1, 0.0D).tex(1.0F, 1.0F).color(0, 0, 0, 255).endVertex();
			builder.pos((double)maxX, (double)this.y0, 0.0D).tex(1.0F, 0.0F).color(0, 0, 0, 255).endVertex();
			builder.pos((double)minX, (double)this.y0, 0.0D).tex(0.0F, 0.0F).color(0, 0, 0, 255).endVertex();
			tes.draw();
			builder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
			builder.pos((double)minX, (double)(scrollValue + maxPosition), 0.0D).tex(0.0F, 1.0F).color(128, 128, 128, 255).endVertex();
			builder.pos((double)maxX, (double)(scrollValue + maxPosition), 0.0D).tex(1.0F, 1.0F).color(128, 128, 128, 255).endVertex();
			builder.pos((double)maxX, (double)scrollValue, 0.0D).tex(1.0F, 0.0F).color(128, 128, 128, 255).endVertex();
			builder.pos((double)minX, (double)scrollValue, 0.0D).tex(0.0F, 0.0F).color(128, 128, 128, 255).endVertex();
			tes.draw();
			builder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
			builder.pos((double)minX, (double)(scrollValue + maxPosition - 1), 0.0D).tex(0.0F, 1.0F).color(192, 192, 192, 255).endVertex();
			builder.pos((double)(maxX - 1), (double)(scrollValue + maxPosition - 1), 0.0D).tex(1.0F, 1.0F).color(192, 192, 192, 255).endVertex();
			builder.pos((double)(maxX - 1), (double)scrollValue, 0.0D).tex(1.0F, 0.0F).color(192, 192, 192, 255).endVertex();
			builder.pos((double)minX, (double)scrollValue, 0.0D).tex(0.0F, 0.0F).color(192, 192, 192, 255).endVertex();
			tes.draw();
			GlStateManager.enableTexture2D();
		}
		GlStateManager.enableTexture2D();
		GlStateManager.disableBlend();
	}
	
	protected void centerScrollOn(E entry) {
		this.setScrollAmount((double)(this.children().indexOf(entry) * this.itemHeight + this.itemHeight / 2 - (this.y1 - this.y0) / 2));
	}
	
	protected void ensureVisible(E entry) {
		int i = this.getRowTop(this.children().indexOf(entry));
		int j = i - this.y0 - 4 - this.itemHeight;
		if (j < 0) {
			this.scroll(j);
		}
		
		int k = this.y1 - i - this.itemHeight - this.itemHeight;
		if (k < 0) {
			this.scroll(-k);
		}
		
	}
	
	private void scroll(int value) {
		this.setScrollAmount(this.getScrollAmount() + (double)value);
	}
	
	public double getScrollAmount() {
		return scrolling ? scrollAmount.getTarget() : scrollAmount.getValue();
	}
	
	public void setScrollAmount(double value) {
		this.scrollAmount.setTarget((float)MathHelper.clamp_double(value, 0.0D, (double)this.getMaxScroll()));
	}
	
	public int getMaxScroll() {
		return Math.max(0, this.getMaxPosition() - (this.y1 - this.y0 - 4));
	}
	
	public int getScrollBottom() {
		return (int)this.getScrollAmount() - this.height - this.headerHeight;
	}
	
	protected void updateScrollingState(double mouseX, double mouseY, int button) {
		this.scrolling = button == 0 && mouseX >= (double)this.getScrollbarPosition() && mouseX < (double)(this.getScrollbarPosition() + 6);
	}
	
	protected int getScrollbarPosition() {
		return this.width / 2 + 124;
	}
	
	@Override
	public boolean mouseClick(double mouseX, double mouseY, int button) {
		this.updateScrollingState(mouseX, mouseY, button);
		if (!this.isMouseOver(mouseX, mouseY)) {
			return false;
		} else {
			E e = this.getEntryAtPosition(mouseX, mouseY);
			if (e != null) {
				if (e.mouseClick(mouseX, mouseY, button)) {
					this.setFocused(e);
					this.setDragging(true);
					return true;
				}
			}
			return this.scrolling;
		}
	}
	
	@Override
	public boolean mouseRelease(double mouseX, double mouseY, int button) {
		scrolling = false;
		if (this.getFocused() != null) {
			this.getFocused().mouseRelease(mouseX, mouseY, button);
		}
		
		return false;
	}
	
	@Override
	public boolean mouseDrag(double mouseX, double mouseY, int button, double dragX, double dragY) {
		if (IInteractableContainer.super.mouseDrag(mouseX, mouseY, button, dragX, dragY)) return true;
		else if (button == 0 && this.scrolling) {
			if (mouseY < (double)this.y0) {
				this.setScrollAmount(0.0D);
			} else if (mouseY > (double)this.y1) {
				this.setScrollAmount((double)this.getMaxScroll());
			} else {
				double d0 = (double)Math.max(1, this.getMaxScroll());
				int i = this.y1 - this.y0;
				int j = MathHelper.clamp_int((int)((float)(i * i) / (float)this.getMaxPosition()), 32, i - 8);
				double d1 = Math.max(1.0D, d0 / (double)(i - j));
				this.setScrollAmount(this.getScrollAmount() + dragY * d1);
			}
			
			return true;
		} else {
			return false;
		}
	}
	
	@Override
	public boolean mouseScroll(double mouseX, double mouseY, double scroll) {
		this.setScrollAmount(this.getScrollAmount() - scroll * (double)this.itemHeight * 2.0D);
		return true;
	}
	
	@Override
	public boolean charTyped(char character, int keyCode) {
		if(IInteractableContainer.super.charTyped(character, keyCode)) return true;
		if(keyCode == Keyboard.KEY_UP) {
			moveSelection(true);
			return true;
		}
		if(keyCode == Keyboard.KEY_DOWN) {
			moveSelection(false);
			return true;
		}
		return false;
	}
	
	protected void moveSelection(boolean up) {
		this.moveSelection(up, value -> true);
	}
	
	protected void refreshSelection() {
		E e = this.getSelected();
		if (e != null) {
			this.setSelected(e);
			this.ensureVisible(e);
		}
		
	}
	
	protected boolean moveSelection(boolean up, Predicate<E> filter) {
		int i = up ? -1 : 1;
		if (!this.children().isEmpty()) {
			int index = this.children().indexOf(this.getSelected());
			while(true) {
				int nextIndex = MathHelper.clamp_int(index + i, 0, this.getItemCount() - 1);
				if (index == nextIndex) break;
				E entry = this.children().get(nextIndex);
				if (filter.test(entry)) {
					this.setSelected(entry);
					this.ensureVisible(entry);
					return true;
				}
				index = nextIndex;
			}
		}
		return false;
	}
	
	@Override
	public boolean isMouseOver(double mouseX, double mouseY) {
		return mouseY >= (double)this.y0 && mouseY <= (double)this.y1 && mouseX >= (double)this.x0 && mouseX <= (double)this.x1;
	}
	
	protected void renderList(int mouseX, int mouseY, float partialTicks) {
		int i = this.getRowLeft();
		int j = this.getRowWidth();
		int k = this.itemHeight - 4;
		int l = this.getItemCount();
		
		for(int i1 = 0; i1 < l; ++i1) {
			int j1 = this.getRowTop(i1);
			int k1 = this.getRowBottom(i1);
			if (k1 >= this.y0 && j1 <= this.y1) {
				this.renderItem(mouseX, mouseY, partialTicks, i1, i, j1, j, k);
			}
		}
	}
	
	protected void renderItem(int mouseX, int mouseY, float partialTicks, int x, int left, int top, int width, int height) {
		E e = this.getEntry(x);
		if (this.renderSelection && this.isSelectedItem(x)) {
			int i = this.isFocused() ? -1 : -8355712;
			this.renderSelection(top, width, height, i, -16777216);
		}
		e.render(x, top, left, width, height, mouseX, mouseY, Objects.equals(this.hovered, e), partialTicks);
	}
	
	protected void renderSelection(int top, int width, int height, int mainColor, int subColor) {
		int minX = this.x0 + (this.width - width) / 2;
		int maxX = this.x0 + (this.width + width) / 2;
		Gui.drawRect(minX, top - 2, maxX, top + height + 2, mainColor);
		Gui.drawRect(minX + 1, top - 1, maxX - 1, top + height + 1, subColor);
	}
	
	public int getRowLeft() {
		return this.x0 + this.width / 2 - this.getRowWidth() / 2 + 2;
	}
	
	public int getRowRight() {
		return this.getRowLeft() + this.getRowWidth();
	}
	
	protected int getRowTop(int index) {
		return this.y0 + 4 - (int)this.getScrollAmount() + index * this.itemHeight + this.headerHeight;
	}
	
	private int getRowBottom(int index) {
		return this.getRowTop(index) + this.itemHeight;
	}
	
	protected boolean isFocused() {
		return false;
	}
	
	@Nullable
	protected E remove(int index) {
		E e = this.children.get(index);
		return (E)(this.removeEntry(this.children.get(index)) ? e : null);
	}
	
	protected boolean removeEntry(E entry) {
		boolean flag = this.children.remove(entry);
		if (flag && entry == this.getSelected()) {
			this.setSelected((E)null);
		}
		
		return flag;
	}
	
	@Nullable
	protected E getHovered() {
		return this.hovered;
	}
	
	void bindEntryToSelf(AbstractScrollList.Entry<E> entry) {
		entry.list = this;
	}
	
	public int getWidgetWidth() { return this.width; }
	public int getWidgetHeight() { return this.height; }
	public int getTop() { return this.y0; }
	public int getBottom() { return this.y1; }
	public int getLeft() { return this.x0; }
	public int getRight() { return this.x1; }
	
	public final boolean isDragging() {
		return this.isDragging;
	}
	
	public final void setDragging(boolean value) {
		this.isDragging = value;
	}
	
	public IInteractable getFocused() {
		return this.focused;
	}
	
	public void setFocused(IInteractable interact) {
		this.focused = interact;
	}

	@Override
	public List<E> children() {
		return children;
	}
	
	public abstract static class Entry<E extends AbstractScrollList.Entry<E>> implements IInteractable {
		protected AbstractScrollList<E> list;
		
		public abstract void render(int x, int top, int left, int width, int height, int mouseX, int mouseY, boolean selected, float partialTicks);
		
		public void tick() {}

		public boolean isMouseOver(double p_93537_, double p_93538_) {
			return Objects.equals(this.list.getEntryAtPosition(p_93537_, p_93538_), this);
		}
	}
	
	public abstract static class ContainerEntry<E extends AbstractScrollList.Entry<E>> extends AbstractScrollList.Entry<E> implements IInteractableContainer {
		@Nullable
		private IInteractable focused;
		private boolean dragging;
		
		public boolean isDragging() {
			return this.dragging;
		}
		
		public void setDragging(boolean dragging) {
			this.dragging = dragging;
		}
		
		public void setFocused(@Nullable IInteractable focused) {
			this.focused = focused;
		}
		
		@Nullable
		public IInteractable getFocused() {
			return this.focused;
		}
		
	}	
	class TrackedList extends AbstractList<E> {
		private final List<E> delegate = Lists.newArrayList();
		
		public E get(int p_93557_) {
			return this.delegate.get(p_93557_);
		}
		
		public int size() {
			return this.delegate.size();
		}
		
		public E set(int p_93559_, E p_93560_) {
			E e = this.delegate.set(p_93559_, p_93560_);
			AbstractScrollList.this.bindEntryToSelf(p_93560_);
			return e;
		}
		
		public void add(int p_93567_, E p_93568_) {
			this.delegate.add(p_93567_, p_93568_);
			AbstractScrollList.this.bindEntryToSelf(p_93568_);
		}

		public E remove(int p_93565_) {
			return this.delegate.remove(p_93565_);
		}
	}
}
