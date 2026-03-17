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
import net.minecraft.client.resources.JsonReloadListener;
import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import speiger.src.collections.objects.maps.interfaces.Object2ObjectMap;

public class SettingsLoader extends JsonReloadListener
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
	protected void apply(Map<ResourceLocation, JsonObject> pObject, IResourceManager pResourceManager, IProfiler pProfiler) {
		settings.clear();
		int[] totalOverrides = new int[1];
		for(Entry<ResourceLocation, JsonObject> entry : pObject.entrySet()) {
			JsonElement values = entry.getValue();
			if(values.isJsonObject()) {
				iterate(values.getAsJsonObject(), entry.getKey().getNamespace(), (K, V) -> {
					if(!V.has("id")) return;
					try {
						Function<JsonObject, IEntrySettings> parser = parsers.get(new ResourceLocation(V.get("id").getAsString()));
						if(parser == null) return;
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
