package carbonconfiglib;

import java.util.Map;
import java.util.Objects;
import java.util.function.BooleanSupplier;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.ModContainer;
import cpw.mods.fml.common.event.FMLInterModComms.IMCEvent;
import cpw.mods.fml.common.event.FMLInterModComms.IMCMessage;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerAboutToStartEvent;
import cpw.mods.fml.common.event.FMLServerStoppingEvent;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.UserListOpsEntry;
import net.minecraftforge.common.MinecraftForge;
import speiger.src.collections.objects.maps.impl.hash.Object2ObjectLinkedOpenHashMap;
import speiger.src.collections.objects.maps.impl.hash.Object2ObjectOpenHashMap;

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
@Mod(modid = "carbonconfig", version = Tags.VERSION, name = "Carbon Config Library", acceptableRemoteVersions = "*", acceptedMinecraftVersions = "[1.7.10]")
public class CarbonConfig
{
	public static final Logger LOGGER = LogManager.getLogger();
	public static final FileSystemWatcher CONFIGS = new FileSystemWatcher(new ConfigLogger(LOGGER), Loader.instance().getConfigDir().toPath(), EventHandler.INSTANCE);
	public static final CarbonNetwork NETWORK = new CarbonNetwork();
	public static BooleanSupplier MOD_GUI = () -> false;
	public static BooleanSupplier DEPENDENCY_VIEWER = () -> false;
	ConfigHandler handler;
	public static BoolValue FORGE_SUPPORT;
	public static BoolValue FORCE_FORGE_SUPPORT;
	public static BoolValue FORCE_CUSTOM_BACKGROUND;
	public static EnumValue<BackgroundTypes> BACKGROUNDS;
	public static BoolValue INGAME_BACKGROUND;
	public static BoolValue AUTO_BACKUP;
	public static BoolValue AUTO_SAVE;
	public static HashSetCache<String> MODS_DISABLED;

	@cpw.mods.fml.common.Mod.EventHandler
	public void onPreInit(FMLPreInitializationEvent event)
	{
		NETWORK.init();
		MinecraftForge.EVENT_BUS.register(EventHandler.INSTANCE);
		FMLCommonHandler.instance().bus().register(EventHandler.INSTANCE);
		if(FMLCommonHandler.instance().getSide().isClient()) {
			InternalFeatures.loadDefaultSettings();
			MinecraftForge.EVENT_BUS.register(CarbonConfigClient.INSTANCE);
			FMLCommonHandler.instance().bus().register(CarbonConfigClient.INSTANCE);
			Config config = new Config("carbonconfig");
			ConfigSection section = config.add("general");
			FORGE_SUPPORT = section.addBool("enable-forge-support", true, "Enables that CarbonConfig automatically adds Forge Configs into its own Config Gui System").setRequiredReload(ReloadMode.GAME);
			FORCE_FORGE_SUPPORT = section.addBool("force-forge-support", true, "Enables that Carbon Config Overrides the config guis of forge mods that have added their own guis").setRequiredReload(ReloadMode.GAME);
			AUTO_SAVE = section.addBool("auto-save", false, "Defines if autosave is enabled by default or not");
			AUTO_BACKUP = section.addBool("auto-backup", false, "Enables that a backup is created everytime a config is saved through the gui");
			ArrayValue blacklist = section.addArray("mod-blacklist", new String[0],
					"Disables these mods from carbon configs Gui System.",
					"This is mainly if a mod doesn't play well with Carbon Config it can be disabled/ignored",
					"List of Blacklisted ModIds").withFilter(Loader::isModLoaded).setRequiredReload(ReloadMode.GAME).forceSuggestions(true).addSuggestionProvider(ModProvider.INSTANCE);
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

	public static boolean hasPermission(EntityPlayer player, int permissionLevel) {
		MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
		if(server.isSinglePlayer() && Objects.equals(player.getGameProfile().getName(), server.getServerOwner())) return true;
		UserListOpsEntry entry = (UserListOpsEntry)server.getConfigurationManager().func_152603_m().func_152683_b(player.getGameProfile());
		return entry != null && entry.func_152644_a() >= permissionLevel;
	}

	@cpw.mods.fml.common.Mod.EventHandler
	public void onCommonLoad(FMLPostInitializationEvent event) {
		for(ConfigHandler handler : CONFIGS.getAllConfigs()) {
			if(PerWorldProxy.isProxy(handler.getProxy())) {
				handler.createDefaultConfig();
			}
		}
		if(FMLCommonHandler.instance().getSide().isClient()) {
			CarbonConfigClient.onClientLoad();
		}
	}

	@cpw.mods.fml.common.Mod.EventHandler
	public void onIMC(IMCEvent event) {
		Map<String, ModContainer> mods = new Object2ObjectOpenHashMap<>();
		Map<ModContainer, ModContainer> mappingTasks = new Object2ObjectLinkedOpenHashMap<>();
		for(IMCMessage message : event.getMessages()) {
			if("registerGui".equalsIgnoreCase(message.key) && message.isStringMessage()) {
				ModContainer container = Loader.instance().getIndexedModList().get(message.getSender());
				if(container == null) continue;
				mods.put(message.getStringValue(), container);
			}
			else if("remapGui".equalsIgnoreCase(message.key) && message.isStringMessage()) {
				ModContainer to = Loader.instance().getIndexedModList().get(message.getSender());
				if(to == null) continue;
				ModContainer from = Loader.instance().getIndexedModList().get(message.getStringValue());
				if(from == null) continue;
				mappingTasks.put(from, to);
			}
		}
		EventHandler.INSTANCE.processIMCEvents(mods, mappingTasks);
	}

	@cpw.mods.fml.common.Mod.EventHandler
	public void load(FMLServerAboutToStartEvent event) {
		for(ConfigHandler handler : CONFIGS.getAllConfigs()) {
			if(PerWorldProxy.isProxy(handler.getProxy())) {
				handler.load();
			}
		}
	}

	@cpw.mods.fml.common.Mod.EventHandler
	public void unload(FMLServerStoppingEvent event) {
		for(ConfigHandler handler : CONFIGS.getAllConfigs()) {
			if(PerWorldProxy.isProxy(handler.getProxy())) {
				handler.unload();
			}
		}
	}
}
