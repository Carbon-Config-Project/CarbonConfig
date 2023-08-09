package carbonconfiglib.impl.internal;

import java.util.List;

import carbonconfiglib.api.ConfigType;
import carbonconfiglib.gui.api.BackgroundTexture;
import carbonconfiglib.gui.api.IModConfig;
import carbonconfiglib.gui.api.IModConfigs;
import net.minecraftforge.fml.ModContainer;
import speiger.src.collections.objects.lists.ObjectArrayList;

public class ModConfigList implements IModConfigs
{
	ModContainer container;
	List<IModConfigs> configs;
	
	public ModConfigList(ModContainer container, List<IModConfigs> configs) {
		this.container = container;
		this.configs = configs;
	}
	
	public static IModConfigs createMultiIfApplicable(ModContainer container, List<IModConfigs> configs) {
		return configs.size() == 1 ? configs.get(0) : new ModConfigList(container, configs);
	}
	
	@Override
	public String getModName() {
		return container.getModInfo().getDisplayName();
	}
	
	@Override
	public List<IModConfig> getConfigInstances(ConfigType type) {
		List<IModConfig> configs = new ObjectArrayList<>();
		for(IModConfigs config : this.configs) {
			configs.addAll(config.getConfigInstances(type));
		}
		return configs;
	}
	
	@Override
	public BackgroundTexture getBackground() {
		for(IModConfigs config : configs) {
			BackgroundTexture texture = config.getBackground();
			if(texture != BackgroundTexture.DEFAULT) return texture;
		}
		return BackgroundTexture.DEFAULT;
	}
}
