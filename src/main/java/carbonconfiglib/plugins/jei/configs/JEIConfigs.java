package carbonconfiglib.plugins.jei.configs;

import java.util.List;

import carbonconfiglib.api.ConfigType;
import carbonconfiglib.gui.api.IModConfig;
import carbonconfiglib.gui.api.IModConfigs;
import carbonconfiglib.gui.api.background.BackgroundTexture;
import carbonconfiglib.gui.api.background.BackgroundTexture.BackgroundHolder;
import carbonconfiglib.gui.impl.carbon.ModConfigs;
import mezz.jei.api.runtime.config.IJeiConfigFile;
import net.fabricmc.loader.api.ModContainer;
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
		BackgroundTexture texture = IModConfigs.TEXTURE_REGISTRY.get(container);
		if(texture != null) return texture.asHolder();
		return ModConfigs.computeTexture(container).orElse(BackgroundTexture.DEFAULT).asHolder();
	}
	
}
