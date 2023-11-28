package carbonconfiglib.gui.config;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.platform.GlStateManager;

import carbonconfiglib.gui.api.BackgroundTexture;
import carbonconfiglib.gui.api.BackgroundTexture.BackgroundHolder;
import carbonconfiglib.gui.screen.SmoothFloat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.list.AbstractOptionList;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.MathHelper;

/**
 * Copyright 2023 Speiger, Meduris
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
public class ElementList extends AbstractOptionList<Element>
{
	BackgroundHolder customBackground;
	int listWidth = 220;
	int scrollPadding = 124;
	Consumer<Element> callback;
	int lastTick = 0;
	boolean isScrolling;
	SmoothFloat value = new SmoothFloat(0.8F);
	
	public ElementList(int width, int height, int screenY, int listY, int itemHeight) {
		super(Minecraft.getInstance(), width, height, screenY, listY, itemHeight);
		setRenderSelection(false);
	}
	
	@Override
	protected boolean isSelectedItem(int index) {
		return Objects.equals(this.getSelected(), this.children().get(index));
	}
	
	public void setCallback(Consumer<Element> callback) {
		this.callback = callback;
	}
	
	public void addElement(Element element) {
		addEntry(element);
	}
	
	public void addElements(List<Element> elements) {
		elements.forEach(this::addEntry);
	}
	
	public void updateList(List<Element> elements) {
		super.replaceEntries(elements);
	}
	
	public void removeElement(Element element) {
		this.removeEntry(element);
	}
	
	public int size() {
		return children().size();
	}
	
	@Override
	public void setSelected(Element p_93462_) {
		super.setSelected(p_93462_);
		if(callback != null && getSelected() != null) {
			callback.accept(getSelected());
		}
	}
	
	public void scrollToElement(Element element, boolean center) {
		int index = children().indexOf(element);
		if(index == -1) return;
		scrollToElement(index, center);
	}
	
	public void scrollToSelected(boolean center) {
		if(getSelected() == null) return;
		scrollToElement(getSelected(), center);
	}
	
	public void scrollToElement(int index, boolean center) {
		if(center) {
			index -= (height / itemHeight) / 3;
		}
		setScrollAmount(Math.max(0, index) * this.itemHeight + this.headerHeight);
	}
	
	public void setListWidth(int listWidth) {
		this.listWidth = listWidth;
	}
	
	public void setScrollPadding(int scrollPadding) {
		this.scrollPadding = scrollPadding;
	}
	
	@Override
	public int getRowWidth() {
		return listWidth;
	}
	
	public int getLastTick() {
		return lastTick;
	}
	
	@Override
	protected int getScrollbarPosition() {
		return this.width / 2 + scrollPadding;
	}
	
	@Override
	protected void updateScrollingState(double mouseX, double mouseY, int button) {
		this.isScrolling = button == 0 && mouseX >= (double)this.getScrollbarPosition() && mouseX < (double)(this.getScrollbarPosition() + 6);
		super.updateScrollingState(mouseX, mouseY, button);
	}
	
	@Override
	public void setScrollAmount(double value) {
		setScrollAmount(value, isScrolling);
	}
	
	public void setScrollAmount(double value, boolean force) {
		float actualValue = (float)MathHelper.clamp(value, 0, getMaxScroll());
		this.value.setTarget(actualValue);
		if(force) this.value.forceFinish();
	}
	
	public int getMaxScroll() {
		return Math.max(0, this.getMaxPosition() - (this.y1 - this.y0 - 4));
	}
	
	@Override
	public double getScrollAmount() {
		return isScrolling ? value.getTarget() : value.getValue();
	}
	
	@Override
	public boolean mouseReleased(double mouseX, double mouseY, int button) {
		isScrolling = false;
		return super.mouseReleased(mouseX, mouseY, button);
	}
	
	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double scroll) {
		this.setScrollAmount(this.getScrollAmount() - scroll * (double)this.itemHeight * 2);
		return true;
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
	
	@Override
	public void render(int mouseX, int mouseY, float partialTicks) {
		value.update(partialTicks);
		super.setScrollAmount(value.getValue());
		int k = this.getRowLeft();
		int l = this.y0 + 4 - (int)this.getScrollAmount();
		renderBackground();
		renderList(k, l, mouseX, mouseY, partialTicks);
		renderScrollbar();
	}
	
	public void setCustomBackground(BackgroundHolder customBackground) {
		this.customBackground = customBackground;
	}
	
	@Override
	protected void renderBackground() {
		if(customBackground == null || (minecraft.world != null && customBackground.shouldDisableInLevel())) return;
		renderBackground(x0, x1, y0, y1, (float)getScrollAmount(), customBackground.getTexture());
	}
		
	@Override
	protected void renderList(int left, int top, int mouseX, int mouseY, float partialTicks) {
		super.renderList(left, top, mouseX, mouseY, partialTicks);
		if(customBackground == null) return;
		renderListOverlay(x0, x1, y0, y1, width, height, customBackground.getTexture());
	}
	
	protected void renderScrollbar() {
		int j1 = this.getMaxScroll();
		if (j1 > 0) {
			int k1 = (int)((float)((this.y1 - this.y0) * (this.y1 - this.y0)) / (float)this.getMaxPosition());
			k1 = MathHelper.clamp(k1, 32, this.y1 - this.y0 - 8);
			int l1 = (int)this.getScrollAmount() * (this.y1 - this.y0 - k1) / j1 + this.y0;
			if (l1 < this.y0) {
				l1 = this.y0;
			}
			int i = this.getScrollbarPosition();
			int j = i + 6;
			GlStateManager.enableBlend();
			GlStateManager.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ZERO, GlStateManager.DestFactor.ONE);
			GlStateManager.disableAlphaTest();
			GlStateManager.shadeModel(7425);
			GlStateManager.disableTexture();
			
			Tessellator tes = Tessellator.getInstance();
			BufferBuilder builder = tes.getBuffer();
			
			builder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
			builder.pos((double)i, (double)this.y1, 0.0D).tex(0.0F, 1.0F).color(0, 0, 0, 255).endVertex();
			builder.pos((double)j, (double)this.y1, 0.0D).tex(1.0F, 1.0F).color(0, 0, 0, 255).endVertex();
			builder.pos((double)j, (double)this.y0, 0.0D).tex(1.0F, 0.0F).color(0, 0, 0, 255).endVertex();
			builder.pos((double)i, (double)this.y0, 0.0D).tex(0.0F, 0.0F).color(0, 0, 0, 255).endVertex();
			tes.draw();
			builder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
			builder.pos((double)i, (double)(l1 + k1), 0.0D).tex(0.0F, 1.0F).color(128, 128, 128, 255).endVertex();
			builder.pos((double)j, (double)(l1 + k1), 0.0D).tex(1.0F, 1.0F).color(128, 128, 128, 255).endVertex();
			builder.pos((double)j, (double)l1, 0.0D).tex(1.0F, 0.0F).color(128, 128, 128, 255).endVertex();
			builder.pos((double)i, (double)l1, 0.0D).tex(0.0F, 0.0F).color(128, 128, 128, 255).endVertex();
			tes.draw();
			builder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
			builder.pos((double)i, (double)(l1 + k1 - 1), 0.0D).tex(0.0F, 1.0F).color(192, 192, 192, 255).endVertex();
			builder.pos((double)(j - 1), (double)(l1 + k1 - 1), 0.0D).tex(1.0F, 1.0F).color(192, 192, 192, 255).endVertex();
			builder.pos((double)(j - 1), (double)l1, 0.0D).tex(1.0F, 0.0F).color(192, 192, 192, 255).endVertex();
			builder.pos((double)i, (double)l1, 0.0D).tex(0.0F, 0.0F).color(192, 192, 192, 255).endVertex();
			tes.draw();
			GlStateManager.enableTexture();
		}
	}
	
	public static void renderListOverlay(int x0, int x1, int y0, int y1, int width, int height, BackgroundTexture texture) {
		Tessellator tes = Tessellator.getInstance();
		BufferBuilder builder = tes.getBuffer();
		Minecraft.getInstance().getTextureManager().bindTexture(texture.getForegroundTexture());
		GlStateManager.enableTexture();
		GlStateManager.enableDepthTest();
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
		GlStateManager.disableDepthTest();
        GlStateManager.enableBlend();
        GlStateManager.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ZERO, GlStateManager.DestFactor.ONE);
        GlStateManager.disableAlphaTest();
        GlStateManager.shadeModel(7425);
		GlStateManager.disableTexture();
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
		GlStateManager.enableAlphaTest();
		GlStateManager.disableBlend();
		GlStateManager.enableTexture();
	}
	
	public static void renderBackground(int x0, int x1, int y0, int y1, float scroll, BackgroundTexture texture) {
		Tessellator tes = Tessellator.getInstance();
		BufferBuilder builder = tes.getBuffer();
		Minecraft.getInstance().getTextureManager().bindTexture(texture.getBackgroundTexture());
		int color = texture.getBackgroundBrightness();
		builder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
		builder.pos(x0, y1, 0D).tex(x0 / 32F, (y1 + scroll) / 32F).color(color, color, color, 255).endVertex();
		builder.pos(x1, y1, 0D).tex(x1 / 32F, (y1 + scroll) / 32F).color(color, color, color, 255).endVertex();
		builder.pos(x1, y0, 0D).tex(x1 / 32F, (y0 + scroll) / 32F).color(color, color, color, 255).endVertex();
		builder.pos(x0, y0, 0D).tex(x0 / 32F, (y0 + scroll) / 32F).color(color, color, color, 255).endVertex();
		tes.draw();
	}
}