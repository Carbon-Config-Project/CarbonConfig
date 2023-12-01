package carbonconfiglib;

import java.util.function.BooleanSupplier;

import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import carbonconfiglib.api.ConfigType;
import carbonconfiglib.config.Config;
import carbonconfiglib.config.ConfigEntry.BoolValue;
import carbonconfiglib.config.ConfigEntry.EnumValue;
import carbonconfiglib.config.ConfigHandler;
import carbonconfiglib.config.ConfigSection;
import carbonconfiglib.config.ConfigSettings;
import carbonconfiglib.config.FileSystemWatcher;
import carbonconfiglib.gui.api.BackgroundTexture;
import carbonconfiglib.gui.api.BackgroundTypes;
import carbonconfiglib.gui.api.IModConfig;
import carbonconfiglib.gui.screen.ConfigScreen;
import carbonconfiglib.gui.screen.ConfigScreen.Navigator;
import carbonconfiglib.gui.screen.RequestScreen;
import carbonconfiglib.impl.PerWorldProxy;
import carbonconfiglib.impl.ReloadMode;
import carbonconfiglib.impl.entries.ColorValue;
import carbonconfiglib.impl.entries.RegistryKeyValue;
import carbonconfiglib.impl.entries.RegistryValue;
import carbonconfiglib.impl.internal.ConfigLogger;
import carbonconfiglib.impl.internal.EventHandler;
import carbonconfiglib.networking.CarbonNetwork;
import carbonconfiglib.utils.AutomationType;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.server.MinecraftServer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.javafmlmod.FMLJavaModLoadingContext;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.gui.ModListScreen;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

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
@Mod("carbonconfig")
public class CarbonConfig
{
	public static final Logger LOGGER = LogUtils.getLogger();
	public static final FileSystemWatcher CONFIGS = new FileSystemWatcher(new ConfigLogger(LOGGER), FMLPaths.CONFIGDIR.get(), EventHandler.INSTANCE);
	public static final CarbonNetwork NETWORK = new CarbonNetwork();
	public static BooleanSupplier MOD_GUI = () -> false;
	ConfigHandler handler;
	public static BoolValue FORGE_SUPPORT; 
	public static BoolValue FORCE_CUSTOM_BACKGROUND;
	public static EnumValue<BackgroundTypes> BACKGROUNDS;
	public static BoolValue INGAME_BACKGROUND;
	
	public CarbonConfig()
	{
		NETWORK.init();
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onCommonLoad);
		NeoForge.EVENT_BUS.addListener(this::load);
		NeoForge.EVENT_BUS.addListener(this::unload);
		NeoForge.EVENT_BUS.register(EventHandler.INSTANCE);
		if(FMLEnvironment.dist.isClient()) {
			FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onClientLoad);
			FMLJavaModLoadingContext.get().getModEventBus().addListener(this::registerKeys);
			NeoForge.EVENT_BUS.addListener(this::onKeyPressed);
			Config config = new Config("carbonconfig");
			ConfigSection section = config.add("general");
			FORGE_SUPPORT = section.addBool("enable-forge-support", true, "Enables that CarbonConfig automatically adds Forge Configs into its own Config Gui System").setRequiredReload(ReloadMode.GAME);
			BACKGROUNDS = section.addEnum("custom-background", BackgroundTypes.PLANKS, BackgroundTypes.class, "Allows to pick for a Custom Background for Configs that use the default Background");
			FORCE_CUSTOM_BACKGROUND = section.addBool("force-custom-background", false, "Allows to force your Selected Background to be used everywhere instead of just default Backgrounds");
			INGAME_BACKGROUND = section.addBool("ingame-background", false, "Allows to set if the background is always visible or only if you are not in a active world");
			handler = CONFIGS.createConfig(config, ConfigSettings.withConfigType(ConfigType.CLIENT).withAutomations(AutomationType.AUTO_LOAD));
			handler.register();
		}
	}
	
	/**
	 * Creates a Setting with a PerWorld Proxy set by default.<br>
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
	 * Creates a Config that is dedicated for color.<br>
	 * It saves the Entry in Hex instead a normal number allowing to set RGB a lot easier and understand it nicer.<br>
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
	 * Creates a ConfigBuilder that contains a Set of "Registry Keys" (ResourceLocation).<br>
	 * The idea behind that is you might want a filter or something about a specific Type of Registry Element.<br>
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
	 * Creates a ConfigBuilder that contains a Set of "Registry Elements" (i.e. Item/Block/Fluid/Enchantment).<br>
	 * The idea behind that is you might want a filter or something about a specific Type of Registry Element.<br>
	 * Compared to the RegistryKeyEntry this actually stores the "Registry Instances". Not the Ids
	 * @param <E> the Class-Type for Config Gui rendering.
	 * @param key the name of the config
	 * @param clz the Class-Type for Config Gui rendering.
	 * @return a Builder for registry Keys
	 */
	public static <E> RegistryValue.Builder<E> createRegistryBuilder(String key, Class<E> clz) {
		return RegistryValue.builder(key, clz);
	}
	
	/**
	 * Helper function that allows to open a specific config folder in a remote config.<br>
	 * Remote config is defined as a config that is on the servers machine.<br>
	 * In Singleplayer that could also mean that client configs do work.
	 * @param config that should be opened
	 * @param path of the folders that should be traversed
	 * @implNote you can't go into CompoundObjects
	 */
	@OnlyIn(Dist.CLIENT)
	public static void openRemoteConfigFolder(IModConfig config, String...path) {
		openRemoteConfigFolder(config, BackgroundTexture.DEFAULT, path);
	}
	
	/**
	 * Helper function that allows to open a specific config folder in a remote config.<br>
	 * Remote config is defined as a config that is on the servers machine.<br>
	 * In Singleplayer that could also mean that client configs do work.
	 * @param config that should be opened
	 * @param texture background that should be used
	 * @param path of the folders that should be traversed
	 * @implNote you can't go into CompoundObjects
	 */
	@OnlyIn(Dist.CLIENT)
	public static void openRemoteConfigFolder(IModConfig config, BackgroundTexture texture, String...path) {
		MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
		if(server != null) {
			openLocalConfigFolder(config, texture, path);
			return;
		}
		else if(config.getConfigType() == ConfigType.CLIENT) {
			CarbonConfig.LOGGER.info("Tried to open a local config in the Remote Opener");
			return;
		}
		Minecraft mc = Minecraft.getInstance();
		if(mc.player == null) {
			CarbonConfig.LOGGER.info("Tried to open a Remote config when there was no remote attached");
			return;
		}
		else if(!mc.player.hasPermissions(4)) {
			CarbonConfig.LOGGER.info("Tried to open a Remote config without permission");			
			return;
		}
		mc.setScreen(new RequestScreen(texture.asHolder(), Navigator.create(config).withWalker(path), mc.screen, config));
	}
	
	/**
	 * Helper function that allows to open a specific config folder in a local config.<br>
	 * Local config is defined as a config that is on the clients machine.<br>
	 * This includes Client/Singleplayer/Shared or Common configs.
	 * @param config that should be opened
	 * @param path of the folders that should be traversed
	 * @implNote you can't go into CompoundObjects
	 */
	@OnlyIn(Dist.CLIENT)
	public static void openLocalConfigFolder(IModConfig config, String...path) {
		openLocalConfigFolder(config, BackgroundTexture.DEFAULT, path);
	}
	
	/**
	 * Helper function that allows to open a specific config folder in a local config.<br>
	 * Local config is defined as a config that is on the clients machine.<br>
	 * This includes Client/Singleplayer/Shared or Common configs.
	 * @param config that should be opened
	 * @param texture background that should be used
	 * @param path of the folders that should be traversed
	 * @implNote you can't go into CompoundObjects
	 */
	@OnlyIn(Dist.CLIENT)
	public static void openLocalConfigFolder(IModConfig config, BackgroundTexture texture, String...path) {
		if(!config.isLocalConfig()) {
			CarbonConfig.LOGGER.info("Tried to open a Remote config in the Local Opener");
			return;
		}
		Minecraft mc = Minecraft.getInstance();
		mc.setScreen(new ConfigScreen(Navigator.create(config).withWalker(path), config, mc.screen, texture.asHolder()));
	}
	
	public void onCommonLoad(FMLCommonSetupEvent event) {
		for(ConfigHandler handler : CONFIGS.getAllConfigs()) {
			if(PerWorldProxy.isProxy(handler.getProxy())) {
				handler.createDefaultConfig();
			}
		}
	}
	
	@OnlyIn(Dist.CLIENT)
	public void onClientLoad(FMLClientSetupEvent event) {
		EventHandler.INSTANCE.onConfigsLoaded();
	}
	
	@OnlyIn(Dist.CLIENT)
	public void registerKeys(RegisterKeyMappingsEvent event) {
		KeyMapping mapping = new KeyMapping("key.carbon_config.key", GLFW.GLFW_KEY_KP_ENTER, "key.carbon_config");
		event.register(mapping);
		MOD_GUI = mapping::isDown;
	}
	
	@OnlyIn(Dist.CLIENT)
	public void onKeyPressed(InputEvent.Key event) {
		Minecraft mc = Minecraft.getInstance();
		if(mc.player != null && event.getAction() == GLFW.GLFW_PRESS && MOD_GUI.getAsBoolean()) {
			mc.setScreen(new ModListScreen(mc.screen));
		}
	}
	
	public void load(ServerAboutToStartEvent event) {
		for(ConfigHandler handler : CONFIGS.getAllConfigs()) {
			if(PerWorldProxy.isProxy(handler.getProxy())) {
				handler.load();
			}
		}
	}
	
	public void unload(ServerStoppingEvent event) {
		for(ConfigHandler handler : CONFIGS.getAllConfigs()) {
			if(PerWorldProxy.isProxy(handler.getProxy())) {
				handler.unload();
			}
		}
	}
}