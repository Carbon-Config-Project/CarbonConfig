package carbonconfiglib.gui.base.helpers;

import java.util.ArrayDeque;
import java.util.Deque;

import org.joml.Matrix4f;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.GlStateManager.DestFactor;
import com.mojang.blaze3d.platform.GlStateManager.SourceFactor;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;

import carbonconfiglib.gui.api.background.BackgroundTexture;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
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
	
	public static float calculateScrollOffset(float width, Font font, Align align, Component text, long startTime) {
		int textWidth = font.width(text);
		if(textWidth > width) {
			float diff = textWidth - width + 2F;
			double timer = (currentMillseconds() - startTime) / 1000F;
			double minDiff = Math.max(diff * 0.5D, 3.0F);
			double offset = Math.sin((Math.PI / 2D) * Math.cos(((Math.PI * 2D) * timer) / minDiff)) / 2D + 0.01F + align.alignCenter();
			return (float)Mth.lerp(offset, 0F, diff);
		}
		return 0;
	}
	
	public static void drawText(GuiGraphics graphics, Font font, Component text, float x, float y, Align align, int color) {
		float offset = align.align(font.width(text));
		graphics.drawString(font, text.getVisualOrderText(), x + offset, y, color, false);
	}
	
	public static void drawSplitText(GuiGraphics graphics, Font font, Component text, float x, float y, Align align, int color, int maxLength) {
		drawSplitText(graphics, font, text, x, y, align, color, maxLength, font.lineHeight);
	}
	
	public static void drawSplitText(GuiGraphics graphics, Font font, Component text, float x, float y, Align align, int color, int maxLength, float lineSplit) {
		for(FormattedCharSequence line : font.split(text, maxLength)) {
			float offset = align.align(font.width(line));
			graphics.drawString(font, line, x + offset, y, color, false);
			y += lineSplit;
		}
	}
	
	public static void drawScrollingText(GuiGraphics graphics, Font font, Component text, float x, float y, float width, float height, Align align, int color, long startTime) {
		int textWidth = font.width(text);
		if(textWidth > width) {
			float diff = textWidth - width + 2F;
			double timer = (currentMillseconds() - startTime) / 1000F;
			double minDiff = Math.max(diff * 0.5D, 3.0F);
			double offset = Math.sin((Math.PI / 2D) * Math.cos(((Math.PI * 2D) * timer) / minDiff)) / 2D + 0.01F + align.alignCenter();
			pushScissors((int)x, (int)y, (int)width, (int)height);
			graphics.drawString(font, text.getVisualOrderText(), x - align.align(width) + align.align(textWidth) + (float)Mth.lerp(offset, 0F, diff), y + (height * 0.5F) - (font.lineHeight * 0.5F), color, false);
			popScissors();
			return;
		}
		float offset = align.align(textWidth);
		graphics.drawString(font, text.getVisualOrderText(), x - align.align(width) + offset, y + (height * 0.5F) - (font.lineHeight * 0.5F), color, false);
	}
	
	public static void drawScrollingShadowText(GuiGraphics graphics, Font font, Component text, float x, float y, float width, float height, Align align, int color, long startTime) {
		int textWidth = font.width(text);
		if(textWidth > width) {
			float diff = textWidth - width + 2F;
			double timer = (currentMillseconds() - startTime) / 1000F;
			double minDiff = Math.max(diff * 0.5D, 3.0F);
			double offset = Math.sin((Math.PI / 2D) * Math.cos(((Math.PI * 2D) * timer) / minDiff)) / 2D + 0.01F + align.alignCenter();
			pushScissors((int)x, (int)y, (int)width, (int)height);
			graphics.drawString(font, text.getVisualOrderText(), x - align.align(width) + align.align(textWidth) + (float)Mth.lerp(offset, 0F, diff), y + (height * 0.5F) - (font.lineHeight * 0.5F), color, true);
			popScissors();
			return;
		}
		float offset = align.align(textWidth);
		graphics.drawString(font, text.getVisualOrderText(), x - align.align(width) + offset, y + (height * 0.5F) - (font.lineHeight * 0.5F), color, true);
	}
	
	public static long currentMillseconds() {
		return Util.getMillis();
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
	
	public static void drawTextureRegion(GuiGraphics graphics, float x, float y, float width, float height, Icon icon, float texWidth, float texHeight) {
		RenderSystem._setShaderTexture(0, icon.getTexture());
		drawTextureRegion(graphics, x, y, icon.getX(), icon.getY(), width, height, texWidth, texHeight, icon.getSheetWidth(), icon.getSheetHeight());
	}
	
	public static void drawTextureRegion(GuiGraphics graphics, float x, float y, int xOff, int yOff, float width, float height, Icon icon, float texWidth, float texHeight) {
		RenderSystem._setShaderTexture(0, icon.getTexture());
		drawTextureRegion(graphics, x, y, icon.getX() + xOff, icon.getY() + yOff, width, height, texWidth, texHeight, icon.getSheetWidth(), icon.getSheetHeight());
	}
    
	public static void drawTextureRegion(GuiGraphics graphics, float x, float y, float texX, float texY, float width, float height, float texWidth, float texHeight, float textureWidth, float textureHeight) {
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		Matrix4f matrix = graphics.pose().last().pose();
		float maxX = x + width;
		float maxY = y + height;
		float t_minX = texX / textureWidth;
		float t_minY = texY / textureHeight;
		float t_maxX = (texX + texWidth) / textureWidth;
		float t_maxY = (texY + texHeight) / textureHeight;
		
		Tesselator tessellator = Tesselator.getInstance();
		BufferBuilder bufferbuilder = tessellator.begin(Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		bufferbuilder.addVertex(matrix, x, maxY, 0).setUv(t_minX, t_maxY);
		bufferbuilder.addVertex(matrix, maxX, maxY, 0).setUv(t_maxX, t_maxY);
		bufferbuilder.addVertex(matrix, maxX, y, 0).setUv(t_maxX, t_minY);
		bufferbuilder.addVertex(matrix, x, y, 0).setUv(t_minX, t_minY);
        BufferUploader.drawWithShader(bufferbuilder.buildOrThrow());
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
		BufferBuilder builder = tes.begin(Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
		RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
		RenderSystem.setShaderTexture(0, texture.getForegroundTexture());
		RenderSystem.enableDepthTest();
		RenderSystem.depthFunc(519);
		int color = texture.getForegroundBrightness();
		builder.addVertex(x0, y0, -100F).setUv(0, y0 / 32F).setColor(color, color, color, 255);
		builder.addVertex(x0 + width, y0, -100F).setUv(width / 32F, y0 / 32F).setColor(color, color, color, 255);
		builder.addVertex(x0 + width, 0F, -100F).setUv(width / 32F, 0F).setColor(color, color, color, 255);
		builder.addVertex(x0, 0F, -100F).setUv(0F, 0F).setColor(color, color, color, 255);
		builder.addVertex(x0, height, -100F).setUv(0F, height / 32F).setColor(color, color, color, 255);
		builder.addVertex(x0 + width, height, -100F).setUv(width / 32F, height / 32F).setColor(color, color, color, 255);
		builder.addVertex(x0 + width, y1, -100F).setUv(width / 32F, y1 / 32F).setColor(color, color, color, 255);
		builder.addVertex(x0, y1, -100F).setUv(0F, y1 / 32F).setColor(color, color, color, 255);
		BufferUploader.drawWithShader(builder.buildOrThrow());
	}
	
	public static void renderListShadow(int x0, int x1, int y0, int y1, int width, int height) {
		Tesselator tes = Tesselator.getInstance();
		BufferBuilder builder = tes.begin(Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
		RenderSystem.depthFunc(515);
		RenderSystem.disableDepthTest();
		RenderSystem.enableBlend();
		RenderSystem.blendFuncSeparate(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA, SourceFactor.ZERO, DestFactor.ONE);
		RenderSystem.setShader(GameRenderer::getPositionColorShader);
		builder.addVertex(x0, y0 + 4, 0F).setColor(0, 0, 0, 0);
		builder.addVertex(x1, y0 + 4, 0F).setColor(0, 0, 0, 0);
		builder.addVertex(x1, y0, 0F).setColor(0, 0, 0, 255);
		builder.addVertex(x0, y0, 0F).setColor(0, 0, 0, 255);
		builder.addVertex(x0, y1, 0F).setColor(0, 0, 0, 255);
		builder.addVertex(x1, y1, 0F).setColor(0, 0, 0, 255);
		builder.addVertex(x1, y1 - 4, 0F).setColor(0, 0, 0, 0);
		builder.addVertex(x0, y1 - 4, 0F).setColor(0, 0, 0, 0);
		BufferUploader.drawWithShader(builder.buildOrThrow());
	}
	
	public static void renderBackground(int x0, int x1, int y0, int y1, float scroll, BackgroundTexture texture) {
		renderBackground(x0, x1, y0, y1, 0F, scroll, texture);
	}
	
	public static void renderBackground(int x0, int x1, int y0, int y1, float xScroll, float yScroll, BackgroundTexture texture) {
		Tesselator tes = Tesselator.getInstance();
		BufferBuilder builder = tes.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
		RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
		RenderSystem.setShaderTexture(0, texture.getBackgroundTexture());
		RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
		int color = texture.getBackgroundBrightness();
		builder.addVertex(x0, y1, 0F).setUv((x0 + xScroll) / 32F, (y1 + yScroll) / 32F).setColor(color, color, color, 255);
		builder.addVertex(x1, y1, 0F).setUv((x1 + xScroll) / 32F, (y1 + yScroll) / 32F).setColor(color, color, color, 255);
		builder.addVertex(x1, y0, 0F).setUv((x1 + xScroll) / 32F, (y0 + yScroll) / 32F).setColor(color, color, color, 255);
		builder.addVertex(x0, y0, 0F).setUv((x0 + xScroll) / 32F, (y0 + yScroll) / 32F).setColor(color, color, color, 255);
		BufferUploader.drawWithShader(builder.buildOrThrow());
	}
	
	public static void drawFrame(GuiGraphics graphics, float minX, float minY, float maxX, float maxY, int color, float width) {
		PoseStack stack = graphics.pose();
		Tesselator tessellator = Tesselator.getInstance();
		BufferBuilder bufferbuilder = tessellator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
		drawQuadArea(stack, minX, minY, maxX, minY+width, bufferbuilder, color);
		drawQuadArea(stack, minX, maxY, maxX, maxY+width, bufferbuilder, color);
		drawQuadArea(stack, minX, minY, minX+width, maxY, bufferbuilder, color);
		drawQuadArea(stack, maxX, minY, maxX+width, maxY+width, bufferbuilder, color);
		GlStateManager._enableBlend();
		RenderSystem.setShader(GameRenderer::getPositionColorShader);
		RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
		BufferUploader.drawWithShader(bufferbuilder.buildOrThrow());
		GlStateManager._disableBlend();
	}
	
	public static void fillDropArea(GuiGraphics graphics, int x, int y, int width, int height, int color, boolean drop) {
		PoseStack stack = graphics.pose();
		Tesselator tessellator = Tesselator.getInstance();
		BufferBuilder builder = tessellator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
		int minX = x;
		int minY = y;
		int maxX = x + width;
		int maxY = y + height;
		if(drop) {
			drawQuadArea(stack, minX - 1, minY - 1, maxX, maxY, builder, -13158601);
			drawQuadArea(stack, minX, minY, maxX + 1, maxY + 1, builder, -1);
		}
		else {
			drawQuadArea(stack, minX, minY, maxX + 1, maxY + 1, builder, -13158601);
			drawQuadArea(stack, minX - 1, minY - 1, maxX, maxY, builder, -1);
		}
		drawQuadArea(stack, minX, minY, maxX, maxY, builder, color);
		GlStateManager._enableBlend();
		RenderSystem.setShader(GameRenderer::getPositionColorShader);
		RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
		BufferUploader.drawWithShader(builder.buildOrThrow());
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
			builder.addVertex(left, bottom, 0.0F).setColor(f, f1, f2, f3);
			builder.addVertex(right, bottom, 0.0F).setColor(f, f1, f2, f3);
			builder.addVertex(right, top, 0.0F).setColor(f, f1, f2, f3);
			builder.addVertex(left, top, 0.0F).setColor(f, f1, f2, f3);
			return;
		}
		Matrix4f stack = matrix.last().pose();
		builder.addVertex(stack, left, bottom, 0.0F).setColor(f, f1, f2, f3);
		builder.addVertex(stack, right, bottom, 0.0F).setColor(f, f1, f2, f3);
		builder.addVertex(stack, right, top, 0.0F).setColor(f, f1, f2, f3);
		builder.addVertex(stack, left, top, 0.0F).setColor(f, f1, f2, f3);
	}
	
	public static void drawCircle(GuiGraphics graphics, float x, float y, float radius, int color, int borderColor, int segments, float borderWidth) {
		Tesselator tes = Tesselator.getInstance();
		BufferBuilder builder = tes.begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR);
		float a = (color >> 24 & 255) / 255F;
		float r = (color >> 16 & 255) / 255F;
		float g = (color >> 8 & 255) / 255F;
		float b = (color & 255) / 255F;
		float innerRadius = radius - Math.max(0, borderWidth);
		if (innerRadius < 0) innerRadius = 0F;
		Matrix4f matrix = graphics == null ? null : graphics.pose().last().pose();
	    float spaceScale = (360.0F / segments) * Mth.DEG_TO_RAD;
		for (int i = 0; i < segments; i++) {
			float startAngle = i * spaceScale;
			float endAngle = ((i+1) % segments) * spaceScale;
			if(matrix != null) {
				builder.addVertex(matrix, x, y, 0).setColor(r, g, b, a);
				builder.addVertex(matrix, (float)(x + Math.cos(endAngle) * innerRadius), (float)(y + Math.sin(endAngle) * innerRadius), 0).setColor(r, g, b, a);
				builder.addVertex(matrix, (float)(x + Math.cos(startAngle) * innerRadius), (float)(y + Math.sin(startAngle) * innerRadius), 0).setColor(r, g, b, a);
				continue;
			}
			builder.addVertex(x, y, 0).setColor(r, g, b, a);
			builder.addVertex(x + (float)Math.cos(endAngle) * innerRadius, y + (float)Math.sin(endAngle) * innerRadius, 0).setColor(r, g, b, a);
			builder.addVertex(x + (float)Math.cos(startAngle) * innerRadius, y + (float)Math.sin(startAngle) * innerRadius, 0).setColor(r, g, b, a);
		}
		if (borderWidth > 0F) {
			float ba = (borderColor >> 24 & 255) / 255F;
			float br = (borderColor >> 16 & 255) / 255F;
			float bg = (borderColor >> 8 & 255) / 255F;
			float bb = (borderColor & 255) / 255F;
			
			for (int i = 0; i < segments; i++) {
				float startAngle = i * spaceScale;
				float endAngle = ((i+1) % segments) * spaceScale;
				
				float x1o = x + Mth.cos(startAngle) * radius;
				float y1o = y + Mth.sin(startAngle) * radius;
				float x2o = x + Mth.cos(endAngle) * radius;
				float y2o = y + Mth.sin(endAngle) * radius;
				
				float x1i = x + Mth.cos(startAngle) * innerRadius;
				float y1i = y + Mth.sin(startAngle) * innerRadius;
				float x2i = x + Mth.cos(endAngle) * innerRadius;
				float y2i = y + Mth.sin(endAngle) * innerRadius;
				
				if(matrix != null) {
					builder.addVertex(matrix, x2o, y2o, 0).setColor(br, bg, bb, ba);
					builder.addVertex(matrix, x1o, y1o, 0).setColor(br, bg, bb, ba);
					builder.addVertex(matrix, x1i, y1i, 0).setColor(br, bg, bb, ba);
					
					builder.addVertex(matrix, x2o, y2o, 0).setColor(br, bg, bb, ba);
					builder.addVertex(matrix, x1i, y1i, 0).setColor(br, bg, bb, ba);
					builder.addVertex(matrix, x2i, y2i, 0).setColor(br, bg, bb, ba);
					continue;
				}
				builder.addVertex(x2o, y2o, 0).setColor(br, bg, bb, ba);
				builder.addVertex(x1o, y1o, 0).setColor(br, bg, bb, ba);
				builder.addVertex(x1i, y1i, 0).setColor(br, bg, bb, ba);
				
				builder.addVertex(x2o, y2o, 0).setColor(br, bg, bb, ba);
				builder.addVertex(x1i, y1i, 0).setColor(br, bg, bb, ba);
				builder.addVertex(x2i, y2i, 0).setColor(br, bg, bb, ba);
				
			}
		}
		RenderSystem.setShader(GameRenderer::getPositionColorShader);
		GlStateManager._enableBlend();
		RenderSystem.blendFuncSeparate(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA, SourceFactor.ONE, DestFactor.ZERO);
		BufferUploader.drawWithShader(builder.buildOrThrow());
		GlStateManager._disableBlend();
	}
	
	public static void drawLine(PoseStack stack, float startX, float startY, float endX, float endY, float width, VertexConsumer builder, int color) {
		float f3 = (float)(color >> 24 & 255) / 255.0F;
		float f = (float)(color >> 16 & 255) / 255.0F;
		float f1 = (float)(color >> 8 & 255) / 255.0F;
		float f2 = (float)(color & 255) / 255.0F;
		float dx = endX - startX;
		float dy = endY - startY;
		float length = Mth.sqrt(dx*dx + dy*dy);
		dx /= length;
		dy /= length;
		
		float px = -dy * (width * 0.5F);
		float py = dx * (width * 0.5F);
		
		float x1 = startX + px;
		float y1 = startY + py;

		float x2 = startX - px;
		float y2 = startY - py;

		float x3 = endX + px;
		float y3 = endY + py;

		float x4 = endX - px;
		float y4 = endY - py;
		
		if(stack == null) {
			builder.addVertex(x3, y3, 0.0F).setColor(f, f1, f2, f3); 
			builder.addVertex(x2, y2, 0.0F).setColor(f, f1, f2, f3);
			builder.addVertex(x1, y1, 0.0F).setColor(f, f1, f2, f3);
			builder.addVertex(x4, y4, 0.0F).setColor(f, f1, f2, f3);
			builder.addVertex(x2, y2, 0.0F).setColor(f, f1, f2, f3); 
			builder.addVertex(x3, y3, 0.0F).setColor(f, f1, f2, f3);
			return;
		}
		Matrix4f matrix = stack.last().pose();
		builder.addVertex(matrix, x3, y3, 0.0F).setColor(f, f1, f2, f3); 
		builder.addVertex(matrix, x2, y2, 0.0F).setColor(f, f1, f2, f3);
		builder.addVertex(matrix, x1, y1, 0.0F).setColor(f, f1, f2, f3);
		builder.addVertex(matrix, x4, y4, 0.0F).setColor(f, f1, f2, f3);
		builder.addVertex(matrix, x2, y2, 0.0F).setColor(f, f1, f2, f3); 
		builder.addVertex(matrix, x3, y3, 0.0F).setColor(f, f1, f2, f3);
	}
	
	public static void blitWithBorder(GuiGraphics graphics, ResourceLocation res, int x, int y, int u, int v, int width, int height, int textureWidth, int textureHeight, int topBorder, int bottomBorder, int leftBorder, int rightBorder, boolean custom) {
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
		
		graphics.blit(res, x, y, u, v, leftBorder, topBorder);
		graphics.blit(res, x + leftBorder + canvasWidth, y, u + leftBorder + fillerWidth, v, rightBorder, topBorder);
		graphics.blit(res, x, y + topBorder + canvasHeight, u, v + topBorder + fillerHeight, leftBorder, bottomBorder);
		graphics.blit(res, x + leftBorder + canvasWidth, y + topBorder + canvasHeight, u + leftBorder + fillerWidth, v + topBorder + fillerHeight, rightBorder, bottomBorder);
		for (int i = 0; i < xPasses + (remainderWidth > 0 ? 1 : 0); i++) {
			graphics.blit(res, x + leftBorder + (i * fillerWidth), y, u + leftBorder, v, (i == xPasses ? remainderWidth : fillerWidth), topBorder);
			graphics.blit(res, x + leftBorder + (i * fillerWidth), y + topBorder + canvasHeight, u + leftBorder, v + topBorder + fillerHeight, (i == xPasses ? remainderWidth : fillerWidth), bottomBorder);

			for (int j = 0; j < yPasses + (remainderHeight > 0 ? 1 : 0); j++) {
				graphics.blit(res, x + leftBorder + (i * fillerWidth), y + topBorder + (j * fillerHeight), u + leftBorder, v + topBorder, (i == xPasses ? remainderWidth : fillerWidth), (j == yPasses ? remainderHeight : fillerHeight));
			}
		}

		for (int j = 0; j < yPasses + (remainderHeight > 0 ? 1 : 0); j++) {
			graphics.blit(res, x, y + topBorder + (j * fillerHeight), u, v + topBorder, leftBorder, (j == yPasses ? remainderHeight : fillerHeight));
			graphics.blit(res, x + leftBorder + canvasWidth, y + topBorder + (j * fillerHeight), u + leftBorder + fillerWidth, v + topBorder, rightBorder, (j == yPasses ? remainderHeight : fillerHeight));
		}
	}
}
