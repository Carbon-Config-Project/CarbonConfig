package carbonconfiglib.plugins.fuzz;

import java.util.List;

import com.electronwill.nightconfig.core.UnmodifiableConfig;

import carbonconfiglib.gui.impl.forge.IConfigSpecProvider;
import net.minecraftforge.common.ForgeConfigSpec;

public record FuzzConfig(ForgeConfigSpec spec) implements IConfigSpecProvider {
	@Override
	public UnmodifiableConfig getValues() { return spec.getValues(); }
	@Override
	public UnmodifiableConfig getSpec() { return spec.getSpec(); }
	@Override
	public String getLevelComment(List<String> path) { return spec.getLevelComment(path); }
	@Override
	public void afterReload() { spec.afterReload(); }
}
