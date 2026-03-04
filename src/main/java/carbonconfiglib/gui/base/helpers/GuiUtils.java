package carbonconfiglib.gui.base.helpers;

import java.util.ArrayDeque;
import java.util.Deque;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.GlStateManager.DestFactor;
import com.mojang.blaze3d.platform.GlStateManager.SourceFactor;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import com.mojang.math.Matrix4f;

import carbonconfiglib.gui.api.background.BackgroundTexture;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraftforge.client.gui.ScreenUtils;

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
public class GuiUtils
{
	private static final ScissorsStack STACK = new ScissorsStack();
	
	public static int brighter(int color, float factor) {
		int r = (color >> 16) & 0xFF;
		int g = (color >> 8) & 0xFF;
		int b = color & 0xFF;
		int i = (int)(1.0 / (1.0 - factor));
		if(r == 0 && g == 0 && b == 0) {
			return (color & 0xFF000000) | ((i & 0xFF) << 16) | ((i & 0xFF) << 8) | (i & 0xFF);
		}
		if(r > 0 && r < i) r = i;
		if(g > 0 && g < i) g = i;
		if(b > 0 && b < i) b = i;
		return (color & 0xFF000000) | Math.min(255, (int)(r / factor)) << 16 | Math.min(255, (int)(g / factor)) << 8 | Math.min(255, (int)(b / factor));
	}
	
	public static float calculateScrollOffset(float width, Font font, Align align, Component text, int seed) {
		int textWidth = font.width(text);
		if(textWidth > width) {
			float diff = textWidth - width + 2F;
			double timer = (Util.getMillis() + seed) / 1000D;
			double minDiff = Math.max(diff * 0.5D, 3.0D);
			double offset = Math.sin((Math.PI / 2D) * Math.cos(((Math.PI * 2D) * timer) / minDiff)) / 2D + 0.01F + align.alignCenter();
			return (float)Mth.lerp(offset, 0D, diff);
		}
		return 0;
	}
	
	public static void drawText(PoseStack stack, Font font, Component text, float x, float y, Align align, int color) {
		float offset = align.align(font.width(text));
		font.draw(stack, text, x + offset, y, color);
	}
	
	public static void drawSplitText(PoseStack stack, Font font, Component text, float x, float y, Align align, int color, int maxLength) {
		drawSplitText(stack, font, text, x, y, align, color, maxLength, font.lineHeight);
	}
	
	public static void drawSplitText(PoseStack stack, Font font, Component text, float x, float y, Align align, int color, int maxLength, float lineSplit) {
		for(FormattedCharSequence line : font.split(text, maxLength)) {
			float offset = align.align(font.width(line));
			font.draw(stack, line, x + offset, y, color);
			y += lineSplit;
		}
	}
	
	public static void drawScrollingText(PoseStack stack, Font font, Component text, float x, float y, float width, float height, Align align, int color, int seed) {
		int textWidth = font.width(text);
		if(textWidth > width) {
			float diff = textWidth - width + 2F;
			double timer = (Util.getMillis() + seed) / 1000D;
			double minDiff = Math.max(diff * 0.5D, 3.0D);
			double offset = Math.sin((Math.PI / 2D) * Math.cos(((Math.PI * 2D) * timer) / minDiff)) / 2D + 0.01F + align.alignCenter();
			pushScissors((int)x, (int)y, (int)width, (int)height);
			font.draw(stack, text, x - align.align(width) + align.align(textWidth) + (float)Mth.lerp(offset, 0D, diff), y + (height * 0.5F) - (font.lineHeight * 0.5F), color);
			popScissors();
			return;
		}
		float offset = align.align(textWidth);
		font.draw(stack, text, x - align.align(width) + offset, y + (height * 0.5F) - (font.lineHeight * 0.5F), color);
	}
	
	public static void drawScrollingShadowText(PoseStack stack, Font font, Component text, float x, float y, float width, float height, Align align, int color, int seed) {
		int textWidth = font.width(text);
		if(textWidth > width) {
			float diff = textWidth - width + 2F;
			double timer = (Util.getMillis() + seed) / 1000D;
			double minDiff = Math.max(diff * 0.5D, 3.0D);
			double offset = Math.sin((Math.PI / 2D) * Math.cos(((Math.PI * 2D) * timer) / minDiff)) / 2D + 0.01F + align.alignCenter();
			pushScissors((int)x, (int)y, (int)width, (int)height);
			font.drawShadow(stack, text, x - align.align(width) + align.align(textWidth) + (float)Mth.lerp(offset, 0D, diff), y + (height * 0.5F) - (font.lineHeight * 0.5F), color);
			popScissors();
			return;
		}
		float offset = align.align(textWidth);
		font.drawShadow(stack, text, x - align.align(width) + offset, y + (height * 0.5F) - (font.lineHeight * 0.5F), color);
	}
	
	public static void pushScissors(int x, int y, int width, int height) {
		pushScissors(new Rect(x, y, width, height));;
	}
	
	public static void pushScissors(Rect rect) {
		STACK.push(rect);
		applyScissors(rect);
	}
	
	public static void popScissors() {
		applyScissors(STACK.pop());
	}
	
	private static void applyScissors(Rect rect) {
		if(rect == null) {
			RenderSystem.disableScissor();
			return;
		}
		Window window = Minecraft.getInstance().getWindow();
		int bottom = rect.maxY;
		double scaledHeight = (double)window.getHeight() / (double)window.getGuiScaledHeight();
		double scaledWidth = (double)window.getWidth() / (double)window.getGuiScaledWidth();
		RenderSystem.enableScissor((int)(rect.getX() * scaledWidth), (int)(window.getHeight() - bottom * scaledHeight), (int)(rect.getWidth() * scaledWidth), (int)(rect.getHeigth() * scaledHeight));
	}
	
	public static void drawTextureRegion(PoseStack stack, float x, float y, float width, float height, Icon icon, float texWidth, float texHeight) {
		RenderSystem._setShaderTexture(0, icon.getTexture());
		drawTextureRegion(stack, x, y, icon.getX(), icon.getY(), width, height, texWidth, texHeight, icon.getSheetWidth(), icon.getSheetHeight());
	}
	
	public static void drawTextureRegion(PoseStack stack, float x, float y, int xOff, int yOff, float width, float height, Icon icon, float texWidth, float texHeight) {
		RenderSystem._setShaderTexture(0, icon.getTexture());
		drawTextureRegion(stack, x, y, icon.getX() + xOff, icon.getY() + yOff, width, height, texWidth, texHeight, icon.getSheetWidth(), icon.getSheetHeight());
	}
    
	public static void drawTextureRegion(PoseStack stack, float x, float y, float texX, float texY, float width, float height, float texWidth, float texHeight, float textureWidth, float textureHeight) {
		Matrix4f matrix = stack.last().pose();
		float maxX = x + width;
		float maxY = y + height;
		float t_minX = texX / textureWidth;
		float t_minY = texY / textureHeight;
		float t_maxX = (texX + texWidth) / textureWidth;
		float t_maxY = (texY + texHeight) / textureHeight;
		
		Tesselator tessellator = Tesselator.getInstance();
		BufferBuilder bufferbuilder = tessellator.getBuilder();
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		bufferbuilder.begin(Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
		bufferbuilder.vertex(matrix, x, maxY, 0).uv(t_minX, t_maxY).endVertex();
		bufferbuilder.vertex(matrix, maxX, maxY, 0).uv(t_maxX, t_maxY).endVertex();
		bufferbuilder.vertex(matrix, maxX, y, 0).uv(t_maxX, t_minY).endVertex();
		bufferbuilder.vertex(matrix, x, y, 0).uv(t_minX, t_minY).endVertex();
		tessellator.end();
	}
	
	public static class Rect {
		int minX;
		int minY;
		int maxX;
		int maxY;
		
		public Rect(int x, int y, int width, int heigth) {
			this.minX = x;
			this.minY = y;
			this.maxX = x + width;
			this.maxY = y + heigth;
		}
		
		public void limit(Rect rect) {
			minX = Math.max(rect.minX, minX);
			minY = Math.max(rect.minY, minY);
			maxX = Math.min(rect.maxX, maxX);
			maxY = Math.min(rect.maxY, maxY);
			if(minX > maxX) minX = maxX;
			if(minY > maxY) minY = maxY;
		}
		
		public int getX() { return minX; }
		public int getY() { return minY; }
		public int getWidth() { return maxX - minX; }
		public int getHeigth() { return maxY - minY; }
	}
	
	public static class ScissorsStack {
		Deque<Rect> stack = new ArrayDeque<>();
		
		public void push(Rect owner) {
			if(stack.isEmpty()) {
				stack.push(owner);
				return;
			}
			owner.limit(stack.peek());
			stack.push(owner);
		}
		
		public Rect pop() {
			stack.pop();
			return stack.peek();
		}
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
	}
	
	public static void renderListShadow(int x0, int x1, int y0, int y1, int width, int height) {
		Tesselator tes = Tesselator.getInstance();
		BufferBuilder builder = tes.getBuilder();
		RenderSystem.depthFunc(515);
		RenderSystem.disableDepthTest();
		RenderSystem.enableBlend();
		RenderSystem.blendFuncSeparate(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA, SourceFactor.ZERO, DestFactor.ONE);
		RenderSystem.disableTexture();
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
	
	public static void drawFrame(PoseStack stack, float minX, float minY, float maxX, float maxY, int color, float width) {
		Tesselator tessellator = Tesselator.getInstance();
		BufferBuilder bufferbuilder = tessellator.getBuilder();
		bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
		drawQuadArea(stack, minX, minY, maxX, minY+width, bufferbuilder, color);
		drawQuadArea(stack, minX, maxY, maxX, maxY+width, bufferbuilder, color);
		drawQuadArea(stack, minX, minY, minX+width, maxY, bufferbuilder, color);
		drawQuadArea(stack, maxX, minY, maxX+width, maxY+width, bufferbuilder, color);
		GlStateManager._enableBlend();
		GlStateManager._disableTexture();
		RenderSystem.setShader(GameRenderer::getPositionColorShader);
		RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
		tessellator.end();
		GlStateManager._enableTexture();
		GlStateManager._disableBlend();
	}
	
	public static void drawQuadArea(PoseStack matrix, float left, float top, float right, float bottom, VertexConsumer builder, int color) {
		if(left < right) {
			float i = left;
			left = right;
			right = i;
		}
		if(top < bottom) {
			float j = top;
			top = bottom;
			bottom = j;
		}
		float f3 = (float)(color >> 24 & 255) / 255.0F;
		float f = (float)(color >> 16 & 255) / 255.0F;
		float f1 = (float)(color >> 8 & 255) / 255.0F;
		float f2 = (float)(color & 255) / 255.0F;
		if(matrix == null) {
			builder.vertex(left, bottom, 0.0F).color(f, f1, f2, f3).endVertex();
			builder.vertex(right, bottom, 0.0F).color(f, f1, f2, f3).endVertex();
			builder.vertex(right, top, 0.0F).color(f, f1, f2, f3).endVertex();
			builder.vertex(left, top, 0.0F).color(f, f1, f2, f3).endVertex();
			return;
		}
		Matrix4f stack = matrix.last().pose();
		builder.vertex(stack, left, bottom, 0.0F).color(f, f1, f2, f3).endVertex();
		builder.vertex(stack, right, bottom, 0.0F).color(f, f1, f2, f3).endVertex();
		builder.vertex(stack, right, top, 0.0F).color(f, f1, f2, f3).endVertex();
		builder.vertex(stack, left, top, 0.0F).color(f, f1, f2, f3).endVertex();
	}
	
	public static void blitWithBorder(PoseStack poseStack, ResourceLocation res, int x, int y, int u, int v, int width, int height, int textureWidth, int textureHeight, int topBorder, int bottomBorder, int leftBorder, int rightBorder, float zLevel, boolean custom) {
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderTexture(0, res);
		blitWithBorder(poseStack, x, y, u, v, width, height, textureWidth, textureHeight, topBorder, bottomBorder, leftBorder, rightBorder, zLevel, custom);
	}
	
	public static void blitWithBorder(PoseStack poseStack, int x, int y, int u, int v, int width, int height, int textureWidth, int textureHeight, int topBorder, int bottomBorder, int leftBorder, int rightBorder, float zLevel, boolean custom) {
		if(!custom) {
			RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
			RenderSystem.enableBlend();
			RenderSystem.defaultBlendFunc();
		}
		
		int fillerWidth = textureWidth - leftBorder - rightBorder;
		int fillerHeight = textureHeight - topBorder - bottomBorder;
		int canvasWidth = width - leftBorder - rightBorder;
		int canvasHeight = height - topBorder - bottomBorder;
		int xPasses = canvasWidth / fillerWidth;
		int remainderWidth = canvasWidth % fillerWidth;
		int yPasses = canvasHeight / fillerHeight;
		int remainderHeight = canvasHeight % fillerHeight;

		ScreenUtils.drawTexturedModalRect(poseStack, x, y, u, v, leftBorder, topBorder, zLevel);
		ScreenUtils.drawTexturedModalRect(poseStack, x + leftBorder + canvasWidth, y, u + leftBorder + fillerWidth, v, rightBorder, topBorder, zLevel);
		ScreenUtils.drawTexturedModalRect(poseStack, x, y + topBorder + canvasHeight, u, v + topBorder + fillerHeight, leftBorder, bottomBorder, zLevel);
		ScreenUtils.drawTexturedModalRect(poseStack, x + leftBorder + canvasWidth, y + topBorder + canvasHeight, u + leftBorder + fillerWidth, v + topBorder + fillerHeight, rightBorder, bottomBorder, zLevel);
		for (int i = 0; i < xPasses + (remainderWidth > 0 ? 1 : 0); i++) {
			ScreenUtils.drawTexturedModalRect(poseStack, x + leftBorder + (i * fillerWidth), y, u + leftBorder, v, (i == xPasses ? remainderWidth : fillerWidth), topBorder, zLevel);
			ScreenUtils.drawTexturedModalRect(poseStack, x + leftBorder + (i * fillerWidth), y + topBorder + canvasHeight, u + leftBorder, v + topBorder + fillerHeight, (i == xPasses ? remainderWidth : fillerWidth), bottomBorder, zLevel);

			for (int j = 0; j < yPasses + (remainderHeight > 0 ? 1 : 0); j++) {
				ScreenUtils.drawTexturedModalRect(poseStack, x + leftBorder + (i * fillerWidth), y + topBorder + (j * fillerHeight), u + leftBorder, v + topBorder, (i == xPasses ? remainderWidth : fillerWidth), (j == yPasses ? remainderHeight : fillerHeight), zLevel);
			}
		}

		for (int j = 0; j < yPasses + (remainderHeight > 0 ? 1 : 0); j++) {
			ScreenUtils.drawTexturedModalRect(poseStack, x, y + topBorder + (j * fillerHeight), u, v + topBorder, leftBorder, (j == yPasses ? remainderHeight : fillerHeight), zLevel);
			ScreenUtils.drawTexturedModalRect(poseStack, x + leftBorder + canvasWidth, y + topBorder + (j * fillerHeight), u + leftBorder + fillerWidth, v + topBorder, rightBorder, (j == yPasses ? remainderHeight : fillerHeight), zLevel);
		}
	}
}
