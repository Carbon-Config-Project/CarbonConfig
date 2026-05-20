package carbonconfiglib.plugins;

import java.lang.reflect.Method;

import carbonconfiglib.gui.impl.forge.IConfigSpecProvider;
import carbonconfiglib.gui.impl.forge.IConfigSpecProvider.ForgeSpec;
import carbonconfiglib.plugins.iceberg.IcebergPlugin;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.config.IConfigSpec;

public class PluginLoader {
	public static void loadPlugins() {
		if(ModList.get().isLoaded("iceberg")) {
			loadIceberg();
		}
	}
	
	private static void loadIceberg() {
		IcebergPlugin.register();
		registerFixSpec();
	}
	
	@SuppressWarnings("unchecked")
	private static void registerFixSpec() {
		try {
			Class<?> clz = Class.forName("fuzs.nightconfigfixes.config.ConfigSpecWrapper");
			if(clz == null) return;
			Method method = clz.getMethod("getSpec");
			method.setAccessible(true);
			IConfigSpecProvider.registerProvider((Class<? extends IConfigSpec<?>>)clz, T -> {
				try { return new ForgeSpec((ForgeConfigSpec)method.invoke(T)); }
				catch(Exception e) { return null; }
			});
		}
		catch(Exception e) {}
	}
}
