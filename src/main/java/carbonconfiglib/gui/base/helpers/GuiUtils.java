package carbonconfiglib.gui.base.helpers;

import carbonconfiglib.gui.api.background.BackgroundTexture;
import carbonconfiglib.gui.base.states.CircleRenderState;
import carbonconfiglib.gui.base.states.LineRenderState;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;

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
	
	public static void drawText(GuiGraphicsExtractor graphics, Font font, Component text, float x, float y, Align align, int color) {
		float offset = align.align(font.width(text));
		graphics.text(font, text.getVisualOrderText(), (int)(x + offset), (int)y, color, false);
	}
	
	public static void drawSplitText(GuiGraphicsExtractor graphics, Font font, Component text, float x, float y, Align align, int color, int maxLength) {
		drawSplitText(graphics, font, text, x, y, align, color, maxLength, font.lineHeight);
	}
	
	public static void drawSplitText(GuiGraphicsExtractor graphics, Font font, Component text, float x, float y, Align align, int color, int maxLength, float lineSplit) {
		for(FormattedCharSequence line : font.split(text, maxLength)) {
			float offset = align.align(font.width(line));
			graphics.text(font, line, (int)(x + offset), (int)y, color, false);
			y += lineSplit;
		}
	}
	
	public static void drawScrollingText(GuiGraphicsExtractor graphics, Font font, Component text, float x, float y, float width, float height, Align align, int color, long startTime) {
		int textWidth = font.width(text);
		if(textWidth > width) {
			float diff = textWidth - width + 2F;
			double timer = (currentMillseconds() - startTime) / 1000F;
			double minDiff = Math.max(diff * 0.5D, 3.0F);
			double offset = Math.sin((Math.PI / 2D) * Math.cos(((Math.PI * 2D) * timer) / minDiff)) / 2D + 0.01F + align.alignCenter();
			graphics.enableScissor((int)x, (int)y, (int)(x+width), (int)(y+height));
			graphics.text(font, text, (int)(x - align.align(width) + align.align(textWidth) + (float)Mth.lerp(offset, 0F, diff)), (int)(y + (height * 0.5F) - (font.lineHeight * 0.5F)), color, false);
			graphics.disableScissor();
			return;
		}
		float offset = align.align(textWidth);
		graphics.text(font, text, (int)(x - align.align(width) + offset), (int)(y + (height * 0.5F) - (font.lineHeight * 0.5F)), color, false);
	}
	
	public static void drawScrollingShadowText(GuiGraphicsExtractor graphics, Font font, Component text, float x, float y, float width, float height, Align align, int color, long startTime) {
		int textWidth = font.width(text);
		if(textWidth > width) {
			float diff = textWidth - width + 2F;
			double timer = (currentMillseconds() - startTime) / 1000F;
			double minDiff = Math.max(diff * 0.5D, 3.0F);
			double offset = Math.sin((Math.PI / 2D) * Math.cos(((Math.PI * 2D) * timer) / minDiff)) / 2D + 0.01F + align.alignCenter();
			graphics.enableScissor((int)x, (int)y, (int)(x+width), (int)(y+height));
			graphics.text(font, text, (int)(x - align.align(width) + align.align(textWidth) + (float)Mth.lerp(offset, 0F, diff)), (int)(y + (height * 0.5F) - (font.lineHeight * 0.5F)), color, true);
			graphics.disableScissor();
			return;
		}
		float offset = align.align(textWidth);
		graphics.text(font, text, (int)(x - align.align(width) + offset), (int)(y + (height * 0.5F) - (font.lineHeight * 0.5F)), color, true);
	}
	
	public static long currentMillseconds() {
		return Util.getMillis();
	}
	
	public static void drawTextureRegion(GuiGraphicsExtractor graphics, float x, float y, float width, float height, Icon icon, float texWidth, float texHeight) {
		drawTextureRegion(graphics, icon.getTexture(), x, y, icon.getX(), icon.getY(), width, height, texWidth, texHeight, icon.getSheetWidth(), icon.getSheetHeight());
	} 
	
	public static void drawTextureRegion(GuiGraphicsExtractor graphics, float x, float y, int xOff, int yOff, float width, float height, Icon icon, float texWidth, float texHeight) {
		drawTextureRegion(graphics, icon.getTexture(), x, y, icon.getX() + xOff, icon.getY() + yOff, width, height, texWidth, texHeight, icon.getSheetWidth(), icon.getSheetHeight());
	}
	
	public static void drawTextureRegion(GuiGraphicsExtractor graphics, Identifier sprite, float x, float y, float texX, float texY, float width, float height, float texWidth, float texHeight, float textureWidth, float textureHeight) {
		int maxX = (int)(x + width);
		int maxY = (int)(y + height);
		float t_minX = texX / textureWidth;
		float t_minY = texY / textureHeight;
		float t_maxX = (texX + texWidth) / textureWidth;
		float t_maxY = (texY + texHeight) / textureHeight;
		//TODO implement own Blit function that allow floating point positioning as the non floating point variants aren't the best
		graphics.blit(sprite, (int)x, (int)y, maxX, maxY, t_minX, t_maxX, t_minY, t_maxY);
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
	
	public static void renderListOverlay(GuiGraphicsExtractor graphics, int x0, int x1, int y0, int y1, int width, int height, BackgroundTexture texture) {
		int color = texture.getForegroundBrightness();
		graphics.blit(RenderPipelines.GUI_TEXTURED, texture.getForegroundTexture(), x0, 0, 0F, 0F, width, y0, 32, 32, ARGB.color(255, color, color, color));
		graphics.blit(RenderPipelines.GUI_TEXTURED, texture.getForegroundTexture(), x0, y1, 0F, 0F, width, height, 32, 32, ARGB.color(255, color, color, color));
	}
	
	public static void renderListShadow(GuiGraphicsExtractor graphics, int x0, int x1, int y0, int y1, int width, int height) {
		graphics.fillGradient(x0, y0, x1, y0+4, ARGB.color(255, 0), 0);
		graphics.fillGradient(x0, y1-4, x1, y1, 0, ARGB.color(255, 0));
	}
	
	public static void renderBackground(GuiGraphicsExtractor graphics, int x0, int x1, int y0, int y1, float scroll, BackgroundTexture texture) {
		renderBackground(graphics, x0, x1, y0, y1, 0F, scroll, texture);
	}
	
	public static void renderBackground(GuiGraphicsExtractor graphics, int x0, int x1, int y0, int y1, float xScroll, float yScroll, BackgroundTexture texture) {
		int width = x1 - x0;
		int height = y1 - y0;
		int color = texture.getBackgroundBrightness();
		graphics.blit(RenderPipelines.GUI_TEXTURED, texture.getBackgroundTexture(), x0, y0, (float)(x1 + xScroll), (float)(y1 + yScroll), width, height, width, height, 32, 32, ARGB.color(255, color, color, color));
	}
	
	public static void drawFrame(GuiGraphicsExtractor graphics, int minX, int minY, int maxX, int maxY, int color, int width) {
		graphics.fill(minX, minY, maxX, minY+width, color);
		graphics.fill(minX, maxY, maxX, maxY+width, color);
		graphics.fill(minX, minY, minX+width, maxY, color);
		graphics.fill(maxX, minY, maxX+width, maxY+width, color);
	}
	
	public static void fillDropArea(GuiGraphicsExtractor graphics, int x, int y, int width, int height, int color, boolean drop) {
		int minX = x;
		int minY = y;
		int maxX = x + width;
		int maxY = y + height;
		if(drop) {
			graphics.fill(minX - 1, minY - 1, maxX, maxY, -13158601);
			graphics.fill(minX, minY, maxX + 1, maxY + 1, -1);
		}
		else {
			graphics.fill(minX, minY, maxX + 1, maxY + 1, -13158601);
			graphics.fill(minX - 1, minY - 1, maxX, maxY, -1);
		}
		graphics.fill(minX, minY, maxX, maxY, color);
	}
	
	public static void drawCircle(GuiGraphicsExtractor graphics, float x, float y, float radius, int color, int borderColor, int segments, float borderWidth) {
		graphics.submitGuiElementRenderState(new CircleRenderState(graphics, x, y, radius, color, borderColor, segments, borderWidth));
	}
	
	public static void drawLine(GuiGraphicsExtractor graphics, float startX, float startY, float endX, float endY, float width, int color) {
		graphics.submitGuiElementRenderState(new LineRenderState(graphics, startX, startY, endX, endY, width, color));
	}
}
