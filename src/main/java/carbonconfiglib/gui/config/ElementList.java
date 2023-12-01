package carbonconfiglib.gui.config;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import com.mojang.blaze3d.platform.GlStateManager.DestFactor;
import com.mojang.blaze3d.platform.GlStateManager.SourceFactor;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;

import carbonconfiglib.gui.api.BackgroundTexture;
import carbonconfiglib.gui.api.BackgroundTexture.BackgroundHolder;
import carbonconfiglib.gui.screen.SmoothFloat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.util.Mth;

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
public class ElementList extends ContainerObjectSelectionList<Element>
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
		float actualValue = (float)Mth.clamp(value, 0, getMaxScroll());
		this.value.setTarget(actualValue);
		if(force) this.value.forceFinish();
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
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
		value.update(partialTicks);
		super.setScrollAmount(value.getValue());
		super.render(graphics, mouseX, mouseY, partialTicks);
	}
	
	public void setCustomBackground(BackgroundHolder customBackground) {
		this.customBackground = customBackground;
		setRenderBackground(this.customBackground == null);
		setRenderTopAndBottom(this.customBackground == null);
	}
	
	@Override
	protected void renderBackground(GuiGraphics graphics) {
		if(customBackground == null || (minecraft.level != null && customBackground.shouldDisableInLevel())) return;
		renderBackground(x0, x1, y0, y1, (float)getScrollAmount(), customBackground.getTexture());
	}
	
	@Override
	protected void renderDecorations(GuiGraphics graphics, int mouseX, int mouseY) {
		if(customBackground == null) return;
		renderListOverlay(x0, x1, y0, y1, width, height, customBackground.getTexture());
	}
	
	public static void renderListOverlay(int x0, int x1, int y0, int y1, int width, int height, BackgroundTexture texture) {
		Tesselator tes = Tesselator.getInstance();
		BufferBuilder builder = tes.getBuilder();
		RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
		RenderSystem.setShaderTexture(0, texture.getForegroundTexture());
		RenderSystem.enableDepthTest();
		RenderSystem.depthFunc(519);
		int color = texture.getForegroundBrightness();
		builder.begin(Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
		builder.vertex(x0, y0, -100D).uv(0, y0 / 32F).color(color, color, color, 255).endVertex();
		builder.vertex(x0 + width, y0, -100D).uv(width / 32F, y0 / 32F).color(color, color, color, 255).endVertex();
		builder.vertex(x0 + width, 0D, -100D).uv(width / 32F, 0F).color(color, color, color, 255).endVertex();
		builder.vertex(x0, 0D, -100D).uv(0F, 0F).color(color, color, color, 255).endVertex();
		builder.vertex(x0, height, -100D).uv(0F, height / 32F).color(color, color, color, 255).endVertex();
		builder.vertex(x0 + width, height, -100D).uv(width / 32F, height / 32F).color(color, color, color, 255).endVertex();
		builder.vertex(x0 + width, y1, -100D).uv(width / 32F, y1 / 32F).color(color, color, color, 255).endVertex();
		builder.vertex(x0, y1, -100D).uv(0F, y1 / 32F).color(color, color, color, 255).endVertex();
		tes.end();
		RenderSystem.depthFunc(515);
		RenderSystem.disableDepthTest();
		RenderSystem.enableBlend();
		RenderSystem.blendFuncSeparate(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA, SourceFactor.ZERO, DestFactor.ONE);
		RenderSystem.setShader(GameRenderer::getPositionColorShader);
		builder.begin(Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
		builder.vertex(x0, y0 + 4, 0D).color(0, 0, 0, 0).endVertex();
		builder.vertex(x1, y0 + 4, 0D).color(0, 0, 0, 0).endVertex();
		builder.vertex(x1, y0, 0D).color(0, 0, 0, 255).endVertex();
		builder.vertex(x0, y0, 0D).color(0, 0, 0, 255).endVertex();
		builder.vertex(x0, y1, 0D).color(0, 0, 0, 255).endVertex();
		builder.vertex(x1, y1, 0D).color(0, 0, 0, 255).endVertex();
		builder.vertex(x1, y1 - 4, 0D).color(0, 0, 0, 0).endVertex();
		builder.vertex(x0, y1 - 4, 0D).color(0, 0, 0, 0).endVertex();
		tes.end();
	}
	
	public static void renderBackground(int x0, int x1, int y0, int y1, float scroll, BackgroundTexture texture) {
		Tesselator tes = Tesselator.getInstance();
		BufferBuilder builder = tes.getBuilder();
		RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
		RenderSystem.setShaderTexture(0, texture.getBackgroundTexture());
		RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
		int color = texture.getBackgroundBrightness();
		builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
		builder.vertex(x0, y1, 0D).uv(x0 / 32F, (y1 + scroll) / 32F).color(color, color, color, 255).endVertex();
		builder.vertex(x1, y1, 0D).uv(x1 / 32F, (y1 + scroll) / 32F).color(color, color, color, 255).endVertex();
		builder.vertex(x1, y0, 0D).uv(x1 / 32F, (y0 + scroll) / 32F).color(color, color, color, 255).endVertex();
		builder.vertex(x0, y0, 0D).uv(x0 / 32F, (y0 + scroll) / 32F).color(color, color, color, 255).endVertex();
		tes.end();
	}
}