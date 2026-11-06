package carbonconfiglib.plugins;

import carbonconfiglib.plugins.mezz.MezzPlugin;
import net.fabricmc.loader.api.FabricLoader;

public class PluginLoader {
	public static void loadPlugins() {
		if(FabricLoader.getInstance().isModLoaded("mezz_config")) {
			loadMezzConfig();
		}
	}
	
	private static void loadMezzConfig() {
		new MezzPlugin();
	}
}
