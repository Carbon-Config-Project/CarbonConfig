package carbonconfiglib;

import java.util.Map;
import java.util.function.BooleanSupplier;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.input.Keyboard;

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
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.GuiModList;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.event.FMLInterModComms.IMCEvent;
import net.minecraftforge.fml.common.event.FMLInterModComms.IMCMessage;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerAboutToStartEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent.KeyInputEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import speiger.src.collections.objects.maps.impl.hash.Object2ObjectLinkedOpenHashMap;
import net.minecraftforge.registries.IForgeRegistryEntry;
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
@Mod(modid = "carbonconfig", version = "1.1.3", name = "Carbon Config Library", acceptableRemoteVersions = "*", acceptedMinecraftVersions = "[1.12]")
public class CarbonConfig
{
	public static final Logger LOGGER = LogManager.getLogger();
	public static final FileSystemWatcher CONFIGS = new FileSystemWatcher(new ConfigLogger(LOGGER), Loader.instance().getConfigDir().toPath(), EventHandler.INSTANCE);
	public static final CarbonNetwork NETWORK = new CarbonNetwork();
	public static BooleanSupplier MOD_GUI = () -> false;
	ConfigHandler handler;
	public static BoolValue FORGE_SUPPORT; 
	public static BoolValue FORCE_FORGE_SUPPORT; 
	public static BoolValue FORCE_CUSTOM_BACKGROUND;
	public static EnumValue<BackgroundTypes> BACKGROUNDS;
	public static BoolValue INGAME_BACKGROUND;
	
	@net.minecraftforge.fml.common.Mod.EventHandler
	public void onPreInit(FMLPreInitializationEvent event)
	{
		NETWORK.init();
		MinecraftForge.EVENT_BUS.register(EventHandler.INSTANCE);
		if(FMLCommonHandler.instance().getSide().isClient()) {
			MinecraftForge.EVENT_BUS.register(this);
			Config config = new Config("carbonconfig");
			ConfigSection section = config.add("general");
			FORGE_SUPPORT = section.addBool("enable-forge-support", true, "Enables that CarbonConfig automatically adds Forge Configs into its own Config Gui System").setRequiredReload(ReloadMode.GAME);
			FORCE_FORGE_SUPPORT = section.addBool("force-forge-support", true, "Enables that Carbon Config Overrides the config guis of forge mods that have added their own guis").setRequiredReload(ReloadMode.GAME);
			BACKGROUNDS = section.addEnum("custom-background", BackgroundTypes.PLANKS, BackgroundTypes.class, "Allows to pick for a Custom Background for Configs that use the default Background");
			FORCE_CUSTOM_BACKGROUND = section.addBool("force-custom-background", false, "Allows to force your Selected Background to be used everywhere instead of just default Backgrounds");
			INGAME_BACKGROUND = section.addBool("ingame-background", false, "Allows to set if the background is always visible or only if you are not in a active world");
			handler = CONFIGS.createConfig(config, ConfigSettings.withConfigType(ConfigType.CLIENT).withAutomations(AutomationType.AUTO_LOAD, AutomationType.AUTO_RELOAD));
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
	 * Creates a ConfigBuilder that contains a Set of "Registry Elements" (i.e. Item/Block/Fluid/Enchantment).
	 * The idea behind that is you might want a filter or something about a specific Type of Registry Element.
	 * Compared to the RegistryKeyEntry this actually stores the "Registry Instances". Not the Ids
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
	@SideOnly(Side.CLIENT)
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
	@SideOnly(Side.CLIENT)
	public static void openRemoteConfigFolder(IModConfig config, BackgroundTexture texture, String...path) {
		MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
		if(server != null) {
			openLocalConfigFolder(config, texture, path);
			return;
		}
		else if(config.getConfigType() == ConfigType.CLIENT) {
			CarbonConfig.LOGGER.info("Tried to open a local config in the Remote Opener");
			return;
		}
		Minecraft mc = Minecraft.getMinecraft();
		if(mc.player == null) {
			CarbonConfig.LOGGER.info("Tried to open a Remote config when there was no remote attached");
			return;
		}
		else if(mc.player.getPermissionLevel() < 4) {
			CarbonConfig.LOGGER.info("Tried to open a Remote config without permission");			
			return;
		}
		mc.displayGuiScreen(new RequestScreen(texture.asHolder(), Navigator.create(config).withWalker(path), mc.currentScreen, config));
	}
	
	/**
	 * Helper function that allows to open a specific config folder in a local config.<br>
	 * Local config is defined as a config that is on the clients machine.<br>
	 * This includes Client/Singleplayer/Shared or Common configs.
	 * @param config that should be opened
	 * @param path of the folders that should be traversed
	 * @implNote you can't go into CompoundObjects
	 */
	@SideOnly(Side.CLIENT)
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
	@SideOnly(Side.CLIENT)
	public static void openLocalConfigFolder(IModConfig config, BackgroundTexture texture, String...path) {
		if(!config.isLocalConfig()) {
			CarbonConfig.LOGGER.info("Tried to open a Remote config in the Local Opener");
			return;
		}
		Minecraft mc = Minecraft.getMinecraft();
		mc.displayGuiScreen(new ConfigScreen(Navigator.create(config).withWalker(path), config, mc.currentScreen, texture.asHolder()));
	}
	
	@net.minecraftforge.fml.common.Mod.EventHandler
	public void onCommonLoad(FMLPostInitializationEvent event) {
		for(ConfigHandler handler : CONFIGS.getAllConfigs()) {
			if(PerWorldProxy.isProxy(handler.getProxy())) {
				handler.createDefaultConfig();
			}
		}
		if(FMLCommonHandler.instance().getSide().isClient()) {
			onClientLoad();
		}
	}
	
	@net.minecraftforge.fml.common.Mod.EventHandler
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
	
	@SideOnly(Side.CLIENT)
	public void onClientLoad() {
		EventHandler.INSTANCE.onConfigsLoaded();
		KeyBinding mapping = new KeyBinding("key.carbon_config.key", Keyboard.KEY_NUMPAD0, "key.carbon_config");
		ClientRegistry.registerKeyBinding(mapping);
		MOD_GUI = mapping::isKeyDown;
	}
	
	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void onKeyPressed(KeyInputEvent event) {
		Minecraft mc = Minecraft.getMinecraft();
		if(mc.player != null && MOD_GUI.getAsBoolean()) {
			mc.displayGuiScreen(new GuiModList(mc.currentScreen));
		}
	}
	
	@net.minecraftforge.fml.common.Mod.EventHandler
	public void load(FMLServerAboutToStartEvent event) {
		for(ConfigHandler handler : CONFIGS.getAllConfigs()) {
			if(PerWorldProxy.isProxy(handler.getProxy())) {
				handler.load();
			}
		}
	}
	
	@net.minecraftforge.fml.common.Mod.EventHandler
	public void unload(FMLServerStoppingEvent event) {
		for(ConfigHandler handler : CONFIGS.getAllConfigs()) {
			if(PerWorldProxy.isProxy(handler.getProxy())) {
				handler.unload();
			}
		}
	}
}