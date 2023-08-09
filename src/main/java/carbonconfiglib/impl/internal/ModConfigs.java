package carbonconfiglib.impl.internal;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import com.electronwill.nightconfig.core.UnmodifiableConfig;

import carbonconfiglib.api.ConfigType;
import carbonconfiglib.config.ConfigHandler;
import carbonconfiglib.gui.api.BackgroundTexture;
import carbonconfiglib.gui.api.BackgroundTexture.Builder;
import carbonconfiglib.gui.api.IModConfig;
import carbonconfiglib.gui.api.IModConfigs;
import carbonconfiglib.gui.impl.carbon.ModConfig;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModLoadingContext;

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
		return new ModConfigs(ModLoadingContext.get().getActiveContainer());
	}
	
	public static ModConfigs of(ConfigHandler handler) {
		ModConfigs configs = new ModConfigs(ModLoadingContext.get().getActiveContainer());
		configs.addConfig(handler);
		return configs;
	}
	
	@Override
	public String getModName() {
		return container.getModInfo().getDisplayName();
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
	public BackgroundTexture getBackground() {
		Optional<Background> texture = container.getCustomExtension(IModConfigs.Background.class);
		if(texture.isPresent()) return texture.get().texture();
		return computeTexture(container).orElse(BackgroundTexture.DEFAULT);
	}
	
	public static Optional<BackgroundTexture> computeTexture(ModContainer container) {
		Object obj = container.getModInfo().getModProperties().get("guiconfig");;
		if(obj instanceof UnmodifiableConfig) {
			UnmodifiableConfig config = (UnmodifiableConfig)obj;
			if(config != null) {
				if(config.contains("texture")) {
					Builder builder = BackgroundTexture.of((String)config.get("texture"));
					if(config.contains("brightness")) builder.withBrightness(Integer.parseInt(config.get("brightness")));
					return Optional.of(builder.build());
				}
				if(config.contains("background")) {
					Builder builder = BackgroundTexture.of((String)config.get("background"));
					if(config.contains("foreground")) builder.withForeground((String)config.get("foreground"));
					if(config.contains("brightness")) builder.withBrightness(Integer.parseInt(config.get("brightness")));
					if(config.contains("background_brightness")) builder.withBackground(Integer.parseInt(config.get("background_brightness")));
					if(config.contains("foreground_brightness")) builder.withForeground(Integer.parseInt(config.get("foreground_brightness")));
					return Optional.of(builder.build());
				}
			}
		}
		return Optional.empty();
	}
}