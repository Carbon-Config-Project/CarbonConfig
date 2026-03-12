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

import carbonconfiglib.gui.api.background.BackgroundTexture;
import carbonconfiglib.gui.base.helpers.Align;
import carbonconfiglib.gui.base.helpers.GuiUtils;
import carbonconfiglib.gui.base.screen.BaseCarbonScreen;
import it.unimi.dsi.fastutil.objects.Object2BooleanLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraftforge.fml.loading.FMLPaths;
import speiger.src.collections.objects.lists.ObjectArrayList;
import speiger.src.collections.objects.maps.impl.hash.Object2ObjectOpenHashMap;

public class TestUI extends BaseCarbonScreen
{
	float scale = 0.32F;
	float x;
	float y;
	ChunkPos pos;
	List<ModNode> nodes = new ObjectArrayList<>();
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
		if(!hasShiftDown() && !nodes.isEmpty()) return;
		nodes.clear();
		nodes.addAll(StreamSupport.stream(provider.getAsJsonArray("mods").spliterator(), false).map(JsonElement::getAsJsonObject).map(T -> T.get("id").getAsString()).sorted().map(ModNode::new).toList());
		Map<String, ModNode> mapped = new Object2ObjectOpenHashMap<>();
		nodes.forEach(T -> mapped.put(T.modId, T));
		ModNode node = mapped.get("minecraft");
		mapped.put("forge", node);
		nodes.remove(node);
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
		layout(nodes, 0, 0, 200F, 250, 15);
		node.x = 0;
		node.y = 0;
		nodes.add(0, node);
	}

	@Override
	public void renderBackground(PoseStack matrix, int mouseX, int mouseY, float partialTicks) {
		if(pos != null && (pos.x != mouseX || pos.z != mouseY)) {
			int diffX = pos.x - mouseX;
			int diffY = pos.z - mouseY;
			x -= diffX / scale;
			y -= diffY / scale;
			pos = new ChunkPos(mouseX, mouseY);
		}
		renderDirtBackground(0);
		GuiUtils.fillDropArea(matrix, 10, 10, width-20, height-20, -3750202, false);
		GuiUtils.fillDropArea(matrix, 15, 15, width-30, height-30, -7631989, true);
		GuiUtils.renderBackground(15, width-15, 15, height-15, 0F, BackgroundTexture.DEFAULT);
		GuiUtils.pushScissors(15, 15, width-30, height-30);
		matrix.pushPose();
		matrix.translate(centerX, centerY, 0F);
		matrix.scale(scale, scale, 1F);
		int radius = 10;
		float textScale = Math.min(2.5F, 1F / scale);
		float quadScale = Mth.clamp(textScale, 1F, 2F)*0.75F;
		for(ModNode vertex : nodes) {
			int x = (int)(vertex.x + this.x);
			int y = (int)(vertex.y + this.y);
			matrix.pushPose();
			matrix.translate(x, y, 0F);
			matrix.scale(quadScale, quadScale, 1F);
//			fill(matrix, (int)-radius, (int)-radius, (int)+radius, (int)+radius, vertex.modId.hashCode() | 0xFF000000);
			GuiUtils.drawCircle(matrix, 0, 0, radius, vertex.modId.hashCode() | 0xFF000000, 0xFF000000, 128, 1F);
			matrix.popPose();
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
		for(ModNode vertex : nodes) {
			int x = (int)(vertex.x + this.x);
			int y = (int)(vertex.y + this.y);
			matrix.pushPose();
			matrix.translate(x, y, 0F);
			matrix.translate(0F, -radius*textScale-font.lineHeight, 0F);
			matrix.scale(textScale, textScale, 1F);
			drawUnalignedText(matrix, Component.literal(vertex.modId), 0F, 0F, Align.CENTER, -1);
			matrix.popPose();
		}
		matrix.popPose();
		GuiUtils.popScissors();
	}
	
	private void drawNode(PoseStack stack, ModNode source, boolean dep, int requiredColor, int optionalColor, VertexConsumer builder) {
		float textScale = Math.min(2.5F, 1F / scale);
		float quadScale = Mth.clamp(textScale, 1F, 2F)*0.75F;
		float radius = (dep ? 2.5F : -2.5F) * quadScale;
		for(Object2BooleanMap.Entry<ModNode> entry : (dep ? source.dependencies : source.dependants).object2BooleanEntrySet()) {
			ModNode child = entry.getKey();
			GuiUtils.drawLine(stack, (float)source.x+this.x+radius, (float)source.y+this.y+radius, (float)child.x+this.x+radius, (float)child.y+this.y+radius, 2F, builder, entry.getBooleanValue() ? requiredColor : optionalColor);
			if(entry.getBooleanValue()) drawNode(stack, child, dep, requiredColor, optionalColor, builder);
		}
	}
	
	@Override
	public void collectTooltips(PoseStack matrix, int mouseX, int mouseY, float partialTicks, Consumer<Component> tooltips) {
		double radius = 10 * scale;
		float xOff = x * scale;
		float yOff = y * scale;
		for(ModNode vertex : nodes) {
			int x = (int)(vertex.x * scale) + centerX;
			int y = (int)(vertex.y * scale) + centerY;
			if(mouseX >= x-radius+xOff && mouseX <= x+radius+xOff && mouseY >= y-radius+yOff && mouseY <= y+radius+yOff) {
				tooltips.accept(Component.literal(vertex.modId));
			}
		}
	}
	
	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if(super.mouseClicked(mouseX, mouseY, button)) return true;
		double radius = 10 * scale;
		float xOff = x * scale;
		float yOff = y * scale;
		for(ModNode vertex : nodes) {
			int x = (int)(vertex.x * scale) + centerX;
			int y = (int)(vertex.y * scale) + centerY;
			if(mouseX >= x-radius+xOff && mouseX <= x+radius+xOff && mouseY >= y-radius+yOff && mouseY <= y+radius+yOff) {
				focused = vertex;
				return true;
			}
		}
		if(button == 0) {
			pos = new ChunkPos((int)mouseX, (int)mouseY);
			return true;
		}
		focused = null;
		return false;
	}
	
	@Override
	public boolean mouseReleased(double pMouseX, double pMouseY, int pButton) {
		if(pos != null) {
			pos = null;
			return true;
		}
		if(super.mouseReleased(pMouseX, pMouseY, pButton)) return true;
		return false;
	}
	
	@Override
	public boolean mouseScrolled(double pMouseX, double pMouseY, double pDelta) {
		scale = Mth.clamp(scale - (float)pDelta * 0.01F, 0.025F, 2F);
		return true;
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
