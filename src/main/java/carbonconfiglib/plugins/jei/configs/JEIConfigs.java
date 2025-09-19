package carbonconfiglib.plugins.jei.configs;

import java.util.List;

import carbonconfiglib.api.ConfigType;
import carbonconfiglib.gui.api.BackgroundTexture;
import carbonconfiglib.gui.api.BackgroundTexture.BackgroundHolder;
import carbonconfiglib.gui.api.IModConfig;
import carbonconfiglib.gui.api.IModConfigs;
import carbonconfiglib.impl.internal.ModConfigs;
import mezz.jei.api.runtime.config.IJeiConfigFile;
import net.fabricmc.loader.api.ModContainer;
import speiger.src.collections.objects.lists.ObjectArrayList;
import speiger.src.collections.objects.utils.ObjectLists;

public class JEIConfigs implements IModConfigs
{
	ModContainer container;
	List<IJeiConfigFile> configs;
	
	public JEIConfigs(ModContainer container, List<IJeiConfigFile> configs) {
		this.container = container;
		this.configs = configs;
	}

	@Override
	public String getModName() {
		return "Jei";
	}
	
	@Override
	public List<IModConfig> getConfigInstances(ConfigType type) {
		if(type != ConfigType.CLIENT) return ObjectLists.empty();
		List<IModConfig> config = new ObjectArrayList<>();
		for(IJeiConfigFile file : configs) {
			config.add(new JEIConfig(file));
		}
		return config;
	}
	
	@Override
	public BackgroundHolder getBackground() {
		BackgroundTexture texture = IModConfigs.TEXTURE_REGISTRY.get(container);
		if(texture != null) return texture.asHolder();
		return ModConfigs.computeTexture(container).orElse(BackgroundTexture.DEFAULT).asHolder();
	}	
}
