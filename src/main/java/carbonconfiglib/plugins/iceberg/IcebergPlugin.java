package carbonconfiglib.plugins.iceberg;

import com.anthonyhilyard.iceberg.forge.config.ForgeIcebergConfigSpec;

import carbonconfiglib.gui.impl.forge.IConfigSpecProvider;

public class IcebergPlugin {

	public static void register() {
		IConfigSpecProvider.registerProvider(ForgeIcebergConfigSpec.class, T -> new IcebergConfig((ForgeIcebergConfigSpec)T));
	}
}
