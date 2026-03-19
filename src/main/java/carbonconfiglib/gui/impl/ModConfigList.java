package carbonconfiglib.gui.impl;

import java.util.List;

import carbonconfiglib.api.ConfigType;
import carbonconfiglib.gui.api.IModConfig;
import carbonconfiglib.gui.api.IModConfigs;
import carbonconfiglib.gui.api.background.BackgroundTexture;
import carbonconfiglib.gui.api.background.BackgroundTexture.BackgroundHolder;
import net.fabricmc.loader.api.ModContainer;
import speiger.src.collections.objects.lists.ObjectArrayList;

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
public class ModConfigList implements IModConfigs
{
	ModContainer container;
	List<IModConfigs> configs;
	
	public ModConfigList(ModContainer container, List<IModConfigs> configs) {
		this.container = container;
		this.configs = configs;
	}
	
	public static IModConfigs createMultiIfApplicable(ModContainer container, List<IModConfigs> configs) {
		return configs.size() == 1 ? configs.get(0) : new ModConfigList(container, configs);
	}
	
	@Override
	public String getModName() {
		return container.getMetadata().getName();
	}
	
	@Override
	public List<IModConfig> getConfigInstances(ConfigType type) {
		List<IModConfig> configs = new ObjectArrayList<>();
		for(IModConfigs config : this.configs) {
			configs.addAll(config.getConfigInstances(type));
		}
		return configs;
	}
	
	@Override
	public BackgroundHolder getBackground() {
		for(IModConfigs config : configs) {
			BackgroundHolder texture = config.getBackground();
			if(texture.getTexture() != BackgroundTexture.DEFAULT) return texture;
		}
		return BackgroundTexture.DEFAULT.asHolder();
	}
}
