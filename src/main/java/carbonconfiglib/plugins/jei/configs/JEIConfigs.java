package carbonconfiglib.plugins.jei.configs;

import java.util.List;
import java.util.Optional;

import carbonconfiglib.api.ConfigType;
import carbonconfiglib.gui.api.IModConfig;
import carbonconfiglib.gui.api.IModConfigs;
import carbonconfiglib.gui.api.background.BackgroundTexture;
import carbonconfiglib.gui.api.background.BackgroundTexture.BackgroundHolder;
import carbonconfiglib.gui.impl.carbon.ModConfigs;
import mezz.jei.api.runtime.config.IJeiConfigFile;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.loading.moddiscovery.ModInfo;
import net.neoforged.neoforgespi.language.IModInfo;
import speiger.src.collections.objects.lists.ObjectArrayList;
import speiger.src.collections.objects.utils.ObjectLists;

/**
 * Copyright 2026 Speiger, Meduris
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
public class JEIConfigs implements IModConfigs
{
	ModContainer container;
	List<IJeiConfigFile> configs;
	
	public JEIConfigs(ModContainer container, List<IJeiConfigFile> configs) {
		this.container = container;
		this.configs = configs;
	}

	@Override
	public String getModName() {
		return "Jei";
	}
	
	@Override
	public List<IModConfig> getConfigInstances(ConfigType type) {
		if(type != ConfigType.CLIENT) return ObjectLists.empty();
		List<IModConfig> config = new ObjectArrayList<>();
		for(IJeiConfigFile file : configs) {
			config.add(new JEIConfig(file));
		}
		return config;
	}
	
	@Override
	public BackgroundHolder getBackground() {
		Optional<Background> texture = container.getCustomExtension(IModConfigs.Background.class);
		if(texture.isPresent()) return texture.get().texture().asHolder();
		Optional<BackgroundTexture> carbon_Texture = ModConfigs.computeTexture(container);
		if(carbon_Texture.isPresent()) return carbon_Texture.get().asHolder();
		return getBackgroundTexture(container.getModInfo()).asHolder();
	}
	
	private static BackgroundTexture getBackgroundTexture(final IModInfo info) {
		String configBackground = (String)info.getModProperties().get("configuredBackground");
		if (configBackground != null) {
			return BackgroundTexture.of(configBackground).build();
		}
		if (info instanceof ModInfo) {
			Optional<String> optional = ((ModInfo)info).getConfigElement(new String[] {"configBackground"});
			if (optional.isPresent()) {
				return BackgroundTexture.of(optional.get()).build();
			}
		}
		return BackgroundTexture.DEFAULT;
	}
	
}
