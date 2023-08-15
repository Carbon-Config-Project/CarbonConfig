package carbonconfiglib.impl.internal;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.IntConsumer;

import carbonconfiglib.api.ConfigType;
import carbonconfiglib.config.ConfigHandler;
import carbonconfiglib.gui.api.BackgroundTexture;
import carbonconfiglib.gui.api.BackgroundTexture.Builder;
import carbonconfiglib.gui.api.IModConfig;
import carbonconfiglib.gui.api.IModConfigs;
import carbonconfiglib.gui.impl.carbon.ModConfig;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.CustomValue;
import net.fabricmc.loader.api.metadata.CustomValue.CvObject;
import net.fabricmc.loader.api.metadata.CustomValue.CvType;

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
		
	public static ModConfigs of(ModContainer container, ConfigHandler handler) {
		ModConfigs configs = new ModConfigs(container);
		configs.addConfig(handler);
		return configs;
	}
	
	@Override
	public String getModName() {
		return container.getMetadata().getName();
	}
	
	public void addConfig(ConfigHandler config) {
		knownConfigs.add(config);
	}
		
	public List<IModConfig> getConfigInstances(ConfigType type) {
		List<IModConfig> instance = new ObjectArrayList<>();
		for(ConfigHandler handler : knownConfigs) {
			if(!handler.isRegistered() || handler.getConfigType() != type) continue;
			instance.add(new ModConfig(container.getMetadata().getId(), handler));
		}
		instance.sort(Comparator.comparing(IModConfig::getConfigName, String.CASE_INSENSITIVE_ORDER));
		return instance;
	}

	@Override
	public BackgroundTexture getBackground() {
		BackgroundTexture texture = IModConfigs.TEXTURE_REGISTRY.get(container);
		if(texture != null) return texture;
		return computeTexture(container).orElse(BackgroundTexture.DEFAULT);
	}
	
	public static Optional<BackgroundTexture> computeTexture(ModContainer container) {
		CustomValue value = container.getMetadata().getCustomValue("guiconfig");
		if(value != null && value.getType() == CvType.OBJECT) {
			CvObject obj = value.getAsObject();
			if(obj.containsKey("texture")) {
				Builder builder = BackgroundTexture.of();
				parseString(obj, "texture", builder::withTexture);
				parseNumber(obj, "brightness", builder::withBrightness);
				return Optional.of(builder.build());
			}
			if(obj.containsKey("background")) {
				Builder builder = BackgroundTexture.of();
				parseString(obj, "background", builder::withTexture);
				parseString(obj, "foreground", builder::withForeground);
				parseNumber(obj, "brightness", builder::withBrightness);
				parseNumber(obj, "background_brightness", builder::withBackground);
				parseNumber(obj, "foreground_brightness", builder::withForeground);
				return Optional.of(builder.build());
			}
		}
		return Optional.empty();
	}
	
	private static void parseString(CvObject obj, String key, Consumer<String> value) { 
		CustomValue result = obj.get(key);
		if(result != null && result.getType() == CvType.STRING) {
			value.accept(result.getAsString());
		}
	}
	
	private static void parseNumber(CvObject obj, String key, IntConsumer value) { 
		CustomValue result = obj.get(key);
		if(result != null) {
			if(result.getType() == CvType.NUMBER) {
				value.accept(result.getAsNumber().intValue());
			}
			else if(result.getType() == CvType.STRING) {
				value.accept(Integer.parseInt(result.getAsString()));
			}
		}
	}
}