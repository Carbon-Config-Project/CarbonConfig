package carbonconfiglib;

import java.util.function.BooleanSupplier;
import java.util.function.Predicate;

import org.lwjgl.glfw.GLFW;

import carbonconfiglib.api.ConfigType;
import carbonconfiglib.config.ConfigHandler;
import carbonconfiglib.gui.api.IModConfig;
import carbonconfiglib.gui.api.background.BackgroundTexture;
import carbonconfiglib.gui.base.states.CarbonRenderPipelines;
import carbonconfiglib.gui.screens.ConfigListScreen;
import carbonconfiglib.gui.screens.ConfigRequestScreen;
import carbonconfiglib.gui.screens.ConfigScreen;
import carbonconfiglib.gui.screens.ModDependencyScreen;
import carbonconfiglib.impl.PerWorldProxy;
import carbonconfiglib.impl.internal.EventHandler;
import carbonconfiglib.impl.internal.SettingsLoader;
import carbonconfiglib.networking.carbon.StateSyncPacket;
import carbonconfiglib.networking.snyc.BulkSyncPacket;
import carbonconfiglib.utils.SyncType;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.KeyMapping.Category;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.permissions.Permissions;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.AddClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent.LoggingIn;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent.LoggingOut;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RegisterRenderPipelinesEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.client.gui.ModListScreen;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import speiger.src.collections.objects.lists.ObjectArrayList;

public class CarbonConfigClient {
	public static final CarbonConfigClient INSTANCE = new CarbonConfigClient();
	public static BooleanSupplier MOD_GUI = () -> false;
	public static Predicate<Object> DEPENDENCY_VIEWER = _ -> false;
	
	public void init(IEventBus bus) {
		NeoForge.EVENT_BUS.register(this);
		bus.addListener(this::onClientLoad);
		bus.addListener(this::registerKeys);
		bus.addListener(this::onResourceReloadRegister);
		bus.addListener(this::registerPipelines);
		NeoForge.EVENT_BUS.addListener(this::onKeyPressed);
		NeoForge.EVENT_BUS.addListener(this::onScreenKeyPressed);
	}
	
	/**
	 * Helper function that allows to open a specific config folder in a remote
	 * config.<br>
	 * Remote config is defined as a config that is on the servers machine.<br>
	 * In Singleplayer that could also mean that client configs do work.
	 * 
	 * @param config that should be opened
	 * @param path   of the folders that should be traversed
	 * @implNote you can't go into CompoundObjects
	 */
	public static void openRemoteConfigFolder(IModConfig config, String... path) {
		openRemoteConfigFolder(config, BackgroundTexture.DEFAULT, path);
	}

	/**
	 * Helper function that allows to open a specific config folder in a remote
	 * config.<br>
	 * Remote config is defined as a config that is on the servers machine.<br>
	 * In Singleplayer that could also mean that client configs do work.
	 * 
	 * @param config  that should be opened
	 * @param texture background that should be used
	 * @param path    of the folders that should be traversed
	 * @implNote you can't go into CompoundObjects
	 */
	public static void openRemoteConfigFolder(IModConfig config, BackgroundTexture texture, String... path) {
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
		} else if (!mc.hasSingleplayerServer() && !mc.player.permissions().hasPermission(Permissions.COMMANDS_OWNER)) {
			CarbonConfig.LOGGER.info("Tried to open a Remote config without permission");
			return;
		}
		mc.setScreen(new ConfigRequestScreen(texture.asHolder(), mc.screen, config, path));
	}

	/**
	 * Helper function that allows to open a specific config folder in a local
	 * config.<br>
	 * Local config is defined as a config that is on the clients machine.<br>
	 * This includes Client/Singleplayer/Shared or Common configs.
	 * 
	 * @param config that should be opened
	 * @param path   of the folders that should be traversed
	 * @implNote you can't go into CompoundObjects
	 */
	public static void openLocalConfigFolder(IModConfig config, String... path) {
		openLocalConfigFolder(config, BackgroundTexture.DEFAULT, path);
	}

	/**
	 * Helper function that allows to open a specific config folder in a local
	 * config.<br>
	 * Local config is defined as a config that is on the clients machine.<br>
	 * This includes Client/Singleplayer/Shared or Common configs.
	 * 
	 * @param config  that should be opened
	 * @param texture background that should be used
	 * @param path    of the folders that should be traversed
	 * @implNote you can't go into CompoundObjects
	 */
	public static void openLocalConfigFolder(IModConfig config, BackgroundTexture texture, String... path) {
		if (!config.isLocalConfig()) {
			CarbonConfig.LOGGER.info("Tried to open a Remote config in the Local Opener");
			return;
		}
		Minecraft mc = Minecraft.getInstance();
		mc.setScreen(new ConfigScreen(config, texture.asHolder(), mc.screen).withWalker(path == null || path.length <= 0 ? null : ObjectArrayList.wrap(path)));
	}
	
	public void onClientLoad(FMLClientSetupEvent event) {
		EventHandler.INSTANCE.onConfigsLoaded();
	}
	
	public void registerKeys(RegisterKeyMappingsEvent event) {
		Category category = new Category(Identifier.fromNamespaceAndPath("carbonconfig", "general"));
		event.registerCategory(category);
		KeyMapping mapping = new KeyMapping("key.carbon_config.key", GLFW.GLFW_KEY_KP_ENTER, category);
		event.register(mapping);
		MOD_GUI = mapping::isDown;
		KeyMapping mappingOther = new KeyMapping("key.carbon_config.dep", GLFW.GLFW_KEY_KP_ADD, category);
		event.register(mappingOther);
		DEPENDENCY_VIEWER = T -> {
			if(T instanceof KeyEvent keyEvent) return mappingOther.matches(keyEvent);
			else if(T instanceof MouseButtonEvent mouseEvent) return mappingOther.matchesMouse(mouseEvent);
			return false;
		};
	}
	
	public void onResourceReloadRegister(AddClientReloadListenersEvent event) {
		event.addListener(Identifier.fromNamespaceAndPath("carbonconfig", "settings"), SettingsLoader.INSTANCE);
	}
	
	public void onKeyPressed(InputEvent.Key event) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.player != null && MOD_GUI.getAsBoolean() && event.getAction() == GLFW.GLFW_PRESS) {
			mc.setScreen(event.getKeyEvent().hasShiftDown() ? new ModListScreen(mc.screen) : new ConfigListScreen(mc.screen, BackgroundTexture.DEFAULT.asHolder(), EventHandler.INSTANCE.getAllConfigs()));
		}
		else if(DEPENDENCY_VIEWER.test(event.getKeyEvent()) && event.getAction() == GLFW.GLFW_PRESS) {
			mc.setScreen(new ModDependencyScreen());
		}
	}
	
	public void onScreenKeyPressed(ScreenEvent.KeyPressed.Pre event) {
		if(event.getScreen() instanceof TitleScreen && DEPENDENCY_VIEWER.test(event.getKeyEvent())) {
			Minecraft.getInstance().setScreen(new ModDependencyScreen());
			event.setCanceled(true);
		}
	}
	
	@SubscribeEvent
	public void onPlayerServerJoinEvent(LoggingIn event) {
		if(Minecraft.getInstance().getCurrentServer() == null) loadMPConfigs();
		CarbonConfig.NETWORK.sendToServer(new StateSyncPacket(Dist.CLIENT));
		BulkSyncPacket packet = BulkSyncPacket.create(CarbonConfig.CONFIGS.getConfigsToSync(), SyncType.CLIENT_TO_SERVER, true);
		if(packet == null) return;
		CarbonConfig.NETWORK.sendToServer(packet);
	}
	
	
	public void registerPipelines(RegisterRenderPipelinesEvent event) {
		event.registerPipeline(CarbonRenderPipelines.GUI);
	}
	
	@SubscribeEvent
	public void onPlayerServerLeaveEvent(LoggingOut event) {
		CarbonConfig.NETWORK.onPlayerLeft(event.getPlayer(), false);
		if(!Minecraft.getInstance().isLocalServer()) {
			for(ConfigHandler handler : CarbonConfig.CONFIGS.getAllConfigs()) {
				if(PerWorldProxy.isProxy(handler.getProxy())) {
					handler.unload();
				}
			}
		}
	}
	
	private void loadMPConfigs() {
		for(ConfigHandler handler : CarbonConfig.CONFIGS.getAllConfigs()) {
			if(PerWorldProxy.isProxy(handler.getProxy())) {
				handler.load();
			}
		}
	}
}
