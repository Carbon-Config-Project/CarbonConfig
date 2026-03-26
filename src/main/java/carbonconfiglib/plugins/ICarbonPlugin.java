package carbonconfiglib.plugins;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import carbonconfiglib.gui.api.IModConfigs;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.ModContainer;

public interface ICarbonPlugin
{
	public static final Map<ModContainer, ICarbonPlugin> LOADED_PLUGINS = new ConcurrentHashMap<>();
	
	public static void registerPlugin(String modId, ICarbonPlugin plugin) {
		ModContainer container = Loader.instance().getIndexedModList().get(modId);
		if(container == null) return;
		LOADED_PLUGINS.put(container, plugin);
	}
	
	public void applyConfigs(ModContainer container, Consumer<IModConfigs> configs);
}
