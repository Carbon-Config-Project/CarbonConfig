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
import com.mojang.blaze3d.vertex.PoseStack;

import carbonconfiglib.gui.base.screen.BaseCarbonScreen;
import it.unimi.dsi.fastutil.objects.Object2BooleanLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
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
		vertexs.addAll(StreamSupport.stream(provider.getAsJsonArray("mods").spliterator(), false).map(JsonElement::getAsJsonObject).map(T -> T.get("id").getAsString()).map(ModNode::new).toList());
		Map<String, ModNode> mapped = new Object2ObjectOpenHashMap<>();
		vertexs.forEach(T -> mapped.put(T.modId, T));
		
		for(JsonElement element : provider.getAsJsonArray("mods")) {
			JsonObject mod = element.getAsJsonObject();
			if(!mod.has("dep")) continue;
			ModNode owner = mapped.get(mod.get("id").getAsString());
			if(owner == null) continue;
			for(JsonElement dep : mod.getAsJsonArray("dep")) {
				ModNode dependency = mapped.get(dep.getAsString());
				if(dependency == null) continue;
				dependency.dependants.put(owner, false);
				owner.dependencies.put(dependency, false);
			}
		}
	}

	@Override
	public void renderBackground(PoseStack matrix, int mouseX, int mouseY, float partialTicks) {
		renderBackground(matrix);
		matrix.pushPose();
		matrix.translate(centerX, centerY, 0F);
		matrix.scale(scale, scale, 1F);
		for(ModNode vertex : vertexs) {
			int x = (int)vertex.x;
			int y = (int)vertex.y;
			fill(matrix, (int)x-5, (int)y-5, (int)x+5, (int)y+5, vertex.modId.hashCode() | 0xFF000000);
		}
		matrix.popPose();
	}
	
	@Override
	public void collectTooltips(PoseStack matrix, int mouseX, int mouseY, float partialTicks, Consumer<Component> tooltips) {
		for(ModNode vertex : vertexs) {
			int x = (int)(vertex.x * scale) + centerX;
			int y = (int)(vertex.y * scale) + centerY;
			if(mouseX >= x-5 && mouseX <= x+5 && mouseY >= y-5 && mouseY <= y+5) {
				tooltips.accept(Component.literal(vertex.modId));
			}
		}
	}
	
	@Override
	public boolean mouseScrolled(double pMouseX, double pMouseY, double pDelta) {
		scale = Mth.clamp(scale - (float)pDelta * 0.01F, 0.001F, 10F);
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

		@Override
		public int hashCode() {
			return modId.hashCode();
		}
		
		@Override
		public boolean equals(Object obj) {
			return obj instanceof ModNode && ((ModNode)obj).modId.equals(modId);
		}
	}
	//TODO implement functions.
}
