package carbonconfiglib.gui.screens;

import java.util.Set;

import carbonconfiglib.gui.api.IModConfigs;
import cpw.mods.fml.client.IModGuiFactory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;

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
public class ConfigScreenFactory implements IModGuiFactory
{
	private static final ThreadLocal<IModConfigs> OPENING_CONFIGS = new ThreadLocal<>();
	IModConfigs configs;

	public ConfigScreenFactory(IModConfigs configs) {
		this.configs = configs;
	}

	@Override
	public void initialize(Minecraft minecraftInstance) {}
	public GuiScreen createConfigGui(GuiScreen parentScreen) { return new ConfigListScreen(parentScreen, configs); }
	@Override
	public Set<RuntimeOptionCategoryElement> runtimeGuiCategories() { return null; }
	@Override
	public Class<? extends GuiScreen> mainConfigGuiClass() {
		OPENING_CONFIGS.set(configs);
		return EntryScreen.class;
	}

	public static class EntryScreen extends ConfigListScreen {
        public EntryScreen(GuiScreen parentScreen) {
            super(parentScreen, takeConfigs());
        }
    }

	private static IModConfigs takeConfigs() {
        IModConfigs configs = OPENING_CONFIGS.get();
        OPENING_CONFIGS.remove();
        if (configs == null) {
            throw new IllegalStateException("GUI opened without prepared configs");
        }
        return configs;
    }

	@Override
	public RuntimeOptionGuiHandler getHandlerFor(RuntimeOptionCategoryElement element) { return null; }	
}
