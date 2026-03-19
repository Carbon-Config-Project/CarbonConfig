package carbonconfiglib.plugins;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import carbonconfiglib.gui.api.IModConfigs;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModList;

public interface ICarbonPlugin
{
	public static final Map<ModContainer, ICarbonPlugin> LOADED_PLUGINS = new ConcurrentHashMap<>();
	
	public static void registerPlugin(String modId, ICarbonPlugin plugin) {
		Optional<? extends ModContainer> container = ModList.get().getModContainerById(modId);
		if(!container.isPresent()) return;
		LOADED_PLUGINS.put(container.get(), plugin);
	}
	
	public void applyConfigs(ModContainer container, Consumer<IModConfigs> configs);
}
