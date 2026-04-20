package carbonconfiglib;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

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
import carbonconfiglib.gui.api.background.BackgroundTypes;
import carbonconfiglib.gui.api.suggestion.SuggestionProviders.ModProvider;
import carbonconfiglib.impl.PerWorldProxy;
import carbonconfiglib.impl.ReloadMode;
import carbonconfiglib.impl.entries.ColorValue;
import carbonconfiglib.impl.entries.RegistryKeyValue;
import carbonconfiglib.impl.entries.RegistryValue;
import carbonconfiglib.impl.internal.ConfigLogger;
import carbonconfiglib.impl.internal.EventHandler;
import carbonconfiglib.impl.internal.InternalFeatures;
import carbonconfiglib.networking.CarbonNetwork;
import carbonconfiglib.utils.AutomationType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.permissions.Permissions;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.fml.loading.FMLPaths;
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

	ConfigHandler handler;
	public static BoolValue FORGE_SUPPORT; 
	public static BoolValue OVERWRITE_FORGE;
	public static BoolValue FORCE_CUSTOM_BACKGROUND;
	public static EnumValue<BackgroundTypes> BACKGROUNDS;
	public static BoolValue INGAME_BACKGROUND;
	public static BoolValue AUTO_BACKUP;
	public static BoolValue BACKUP_TOASTS;
	public static BoolValue AUTO_SAVE;
	public static HashSetCache<String> MODS_DISABLED;
	public static BoolValue SHOW_MISSING_ENCHANTMENT_TEXTURE;

	public CarbonConfig(IEventBus bus)
	{
		bus.addListener(NETWORK::init);
		bus.addListener(this::onCommonLoad);
		NeoForge.EVENT_BUS.addListener(this::load);
		NeoForge.EVENT_BUS.addListener(this::unload);
		NeoForge.EVENT_BUS.register(EventHandler.INSTANCE);
		if(FMLEnvironment.getDist().isClient()) {
			InternalFeatures.loadDefaultSettings();
			CarbonConfigClient.INSTANCE.init(bus);
			Config config = new Config("carbonconfig");
			ConfigSection section = config.add("general");
			FORGE_SUPPORT = section.addBool("enable-forge-support", true, "Enables that CarbonConfig automatically adds Forge Configs into its own Config Gui System").setRequiredReload(ReloadMode.GAME);
			AUTO_SAVE = section.addBool("auto-save", false, "Defines if autosave is enabled by default or not");
			AUTO_BACKUP = section.addBool("auto-backup", false, "Enables that a backup is created everytime a config is saved through the gui");
			BACKUP_TOASTS = section.addBool("backup-toasts", true, "Show toasts when backups were created or loaded to give feedback");
			SHOW_MISSING_ENCHANTMENT_TEXTURE = section.addBool("show-missing-texture", true, "Enables that if enchantments are not accessible that missing textures will be shown instead of nothing");
			OVERWRITE_FORGE = section.addBool("overwrite-neoforge-config-guis", true, 
					"Enables that Forge Config GUIs get overwritten by Carbon", 
					"This has a couple upsides such as:", 
					"\t- You can edit world specific configs without opening them", 
					"\t- Edit Multiplayer Configs inside servers (assuming permission)",
					"\t- And Edit Server sided Common Configs (assuming permissions)",
					"\tOn top of the bigger customization.");
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
	 * Creates a Setting with a PerWorld Proxy set by default.<br>
	 * And sets the config to be loaded at the right time!
	 * 
	 * @return ConfigSettings with PerWorld Proxy being set
	 */
	public static ConfigSettings getPerWorldProxy() {
		return PerWorldProxy.perWorld();
	}

	/**
	 * Creates a Setting that will allow Late Loading more easily.
	 * 
	 * @return ConfigSetting with just sync/Auto reload
	 * @apiNote Not required for a Per World Config
	 */
	public ConfigSettings createLateLoadSettings() {
		return ConfigSettings.withSettings(AutomationType.AUTO_RELOAD, AutomationType.AUTO_SYNC);
	}

	/**
	 * Creates a Config that is dedicated for color.<br>
	 * It saves the Entry in Hex instead a normal number allowing to set RGB a lot
	 * easier and understand it nicer.<br>
	 * On top of that the Ingame Gui renders the Color next to the config value.
	 * 
	 * @param key      the name of the config
	 * @param color    the default value
	 * @param comments what the config entry does
	 * @return a ColorValue
	 */
	public static ColorValue createColor(String key, int color, String... comments) {
		return new ColorValue(key, color, comments);
	}

	/**
	 * Creates a ConfigBuilder that contains a Set of "Registry Keys"
	 * (Identifier).<br>
	 * The idea behind that is you might want a filter or something about a specific
	 * Type of Registry Element.<br>
	 * Compared to the RegistryEntry this doesn't actually store the "Registry
	 * Instances" but only the Ids.
	 * 
	 * @param <E> the Class-Type for Config Gui rendering.
	 * @param key the name of the config
	 * @param clz the Class-Type for Config Gui rendering.
	 * @return a Builder for registry Keys
	 */
	public static <E> RegistryKeyValue.Builder<E> createRegistryKeyBuilder(String key, Class<E> clz) {
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
	public static <E> RegistryValue.Builder<E> createRegistryBuilder(String key, Class<E> clz) {
		return RegistryValue.builder(key, clz);
	}
	
	public static boolean hasPermission(Player player, int permissionLevel) {
		MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
		if (server.isSingleplayer() && server.isSingleplayerOwner(player.nameAndId())) return true;
		return player.permissions().hasPermission(Permissions.COMMANDS_OWNER);
	}

	public void onCommonLoad(FMLCommonSetupEvent event) {
		for (ConfigHandler handler : CONFIGS.getAllConfigs()) {
			if (PerWorldProxy.isProxy(handler.getProxy())) {
				handler.createDefaultConfig();
			}
		}
	}
	
	public void load(ServerAboutToStartEvent event) {
		for (ConfigHandler handler : CONFIGS.getAllConfigs()) {
			if (PerWorldProxy.isProxy(handler.getProxy())) {
				handler.load();
			}
		}
	}

	public void unload(ServerStoppingEvent event) {
		for (ConfigHandler handler : CONFIGS.getAllConfigs()) {
			if (PerWorldProxy.isProxy(handler.getProxy())) {
				handler.unload();
			}
		}
	}
}