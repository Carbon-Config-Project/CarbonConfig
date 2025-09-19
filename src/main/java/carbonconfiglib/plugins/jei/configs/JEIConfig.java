package carbonconfiglib.plugins.jei.configs;

import java.nio.file.Path;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Predicate;

import carbonconfiglib.api.ConfigType;
import carbonconfiglib.gui.api.IConfigNode;
import carbonconfiglib.gui.api.IModConfig;
import mezz.jei.api.runtime.config.IJeiConfigCategory;
import mezz.jei.api.runtime.config.IJeiConfigFile;
import mezz.jei.api.runtime.config.IJeiConfigValue;
import mezz.jei.common.config.file.IConfigSchema;
import net.minecraft.network.FriendlyByteBuf;
import speiger.src.collections.objects.lists.ObjectArrayList;

public class JEIConfig implements IModConfig
{
	IJeiConfigFile file;
	List<IJeiConfigValue<?>> entries = new ObjectArrayList<>();
	
	public JEIConfig(IJeiConfigFile file) {
		this.file = file;
		for(IJeiConfigCategory cat : file.getCategories()) {
			entries.addAll(cat.getConfigValues());
		}
	}
	@Override
	public String getFileName() { return file.getPath().getFileName().toString(); }
	@Override
	public String getConfigName() { return file.getPath().getFileName().toString(); }
	@Override
	public String getModId() { return "jei"; }
	@Override
	public boolean isDynamicConfig() { return false; }
	@Override
	public ConfigType getConfigType() { return ConfigType.CLIENT; }
	@Override
	public boolean isLocalConfig() { return false; }
	@Override
	public boolean canCreateConfigs() { return false; }
	@Override
	public boolean createConfig(Path path) { return false; }	
	
	@Override
	public IConfigNode getRootNode() {
		return new JEIRoot(file);
	}
	
	@Override
	public boolean isDefault() {
		for(IJeiConfigValue<?> entry : entries) {
			if(!JEIHelpers.isDefault(entry)) return false;
		}
		return true;
	}
	
	@Override
	public void restoreDefault() {
		entries.forEach(JEIHelpers::setDefault);
	}
	
	@Override
	public List<IConfigTarget> getPotentialFiles() { return null; }
	@Override
	public IModConfig loadFromFile(Path path) { return null; }
	@Override
	public IModConfig loadFromNetworking(UUID requestId, Consumer<Predicate<FriendlyByteBuf>> network) { return null; }
	
	@Override
	public void save() {
		if(file instanceof IConfigSchema schema) {
			schema.markDirty();
		}
	}
	
}
