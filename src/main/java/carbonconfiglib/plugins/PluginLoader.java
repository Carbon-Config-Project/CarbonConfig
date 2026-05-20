package carbonconfiglib.plugins;

import carbonconfiglib.plugins.iceberg.IcebergPlugin;
import net.neoforged.fml.ModList;

public class PluginLoader {
	public static void loadPlugins() {
		if(ModList.get().isLoaded("iceberg")) {
			loadIceberg();
		}
	}
	
	private static void loadIceberg() {
		IcebergPlugin.register();
	}
}
