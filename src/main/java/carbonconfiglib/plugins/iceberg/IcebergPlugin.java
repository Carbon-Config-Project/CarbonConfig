package carbonconfiglib.plugins.iceberg;

import com.anthonyhilyard.iceberg.config.IcebergConfigSpec;

import carbonconfiglib.gui.impl.forge.IConfigSpecProvider;

public class IcebergPlugin {

	public static void register() {
		IConfigSpecProvider.registerProvider(IcebergConfigSpec.class, T -> new IcebergConfig((IcebergConfigSpec)T));
	}
}
