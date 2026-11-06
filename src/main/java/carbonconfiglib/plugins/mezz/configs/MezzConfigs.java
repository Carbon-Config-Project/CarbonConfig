package carbonconfiglib.plugins.mezz.configs;

import java.util.List;

import carbonconfiglib.api.ConfigType;
import carbonconfiglib.gui.api.IModConfig;
import carbonconfiglib.gui.api.IModConfigs;
import carbonconfiglib.gui.api.background.BackgroundTexture;
import carbonconfiglib.gui.api.background.BackgroundTexture.BackgroundHolder;
import carbonconfiglib.gui.impl.carbon.ModConfigs;
import net.fabricmc.loader.api.ModContainer;
import net.mezzdev.config.api.schema.IConfigSchema;
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
public class MezzConfigs implements IModConfigs
{
	ModContainer container;
	List<IConfigSchema> configs;
	
	public MezzConfigs(ModContainer container, List<IConfigSchema> configs) {
		this.container = container;
		this.configs = configs;
	}

	@Override
	public String getModName() {
		return container.getMetadata().getName();
	}
	
	@Override
	public List<IModConfig> getConfigInstances(ConfigType type) {
		List<IModConfig> config = new ObjectArrayList<>();
		for(IConfigSchema file : configs) {
			if(MezzHelpers.toType(file.getType()) != type || !file.isActive()) continue;
			config.add(new MezzConfig(file));
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
