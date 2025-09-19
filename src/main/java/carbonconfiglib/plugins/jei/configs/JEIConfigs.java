package carbonconfiglib.plugins.jei.configs;

import java.util.List;
import java.util.Optional;

import carbonconfiglib.api.ConfigType;
import carbonconfiglib.gui.api.BackgroundTexture;
import carbonconfiglib.gui.api.BackgroundTexture.BackgroundHolder;
import carbonconfiglib.gui.api.IModConfig;
import carbonconfiglib.gui.api.IModConfigs;
import carbonconfiglib.impl.internal.ModConfigs;
import mezz.jei.api.runtime.config.IJeiConfigFile;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.loading.moddiscovery.ModInfo;
import net.neoforged.neoforgespi.language.IModInfo;
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
		Optional<Background> texture = container.getCustomExtension(IModConfigs.Background.class);
		if(texture.isPresent()) return texture.get().texture().asHolder();
		Optional<BackgroundTexture> carbon_Texture = ModConfigs.computeTexture(container);
		if(carbon_Texture.isPresent()) return carbon_Texture.get().asHolder();
		return getBackgroundTexture(container.getModInfo()).asHolder();
	}
	
	private static BackgroundTexture getBackgroundTexture(final IModInfo info) {
		String configBackground = (String)info.getModProperties().get("configuredBackground");
		if (configBackground != null) {
			return BackgroundTexture.of(configBackground).build();
		}
		if (info instanceof ModInfo) {
			Optional<String> optional = ((ModInfo)info).getConfigElement(new String[] {"configBackground"});
			if (optional.isPresent()) {
				return BackgroundTexture.of(optional.get()).build();
			}
		}
		return BackgroundTexture.DEFAULT;
	}
	
}
