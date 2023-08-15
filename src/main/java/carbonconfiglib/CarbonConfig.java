package carbonconfiglib;

import java.util.List;
import java.util.function.Consumer;

import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import carbonconfiglib.api.ConfigType;
import carbonconfiglib.config.Config;
import carbonconfiglib.config.ConfigEntry.BoolValue;
import carbonconfiglib.config.ConfigHandler;
import carbonconfiglib.config.ConfigSettings;
import carbonconfiglib.config.FileSystemWatcher;
import carbonconfiglib.impl.PerWorldProxy;
import carbonconfiglib.impl.ReloadMode;
import carbonconfiglib.impl.entries.ColorValue;
import carbonconfiglib.impl.entries.RegistryKeyValue;
import carbonconfiglib.impl.entries.RegistryValue;
import carbonconfiglib.impl.internal.ConfigLogger;
import carbonconfiglib.impl.internal.EventHandler;
import carbonconfiglib.networking.CarbonNetwork;
import carbonconfiglib.utils.AutomationType;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.impl.client.keybinding.KeyBindingRegistryImpl;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.screens.Screen;
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
public class CarbonConfig implements ModInitializer
{
	public static final Logger LOGGER = LogUtils.getLogger();
	private static final FileSystemWatcher CONFIGS = new FileSystemWatcher(new ConfigLogger(LOGGER), FabricLoader.getInstance().getConfigDir(), EventHandler.INSTANCE);
	public static final CarbonNetwork NETWORK = new CarbonNetwork();
	private static final List<Runnable> LATE_INIT = ObjectLists.synchronize(new ObjectArrayList<>());
	private static boolean REGISTRIES_LOADED = false;
	ConfigHandler handler;
	public static BoolValue MOD_MENU_SUPPORT; 
	
	@Override
	public void onInitialize()
	{
		NETWORK.init();
		if(FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) EventHandler.INSTANCE.initClientEvents(this::onClientLoad, this::registerKeys);
		else EventHandler.INSTANCE.initServerEvents(this::onCommonLoad);
		ServerLifecycleEvents.SERVER_STARTING.register(T -> load());
		ServerLifecycleEvents.SERVER_STOPPING.register(T -> unload());
		
		if(FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
			Config config = new Config("carbonconfig");
			MOD_MENU_SUPPORT = config.add("general").addBool("enable-modmenu-support", true, "Enables that CarbonConfig automatically adds Mod Menu Support for all Carbon Configs").setRequiredReload(ReloadMode.GAME);
			handler = createConfig("carbonconfig", config, ConfigSettings.withConfigType(ConfigType.CLIENT).withAutomations(AutomationType.AUTO_LOAD));
			handler.register();
		}
	}
	
	public static ConfigHandler createConfig(String modId, Config config) {
		EventHandler.ACTIVE_MOD.set(FabricLoader.getInstance().getModContainer(modId).get());
		try {
			return CONFIGS.createConfig(config);
		}
		finally {
			EventHandler.ACTIVE_MOD.set(null);
		}
	}
	
	public static ConfigHandler createConfig(String modId, Config config, ConfigSettings settings) {
		EventHandler.ACTIVE_MOD.set(FabricLoader.getInstance().getModContainer(modId).get());
		try {
			return CONFIGS.createConfig(config, settings);
		}
		finally {
			EventHandler.ACTIVE_MOD.set(null);
		}
	}

	public static FileSystemWatcher getConfigs() {
		return CONFIGS;
	}
	
	/**
	 * Creates a Setting with a PerWorld Proxy set by default.
	 * And sets the config to be loaded at the right time!
	 * @return ConfigSettings with PerWorld Proxy being set
	 */
	public static ConfigSettings getPerWorldProxy() {
		return PerWorldProxy.perWorld();
	}
	
	/**
	 * Creates a Setting that will allow Late Loading more easily.
	 * @return ConfigSetting with just sync/Auto reload
	 * @apiNote Not required for a Per World Config
	 */
	public ConfigSettings createLateLoadSettings() {
		return ConfigSettings.withSettings(AutomationType.AUTO_RELOAD, AutomationType.AUTO_SYNC);
	}
	
	/**
	 * Creates a Config that is dedicated for color.
	 * It saves the Entry in Hex instead a normal number allowing to set RGB a lot easier and understand it nicer.
	 * On top of that the Ingame Gui renders the Color next to the config value.
	 * @param key the name of the config
	 * @param color the default value
	 * @param comments what the config entry does
	 * @return a ColorValue
	 */
	public static ColorValue createColor(String key, int color, String...comments) {
		return new ColorValue(key, color, comments);
	}
	
	/**
	 * Creates a ConfigBuilder that contains a Set of "Registry Keys" (ResourceLocation).
	 * The idea behind that is you might want a filter or something about a specific Type of Registry Element.
	 * Compared to the RegistryEntry this doesn't actually store the "Registry Instances" but only the Ids.
	 * @param <E> the Class-Type for Config Gui rendering.
	 * @param key the name of the config
	 * @param clz the Class-Type for Config Gui rendering.
	 * @return a Builder for registry Keys
	 */
	public static <E> RegistryKeyValue.Builder<E> createRegistryKeyBuilder(String key, Class<E> clz) {
		return RegistryKeyValue.builder(key, clz);
	}
	
	/**
	 * Creates a ConfigBuilder that contains a Set of "Registry Elements" (i.e. Item/Block/Fluid/Enchantment).
	 * The idea behind that is you might want a filter or something about a specific Type of Registry Element.
	 * Compared to the RegistryKeyEntry this actually stores the "Registry Instances". Not the Ids
	 * @param <E> the Class-Type for Config Gui rendering.
	 * @param key the name of the config
	 * @param clz the Class-Type for Config Gui rendering.
	 * @return a Builder for registry Keys
	 */
	public static <E> RegistryValue.Builder<E> createRegistryBuilder(String key, Class<E> clz) {
		return RegistryValue.builder(key, clz);
	}
	
	public static void runAfterRegistries(Runnable run) {
		if(REGISTRIES_LOADED) {
			run.run();
			return;
		}
		LATE_INIT.add(run);
	}
	
	public void onCommonLoad() {
		REGISTRIES_LOADED = true;
		LATE_INIT.forEach(Runnable::run);
		LATE_INIT.clear();
		for(ConfigHandler handler : CONFIGS.getAllConfigs()) {
			if(PerWorldProxy.isProxy(handler.getProxy())) {
				handler.createDefaultConfig();
			}
		}
	}
	
	public void onClientLoad() {
		onCommonLoad();
		EventHandler.INSTANCE.onConfigsLoaded();
	}
	
	@Environment(EnvType.CLIENT)
	public void registerKeys() {
		if(FabricLoader.getInstance().isModLoaded("modmenu")) {
			KeyMapping mapping = new KeyMapping("key.carbon_config.key", GLFW.GLFW_KEY_KP_ENTER, "key.carbon_config");
			KeyBindingRegistryImpl.registerKeyBinding(mapping);
			ClientTickEvents.END_CLIENT_TICK.register(T -> {
				if(T.player != null && mapping.isDown()) {
					createModMenuScreen(T.screen, T::setScreen);
				}
			});
		}
	}
	
	@Environment(EnvType.CLIENT)
	private void createModMenuScreen(Screen parent, Consumer<Screen> toOpen) {
		try { toOpen.accept((Screen)Class.forName("com.terraformersmc.modmenu.gui.ModsScreen").getDeclaredConstructor(Screen.class).newInstance(parent)); }
		catch(Exception e) { e.printStackTrace(); }
	}
	
	public void load() {
		for(ConfigHandler handler : CONFIGS.getAllConfigs()) {
			if(PerWorldProxy.isProxy(handler.getProxy())) {
				handler.load();
			}
		}
	}
	
	public void unload() {
		for(ConfigHandler handler : CONFIGS.getAllConfigs()) {
			if(PerWorldProxy.isProxy(handler.getProxy())) {
				handler.unload();
			}
		}
	}
}