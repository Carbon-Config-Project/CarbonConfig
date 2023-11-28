package carbonconfiglib.gui.widgets;

import java.util.ArrayDeque;
import java.util.Deque;

import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.platform.GlStateManager;

import carbonconfiglib.gui.config.ConfigElement.GuiAlign;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
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
public class GuiUtils
{
	private static final float U_SCALE = 1F / 0x100;
	private static final float V_SCALE = 1F / 0x100;
	private static final ScissorsStack STACK = new ScissorsStack();
	
	public static float calculateScrollOffset(float width, FontRenderer font, GuiAlign align, String text, int seed) {
		int textWidth = font.getStringWidth(text);
		if(textWidth > width) {
			float diff = textWidth - width + 2F;
			double timer = (Util.milliTime() + seed) / 1000D;
			double minDiff = Math.max(diff * 0.5D, 3.0D);
			double offset = Math.sin((Math.PI / 2D) * Math.cos(((Math.PI * 2D) * timer) / minDiff)) / 2D + 0.01F + align.alignCenter();
			return (float)MathHelper.lerp(offset, 0D, diff);
		}
		return 0;
	}
	
	public static void drawScrollingString(FontRenderer font, String text, float x, float y, float width, float height, GuiAlign align, int color, int seed) {
		int textWidth = font.getStringWidth(text);
		if(textWidth > width) {
			float diff = textWidth - width + 2F;
			double timer = (Util.milliTime() + seed) / 1000D;
			double minDiff = Math.max(diff * 0.5D, 3.0D);
			double offset = Math.sin((Math.PI / 2D) * Math.cos(((Math.PI * 2D) * timer) / minDiff)) / 2D + 0.01F + align.alignCenter();
			pushScissors((int)x, (int)y, (int)width, (int)height);
			font.drawString(text, x - align.align(width) + align.align(textWidth) + (float)MathHelper.lerp(offset, 0D, diff), y + (height / 2) - (font.FONT_HEIGHT / 3), color);
			popScissors();
			return;
		}
		float offset = align.align(textWidth);
		font.drawString(text, x - align.align(width) + offset, y + (height / 2) - (font.FONT_HEIGHT / 3), color);
	}
	
	public static void drawScrollingShadowString(FontRenderer font, String text, float x, float y, float width, float height, GuiAlign align, int color, int seed) {
		int textWidth = font.getStringWidth(text);
		if(textWidth > width) {
			float diff = textWidth - width + 2F;
			double timer = (Util.milliTime() + seed) / 1000D;
			double minDiff = Math.max(diff * 0.5D, 3.0D);
			double offset = Math.sin((Math.PI / 2D) * Math.cos(((Math.PI * 2D) * timer) / minDiff)) / 2D + 0.01F + align.alignCenter();
			pushScissors((int)x, (int)y, (int)width, (int)height);
			font.drawStringWithShadow(text, x - align.align(width) + align.align(textWidth) + (float)MathHelper.lerp(offset, 0D, diff), y + (height / 2) - (font.FONT_HEIGHT / 3), color);
			popScissors();
			return;
		}
		float offset = align.align(textWidth);
		font.drawStringWithShadow(text, x - align.align(width) + offset, y + (height / 2) - (font.FONT_HEIGHT / 3), color);
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
		MainWindow window = Minecraft.getInstance().mainWindow;
		int bottom = rect.maxY;
		double scaledHeight = (double)window.getHeight() / (double)window.getScaledHeight();
		double scaledWidth = (double)window.getWidth() / (double)window.getScaledWidth();
		GL11.glEnable(GL11.GL_SCISSOR_TEST);
		GL11.glScissor((int)(rect.getX() * scaledWidth), (int)(window.getHeight() - bottom * scaledHeight), (int)(rect.getWidth() * scaledWidth), (int)(rect.getHeigth() * scaledHeight));
	}
	
	public static void drawTextureWithBorder(ResourceLocation res, int x, int y, int u, int v, int width, int height, int textureWidth, int textureHeight, int topBorder, int bottomBorder, int leftBorder, int rightBorder, float zLevel) {
		Minecraft.getInstance().getTextureManager().bindTexture(res);
		drawTexture(x, y, u, v, width, height, textureWidth, textureHeight, topBorder, bottomBorder, leftBorder, rightBorder, zLevel);
	}
	
	private static void drawTexture(int x, int y, int u, int v, int width, int height, int textureWidth, int textureHeight, int topBorder, int bottomBorder, int leftBorder, int rightBorder, float zLevel) {
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder builder = tessellator.getBuffer();
		builder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
		GlStateManager.enableBlend();
		GlStateManager.blendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
		
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
		tessellator.draw();
	}
	
	private static void drawTextured(int x, int y, int u, int v, int width, int height, float zLevel, BufferBuilder builder) {
		builder.pos(x, y + height, zLevel).tex(u * U_SCALE, (v + height) * V_SCALE).endVertex();
		builder.pos(x + width, y + height, zLevel).tex((u + width) * U_SCALE, (v + height) * V_SCALE).endVertex();
		builder.pos(x + width, y, zLevel).tex((u + width) * U_SCALE, v * V_SCALE).endVertex();
		builder.pos(x, y, zLevel).tex(u * U_SCALE, v * V_SCALE).endVertex();
	}
	
	public static void drawTextureRegion(float x, float y, float width, float height, Icon icon, float texWidth, float texHeight) {
		Minecraft.getInstance().getTextureManager().bindTexture(icon.getTexture());
		drawTextureRegion(x, y, icon.getX(), icon.getY(), width, height, texWidth, texHeight, icon.getSheetWidth(), icon.getSheetHeight());
	}
	
	public static void drawTextureRegion(float x, float y, int xOff, int yOff, float width, float height, Icon icon, float texWidth, float texHeight) {
		Minecraft.getInstance().getTextureManager().bindTexture(icon.getTexture());
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
		BufferBuilder bufferbuilder = tessellator.getBuffer();
		bufferbuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
		bufferbuilder.pos(x, maxY, 0).tex(t_minX, t_maxY).endVertex();
		bufferbuilder.pos(maxX, maxY, 0).tex(t_maxX, t_maxY).endVertex();
		bufferbuilder.pos(maxX, y, 0).tex(t_maxX, t_minY).endVertex();
		bufferbuilder.pos(x, y, 0).tex(t_minX, t_minY).endVertex();
		
        GlStateManager.enableBlend();
        GlStateManager.blendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
		tessellator.draw();
		GlStateManager.disableBlend();
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
		}
		
		public Rect pop() {
			stack.pop();
			return stack.peek();
		}
	}
}
