package carbonconfiglib.impl.internal;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import carbonconfiglib.CarbonConfig;
import carbonconfiglib.api.IEntrySettings;
import carbonconfiglib.gui.api.node.ConfigPath;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.IResourceManagerReloadListener;
import net.minecraft.util.ResourceLocation;
import speiger.src.collections.objects.lists.ObjectArrayList;
import speiger.src.collections.objects.maps.interfaces.Object2ObjectMap;

public class SettingsLoader implements IResourceManagerReloadListener
{
	public static final SettingsLoader INSTANCE = new SettingsLoader();
	Map<ResourceLocation, Function<JsonObject, IEntrySettings>> parsers = Object2ObjectMap.builder().map();
	Map<String, IEntrySettings> settings = Object2ObjectMap.builder().map();
	
	public void registerParser(ResourceLocation id, Function<JsonObject, IEntrySettings> parser) {
		Objects.requireNonNull(id);
		Objects.requireNonNull(parser);
		parsers.put(id, parser);
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public void onResourceManagerReload(IResourceManager resourceManager) {
		settings.clear();
		int[] totalOverrides = new int[1];
		JsonParser parse = new JsonParser();
		List<ResourceLocation> modStuff = new ObjectArrayList<>();
		try {
			for(IResource resource : (List<IResource>)resourceManager.getAllResources(new ResourceLocation("carbonconfig", "carbonoverrides/override.json"))) {
				JsonObject obj = parse.parse(new InputStreamReader(resource.getInputStream())).getAsJsonObject();
				if(!obj.has("overrides")) continue;
				for(JsonElement el : obj.getAsJsonArray("overrides")) {
					try { modStuff.add(new ResourceLocation(el.getAsString())); }
					catch(Exception e) { e.printStackTrace(); }
				}
			}
			for(ResourceLocation id : modStuff) {
				try
				{
					iterate(parse.parse(new InputStreamReader(resourceManager.getResource(id).getInputStream())).getAsJsonObject(), id.getResourceDomain(), (K, V) -> {
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
				catch(Exception e)
				{
					e.printStackTrace();
				}
			}
		}
		catch(IOException e) {
			e.printStackTrace();
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
