package carbonconfiglib.gui.base.states;

import org.joml.Matrix3x2f;
import org.jspecify.annotations.Nullable;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.renderer.state.gui.GuiElementRenderState;
import net.minecraft.util.Mth;

public record LineRenderState(RenderPipeline pipeline, Matrix3x2f pose, float startX, float startY, float endX, float endY, float width, int color, @Nullable ScreenRectangle scissorArea, @Nullable ScreenRectangle bounds) implements GuiElementRenderState {
	public LineRenderState(RenderPipeline pipeline, Matrix3x2f pose, float startX, float startY, float endX, float endY, float width, int color, @Nullable ScreenRectangle scissorArea) {
		this(pipeline, pose, startX, startY, endX, endY, width, color, scissorArea, getBounds(startX, startY, endX, endY, pose, scissorArea));
	}
	
	public LineRenderState(GuiGraphicsExtractor graphics, float startX, float startY, float endX, float endY, float width, int color) {
		this(CarbonRenderPipelines.GUI, new Matrix3x2f(graphics.pose()), startX, startY, endX, endY, width, color, graphics.peekScissorStack());
	}
	
    private static @Nullable ScreenRectangle getBounds(float x0, float y0, float x1, float y1, Matrix3x2f pose, @Nullable ScreenRectangle scissorArea) {
        ScreenRectangle bounds = new ScreenRectangle((int)x0, (int)y0, Math.max(1, Mth.ceil(x1 - x0)), Math.max(1, Mth.ceil(y1 - y0))).transformMaxBounds(pose);
        return scissorArea != null ? scissorArea.intersection(bounds) : bounds;
    }
	
	@Override
	public void buildVertices(VertexConsumer builder) {
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
		
		if(pose == null) {
			builder.addVertex(x3, y3, 0.0F).setColor(f, f1, f2, f3); 
			builder.addVertex(x2, y2, 0.0F).setColor(f, f1, f2, f3);
			builder.addVertex(x1, y1, 0.0F).setColor(f, f1, f2, f3);
			builder.addVertex(x4, y4, 0.0F).setColor(f, f1, f2, f3);
			builder.addVertex(x2, y2, 0.0F).setColor(f, f1, f2, f3); 
			builder.addVertex(x3, y3, 0.0F).setColor(f, f1, f2, f3);
			return;
		}
		builder.addVertexWith2DPose(pose, x3, y3).setColor(f, f1, f2, f3); 
		builder.addVertexWith2DPose(pose, x2, y2).setColor(f, f1, f2, f3);
		builder.addVertexWith2DPose(pose, x1, y1).setColor(f, f1, f2, f3);
		builder.addVertexWith2DPose(pose, x4, y4).setColor(f, f1, f2, f3);
		builder.addVertexWith2DPose(pose, x2, y2).setColor(f, f1, f2, f3); 
		builder.addVertexWith2DPose(pose, x3, y3).setColor(f, f1, f2, f3);
	}

	@Override
	public TextureSetup textureSetup() {
		return TextureSetup.noTexture();
	}
	
}
