package carbonconfiglib.plugins.fuzz;

import carbonconfiglib.gui.impl.forge.IConfigSpecProvider;
import fuzs.forgeconfigapiport.neoforge.impl.forge.ForgeConfigSpecAdapter;
import net.minecraftforge.common.ForgeConfigSpec;

public class FuzzPlugin {
	public static void register() {
		IConfigSpecProvider.registerProvider(ForgeConfigSpecAdapter.class, T -> new FuzzConfig((ForgeConfigSpec)((ForgeConfigSpecAdapter)T).spec()));
	}
}
