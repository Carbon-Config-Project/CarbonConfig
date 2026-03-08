package carbonconfiglib.gui.screens;

import java.io.BufferedReader;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.StreamSupport;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.GlStateManager.DestFactor;
import com.mojang.blaze3d.platform.GlStateManager.SourceFactor;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import com.mojang.math.Matrix4f;

import carbonconfiglib.gui.base.helpers.Align;
import carbonconfiglib.gui.base.screen.BaseCarbonScreen;
import it.unimi.dsi.fastutil.objects.Object2BooleanLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraftforge.fml.loading.FMLPaths;
import speiger.src.collections.objects.lists.ObjectArrayList;
import speiger.src.collections.objects.maps.impl.hash.Object2ObjectOpenHashMap;

public class TestUI extends BaseCarbonScreen
{
	float scale = 1F;
	List<ModNode> vertexs = new ObjectArrayList<>();
	JsonObject provider = new JsonObject();
	ModNode focused;
	
	public TestUI() {
		//Test Data
		try(BufferedReader reader = Files.newBufferedReader(FMLPaths.GAMEDIR.get().resolve("data.json"))) {
			provider = JsonParser.parseReader(reader).getAsJsonObject();
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void init() {
		super.init();
	}
	
	@Override
	public void tick() {
		if(!hasShiftDown() && !vertexs.isEmpty()) return;
		vertexs.clear();
		vertexs.addAll(StreamSupport.stream(provider.getAsJsonArray("mods").spliterator(), false).map(JsonElement::getAsJsonObject).map(T -> T.get("id").getAsString()).sorted().map(ModNode::new).toList());
		Map<String, ModNode> mapped = new Object2ObjectOpenHashMap<>();
		vertexs.forEach(T -> mapped.put(T.modId, T));
		ModNode node = mapped.get("minecraft");
		mapped.put("forge", node);
		vertexs.remove(node);
		for(JsonElement element : provider.getAsJsonArray("mods")) {
			JsonObject mod = element.getAsJsonObject();
			if(!mod.has("dep")) continue;
			ModNode owner = mapped.get(mod.get("id").getAsString());
			if(owner == null) continue;
			for(JsonElement dep : mod.getAsJsonArray("dep")) {
				JsonObject modDep = dep.getAsJsonObject();
				ModNode dependency = mapped.get(modDep.get("id").getAsString());
				if(dependency == null || owner == dependency) continue;
				boolean optional = modDep.get("required").getAsBoolean();
				dependency.dependants.put(owner, optional);
				owner.dependencies.put(dependency, optional);
			}
		}
		layout(vertexs, 0, 0, 200F, 150, 15);
		node.x = 0;
		node.y = 0;
		vertexs.add(0, node);
	}

	@Override
	public void renderBackground(PoseStack matrix, int mouseX, int mouseY, float partialTicks) {
		renderDirtBackground(0);
		matrix.pushPose();
		matrix.translate(centerX, centerY, 0F);
		matrix.scale(scale, scale, 1F);
		int radius = 10;
		for(ModNode vertex : vertexs) {
			int x = (int)vertex.x;
			int y = (int)vertex.y;
			fill(matrix, (int)x-radius, (int)y-radius, (int)x+radius, (int)y+radius, vertex.modId.hashCode() | 0xFF000000);
			drawText(matrix, Component.literal(vertex.modId), x-centerX, y-centerY-radius-font.lineHeight*2, Align.CENTER, -1);
		}
		if(focused != null) {
			Tesselator tes = Tesselator.getInstance();
			BufferBuilder builder = tes.getBuilder();
			builder.begin(Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR);
			drawNode(matrix, focused, false, 0xFF0000FF, 0xFF00FFFF, builder);
			drawNode(matrix, focused, true, 0xFFFF0000, 0xFFFFFF00, builder);
			RenderSystem.setShader(GameRenderer::getPositionColorShader);
			GlStateManager._disableTexture();
			GlStateManager._enableBlend();
			RenderSystem.blendFuncSeparate(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA, SourceFactor.ONE, DestFactor.ZERO);
			tes.end();
			GlStateManager._enableTexture();
			GlStateManager._disableBlend();
		}

		matrix.popPose();
	}
	
	private void drawNode(PoseStack stack, ModNode source, boolean dep, int requiredColor, int optionalColor, VertexConsumer builder) {
		float radius = dep ? 5 : -5;
		for(Object2BooleanMap.Entry<ModNode> entry : (dep ? source.dependencies : source.dependants).object2BooleanEntrySet()) {
			ModNode child = entry.getKey();
			drawLine(stack, (float)source.x+radius, (float)source.y+radius, (float)child.x+radius, (float)child.y+radius, 2F, builder, entry.getBooleanValue() ? requiredColor : optionalColor);
			if(entry.getBooleanValue()) drawNode(stack, child, dep, requiredColor, optionalColor, builder);
		}
	}
	
	@Override
	public void collectTooltips(PoseStack matrix, int mouseX, int mouseY, float partialTicks, Consumer<Component> tooltips) {
		double radius = 10 * scale;
		for(ModNode vertex : vertexs) {
			int x = (int)(vertex.x * scale) + centerX;
			int y = (int)(vertex.y * scale) + centerY;
			if(mouseX >= x-radius && mouseX <= x+radius && mouseY >= y-radius && mouseY <= y+radius) {
				tooltips.accept(Component.literal(vertex.modId));
			}
		}
	}
	
	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int pButton) {
		double radius = 10 * scale;
		for(ModNode vertex : vertexs) {
			int x = (int)(vertex.x * scale) + centerX;
			int y = (int)(vertex.y * scale) + centerY;
			if(mouseX >= x-radius && mouseX <= x+radius && mouseY >= y-radius && mouseY <= y+radius) {
				focused = vertex;
				return true;
			}
		}
		return super.mouseClicked(mouseX, mouseY, pButton);
	}
	
	@Override
	public boolean mouseScrolled(double pMouseX, double pMouseY, double pDelta) {
		scale = Mth.clamp(scale - (float)pDelta * 0.01F, 0.001F, 10F);
		return true;
	}
	
	//Ripped out of ChunkPregen xD
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
	
	static class ModNode {
		String modId;
		double x;
		double y;
		int layer = -1;
		Object2BooleanMap<ModNode> dependencies = new Object2BooleanLinkedOpenHashMap<>();
		Object2BooleanMap<ModNode> dependants = new Object2BooleanLinkedOpenHashMap<>();
		
		public ModNode(String modId) {
			this.modId = modId;
		}
		
		public void setPos(double x, double y) {
			this.x = x;
			this.y = y;
		}
		
		@Override
		public int hashCode() {
			return modId.hashCode();
		}
		
		@Override
		public boolean equals(Object obj) {
			return obj instanceof ModNode && ((ModNode)obj).modId.equals(modId);
		}
	}
	
	public static void layout(List<ModNode> nodes, double centerX, double centerY, double ringSpacing, double advance, int baseRingCapacity) {   
		int ringIndex = 1;
		for(int i = 0,m=nodes.size();i<m;) {
			int countThisRing = Math.min(baseRingCapacity * ringIndex, m - i);

			double radius = ringSpacing + (advance * (ringIndex-1));
			double angleStep = (2 * Math.PI) / countThisRing;

			for (int j = 0; j < countThisRing; j++) {
				double angle = j * angleStep;
				nodes.get(i++).setPos(centerX + Math.cos(angle) * radius, centerY + Math.sin(angle) * radius);
			}
			ringIndex++;
		}
	}
}
