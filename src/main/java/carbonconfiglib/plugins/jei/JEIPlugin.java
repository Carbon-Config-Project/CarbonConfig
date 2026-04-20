package carbonconfiglib.plugins.jei;

import java.util.function.Consumer;

import carbonconfiglib.gui.api.IModConfigs;
import carbonconfiglib.plugins.ICarbonPlugin;
import carbonconfiglib.plugins.jei.configs.JEIConfigs;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.runtime.config.IJeiConfigManager;
import net.minecraft.resources.Identifier;
import net.neoforged.fml.ModContainer;
import speiger.src.collections.objects.lists.ObjectArrayList;

@JeiPlugin
public class JEIPlugin implements IModPlugin, ICarbonPlugin
{
	private IJeiConfigManager manager;
	
	public JEIPlugin() {
		ICarbonPlugin.registerPlugin("jei", this);
	}
	
	@Override
	public Identifier getPluginUid() {
		return Identifier.fromNamespaceAndPath("carbonconfig", "jeiplugin");
	}
	
	@Override
	public void onConfigManagerAvailable(IJeiConfigManager configManager) {
		this.manager = configManager;
	}

	@Override
	public void applyConfigs(ModContainer container, Consumer<IModConfigs> configs) {
		if(manager == null) return;
		configs.accept(new JEIConfigs(container, new ObjectArrayList<>(manager.getConfigFiles())));
	}
}
