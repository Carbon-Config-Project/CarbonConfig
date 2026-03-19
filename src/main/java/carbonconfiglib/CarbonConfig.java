package carbonconfiglib;

import java.util.function.BiPredicate;
import java.util.function.BooleanSupplier;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;

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
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.resources.SimpleReloadableResourceManager;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.InputEvent.KeyInputEvent;
import net.minecraftforge.client.event.ParticleFactoryRegisterEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.client.gui.screen.ModListScreen;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.server.FMLServerAboutToStartEvent;
import net.minecraftforge.fml.event.server.FMLServerStoppingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import net.minecraftforge.registries.IForgeRegistryEntry;
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
@Mod("carbonconfig")
public class CarbonConfig
{
	public static final Logger LOGGER = LogManager.getLogger();
	public static final FileSystemWatcher CONFIGS = new FileSystemWatcher(new ConfigLogger(LOGGER), FMLPaths.CONFIGDIR.get(), EventHandler.INSTANCE);
	public static final CarbonNetwork NETWORK = new CarbonNetwork();
	public static BooleanSupplier MOD_GUI = () -> false;
	public static BiPredicate<Integer, Integer> DEPENDENCY_VIEWER = (K, V) -> false;
	ConfigHandler handler;
	public static BoolValue FORGE_SUPPORT; 
	public static BoolValue FORCE_CUSTOM_BACKGROUND;
	public static EnumValue<BackgroundTypes> BACKGROUNDS;
	public static BoolValue INGAME_BACKGROUND;
	public static BoolValue AUTO_BACKUP;
	public static BoolValue BACKUP_TOASTS;
	public static BoolValue AUTO_SAVE;
	public static HashSetCache<String> MODS_DISABLED;
	
	public CarbonConfig()
	{
		NETWORK.init();
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onCommonLoad);
		MinecraftForge.EVENT_BUS.addListener(this::load);
		MinecraftForge.EVENT_BUS.addListener(this::unload);
		MinecraftForge.EVENT_BUS.register(EventHandler.INSTANCE);
		if(FMLEnvironment.dist.isClient()) {
			InternalFeatures.loadDefaultSettings();
			FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onClientLoad);
			FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onResourceReloadRegister);
			MinecraftForge.EVENT_BUS.addListener(this::onKeyPressed);
			Config config = new Config("carbonconfig");
			ConfigSection section = config.add("general");
			FORGE_SUPPORT = section.addBool("enable-forge-support", true, "Enables that CarbonConfig automatically adds Forge Configs into its own Config Gui System").setRequiredReload(ReloadMode.GAME);
			AUTO_SAVE = section.addBool("auto-save", false, "Defines if autosave is enabled by default or not");
			AUTO_BACKUP = section.addBool("auto-backup", false, "Enables that a backup is created everytime a config is saved through the gui");
			BACKUP_TOASTS = section.addBool("backup-toasts", true, "Show toasts when backups were created or loaded to give feedback");
			ArrayValue blacklist = section.addArray("mod-blacklist", new String[0], 
					"Disables these mods from carbon configs Gui System.",
					"This is mainly if a mod doesn't play well with Carbon Config it can be disabled/ignored",
					"List of Blacklisted ModIds").withFilter(ModList.get()::isLoaded).setRequiredReload(ReloadMode.GAME).forceSuggestions(true).addSuggestionProvider(ModProvider.INSTANCE);
			BACKGROUNDS = section.addEnum("custom-background", BackgroundTypes.RAW_IRON, BackgroundTypes.class, "Allows to pick for a Custom Background for Configs that use the default Background");
			FORCE_CUSTOM_BACKGROUND = section.addBool("force-custom-background", false, "Allows to force your Selected Background to be used everywhere instead of just default Backgrounds");
			INGAME_BACKGROUND = section.addBool("ingame-background", false, "Allows to set if the background is always visible or only if you are not in a active world");
			handler = CONFIGS.createConfig(config, ConfigSettings.withConfigType(ConfigType.CLIENT).withAutomations(AutomationType.AUTO_LOAD));
			MODS_DISABLED = HashSetCache.create(blacklist, handler);
			handler.register();
		}
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
	public static <E extends IForgeRegistryEntry<E>> RegistryKeyValue.Builder<E> createRegistryKeyBuilder(String key, Class<E> clz) {
		return RegistryKeyValue.builder(key, clz);
	}

	/**
	 * Creates a ConfigBuilder that contains a Set of "Registry Elements" (i.e.
	 * Item/Block/Fluid/Enchantment).<br>
	 * The idea behind that is you might want a filter or something about a specific
	 * Type of Registry Element.<br>
	 * Compared to the RegistryKeyEntry this actually stores the "Registry
	 * Instances". Not the Ids
	 * 
	 * @param <E> the Class-Type for Config Gui rendering.
	 * @param key the name of the config
	 * @param clz the Class-Type for Config Gui rendering.
	 * @return a Builder for registry Keys
	 */
	public static <E extends IForgeRegistryEntry<E>> RegistryValue.Builder<E> createRegistryBuilder(String key, Class<E> clz) {
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
		} else if (!mc.isSingleplayer() && !mc.player.hasPermissionLevel(4)) {
			CarbonConfig.LOGGER.info("Tried to open a Remote config without permission");
			return;
		}
		mc.displayGuiScreen(new ConfigRequestScreen(texture.asHolder(), mc.currentScreen, config, path));
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
		if (!config.isLocalConfig()) {
			CarbonConfig.LOGGER.info("Tried to open a Remote config in the Local Opener");
			return;
		}
		Minecraft mc = Minecraft.getInstance();
		mc.displayGuiScreen(new ConfigScreen(config, texture.asHolder(), mc.currentScreen).withWalker(path == null || path.length <= 0 ? null : ObjectArrayList.wrap(path)));
	}
	
	public static boolean hasPermission(PlayerEntity player, int permissionLevel) {
		MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
		if(server.isSinglePlayer() && server.isServerOwner(player.getGameProfile())) return true;
		return player.hasPermissionLevel(permissionLevel);
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
		KeyBinding mapping = new KeyBinding("key.carbon_config.key", GLFW.GLFW_KEY_KP_ENTER, "key.carbon_config");
		ClientRegistry.registerKeyBinding(mapping);
		MOD_GUI = mapping::isKeyDown;
		KeyBinding mappingOther = new KeyBinding("key.carbon_config.dep", GLFW.GLFW_KEY_KP_ADD, "key.carbon_config");
		ClientRegistry.registerKeyBinding(mappingOther);
		DEPENDENCY_VIEWER = (K, S) -> mappingOther.matchesKey(K, S) || mappingOther.matchesMouseKey(K);
	}
	
	@OnlyIn(Dist.CLIENT)
	public void onKeyPressed(KeyInputEvent event) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.player != null && MOD_GUI.getAsBoolean() && event.getAction() == GLFW.GLFW_PRESS) {
			mc.displayGuiScreen(Screen.hasShiftDown() ? new ModListScreen(mc.currentScreen) : new ConfigListScreen(mc.currentScreen, BackgroundTexture.DEFAULT.asHolder(), EventHandler.INSTANCE.getAllConfigs()));
		}
		if(DEPENDENCY_VIEWER.test(event.getKey(), event.getScanCode()) && event.getAction() == GLFW.GLFW_PRESS) {
			mc.displayGuiScreen(new ModDependencyScreen());
		}
	}
	
	@OnlyIn(Dist.CLIENT)
	public void onResourceReloadRegister(ParticleFactoryRegisterEvent event) {
		((SimpleReloadableResourceManager)Minecraft.getInstance().getResourceManager()).addReloadListener(SettingsLoader.INSTANCE);
	}
	
	public void load(FMLServerAboutToStartEvent event) {
		for(ConfigHandler handler : CONFIGS.getAllConfigs()) {
			if(PerWorldProxy.isProxy(handler.getProxy())) {
				handler.load();
			}
		}
	}
	
	public void unload(FMLServerStoppingEvent event) {
		for(ConfigHandler handler : CONFIGS.getAllConfigs()) {
			if(PerWorldProxy.isProxy(handler.getProxy())) {
				handler.unload();
			}
		}
	}
}