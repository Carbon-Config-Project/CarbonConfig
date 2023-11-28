package carbonconfiglib.gui.impl.forge;

import java.util.EnumMap;
import java.util.List;
import java.util.Optional;

import carbonconfiglib.api.ConfigType;
import carbonconfiglib.gui.api.BackgroundTexture;
import carbonconfiglib.gui.api.BackgroundTexture.BackgroundHolder;
import carbonconfiglib.gui.api.IModConfig;
import carbonconfiglib.gui.api.IModConfigs;
import carbonconfiglib.impl.internal.ModConfigs;
import it.unimi.dsi.fastutil.objects.ObjectLists;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.forgespi.language.IModInfo;

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
	EnumMap<ModConfig.Type, ModConfig> configs;
	
	public ForgeConfigs(ModContainer container) {
		this.container = container;
		configs = ObfuscationReflectionHelper.getPrivateValue(ModContainer.class, container, "configs");
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
		ModConfig config = configs.get(fromType(type));
		return config == null ? ObjectLists.emptyList() : ObjectLists.singleton(new ForgeConfig(config));
	}
	
	@Override
	public BackgroundHolder getBackground() {
		Optional<BackgroundTexture> texture = container.getCustomExtension(IModConfigs.BACKGROUND);
		if(texture.isPresent()) return texture.get().asHolder();
		Optional<BackgroundTexture> carbon_Texture = ModConfigs.computeTexture(container);
		if(carbon_Texture.isPresent()) return carbon_Texture.get().asHolder();
		return getBackgroundTexture(container.getModInfo()).asHolder();
	}
	
	private static BackgroundTexture getBackgroundTexture(IModInfo info) {
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
