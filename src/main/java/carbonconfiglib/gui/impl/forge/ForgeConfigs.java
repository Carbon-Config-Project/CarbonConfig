package carbonconfiglib.gui.impl.forge;

import java.util.List;

import carbonconfiglib.api.ConfigType;
import carbonconfiglib.gui.api.BackgroundTexture;
import carbonconfiglib.gui.api.BackgroundTexture.BackgroundHolder;
import carbonconfiglib.gui.api.IModConfig;
import carbonconfiglib.gui.api.IModConfigs;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.ModContainer;
import speiger.src.collections.objects.lists.ObjectArrayList;
import speiger.src.collections.objects.utils.ObjectLists;

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
public class ForgeConfigs implements IModConfigs
{
	ModContainer container;
	List<Configuration> configs;
	
	public ForgeConfigs(ModContainer container, List<Configuration> configs) {
		this.container = container;
		this.configs = configs;
	}

	@Override
	public String getModName() { return container.getName(); }
	@Override
	public BackgroundHolder getBackground() { return BackgroundTexture.DEFAULT.asHolder(); }
	@Override
	public List<IModConfig> getConfigInstances(ConfigType type) {
		if(type != ConfigType.SHARED) return ObjectLists.empty();
		List<IModConfig> result = new ObjectArrayList<>();
		for(Configuration config : configs) {
			result.add(new ForgeConfig(container, config));
		}
		return result;
	}
}
