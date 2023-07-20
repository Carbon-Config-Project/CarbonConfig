package carbonconfiglib.gui.widgets;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import com.mojang.math.Matrix4f;

import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;

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
	private static final FormattedText DOTS = FormattedText.of("...");
	
	public static FormattedText ellipsizeStyled(Component text, int maxWidth, Font font) {
		final Component dots = Component.literal("...").withStyle(text.getStyle());
		final int strWidth = font.width(text);
		final int ellipsisWidth = font.width(dots);
		if (strWidth > maxWidth) {
			if (ellipsisWidth >= maxWidth) return font.substrByWidth(text, maxWidth);
			return FormattedText.composite(font.substrByWidth(text, maxWidth - ellipsisWidth), dots);
		}
		return text;
	}
	
	public static FormattedText ellipsize(FormattedText text, int maxWidth, Font font) {
		final int strWidth = font.width(text);
		final int ellipsisWidth = font.width(DOTS);
		if (strWidth > maxWidth) {
			if (ellipsisWidth >= maxWidth) return font.substrByWidth(text, maxWidth);
			return FormattedText.composite(font.substrByWidth(text, maxWidth - ellipsisWidth), DOTS);
		}
		return text;
	}
	
	public static void drawTextureRegion(PoseStack stack, float x, float y, float width, float height, Icon icon, float texWidth, float texHeight) {
		RenderSystem._setShaderTexture(0, icon.getTexture());
		drawTextureRegion(stack, x, y, icon.getX(), icon.getY(), width, height, texWidth, texHeight, icon.getSheetWidth(), icon.getSheetHeight());
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
}
