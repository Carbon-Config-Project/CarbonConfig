package carbonconfiglib;

import java.util.function.Consumer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Window;

import carbonconfiglib.api.ConfigType;
import carbonconfiglib.config.Config;
import carbonconfiglib.config.ConfigEntry.ArrayValue;
import carbonconfiglib.config.ConfigEntry.BoolValue;
import carbonconfiglib.config.ConfigEntry.EnumValue;
import carbonconfiglib.config.ConfigHandler;
import carbonconfiglib.config.ConfigSection;
import carbonconfiglib.config.ConfigSettings;
import carbonconfiglib.config.FileSystemWatcher;
import carbonconfiglib.config.HashSetCache;
import carbonconfiglib.gui.api.IModConfig;
import carbonconfiglib.gui.api.background.BackgroundTexture;
import carbonconfiglib.gui.api.background.BackgroundTypes;
import carbonconfiglib.gui.api.suggestion.SuggestionProviders.ModProvider;
import carbonconfiglib.gui.screens.ConfigListScreen;
import carbonconfiglib.gui.screens.ConfigRequestScreen;
import carbonconfiglib.gui.screens.ConfigScreen;
import carbonconfiglib.gui.screens.ModDependencyScreen;
import carbonconfiglib.impl.PerWorldProxy;
import carbonconfiglib.impl.ReloadMode;
import carbonconfiglib.impl.entries.ColorValue;
import carbonconfiglib.impl.entries.RegistryKeyValue;
import carbonconfiglib.impl.entries.RegistryValue;
import carbonconfiglib.impl.internal.ConfigLogger;
import carbonconfiglib.impl.internal.EventHandler;
import carbonconfiglib.impl.internal.InternalFeatures;
import carbonconfiglib.impl.internal.SettingsLoader;
import carbonconfiglib.networking.CarbonNetwork;
import carbonconfiglib.utils.AutomationType;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.impl.client.keybinding.KeyBindingRegistryImpl;
import net.fabricmc.fabric.impl.resource.loader.ResourceManagerHelperImpl;
import net.fabricmc.fabric.mixin.client.keybinding.KeyCodeAccessor;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.PackType;
import net.minecraft.world.entity.player.Player;
import speiger.src.collections.objects.lists.ObjectArrayList;

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
	public static final Logger LOGGER = LogManager.getLogger("CarbonConfig");
	private static final FileSystemWatcher CONFIGS = new FileSystemWatcher(new ConfigLogger(LOGGER), FabricLoader.getInstance().getConfigDir(), EventHandler.INSTANCE);
	public static final CarbonNetwork NETWORK = new CarbonNetwork();
	ConfigHandler handler;
	public static BoolValue MOD_MENU_SUPPORT; 
	public static BoolValue FORCE_CUSTOM_BACKGROUND;
	public static EnumValue<BackgroundTypes> BACKGROUNDS;
	public static BoolValue INGAME_BACKGROUND;
	public static BoolValue AUTO_BACKUP;
	public static BoolValue BACKUP_TOASTS;
	public static BoolValue AUTO_SAVE;
	public static HashSetCache<String> MODS_DISABLED;
	@Override
	public void onInitialize()
	{
		NETWORK.init();
		if(FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) EventHandler.INSTANCE.initClientEvents(this::onClientLoad, this::registerKeys);
		else EventHandler.INSTANCE.initServerEvents(this::onCommonLoad);
		ServerLifecycleEvents.SERVER_STARTING.register(T -> load());
		ServerLifecycleEvents.SERVER_STOPPING.register(T -> unload());
		
		if(FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
			InternalFeatures.loadDefaultSettings();
			Config config = new Config("carbonconfig");
			ConfigSection section = config.add("general");
			MOD_MENU_SUPPORT = section.addBool("enable-modmenu-support", true, "Enables that CarbonConfig automatically adds Mod Menu Support for all Carbon Configs").setRequiredReload(ReloadMode.GAME);
			AUTO_SAVE = section.addBool("auto-save", false, "Defines if autosave is enabled by default or not");
			AUTO_BACKUP = section.addBool("auto-backup", false, "Enables that a backup is created everytime a config is saved through the gui");
			BACKUP_TOASTS = section.addBool("backup-toasts", true, "Show toasts when backups were created or loaded to give feedback");
			ArrayValue blacklist = section.addArray("mod-blacklist", new String[0], 
					"Disables these mods from carbon configs Gui System.",
					"This is mainly if a mod doesn't play well with Carbon Config it can be disabled/ignored",
					"List of Blacklisted ModIds").withFilter(FabricLoader.getInstance()::isModLoaded).setRequiredReload(ReloadMode.GAME).forceSuggestions(true).addSuggestionProvider(ModProvider.INSTANCE);
			BACKGROUNDS = section.addEnum("custom-background", BackgroundTypes.RAW_IRON, BackgroundTypes.class, "Allows to pick for a Custom Background for Configs that use the default Background");
			FORCE_CUSTOM_BACKGROUND = section.addBool("force-custom-background", false, "Allows to force your Selected Background to be used everywhere instead of just default Backgrounds");
			INGAME_BACKGROUND = section.addBool("ingame-background", false, "Allows to set if the background is always visible or only if you are not in a active world");
			handler = createConfig("carbonconfig", config, ConfigSettings.withConfigType(ConfigType.CLIENT).withAutomations(AutomationType.AUTO_LOAD));
			MODS_DISABLED = HashSetCache.create(blacklist, handler);
			handler.register();
			ResourceManagerHelperImpl.get(PackType.CLIENT_RESOURCES).registerReloadListener(SettingsLoader.INSTANCE);
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
	
	/**
	 * Helper function that allows to open a specific config folder in a remote config.<br>
	 * Remote config is defined as a config that is on the servers machine.<br>
	 * In Singleplayer that could also mean that client configs do work.
	 * @param config that should be opened
	 * @param path of the folders that should be traversed
	 * @implNote you can't go into CompoundObjects
	 */
	@Environment(EnvType.CLIENT)
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
	@Environment(EnvType.CLIENT)
	public static void openRemoteConfigFolder(IModConfig config, BackgroundTexture texture, String...path) {
		MinecraftServer server = EventHandler.getServer();
		if (server != null) {
			openLocalConfigFolder(config, texture, path);
			return;
		} else if (config.getConfigType() == ConfigType.CLIENT) {
			CarbonConfig.LOGGER.info("Tried to open a local config in the Remote Opener");
			return;
		}
		Minecraft mc = Minecraft.getInstance();
		if (mc.player == null) {
			CarbonConfig.LOGGER.info("Tried to open a Remote config when there was no remote attached");
			return;
		} else if (!mc.hasSingleplayerServer() && !mc.player.hasPermissions(4)) {
			CarbonConfig.LOGGER.info("Tried to open a Remote config without permission");
			return;
		}
		mc.setScreen(new ConfigRequestScreen(texture.asHolder(), mc.screen, config, path));
	}
	
	/**
	 * Helper function that allows to open a specific config folder in a local config.<br>
	 * Local config is defined as a config that is on the clients machine.<br>
	 * This includes Client/Singleplayer/Shared or Common configs.
	 * @param config that should be opened
	 * @param path of the folders that should be traversed
	 * @implNote you can't go into CompoundObjects
	 */
	@Environment(EnvType.CLIENT)
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
	@Environment(EnvType.CLIENT)
	public static void openLocalConfigFolder(IModConfig config, BackgroundTexture texture, String...path) {
		if (!config.isLocalConfig()) {
			CarbonConfig.LOGGER.info("Tried to open a Remote config in the Local Opener");
			return;
		}
		Minecraft mc = Minecraft.getInstance();
		mc.setScreen(new ConfigScreen(config, texture.asHolder(), mc.screen).withWalker(path == null || path.length <= 0 ? null : ObjectArrayList.wrap(path)));
	}
	
	public static boolean hasPermission(Player player, int permissionLevel) {
		MinecraftServer server = EventHandler.getServer();
		if(server.isSingleplayer() && server.isSingleplayerOwner(player.getGameProfile())) return true;
		return player.hasPermissions(permissionLevel);
	}
	
	public void onCommonLoad() {
		for(ConfigHandler handler : CONFIGS.getAllConfigs()) {
			if(PerWorldProxy.isProxy(handler.getProxy())) {
				handler.createDefaultConfig();
			}
		}
	}
	
	@Environment(EnvType.CLIENT)
	public void onClientLoad() {
		onCommonLoad();
		EventHandler.INSTANCE.onConfigsLoaded();
	}
	
	@Environment(EnvType.CLIENT)
	public void registerKeys() {
		KeyMapping mapping = new KeyMapping("key.carbon_config.key", GLFW.GLFW_KEY_KP_ENTER, "key.carbon_config");
		KeyBindingRegistryImpl.registerKeyBinding(mapping);
		KeyMapping mappingOther = new KeyMapping("key.carbon_config.dep", GLFW.GLFW_KEY_KP_ADD, "key.carbon_config");
		KeyBindingRegistryImpl.registerKeyBinding(mappingOther);
		ClientTickEvents.END_CLIENT_TICK.register(T -> {
			Window window = T.getWindow();
			if(T.player != null && mapping.isDown()) {
				if(Screen.hasShiftDown() && createModMenuScreen(T.screen, T::setScreen)) return;
				T.setScreen(new ConfigListScreen(T.screen, BackgroundTexture.DEFAULT.asHolder(), EventHandler.INSTANCE.getAllConfigs()));
			}
			else if(InputConstants.isKeyDown(window.getWindow(), ((KeyCodeAccessor)mappingOther).fabric_getBoundKey().getValue())) {
				T.setScreen(new ModDependencyScreen());
			}
		});
	}
	
	@Environment(EnvType.CLIENT)
	public static boolean createModMenuScreen(Screen parent, Consumer<Screen> toOpen) {
		try {
			toOpen.accept((Screen)Class.forName("com.terraformersmc.modmenu.gui.ModsScreen").getDeclaredConstructor(Screen.class).newInstance(parent)); 
			return true;
		}
		catch(Exception e) { return false; }
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