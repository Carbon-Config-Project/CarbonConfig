package carbonconfiglib.impl.internal;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import carbonconfiglib.api.ConfigType;
import carbonconfiglib.config.ConfigHandler;
import carbonconfiglib.gui.api.BackgroundTexture;
import carbonconfiglib.gui.api.BackgroundTexture.BackgroundHolder;
import carbonconfiglib.gui.api.BackgroundTexture.Builder;
import carbonconfiglib.gui.api.IModConfig;
import carbonconfiglib.gui.api.IModConfigs;
import carbonconfiglib.gui.impl.carbon.ModConfig;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.ModContainer;
import speiger.src.collections.objects.lists.ObjectArrayList;
import speiger.src.collections.objects.maps.impl.hash.Object2ObjectOpenHashMap;

/**
 * Copyright 2023 Speiger, Meduris
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
public class ModConfigs implements IModConfigs
{
	ModContainer container;
	List<ConfigHandler> knownConfigs = new ObjectArrayList<>();
	
	public ModConfigs(ModContainer container) {
		this.container = container;
	}
	
	public static ModConfigs of() {
		return new ModConfigs(Loader.instance().activeModContainer());
	}
	
	public static ModConfigs of(ConfigHandler handler) {
		ModConfigs configs = new ModConfigs(Loader.instance().activeModContainer());
		configs.addConfig(handler);
		return configs;
	}
	
	@Override
	public String getModName() {
		return container.getName();
	}
	
	public void addConfig(ConfigHandler config) {
		knownConfigs.add(config);
	}
		
	public List<IModConfig> getConfigInstances(ConfigType type) {
		List<IModConfig> instance = new ObjectArrayList<>();
		for(ConfigHandler handler : knownConfigs) {
			if(!handler.isRegistered() || handler.getConfigType() != type) continue;
			instance.add(new ModConfig(container.getModId(), handler));
		}
		instance.sort(Comparator.comparing(IModConfig::getConfigName, String.CASE_INSENSITIVE_ORDER));
		return instance;
	}

	@Override
	public BackgroundHolder getBackground() {
		BackgroundTexture texture = IModConfigs.TEXTURE_REGISTRY.get(container);
		if(texture != null) return texture.asHolder();
		return computeTexture(container).orElse(BackgroundTexture.DEFAULT).asHolder();
	}
	
	public static Optional<BackgroundTexture> computeTexture(ModContainer container) {
		String[] values = container.getCustomModProperties().getOrDefault("guiconfig", "").split(";");
		if(values.length < 2) return Optional.empty();
		Map<String, String> arguments = new Object2ObjectOpenHashMap<>();
		for(int i = 0,m=values.length;i+1<m;i+=2) {
			arguments.put(values[i], values[i+1]);
		}
		if(arguments.containsKey("texture")) {
			Builder builder = BackgroundTexture.of(arguments.get("texture"));
			if(arguments.containsKey("brightness")) builder.withBrightness(Integer.parseInt(arguments.get("brightness")));
			return Optional.of(builder.build());
		}
		if(arguments.containsKey("background")) {
			Builder builder = BackgroundTexture.of(arguments.get("background"));
			if(arguments.containsKey("foreground")) builder.withForeground(arguments.get("foreground"));
			if(arguments.containsKey("brightness")) builder.withBrightness(Integer.parseInt(arguments.get("brightness")));
			if(arguments.containsKey("background_brightness")) builder.withBackground(Integer.parseInt(arguments.get("background_brightness")));
			if(arguments.containsKey("foreground_brightness")) builder.withForeground(Integer.parseInt(arguments.get("foreground_brightness")));
			return Optional.of(builder.build());
		}
		return Optional.empty();
	}
}