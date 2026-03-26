package carbonconfiglib.gui.base.helpers;

import java.util.ArrayDeque;
import java.util.Deque;

import org.lwjgl.opengl.GL11;

import carbonconfiglib.gui.api.background.BackgroundTexture;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
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
		int textWidth = font.getStringWidth(text.getFormattedText());
		if(textWidth > width) {
			float diff = textWidth - width + 2F;
			double timer = (currentMillseconds() - startTime) / 1000D;
			double minDiff = Math.max(diff * 0.5D, 3.0D);
			double offset = Math.sin((Math.PI / 2D) * Math.cos(((Math.PI * 2D) * timer) / minDiff)) / 2D + 0.01F + align.alignCenter();
			return (float)lerp(offset, 0D, diff);
		}
		return 0;
	}
	
	public static void drawText(FontRenderer font, ITextComponent comp, float x, float y, Align align, int color) {
		drawText(font, comp.getFormattedText(), x, y, align, color);
	}
		
	public static void drawText(FontRenderer font, String text, float x, float y, Align align, int color) {
		GlStateManager.enableTexture2D();
		float offset = align.align(font.getStringWidth(text));
		font.drawString(text, x + offset, y, color, false);
	}
	
	public static void drawSplitText(FontRenderer font, ITextComponent text, float x, float y, Align align, int color, int maxLength) {
		drawSplitText(font, text.getFormattedText(), x, y, align, color, maxLength, font.FONT_HEIGHT);
	}
	
	public static void drawSplitText(FontRenderer font, String text, float x, float y, Align align, int color, int maxLength) {
		drawSplitText(font, text, x, y, align, color, maxLength, font.FONT_HEIGHT);
	}
	
	public static void drawSplitText(FontRenderer font, ITextComponent text, float x, float y, Align align, int color, int maxLength, float lineSplit) {
		drawSplitText(font, text.getFormattedText(), x, y, align, color, maxLength, lineSplit);
	}
	
	public static void drawSplitText(FontRenderer font, String text, float x, float y, Align align, int color, int maxLength, float lineSplit) {
		GlStateManager.enableTexture2D();
		for(String subLine : font.listFormattedStringToWidth(text.replace("\\n", "\n"), maxLength)) {
			float offset = align.align(font.getStringWidth(subLine));
			font.drawString(subLine, x + offset, y, color, false);
			y += lineSplit;
		}	
	}
	
	public static void drawScrollingText(FontRenderer font, ITextComponent comp, float x, float y, float width, float height, Align align, int color, long startTime) {
		drawScrollingText(font, comp.getFormattedText(), x, y, width, height, align, color, startTime);
	}
	
	public static void drawScrollingText(FontRenderer font, String text, float x, float y, float width, float height, Align align, int color, long startTime) {
		GlStateManager.enableTexture2D();
		int textWidth = font.getStringWidth(text);
		if(textWidth > width) {
			float diff = textWidth - width + 2F;
			double timer = (currentMillseconds() - startTime) / 1000D;
			double minDiff = Math.max(diff * 0.5D, 3.0D);
			double offset = Math.sin((Math.PI / 2D) * Math.cos(((Math.PI * 2D) * timer) / minDiff)) / 2D + 0.01F + align.alignCenter();
			pushScissors((int)x, (int)y, (int)width, (int)height);
			font.drawString(text, x - align.align(width) + align.align(textWidth) + (float)lerp(offset, 0D, diff), y + (height * 0.5F) - (font.FONT_HEIGHT * 0.5F), color, false);
			popScissors();
			return;
		}
		float offset = align.align(textWidth);
		font.drawString(text, x - align.align(width) + offset, y + (height * 0.5F) - (font.FONT_HEIGHT * 0.5F), color, false);
	}
	
	public static void drawScrollingShadowText(FontRenderer font, ITextComponent comp, float x, float y, float width, float height, Align align, int color, long startTime) {
		drawScrollingShadowText(font, comp.getFormattedText(), x, y, width, height, align, color, startTime);
	}
	
	public static void drawScrollingShadowText(FontRenderer font, String text, float x, float y, float width, float height, Align align, int color, long startTime) {
		GlStateManager.enableTexture2D();
		int textWidth = font.getStringWidth(text);
		if(textWidth > width) {
			float diff = textWidth - width + 2F;
			double timer = (currentMillseconds() - startTime) / 1000D;
			double minDiff = Math.max(diff * 0.5D, 3.0D);
			double offset = Math.sin((Math.PI / 2D) * Math.cos(((Math.PI * 2D) * timer) / minDiff)) / 2D + 0.01F + align.alignCenter();
			pushScissors((int)x, (int)y, (int)width, (int)height);
			font.drawStringWithShadow(text, x - align.align(width) + align.align(textWidth) + (float)lerp(offset, 0D, diff), y + (height * 0.5F) - (font.FONT_HEIGHT * 0.5F), color);
			popScissors();
			return;
		}
		float offset = align.align(textWidth);
		font.drawStringWithShadow(text, x - align.align(width) + offset, y + (height * 0.5F) - (font.FONT_HEIGHT * 0.5F), color);
	}
	
	private static double lerp(double value, double min, double max) {
		return min + value * (max - min);
	}
	
	public static long currentMillseconds() {
		return System.currentTimeMillis();
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
			GL11.glDisable(GL11.GL_SCISSOR_TEST);
			return;
		}
		Minecraft mc = Minecraft.getMinecraft();
		ScaledResolution res = new ScaledResolution(mc);
		int bottom = rect.maxY;
		double scaledHeight = (double)mc.displayHeight / (double)res.getScaledHeight_double();
		double scaledWidth = (double)mc.displayWidth / (double)res.getScaledWidth_double();
		GL11.glEnable(GL11.GL_SCISSOR_TEST);
		GL11.glScissor((int)(rect.getX() * scaledWidth), (int)(mc.displayHeight - bottom * scaledHeight), (int)(rect.getWidth() * scaledWidth), (int)(rect.getHeigth() * scaledHeight));
	}
	
	public static void drawTextureRegion(float x, float y, float width, float height, Icon icon, float texWidth, float texHeight) {
		Minecraft.getMinecraft().getTextureManager().bindTexture(icon.getTexture());
		drawTextureRegion(x, y, icon.getX(), icon.getY(), width, height, texWidth, texHeight, icon.getSheetWidth(), icon.getSheetHeight());
	}
	
	public static void drawTextureRegion(float x, float y, int xOff, int yOff, float width, float height, Icon icon, float texWidth, float texHeight) {
		Minecraft.getMinecraft().getTextureManager().bindTexture(icon.getTexture());
		drawTextureRegion(x, y, icon.getX() + xOff, icon.getY() + yOff, width, height, texWidth, texHeight, icon.getSheetWidth(), icon.getSheetHeight());
	}
    
	public static void drawTextureRegion(float x, float y, float texX, float texY, float width, float height, float texWidth, float texHeight, float textureWidth, float textureHeight) {
		float maxX = x + width;
		float maxY = y + height;
		float t_minX = texX / textureWidth;
		float t_minY = texY / textureHeight;
		float t_maxX = (texX + texWidth) / textureWidth;
		float t_maxY = (texY + texHeight) / textureHeight;
		
		Tessellator tessellator = Tessellator.getInstance();
		VertexBuffer bufferbuilder = tessellator.getBuffer();
		bufferbuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
		bufferbuilder.pos(x, maxY, 0).tex(t_minX, t_maxY).endVertex();
		bufferbuilder.pos(maxX, maxY, 0).tex(t_maxX, t_maxY).endVertex();
		bufferbuilder.pos(maxX, y, 0).tex(t_maxX, t_minY).endVertex();
		bufferbuilder.pos(x, y, 0).tex(t_minX, t_minY).endVertex();
		
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
		tessellator.draw();
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
		Tessellator tes = Tessellator.getInstance();
		VertexBuffer builder = tes.getBuffer();
		Minecraft.getMinecraft().getTextureManager().bindTexture(texture.getForegroundTexture());
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
	}
	
	public static void renderListShadow(int x0, int x1, int y0, int y1, int width, int height) {
		Tessellator tes = Tessellator.getInstance();
		VertexBuffer builder = tes.getBuffer();
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
	}
	
	public static void renderBackground(int x0, int x1, int y0, int y1, float scroll, BackgroundTexture texture) {
		renderBackground(x0, x1, y0, y1, 0F, scroll, texture);
	}
	
	public static void renderBackground(int x0, int x1, int y0, int y1, float xScroll, float yScroll, BackgroundTexture texture) {
		Tessellator tes = Tessellator.getInstance();
		VertexBuffer builder = tes.getBuffer();
		Minecraft.getMinecraft().getTextureManager().bindTexture(texture.getForegroundTexture());
		GlStateManager.color(1F, 1F, 1F, 1F);
		int color = texture.getBackgroundBrightness();
		builder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
		builder.pos(x0, y1, 0D).tex((x0 + xScroll) / 32F, (y1 + yScroll) / 32F).color(color, color, color, 255).endVertex();
		builder.pos(x1, y1, 0D).tex((x1 + xScroll) / 32F, (y1 + yScroll) / 32F).color(color, color, color, 255).endVertex();
		builder.pos(x1, y0, 0D).tex((x1 + xScroll) / 32F, (y0 + yScroll) / 32F).color(color, color, color, 255).endVertex();
		builder.pos(x0, y0, 0D).tex((x0 + xScroll) / 32F, (y0 + yScroll) / 32F).color(color, color, color, 255).endVertex();
		tes.draw();
	}
	
	public static void drawFrame(float minX, float minY, float maxX, float maxY, int color, float width) {
		Tessellator tes = Tessellator.getInstance();
		VertexBuffer builder = tes.getBuffer();
		builder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
		drawQuadArea(minX, minY, maxX, minY+width, builder, color);
		drawQuadArea(minX, maxY, maxX, maxY+width, builder, color);
		drawQuadArea(minX, minY, minX+width, maxY, builder, color);
		drawQuadArea(maxX, minY, maxX+width, maxY+width, builder, color);
		GlStateManager.enableBlend();
		GlStateManager.disableTexture2D();
		GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
		tes.draw();
		GlStateManager.enableTexture2D();
		GlStateManager.disableBlend();
	}
	
	public static void fillDropArea(int x, int y, int width, int height, int color, boolean drop) {
		Tessellator tes = Tessellator.getInstance();
		VertexBuffer builder = tes.getBuffer();
		builder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
		int minX = x;
		int minY = y;
		int maxX = x + width;
		int maxY = y + height;
		if(drop) {
			drawQuadArea(minX - 1, minY - 1, maxX, maxY, builder, -13158601);
			drawQuadArea(minX, minY, maxX + 1, maxY + 1, builder, -1);
		}
		else {
			drawQuadArea(minX, minY, maxX + 1, maxY + 1, builder, -13158601);
			drawQuadArea(minX - 1, minY - 1, maxX, maxY, builder, -1);
		}
		drawQuadArea(minX, minY, maxX, maxY, builder, color);
		GlStateManager.enableBlend();
		GlStateManager.disableTexture2D();
		GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
		tes.draw();
		GlStateManager.enableTexture2D();
		GlStateManager.disableBlend();
	}
	
	public static void drawQuadArea(float left, float top, float right, float bottom, VertexBuffer builder, int color) {
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
		builder.pos(left, bottom, 0.0F).color(f, f1, f2, f3).endVertex();
		builder.pos(right, bottom, 0.0F).color(f, f1, f2, f3).endVertex();
		builder.pos(right, top, 0.0F).color(f, f1, f2, f3).endVertex();
		builder.pos(left, top, 0.0F).color(f, f1, f2, f3).endVertex();
	}
	
	public static void drawCircle(float x, float y, float radius, int color, int borderColor, int segments, float borderWidth) {
		Tessellator tes = Tessellator.getInstance();
		VertexBuffer builder = tes.getBuffer();
		builder.begin(GL11.GL_TRIANGLES, DefaultVertexFormats.POSITION_COLOR);
		float a = (color >> 24 & 255) / 255F;
		float r = (color >> 16 & 255) / 255F;
		float g = (color >> 8 & 255) / 255F;
		float b = (color & 255) / 255F;
		float innerRadius = radius - Math.max(0, borderWidth);
		if (innerRadius < 0) innerRadius = 0F;
	    float spaceScale = (360.0F / segments) * (float)Math.PI / 180F;
		for (int i = 0; i < segments; i++) {
			float startAngle = i * spaceScale;
			float endAngle = ((i+1) % segments) * spaceScale;
			builder.pos(x, y, 0).color(r, g, b, a).endVertex();
			builder.pos(x + Math.cos(endAngle) * innerRadius, y + Math.sin(endAngle) * innerRadius, 0).color(r, g, b, a).endVertex();
			builder.pos(x + Math.cos(startAngle) * innerRadius, y + Math.sin(startAngle) * innerRadius, 0).color(r, g, b, a).endVertex();
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
				
				builder.pos(x2o, y2o, 0).color(br, bg, bb, ba).endVertex();
				builder.pos(x1o, y1o, 0).color(br, bg, bb, ba).endVertex();
				builder.pos(x1i, y1i, 0).color(br, bg, bb, ba).endVertex();
				
				builder.pos(x2o, y2o, 0).color(br, bg, bb, ba).endVertex();
				builder.pos(x1i, y1i, 0).color(br, bg, bb, ba).endVertex();
				builder.pos(x2i, y2i, 0).color(br, bg, bb, ba).endVertex();
				
			}
		}
		GlStateManager.disableTexture2D();
		GlStateManager.enableBlend();
		GlStateManager.tryBlendFuncSeparate(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA, SourceFactor.ONE, DestFactor.ZERO);
		tes.draw();
		GlStateManager.enableTexture2D();
		GlStateManager.disableBlend();
	}
	
	public static void drawLine(float startX, float startY, float endX, float endY, float width, VertexBuffer builder, int color) {
		float f3 = (float)(color >> 24 & 255) / 255.0F;
		float f = (float)(color >> 16 & 255) / 255.0F;
		float f1 = (float)(color >> 8 & 255) / 255.0F;
		float f2 = (float)(color & 255) / 255.0F;
		float dx = endX - startX;
		float dy = endY - startY;
		float length = MathHelper.sqrt_float(dx*dx + dy*dy);
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
		builder.pos(x3, y3, 0.0F).color(f, f1, f2, f3).endVertex(); 
		builder.pos(x2, y2, 0.0F).color(f, f1, f2, f3).endVertex();
		builder.pos(x1, y1, 0.0F).color(f, f1, f2, f3).endVertex();
		builder.pos(x4, y4, 0.0F).color(f, f1, f2, f3).endVertex();
		builder.pos(x2, y2, 0.0F).color(f, f1, f2, f3).endVertex(); 
		builder.pos(x3, y3, 0.0F).color(f, f1, f2, f3).endVertex();
	}
	
	public static void blitWithBorder(ResourceLocation res, int x, int y, int u, int v, int width, int height, int textureWidth, int textureHeight, int topBorder, int bottomBorder, int leftBorder, int rightBorder, float zLevel, boolean custom) {
		Minecraft.getMinecraft().getTextureManager().bindTexture(res);
		blitWithBorder(x, y, u, v, width, height, textureWidth, textureHeight, topBorder, bottomBorder, leftBorder, rightBorder, zLevel, custom);
	}
	
	public static void blitWithBorder(int x, int y, int u, int v, int width, int height, int textureWidth, int textureHeight, int topBorder, int bottomBorder, int leftBorder, int rightBorder, float zLevel, boolean custom) {
		if(!custom) {
			GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
			GlStateManager.enableBlend();
			GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
		}
		
		Tessellator tes = Tessellator.getInstance();
		VertexBuffer builder = tes.getBuffer();
		builder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
		
		int fillerWidth = textureWidth - leftBorder - rightBorder;
		int fillerHeight = textureHeight - topBorder - bottomBorder;
		int canvasWidth = width - leftBorder - rightBorder;
		int canvasHeight = height - topBorder - bottomBorder;
		int xPasses = canvasWidth / fillerWidth;
		int remainderWidth = canvasWidth % fillerWidth;
		int yPasses = canvasHeight / fillerHeight;
		int remainderHeight = canvasHeight % fillerHeight;

		drawTextured(x, y, u, v, leftBorder, topBorder, zLevel, builder);
		drawTextured(x + leftBorder + canvasWidth, y, u + leftBorder + fillerWidth, v, rightBorder, topBorder, zLevel, builder);
		drawTextured(x, y + topBorder + canvasHeight, u, v + topBorder + fillerHeight, leftBorder, bottomBorder, zLevel, builder);
		drawTextured(x + leftBorder + canvasWidth, y + topBorder + canvasHeight, u + leftBorder + fillerWidth, v + topBorder + fillerHeight, rightBorder, bottomBorder, zLevel, builder);

		for (int i = 0; i < xPasses + (remainderWidth > 0 ? 1 : 0); i++) {
			drawTextured(x + leftBorder + (i * fillerWidth), y, u + leftBorder, v, (i == xPasses ? remainderWidth : fillerWidth), topBorder, zLevel, builder);
			drawTextured(x + leftBorder + (i * fillerWidth), y + topBorder + canvasHeight, u + leftBorder, v + topBorder + fillerHeight, (i == xPasses ? remainderWidth : fillerWidth), bottomBorder, zLevel, builder);
			for (int j = 0; j < yPasses + (remainderHeight > 0 ? 1 : 0); j++)
				drawTextured(x + leftBorder + (i * fillerWidth), y + topBorder + (j * fillerHeight), u + leftBorder, v + topBorder, (i == xPasses ? remainderWidth : fillerWidth), (j == yPasses ? remainderHeight : fillerHeight), zLevel, builder);
		}

		for (int j = 0; j < yPasses + (remainderHeight > 0 ? 1 : 0); j++) {
			drawTextured(x, y + topBorder + (j * fillerHeight), u, v + topBorder, leftBorder, (j == yPasses ? remainderHeight : fillerHeight), zLevel, builder);
			drawTextured(x + leftBorder + canvasWidth, y + topBorder + (j * fillerHeight), u + leftBorder + fillerWidth, v + topBorder, rightBorder, (j == yPasses ? remainderHeight : fillerHeight), zLevel, builder);
		}
		tes.draw();
	}
	
	private static void drawTextured(int x, int y, int u, int v, int width, int height, float zLevel, VertexBuffer builder) {
		builder.pos(x, y + height, zLevel).tex(u * U_SCALE, (v + height) * V_SCALE).endVertex();
		builder.pos(x + width, y + height, zLevel).tex((u + width) * U_SCALE, (v + height) * V_SCALE).endVertex();
		builder.pos(x + width, y, zLevel).tex((u + width) * U_SCALE, v * V_SCALE).endVertex();
		builder.pos(x, y, zLevel).tex(u * U_SCALE, v * V_SCALE).endVertex();
	}
}
