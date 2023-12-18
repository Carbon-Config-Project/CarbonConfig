package carbonconfiglib.gui.widgets;

import java.util.ArrayDeque;
import java.util.Deque;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;

import carbonconfiglib.gui.config.ConfigElement.GuiAlign;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;
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
	private static final float U_SCALE = 1F / 0x100;
	private static final float V_SCALE = 1F / 0x100;
	private static final ScissorsStack STACK = new ScissorsStack();
	
	public static float calculateScrollOffset(float width, Font font, GuiAlign align, Component text, int seed) {
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
	
	public static void drawScrollingString(GuiGraphics stack, Font font, Component text, float x, float y, float width, float height, GuiAlign align, int color, int seed) {
		int textWidth = font.width(text);
		if(textWidth > width) {
			float diff = textWidth - width + 2F;
			double timer = (Util.getMillis() + seed) / 1000D;
			double minDiff = Math.max(diff * 0.5D, 3.0D);
			double offset = Math.sin((Math.PI / 2D) * Math.cos(((Math.PI * 2D) * timer) / minDiff)) / 2D + 0.01F + align.alignCenter();
			pushScissors((int)x, (int)y, (int)width, (int)height);
			stack.drawString(font, text, (int) (x - align.align(width) + align.align(textWidth) + (float)Mth.lerp(offset, 0D, diff)), (int) (y + (height / 2) - (font.lineHeight / 3)), color);
			popScissors();
			return;
		}
		float offset = align.align(textWidth);
		stack.drawString(font, text, (int) (x - align.align(width) + offset), (int) (y + (height / 2) - (font.lineHeight / 3)), color);
	}
	
	public static void drawScrollingShadowString(GuiGraphics stack, Font font, Component text, float x, float y, float width, float height, GuiAlign align, int color, int seed) {
		int textWidth = font.width(text);
		if(textWidth > width) {
			float diff = textWidth - width + 2F;
			double timer = (Util.getMillis() + seed) / 1000D;
			double minDiff = Math.max(diff * 0.5D, 3.0D);
			double offset = Math.sin((Math.PI / 2D) * Math.cos(((Math.PI * 2D) * timer) / minDiff)) / 2D + 0.01F + align.alignCenter();
			pushScissors((int)x, (int)y, (int)width, (int)height);
			stack.drawString(font, text, (int) (x - align.align(width) + align.align(textWidth) + (int)Mth.lerp(offset, 0D, diff)), (int) (y + (height / 2) - (font.lineHeight / 3)), color);
			popScissors();
			return;
		}
		float offset = align.align(textWidth);
		stack.drawString(font, text, (int) (x - align.align(width) + offset), (int) (y + (height / 2) - (font.lineHeight / 3)), color);
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
	
	public static void drawTextureWithBorder(GuiGraphics poseStack, ResourceLocation res, int x, int y, int u, int v, int width, int height, int textureWidth, int textureHeight, int topBorder, int bottomBorder, int leftBorder, int rightBorder) {
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderTexture(0, res);
		drawTexture(poseStack, x, y, u, v, width, height, textureWidth, textureHeight, topBorder, bottomBorder, leftBorder, rightBorder, 0);
	}
	
	private static void drawTexture(GuiGraphics poseStack, int x, int y, int u, int v, int width, int height, int textureWidth, int textureHeight, int topBorder, int bottomBorder, int leftBorder, int rightBorder, float zLevel) {
		Tesselator tessellator = Tesselator.getInstance();
		BufferBuilder builder = tessellator.getBuilder();
		builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
		Matrix4f matrix = poseStack.pose().last().pose();
		RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		
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
		tessellator.end();
	}
	
	private static void drawTextured(GuiGraphics poseStack, int x, int y, int u, int v, int width, int height, float zLevel, BufferBuilder builder, Matrix4f matrix) {
		builder.vertex(matrix, x, y + height, zLevel).uv(u * U_SCALE, (v + height) * V_SCALE).endVertex();
		builder.vertex(matrix, x + width, y + height, zLevel).uv((u + width) * U_SCALE, (v + height) * V_SCALE).endVertex();
		builder.vertex(matrix, x + width, y, zLevel).uv((u + width) * U_SCALE, v * V_SCALE).endVertex();
		builder.vertex(matrix, x, y, zLevel).uv(u * U_SCALE, v * V_SCALE).endVertex();
	}
	
	public static void drawTextureRegion(GuiGraphics stack, float x, float y, float width, float height, Icon icon, float texWidth, float texHeight) {
		RenderSystem._setShaderTexture(0, icon.getTexture());
		drawTextureRegion(stack, x, y, icon.getX(), icon.getY(), width, height, texWidth, texHeight, icon.getSheetWidth(), icon.getSheetHeight());
	}
	
	public static void drawTextureRegion(GuiGraphics stack, float x, float y, int xOff, int yOff, float width, float height, Icon icon, float texWidth, float texHeight) {
		RenderSystem._setShaderTexture(0, icon.getTexture());
		drawTextureRegion(stack, x, y, icon.getX() + xOff, icon.getY() + yOff, width, height, texWidth, texHeight, icon.getSheetWidth(), icon.getSheetHeight());
	}
    
	public static void drawTextureRegion(GuiGraphics stack, float x, float y, float texX, float texY, float width, float height, float texWidth, float texHeight, float textureWidth, float textureHeight) {
		Matrix4f matrix = stack.pose().last().pose();
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
