package carbonconfiglib.gui.impl.minecraft;

import java.util.List;

import carbonconfiglib.api.ConfigType;
import carbonconfiglib.gui.api.BackgroundTexture;
import carbonconfiglib.gui.api.BackgroundTexture.BackgroundHolder;
import carbonconfiglib.gui.api.IModConfig;
import carbonconfiglib.gui.api.IModConfigs;
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
public class MinecraftConfigs implements IModConfigs
{

	@Override
	public String getModName() {
		return "Minecraft";
	}
	
	@Override
	public List<IModConfig> getConfigInstances(ConfigType type) {
		return type == ConfigType.SERVER ? ObjectLists.singleton(new MinecraftConfig()) : ObjectLists.empty();
	}
	
	@Override
	public BackgroundHolder getBackground() {
		return BackgroundTexture.DEFAULT.asHolder();
	}
	
}
