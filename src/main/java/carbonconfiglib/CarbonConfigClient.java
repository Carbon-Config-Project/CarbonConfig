package carbonconfiglib;

import org.lwjgl.input.Keyboard;

import carbonconfiglib.api.ConfigType;
import carbonconfiglib.gui.api.IModConfig;
import carbonconfiglib.gui.api.background.BackgroundTexture;
import carbonconfiglib.gui.base.screen.LayeredScreen;
import carbonconfiglib.gui.screens.ConfigListScreen;
import carbonconfiglib.gui.screens.ConfigRequestScreen;
import carbonconfiglib.gui.screens.ConfigScreen;
import carbonconfiglib.gui.screens.ModDependencyScreen;
import carbonconfiglib.impl.internal.EventHandler;
import cpw.mods.fml.client.GuiModList;
import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.InputEvent.KeyInputEvent;
import cpw.mods.fml.common.gameevent.TickEvent.ClientTickEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.server.MinecraftServer;
import speiger.src.collections.objects.lists.ObjectArrayList;

@SideOnly(Side.CLIENT)
public class CarbonConfigClient {
	public static final CarbonConfigClient INSTANCE = new CarbonConfigClient();

	public static void onClientLoad() {
		EventHandler.INSTANCE.onConfigsLoaded();
		KeyBinding mapping = new KeyBinding("key.carbon_config.key", Keyboard.KEY_NUMPAD0, "key.carbon_config");
		ClientRegistry.registerKeyBinding(mapping);
		CarbonConfig.MOD_GUI = mapping::getIsKeyPressed;
		KeyBinding mappingOther = new KeyBinding("key.carbon_config.dep", Keyboard.KEY_NUMPAD1, "key.carbon_config");
		ClientRegistry.registerKeyBinding(mappingOther);
		CarbonConfig.DEPENDENCY_VIEWER = () -> GameSettings.isKeyDown(mappingOther);
	}

	@SubscribeEvent
	public void onClientTickEvent(ClientTickEvent event) {
		if(CarbonConfig.DEPENDENCY_VIEWER.getAsBoolean() && !(LayeredScreen.topScreen() instanceof ModDependencyScreen)) {
			Minecraft.getMinecraft().displayGuiScreen(new ModDependencyScreen());
		}
	}

	@SubscribeEvent
	public void onKeyPressed(KeyInputEvent event) {
		Minecraft mc = Minecraft.getMinecraft();
		if(mc.thePlayer != null && CarbonConfig.MOD_GUI.getAsBoolean()) {
			mc.displayGuiScreen(GuiScreen.isShiftKeyDown() ? new GuiModList(mc.currentScreen) : new ConfigListScreen(mc.currentScreen, BackgroundTexture.DEFAULT.asHolder(), EventHandler.INSTANCE.getAllConfigs()));
		}
	}

	public static void openRemoteConfigFolder(IModConfig config, String...path) {
		openRemoteConfigFolder(config, BackgroundTexture.DEFAULT, path);
	}

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
		if(mc.thePlayer == null) {
			CarbonConfig.LOGGER.info("Tried to open a Remote config when there was no remote attached");
			return;
		}
		else if(!CarbonConfig.NETWORK.hasPermissions()) {
			CarbonConfig.LOGGER.info("Tried to open a Remote config without permission");
			return;
		}
		mc.displayGuiScreen(new ConfigRequestScreen(texture.asHolder(), mc.currentScreen, config, path));
	}

	public static void openLocalConfigFolder(IModConfig config, String...path) {
		openLocalConfigFolder(config, BackgroundTexture.DEFAULT, path);
	}

	public static void openLocalConfigFolder(IModConfig config, BackgroundTexture texture, String...path) {
		if(!config.isLocalConfig()) {
			CarbonConfig.LOGGER.info("Tried to open a Remote config in the Local Opener");
			return;
		}
		Minecraft mc = Minecraft.getMinecraft();
		mc.displayGuiScreen(new ConfigScreen(config, texture.asHolder(), mc.currentScreen).withWalker(path == null || path.length <= 0 ? null : ObjectArrayList.wrap(path)));
	}
}
