package carbonconfiglib.impl.internal;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import carbonconfiglib.CarbonConfig;
import carbonconfiglib.api.IEntrySettings;
import carbonconfiglib.gui.api.node.ConfigPath;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import speiger.src.collections.objects.maps.interfaces.Object2ObjectMap;

public class SettingsLoader extends SimpleJsonResourceReloadListener
{
	public static final SettingsLoader INSTANCE = new SettingsLoader();
	Map<ResourceLocation, Function<JsonObject, IEntrySettings>> parsers = Object2ObjectMap.builder().map();
	Map<String, IEntrySettings> settings = Object2ObjectMap.builder().map();
	
	public SettingsLoader() {
		super(new Gson(), "carbonoverrides");
	}
	
	public void registerParser(ResourceLocation id, Function<JsonObject, IEntrySettings> parser) {
		Objects.requireNonNull(id);
		Objects.requireNonNull(parser);
		parsers.put(id, parser);
	}

	@Override
	protected void apply(Map<ResourceLocation, JsonElement> pObject, ResourceManager pResourceManager, ProfilerFiller pProfiler) {
		settings.clear();
		int[] totalOverrides = new int[1];
		for(Entry<ResourceLocation, JsonElement> entry : pObject.entrySet()) {
			JsonElement values = entry.getValue();
			if(values.isJsonObject()) {
				iterate(values.getAsJsonObject(), entry.getKey().getNamespace(), (K, V) -> {
					if(!V.has("id")) return;
					ResourceLocation id = ResourceLocation.tryParse(V.get("id").getAsString());
					if(id == null) return;
					Function<JsonObject, IEntrySettings> parser = parsers.get(id);
					if(parser == null) return;
					try {
						IEntrySettings setting = parser.apply(V);
						if(setting == null) return;
						settings.merge(K, setting, IEntrySettings::merge);
						totalOverrides[0]++;
					}
					catch(Exception e) {
						e.printStackTrace();
					}
				});
			}
		}
		CarbonConfig.LOGGER.info("Loaded ["+totalOverrides[0]+"] overrides loaded");
	}
	
	public IEntrySettings getOverride(ConfigPath path) {
		return path == null ? null : settings.get(path.toPath());
	}
	
	private void iterate(JsonObject source, String path, BiConsumer<String, JsonObject> result) {
		if(hasOnlyValues(source)) {
			result.accept(path, source);
			return;
		}
		for(Entry<String, JsonElement> entry : source.entrySet()) {
			JsonElement value = entry.getValue();
			if(value.isJsonObject()) iterate(value.getAsJsonObject(), path+"."+entry.getKey(), result);
			else if(value.isJsonArray()) {
				String key = path+"."+entry.getKey();
				for(JsonElement array : value.getAsJsonArray()) {
					if(array.isJsonObject()) {
						result.accept(key, array.getAsJsonObject());
					}
				}
			}
		}
	}
	
	private boolean hasOnlyValues(JsonObject object) {
		for(Entry<String, JsonElement> entry : object.entrySet()) {
			if(entry.getValue().isJsonPrimitive()) return true;
		}
		return false;
	}
}
