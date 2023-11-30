package carbonconfiglib.gui.config;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import org.lwjgl.opengl.GL11;

import carbonconfiglib.gui.api.BackgroundTexture;
import carbonconfiglib.gui.widgets.screen.AbstractScrollList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;

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
public class ElementList extends AbstractScrollList<Element>
{
	int listWidth = 220;
	int scrollPadding = 124;
	Consumer<Element> callback;
	
	public ElementList(int width, int height, int screenY, int listY, int itemHeight) {
		super(width, height, screenY, listY, itemHeight);
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
	
	@Override
	protected int getScrollbarPosition() {
		return this.width / 2 + scrollPadding;
	}
	
	public int getMaxScroll() {
		return Math.max(0, this.getMaxPosition() - (this.y1 - this.y0 - 4));
	}

	public static void renderBackground(int x0, int x1, int y0, int y1, float scroll, BackgroundTexture texture) {
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		Minecraft.getMinecraft().getTextureManager().bindTexture(texture.getBackgroundTexture());
		int color = texture.getBackgroundBrightness();
		Tessellator tes = Tessellator.getInstance();
		VertexBuffer builder = tes.getBuffer();
		builder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
		builder.pos(x0, y1, 0D).tex(x0 / 32F, (y1 + scroll) / 32F).color(color, color, color, 255).endVertex();
		builder.pos(x1, y1, 0D).tex(x1 / 32F, (y1 + scroll) / 32F).color(color, color, color, 255).endVertex();
		builder.pos(x1, y0, 0D).tex(x1 / 32F, (y0 + scroll) / 32F).color(color, color, color, 255).endVertex();
		builder.pos(x0, y0, 0D).tex(x0 / 32F, (y0 + scroll) / 32F).color(color, color, color, 255).endVertex();
		tes.draw();
	}

	public static void renderListOverlay(int x0, int x1, int y0, int y1, int width, int height, BackgroundTexture texture) {
		Tessellator tes = Tessellator.getInstance();
		VertexBuffer builder = tes.getBuffer();
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
	
	
}