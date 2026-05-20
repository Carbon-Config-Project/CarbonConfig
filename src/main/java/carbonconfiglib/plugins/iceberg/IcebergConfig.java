package carbonconfiglib.plugins.iceberg;

import java.util.List;

import com.anthonyhilyard.iceberg.forge.config.ForgeIcebergConfigSpec;
import com.electronwill.nightconfig.core.UnmodifiableConfig;

import carbonconfiglib.gui.impl.forge.IConfigSpecProvider;

public record IcebergConfig(ForgeIcebergConfigSpec spec) implements IConfigSpecProvider {
	@Override
	public UnmodifiableConfig getValues() { return spec.getValues(); }
	@Override
	public UnmodifiableConfig getSpec() { return spec.getSpec(); }
	@Override
	public String getLevelComment(List<String> path) { return spec.getLevelComment(path); }
	@Override
	public void afterReload() { spec.afterReload(); }
	
}