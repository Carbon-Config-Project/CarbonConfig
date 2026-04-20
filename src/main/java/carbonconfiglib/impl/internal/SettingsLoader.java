package carbonconfiglib.impl.internal;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;

import carbonconfiglib.CarbonConfig;
import carbonconfiglib.api.IEntrySettings;
import carbonconfiglib.gui.api.node.ConfigPath;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import speiger.src.collections.objects.maps.interfaces.Object2ObjectMap;

public class SettingsLoader extends SimpleJsonResourceReloadListener<Map<String, IEntrySettings>>
{
	public static final SettingsLoader INSTANCE = new SettingsLoader();
	Map<Identifier, Function<JsonObject, IEntrySettings>> parsers = Object2ObjectMap.builder().map();
	Map<String, IEntrySettings> settings = Object2ObjectMap.builder().map();
	
	public SettingsLoader() {
		super(createCodec(T -> INSTANCE.parsers.get(T)), FileToIdConverter.json("carbonoverrides"));
	}
	
	public void registerParser(Identifier id, Function<JsonObject, IEntrySettings> parser) {
		Objects.requireNonNull(id);
		Objects.requireNonNull(parser);
		parsers.put(id, parser);
	}
	
	@Override
	protected void apply(Map<Identifier, Map<String, IEntrySettings>> preparations, ResourceManager manager, ProfilerFiller profiler) {
		settings.clear();
		preparations.entrySet().stream().peek(T -> CarbonConfig.LOGGER.info("Testing ID=["+T.getKey()+"]\n")).flatMap(T -> T.getValue().entrySet().stream()).forEach(T -> {
			CarbonConfig.LOGGER.info("Path=["+T.getKey()+"], Class=["+T.getValue().getClass()+"]");
		});
	}
	
	
	private static Codec<Map<String, IEntrySettings>> createCodec(Function<Identifier, Function<JsonObject, IEntrySettings>> parserLookup) {
		return Codec.unboundedMap(Codec.STRING, Codec.PASSTHROUGH).comapFlatMap(T -> {
			Map<String, IEntrySettings> result = Object2ObjectMap.builder().map();
			collectEntries("", T, result, parserLookup);
			return DataResult.success(result);
		}, _ -> null);
	}
	
	private static void collectEntries(String path, Map<String, Dynamic<?>> map, Map<String, IEntrySettings> results, Function<Identifier, Function<JsonObject, IEntrySettings>> parserLookup) {
		map.forEach((S, D) -> {
			String currentPath = path.isEmpty() ? S : path + "." + S;
	        JsonElement json = D.convert(JsonOps.INSTANCE).getValue();
	        if(!json.isJsonObject()) return;
	        iterate(json.getAsJsonObject(), currentPath, (K, V) -> {
				if(!V.has("id")) return;
				Identifier id = Identifier.tryParse(V.get("id").getAsString());
				if(id == null) return;
				Function<JsonObject, IEntrySettings> parser = parserLookup.apply(id);
				if(parser == null) return;
				try {
					IEntrySettings setting = parser.apply(V);
					if(setting == null) return;
					results.merge(K, setting, IEntrySettings::merge);
				}
				catch(Exception e) {
					e.printStackTrace();
				}
	        });
		});
	}
	
//	@Override
//	protected void apply(Map<Identifier, JsonElement> pObject, ResourceManager pResourceManager, ProfilerFiller pProfiler) {
//		settings.clear();
//		int[] totalOverrides = new int[1];
//		for(Entry<Identifier, JsonElement> entry : pObject.entrySet()) {
//			JsonElement values = entry.getValue();
//			if(values.isJsonObject()) {
//				iterate(values.getAsJsonObject(), entry.getKey().getNamespace(), (K, V) -> {
//					if(!V.has("id")) return;
//					Identifier id = Identifier.tryParse(V.get("id").getAsString());
//					if(id == null) return;
//					Function<JsonObject, IEntrySettings> parser = parsers.get(id);
//					if(parser == null) return;
//					try {
//						IEntrySettings setting = parser.apply(V);
//						if(setting == null) return;
//						settings.merge(K, setting, IEntrySettings::merge);
//						totalOverrides[0]++;
//					}
//					catch(Exception e) {
//						e.printStackTrace();
//					}
//				});
//			}
//		}
//		CarbonConfig.LOGGER.info("Loaded ["+totalOverrides[0]+"] overrides loaded");
//	}
	
	public IEntrySettings getOverride(ConfigPath path) {
		return path == null ? null : settings.get(path.toPath());
	}
	
	private static void iterate(JsonObject source, String path, BiConsumer<String, JsonObject> result) {
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
	
	private static boolean hasOnlyValues(JsonObject object) {
		for(Entry<String, JsonElement> entry : object.entrySet()) {
			if(entry.getValue().isJsonPrimitive()) return true;
		}
		return false;
	}
}
