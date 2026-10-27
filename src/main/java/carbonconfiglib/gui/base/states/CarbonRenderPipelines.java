package carbonconfiglib.gui.base.states;

import com.mojang.blaze3d.PrimitiveTopology;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;

import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

public class CarbonRenderPipelines {
    public static final RenderPipeline GUI = RenderPipeline.builder(RenderPipelines.GUI_SNIPPET).withLocation(Identifier.fromNamespaceAndPath("carbonconfig", "pipeline/_gui")).withVertexBinding(0, DefaultVertexFormat.POSITION_COLOR).withPrimitiveTopology(PrimitiveTopology.TRIANGLES).build();

}
