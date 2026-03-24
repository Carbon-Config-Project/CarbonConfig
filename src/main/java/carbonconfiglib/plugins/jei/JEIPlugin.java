package carbonconfiglib.plugins.jei;

import java.util.function.Consumer;

import carbonconfiglib.gui.api.IModConfigs;
import carbonconfiglib.plugins.ICarbonPlugin;
import carbonconfiglib.plugins.jei.configs.JEIConfigs;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.runtime.config.IJeiConfigManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.ModContainer;
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
@JeiPlugin
public class JEIPlugin implements IModPlugin, ICarbonPlugin
{
	private IJeiConfigManager manager;
	
	public JEIPlugin() {
		ICarbonPlugin.registerPlugin("jei", this);
	}
	
	@Override
	public ResourceLocation getPluginUid() {
		return ResourceLocation.fromNamespaceAndPath("carbonconfig", "jeiplugin");
	}
	
	@Override
	public void onConfigManagerAvailable(IJeiConfigManager configManager) {
		this.manager = configManager;
	}

	@Override
	public void applyConfigs(ModContainer container, Consumer<IModConfigs> configs) {
		if(manager == null) return;
		configs.accept(new JEIConfigs(container, new ObjectArrayList<>(manager.getConfigFiles())));
	}
}
