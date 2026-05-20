package carbonconfiglib.plugins.iceberg;

import com.anthonyhilyard.iceberg.neoforge.config.NeoForgeIcebergConfigSpec;

import carbonconfiglib.gui.impl.forge.IConfigSpecProvider;

public class IcebergPlugin {

	public static void register() {
		IConfigSpecProvider.registerProvider(NeoForgeIcebergConfigSpec.class, T -> new IcebergConfig((NeoForgeIcebergConfigSpec)T));
	}
}
