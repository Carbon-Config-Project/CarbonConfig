package carbonconfiglib.gui.impl.forge;

import java.util.List;
import java.util.Optional;

import carbonconfiglib.api.ConfigType;
import carbonconfiglib.gui.api.IModConfig;
import carbonconfiglib.gui.api.IModConfigs;
import carbonconfiglib.gui.api.background.BackgroundTexture;
import carbonconfiglib.gui.api.background.BackgroundTexture.BackgroundHolder;
import carbonconfiglib.gui.impl.carbon.ModConfigs;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.loading.moddiscovery.ModInfo;
import net.neoforged.neoforgespi.language.IModInfo;
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
	List<ModConfig> configs;
	
	public ForgeConfigs(ModContainer container) {
		this.container = container;
		configs = ForgeHelpers.getConfigs().getOrDefault(container.getModId(), ObjectLists.empty());
	}
		
	public boolean hasConfigs() {
		return !configs.isEmpty();
	}
	
	@Override
	public String getModName() {
		return container.getModInfo().getDisplayName();
	}
	
	@Override
	public List<IModConfig> getConfigInstances(ConfigType type) {
		ModConfig.Type value = fromType(type);
		List<IModConfig> configs = new ObjectArrayList<>();
		for(ModConfig config : this.configs) {
			if(config.getType() == value) {
				configs.add(new ForgeConfig(config));
			}
		}
		return configs;
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
	
	private ModConfig.Type fromType(ConfigType type) {
		switch(type) {
			case CLIENT: return ModConfig.Type.CLIENT;
			case SERVER: return ModConfig.Type.SERVER;
			case SHARED: return ModConfig.Type.COMMON;
			default: throw new UnsupportedOperationException();
		}
	}
}
