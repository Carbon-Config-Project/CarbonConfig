package carbonconfiglib.plugins;

import carbonconfiglib.plugins.iceberg.IcebergPlugin;
import carbonconfiglib.plugins.mezz.MezzPlugin;
import net.neoforged.fml.ModList;

public class PluginLoader {
	public static void loadPlugins() {
		if(ModList.get().isLoaded("iceberg")) {
			loadIceberg();
		}
		if(ModList.get().isLoaded("mezz_config")) {
			loadMezzConfig();
		}
	}
	
	private static void loadMezzConfig() {
		new MezzPlugin();
	}
	
	private static void loadIceberg() {
		IcebergPlugin.register();
	}
}
