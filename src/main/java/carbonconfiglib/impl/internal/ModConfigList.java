package carbonconfiglib.impl.internal;

import java.util.List;

import carbonconfiglib.api.ConfigType;
import carbonconfiglib.gui.api.BackgroundTexture;
import carbonconfiglib.gui.api.BackgroundTexture.BackgroundHolder;
import carbonconfiglib.gui.api.IModConfig;
import carbonconfiglib.gui.api.IModConfigs;
import cpw.mods.fml.common.ModContainer;
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
		return container.getName();
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
	public BackgroundHolder getBackground() {
		for(IModConfigs config : configs) {
			BackgroundHolder texture = config.getBackground();
			if(texture.getTexture() != BackgroundTexture.DEFAULT) return texture;
		}
		return BackgroundTexture.DEFAULT.asHolder();
	}
}
