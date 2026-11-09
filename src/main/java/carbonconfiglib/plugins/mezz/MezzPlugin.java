package carbonconfiglib.plugins.mezz;

import java.util.List;
import java.util.function.Consumer;

import carbonconfiglib.gui.api.IModConfigs;
import carbonconfiglib.plugins.ICarbonPlugin;
import carbonconfiglib.plugins.mezz.configs.MezzConfigs;
import net.mezzdev.config.api.Configs;
import net.mezzdev.config.api.schema.IConfigSchema;
import net.neoforged.fml.ModContainer;
import speiger.src.collections.objects.lists.ObjectArrayList;

public class MezzPlugin implements ICarbonPlugin
{
	public MezzPlugin() {
		Configs.getSchemas().stream().map(IConfigSchema::getModId).distinct().forEach(T -> ICarbonPlugin.registerPlugin(T, this));
	}
	@Override
	public void applyConfigs(ModContainer container, Consumer<IModConfigs> configs) {
		String modId = container.getModId();
		List<IConfigSchema> configList = new ObjectArrayList<>(Configs.getSchemas());
		configList.removeIf(T -> !modId.equals(T.getModId()));
		if(configList.isEmpty()) return;
		configs.accept(new MezzConfigs(container, configList));
	}
}
