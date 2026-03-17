package carbonconfiglib.gui.base.helpers;

import java.util.ArrayDeque;
import java.util.Deque;

import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.GlStateManager.DestFactor;
import com.mojang.blaze3d.platform.GlStateManager.SourceFactor;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.IVertexConsumer;

import carbonconfiglib.gui.api.background.BackgroundTexture;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.IReorderingProcessor;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.text.ITextComponent;

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
	private static final float U_SCALE = 1F / 0x100;
	private static final float V_SCALE = 1F / 0x100;
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
	
	public static float calculateScrollOffset(float width, FontRenderer font, Align align, ITextComponent text, long startTime) {
		int textWidth = font.width(text);
		if(textWidth > width) {
			float diff = textWidth - width + 2F;
			double timer = (currentMillseconds() - startTime) / 1000D;
			double minDiff = Math.max(diff * 0.5D, 3.0D);
			double offset = Math.sin((Math.PI / 2D) * Math.cos(((Math.PI * 2D) * timer) / minDiff)) / 2D + 0.01F + align.alignCenter();
			return (float)MathHelper.lerp(offset, 0D, diff);
		}
		return 0;
	}
	
	public static void drawText(MatrixStack stack, FontRenderer font, ITextComponent text, float x, float y, Align align, int color) {
		float offset = align.align(font.width(text));
		font.draw(stack, text, x + offset, y, color);
	}
	
	public static void drawSplitText(MatrixStack stack, FontRenderer font, ITextComponent text, float x, float y, Align align, int color, int maxLength) {
		drawSplitText(stack, font, text, x, y, align, color, maxLength, font.lineHeight);
	}
	
	public static void drawSplitText(MatrixStack stack, FontRenderer font, ITextComponent text, float x, float y, Align align, int color, int maxLength, float lineSplit) {
		for(IReorderingProcessor line : font.split(text, maxLength)) {
			float offset = align.align(font.width(line));
			font.draw(stack, line, x + offset, y, color);
			y += lineSplit;
		}
	}
	
	public static void drawScrollingText(MatrixStack stack, FontRenderer font, ITextComponent text, float x, float y, float width, float height, Align align, int color, long startTime) {
		int textWidth = font.width(text);
		if(textWidth > width) {
			float diff = textWidth - width + 2F;
			double timer = (currentMillseconds() - startTime) / 1000D;
			double minDiff = Math.max(diff * 0.5D, 3.0D);
			double offset = Math.sin((Math.PI / 2D) * Math.cos(((Math.PI * 2D) * timer) / minDiff)) / 2D + 0.01F + align.alignCenter();
			pushScissors((int)x, (int)y, (int)width, (int)height);
			font.draw(stack, text, x - align.align(width) + align.align(textWidth) + (float)MathHelper.lerp(offset, 0D, diff), y + (height * 0.5F) - (font.lineHeight * 0.5F), color);
			popScissors();
			return;
		}
		float offset = align.align(textWidth);
		font.draw(stack, text, x - align.align(width) + offset, y + (height * 0.5F) - (font.lineHeight * 0.5F), color);
	}
	
	public static void drawScrollingShadowText(MatrixStack stack, FontRenderer font, ITextComponent text, float x, float y, float width, float height, Align align, int color, long startTime) {
		int textWidth = font.width(text);
		if(textWidth > width) {
			float diff = textWidth - width + 2F;
			double timer = (currentMillseconds() - startTime) / 1000D;
			double minDiff = Math.max(diff * 0.5D, 3.0D);
			double offset = Math.sin((Math.PI / 2D) * Math.cos(((Math.PI * 2D) * timer) / minDiff)) / 2D + 0.01F + align.alignCenter();
			pushScissors((int)x, (int)y, (int)width, (int)height);
			font.drawShadow(stack, text, x - align.align(width) + align.align(textWidth) + (float)MathHelper.lerp(offset, 0D, diff), y + (height * 0.5F) - (font.lineHeight * 0.5F), color);
			popScissors();
			return;
		}
		float offset = align.align(textWidth);
		font.drawShadow(stack, text, x - align.align(width) + offset, y + (height * 0.5F) - (font.lineHeight * 0.5F), color);
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
		MainWindow window = Minecraft.getInstance().getWindow();
		int bottom = rect.maxY;
		double scaledHeight = (double)window.getHeight() / (double)window.getGuiScaledHeight();
		double scaledWidth = (double)window.getWidth() / (double)window.getGuiScaledWidth();
		RenderSystem.enableScissor((int)(rect.getX() * scaledWidth), (int)(window.getHeight() - bottom * scaledHeight), (int)(rect.getWidth() * scaledWidth), (int)(rect.getHeigth() * scaledHeight));
	}
	
	public static void drawTextureRegion(MatrixStack stack, float x, float y, float width, float height, Icon icon, float texWidth, float texHeight) {
		Minecraft.getInstance().getTextureManager().bind(icon.getTexture());
		drawTextureRegion(stack, x, y, icon.getX(), icon.getY(), width, height, texWidth, texHeight, icon.getSheetWidth(), icon.getSheetHeight());
	}
	
	public static void drawTextureRegion(MatrixStack stack, float x, float y, int xOff, int yOff, float width, float height, Icon icon, float texWidth, float texHeight) {
		Minecraft.getInstance().getTextureManager().bind(icon.getTexture());
		drawTextureRegion(stack, x, y, icon.getX() + xOff, icon.getY() + yOff, width, height, texWidth, texHeight, icon.getSheetWidth(), icon.getSheetHeight());
	}
    
	public static void drawTextureRegion(MatrixStack stack, float x, float y, float texX, float texY, float width, float height, float texWidth, float texHeight, float textureWidth, float textureHeight) {
		Matrix4f matrix = stack.last().pose();
		float maxX = x + width;
		float maxY = y + height;
		float t_minX = texX / textureWidth;
		float t_minY = texY / textureHeight;
		float t_maxX = (texX + texWidth) / textureWidth;
		float t_maxY = (texY + texHeight) / textureHeight;
		
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder bufferbuilder = tessellator.getBuilder();
		bufferbuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
		bufferbuilder.vertex(matrix, x, maxY, 0).uv(t_minX, t_maxY).endVertex();
		bufferbuilder.vertex(matrix, maxX, maxY, 0).uv(t_maxX, t_maxY).endVertex();
		bufferbuilder.vertex(matrix, maxX, y, 0).uv(t_maxX, t_minY).endVertex();
		bufferbuilder.vertex(matrix, x, y, 0).uv(t_minX, t_minY).endVertex();
		
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
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
	
	@SuppressWarnings("deprecation")
	public static void renderListOverlay(int x0, int x1, int y0, int y1, int width, int height, BackgroundTexture texture) {
		Tessellator tes = Tessellator.getInstance();
		BufferBuilder builder = tes.getBuilder();
		Minecraft.getInstance().getTextureManager().bind(texture.getForegroundTexture());
		RenderSystem.enableDepthTest();
		RenderSystem.depthFunc(519);
		int color = texture.getForegroundBrightness();
		builder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
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
		Tessellator tes = Tessellator.getInstance();
		BufferBuilder builder = tes.getBuilder();
		RenderSystem.depthFunc(515);
		RenderSystem.disableDepthTest();
		RenderSystem.enableBlend();
		RenderSystem.blendFuncSeparate(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA, SourceFactor.ZERO, DestFactor.ONE);
		RenderSystem.disableTexture();
		builder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
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
		renderBackground(x0, x1, y0, y1, 0F, scroll, texture);
	}
	
	@SuppressWarnings("deprecation")
	public static void renderBackground(int x0, int x1, int y0, int y1, float xScroll, float yScroll, BackgroundTexture texture) {
		Tessellator tes = Tessellator.getInstance();
		BufferBuilder builder = tes.getBuilder();
		Minecraft.getInstance().getTextureManager().bind(texture.getForegroundTexture());
		RenderSystem.color4f(1F, 1F, 1F, 1F);
		int color = texture.getBackgroundBrightness();
		builder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
		builder.vertex(x0, y1, 0D).uv((x0 + xScroll) / 32F, (y1 + yScroll) / 32F).color(color, color, color, 255).endVertex();
		builder.vertex(x1, y1, 0D).uv((x1 + xScroll) / 32F, (y1 + yScroll) / 32F).color(color, color, color, 255).endVertex();
		builder.vertex(x1, y0, 0D).uv((x1 + xScroll) / 32F, (y0 + yScroll) / 32F).color(color, color, color, 255).endVertex();
		builder.vertex(x0, y0, 0D).uv((x0 + xScroll) / 32F, (y0 + yScroll) / 32F).color(color, color, color, 255).endVertex();
		tes.end();
	}
	
	public static void drawFrame(MatrixStack stack, float minX, float minY, float maxX, float maxY, int color, float width) {
		Tessellator tes = Tessellator.getInstance();
		BufferBuilder builder = tes.getBuilder();
		builder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
		drawQuadArea(stack, minX, minY, maxX, minY+width, builder, color);
		drawQuadArea(stack, minX, maxY, maxX, maxY+width, builder, color);
		drawQuadArea(stack, minX, minY, minX+width, maxY, builder, color);
		drawQuadArea(stack, maxX, minY, maxX+width, maxY+width, builder, color);
		GlStateManager._enableBlend();
		GlStateManager._disableTexture();
		RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
		tes.end();
		GlStateManager._enableTexture();
		GlStateManager._disableBlend();
	}
	
	public static void fillDropArea(MatrixStack stack, int x, int y, int width, int height, int color, boolean drop) {
		Tessellator tes = Tessellator.getInstance();
		BufferBuilder builder = tes.getBuilder();
		builder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
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
		GlStateManager._disableTexture();
		RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
		tes.end();
		GlStateManager._enableTexture();
		GlStateManager._disableBlend();
	}
	
	public static void drawQuadArea(MatrixStack matrix, float left, float top, float right, float bottom, IVertexConsumer builder, int color) {
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
	
	public static void drawCircle(MatrixStack stack, float x, float y, float radius, int color, int borderColor, int segments, float borderWidth) {
		Tessellator tes = Tessellator.getInstance();
		BufferBuilder builder = tes.getBuilder();
		builder.begin(GL11.GL_TRIANGLES, DefaultVertexFormats.POSITION_COLOR);
		float a = (color >> 24 & 255) / 255F;
		float r = (color >> 16 & 255) / 255F;
		float g = (color >> 8 & 255) / 255F;
		float b = (color & 255) / 255F;
		float innerRadius = radius - Math.max(0, borderWidth);
		if (innerRadius < 0) innerRadius = 0F;
		Matrix4f matrix = stack == null ? null : stack.last().pose();
	    float spaceScale = (360.0F / segments) * (float)Math.PI / 180F;
		for (int i = 0; i < segments; i++) {
			float startAngle = i * spaceScale;
			float endAngle = ((i+1) % segments) * spaceScale;
			if(matrix != null) {
				builder.vertex(matrix, x, y, 0).color(r, g, b, a).endVertex();
				builder.vertex(matrix, (float)(x + Math.cos(endAngle) * innerRadius), (float)(y + Math.sin(endAngle) * innerRadius), 0).color(r, g, b, a).endVertex();
				builder.vertex(matrix, (float)(x + Math.cos(startAngle) * innerRadius), (float)(y + Math.sin(startAngle) * innerRadius), 0).color(r, g, b, a).endVertex();
				continue;
			}
			builder.vertex(x, y, 0).color(r, g, b, a).endVertex();
			builder.vertex(x + Math.cos(endAngle) * innerRadius, y + Math.sin(endAngle) * innerRadius, 0).color(r, g, b, a).endVertex();
			builder.vertex(x + Math.cos(startAngle) * innerRadius, y + Math.sin(startAngle) * innerRadius, 0).color(r, g, b, a).endVertex();
		}
		if (borderWidth > 0F) {
			float ba = (borderColor >> 24 & 255) / 255F;
			float br = (borderColor >> 16 & 255) / 255F;
			float bg = (borderColor >> 8 & 255) / 255F;
			float bb = (borderColor & 255) / 255F;
			
			for (int i = 0; i < segments; i++) {
				float startAngle = i * spaceScale;
				float endAngle = ((i+1) % segments) * spaceScale;
				
				float x1o = x + MathHelper.cos(startAngle) * radius;
				float y1o = y + MathHelper.sin(startAngle) * radius;
				float x2o = x + MathHelper.cos(endAngle) * radius;
				float y2o = y + MathHelper.sin(endAngle) * radius;
				
				float x1i = x + MathHelper.cos(startAngle) * innerRadius;
				float y1i = y + MathHelper.sin(startAngle) * innerRadius;
				float x2i = x + MathHelper.cos(endAngle) * innerRadius;
				float y2i = y + MathHelper.sin(endAngle) * innerRadius;
				
				if(matrix != null) {
					builder.vertex(matrix, x2o, y2o, 0).color(br, bg, bb, ba).endVertex();
					builder.vertex(matrix, x1o, y1o, 0).color(br, bg, bb, ba).endVertex();
					builder.vertex(matrix, x1i, y1i, 0).color(br, bg, bb, ba).endVertex();
					
					builder.vertex(matrix, x2o, y2o, 0).color(br, bg, bb, ba).endVertex();
					builder.vertex(matrix, x1i, y1i, 0).color(br, bg, bb, ba).endVertex();
					builder.vertex(matrix, x2i, y2i, 0).color(br, bg, bb, ba).endVertex();
					continue;
				}
				builder.vertex(x2o, y2o, 0).color(br, bg, bb, ba).endVertex();
				builder.vertex(x1o, y1o, 0).color(br, bg, bb, ba).endVertex();
				builder.vertex(x1i, y1i, 0).color(br, bg, bb, ba).endVertex();
				
				builder.vertex(x2o, y2o, 0).color(br, bg, bb, ba).endVertex();
				builder.vertex(x1i, y1i, 0).color(br, bg, bb, ba).endVertex();
				builder.vertex(x2i, y2i, 0).color(br, bg, bb, ba).endVertex();
				
			}
		}
		GlStateManager._disableTexture();
		GlStateManager._enableBlend();
		RenderSystem.blendFuncSeparate(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA, SourceFactor.ONE, DestFactor.ZERO);
		tes.end();
		GlStateManager._enableTexture();
		GlStateManager._disableBlend();
	}
	
	public static void drawLine(MatrixStack stack, float startX, float startY, float endX, float endY, float width, IVertexConsumer builder, int color) {
		float f3 = (float)(color >> 24 & 255) / 255.0F;
		float f = (float)(color >> 16 & 255) / 255.0F;
		float f1 = (float)(color >> 8 & 255) / 255.0F;
		float f2 = (float)(color & 255) / 255.0F;
		float dx = endX - startX;
		float dy = endY - startY;
		float length = MathHelper.sqrt(dx*dx + dy*dy);
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
			builder.vertex(x3, y3, 0.0F).color(f, f1, f2, f3).endVertex(); 
			builder.vertex(x2, y2, 0.0F).color(f, f1, f2, f3).endVertex();
			builder.vertex(x1, y1, 0.0F).color(f, f1, f2, f3).endVertex();
			builder.vertex(x4, y4, 0.0F).color(f, f1, f2, f3).endVertex();
			builder.vertex(x2, y2, 0.0F).color(f, f1, f2, f3).endVertex(); 
			builder.vertex(x3, y3, 0.0F).color(f, f1, f2, f3).endVertex();
			return;
		}
		Matrix4f matrix = stack.last().pose();
		builder.vertex(matrix, x3, y3, 0.0F).color(f, f1, f2, f3).endVertex(); 
		builder.vertex(matrix, x2, y2, 0.0F).color(f, f1, f2, f3).endVertex();
		builder.vertex(matrix, x1, y1, 0.0F).color(f, f1, f2, f3).endVertex();
		builder.vertex(matrix, x4, y4, 0.0F).color(f, f1, f2, f3).endVertex();
		builder.vertex(matrix, x2, y2, 0.0F).color(f, f1, f2, f3).endVertex(); 
		builder.vertex(matrix, x3, y3, 0.0F).color(f, f1, f2, f3).endVertex();
	}
	
	public static void blitWithBorder(MatrixStack poseStack, ResourceLocation res, int x, int y, int u, int v, int width, int height, int textureWidth, int textureHeight, int topBorder, int bottomBorder, int leftBorder, int rightBorder, float zLevel, boolean custom) {
		Minecraft.getInstance().getTextureManager().bind(res);
		blitWithBorder(poseStack, x, y, u, v, width, height, textureWidth, textureHeight, topBorder, bottomBorder, leftBorder, rightBorder, zLevel, custom);
	}
	
	@SuppressWarnings("deprecation")
	public static void blitWithBorder(MatrixStack poseStack, int x, int y, int u, int v, int width, int height, int textureWidth, int textureHeight, int topBorder, int bottomBorder, int leftBorder, int rightBorder, float zLevel, boolean custom) {
		if(!custom) {
			RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
			RenderSystem.enableBlend();
			RenderSystem.defaultBlendFunc();
		}
		
		Tessellator tes = Tessellator.getInstance();
		BufferBuilder builder = tes.getBuilder();
		builder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
		Matrix4f matrix = poseStack.last().pose();
		
		int fillerWidth = textureWidth - leftBorder - rightBorder;
		int fillerHeight = textureHeight - topBorder - bottomBorder;
		int canvasWidth = width - leftBorder - rightBorder;
		int canvasHeight = height - topBorder - bottomBorder;
		int xPasses = canvasWidth / fillerWidth;
		int remainderWidth = canvasWidth % fillerWidth;
		int yPasses = canvasHeight / fillerHeight;
		int remainderHeight = canvasHeight % fillerHeight;

		drawTextured(poseStack, x, y, u, v, leftBorder, topBorder, zLevel, builder, matrix);
		drawTextured(poseStack, x + leftBorder + canvasWidth, y, u + leftBorder + fillerWidth, v, rightBorder, topBorder, zLevel, builder, matrix);
		drawTextured(poseStack, x, y + topBorder + canvasHeight, u, v + topBorder + fillerHeight, leftBorder, bottomBorder, zLevel, builder, matrix);
		drawTextured(poseStack, x + leftBorder + canvasWidth, y + topBorder + canvasHeight, u + leftBorder + fillerWidth, v + topBorder + fillerHeight, rightBorder, bottomBorder, zLevel, builder, matrix);

		for (int i = 0; i < xPasses + (remainderWidth > 0 ? 1 : 0); i++) {
			drawTextured(poseStack, x + leftBorder + (i * fillerWidth), y, u + leftBorder, v, (i == xPasses ? remainderWidth : fillerWidth), topBorder, zLevel, builder, matrix);
			drawTextured(poseStack, x + leftBorder + (i * fillerWidth), y + topBorder + canvasHeight, u + leftBorder, v + topBorder + fillerHeight, (i == xPasses ? remainderWidth : fillerWidth), bottomBorder, zLevel, builder, matrix);
			for (int j = 0; j < yPasses + (remainderHeight > 0 ? 1 : 0); j++)
				drawTextured(poseStack, x + leftBorder + (i * fillerWidth), y + topBorder + (j * fillerHeight), u + leftBorder, v + topBorder, (i == xPasses ? remainderWidth : fillerWidth), (j == yPasses ? remainderHeight : fillerHeight), zLevel, builder, matrix);
		}

		for (int j = 0; j < yPasses + (remainderHeight > 0 ? 1 : 0); j++) {
			drawTextured(poseStack, x, y + topBorder + (j * fillerHeight), u, v + topBorder, leftBorder, (j == yPasses ? remainderHeight : fillerHeight), zLevel, builder, matrix);
			drawTextured(poseStack, x + leftBorder + canvasWidth, y + topBorder + (j * fillerHeight), u + leftBorder + fillerWidth, v + topBorder, rightBorder, (j == yPasses ? remainderHeight : fillerHeight), zLevel, builder, matrix);
		}
		tes.end();
	}
	
	private static void drawTextured(MatrixStack poseStack, int x, int y, int u, int v, int width, int height, float zLevel, BufferBuilder builder, Matrix4f matrix) {
		builder.vertex(matrix, x, y + height, zLevel).uv(u * U_SCALE, (v + height) * V_SCALE).endVertex();
		builder.vertex(matrix, x + width, y + height, zLevel).uv((u + width) * U_SCALE, (v + height) * V_SCALE).endVertex();
		builder.vertex(matrix, x + width, y, zLevel).uv((u + width) * U_SCALE, v * V_SCALE).endVertex();
		builder.vertex(matrix, x, y, zLevel).uv(u * U_SCALE, v * V_SCALE).endVertex();
	}
}
