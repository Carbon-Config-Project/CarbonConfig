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

public record CircleRenderState(RenderPipeline pipeline, Matrix3x2f pose, float x, float y, float radius, int color, int borderColor, int segments, float borderWidth, @Nullable ScreenRectangle scissorArea, @Nullable ScreenRectangle bounds) implements GuiElementRenderState {
	
	public CircleRenderState(GuiGraphicsExtractor graphics, float x, float y, float radius, int color, int borderColor, int segments, float borderWidth) {
		this(CarbonRenderPipelines.GUI, new Matrix3x2f(graphics.pose()), x, y, radius, color, borderColor, segments, borderWidth, graphics.peekScissorStack());
	}
	
	public CircleRenderState(RenderPipeline pipeline, Matrix3x2f pose, float x, float y, float radius, int color, int borderColor, int segments, float borderWidth, @Nullable ScreenRectangle scissorArea) {
		this(pipeline, pose, x, y, radius, color, borderColor, segments, borderWidth, scissorArea, getBounds(x, y, radius, borderWidth, pose, scissorArea));
	}
	
    private static @Nullable ScreenRectangle getBounds(float x, float y, float radius, float borderWidth, Matrix3x2f pose, @Nullable ScreenRectangle scissorArea) {
    	float realRadius = (radius + borderWidth);
    	int bound = (int)(realRadius * 2F);
        ScreenRectangle bounds = new ScreenRectangle((int)(x - realRadius), (int)(y - realRadius), bound, bound).transformMaxBounds(pose);
        return scissorArea != null ? scissorArea.intersection(bounds) : bounds;
    }
    
	@Override
	public void buildVertices(VertexConsumer builder) {
		float a = (color >> 24 & 255) / 255F;
		float r = (color >> 16 & 255) / 255F;
		float g = (color >> 8 & 255) / 255F;
		float b = (color & 255) / 255F;
		float innerRadius = radius - Math.max(0, borderWidth);
		if (innerRadius < 0) innerRadius = 0F;
	    float spaceScale = (360.0F / segments) * Mth.DEG_TO_RAD;
		for (int i = 0; i < segments; i++) {
			float startAngle = i * spaceScale;
			float endAngle = ((i+1) % segments) * spaceScale;
			if(pose != null) {
				builder.addVertexWith2DPose(pose, x, y).setColor(r, g, b, a);
				builder.addVertexWith2DPose(pose, (float)(x + Math.cos(endAngle) * innerRadius), (float)(y + Math.sin(endAngle) * innerRadius)).setColor(r, g, b, a);
				builder.addVertexWith2DPose(pose, (float)(x + Math.cos(startAngle) * innerRadius), (float)(y + Math.sin(startAngle) * innerRadius)).setColor(r, g, b, a);
				continue;
			}
			builder.addVertex(x, y, 0F).setColor(r, g, b, a);
			builder.addVertex(x + (float)Math.cos(endAngle) * innerRadius, y + (float)Math.sin(endAngle) * innerRadius, 0F).setColor(r, g, b, a);
			builder.addVertex(x + (float)Math.cos(startAngle) * innerRadius, y + (float)Math.sin(startAngle) * innerRadius, 0F).setColor(r, g, b, a);
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
				
				if(pose != null) {
					builder.addVertexWith2DPose(pose, x2o, y2o).setColor(br, bg, bb, ba);
					builder.addVertexWith2DPose(pose, x1o, y1o).setColor(br, bg, bb, ba);
					builder.addVertexWith2DPose(pose, x1i, y1i).setColor(br, bg, bb, ba);
					
					builder.addVertexWith2DPose(pose, x2o, y2o).setColor(br, bg, bb, ba);
					builder.addVertexWith2DPose(pose, x1i, y1i).setColor(br, bg, bb, ba);
					builder.addVertexWith2DPose(pose, x2i, y2i).setColor(br, bg, bb, ba);
					continue;
				}
				builder.addVertex(x2o, y2o, 0F).setColor(br, bg, bb, ba);
				builder.addVertex(x1o, y1o, 0F).setColor(br, bg, bb, ba);
				builder.addVertex(x1i, y1i, 0F).setColor(br, bg, bb, ba);
				
				builder.addVertex(x2o, y2o, 0F).setColor(br, bg, bb, ba);
				builder.addVertex(x1i, y1i, 0F).setColor(br, bg, bb, ba);
				builder.addVertex(x2i, y2i, 0F).setColor(br, bg, bb, ba);
				
			}
		}
	}

	@Override
	public TextureSetup textureSetup() {
		return TextureSetup.noTexture();
	}
}
