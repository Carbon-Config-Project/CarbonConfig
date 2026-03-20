package carbonconfiglib.examples;

import carbonconfiglib.CarbonConfig;
import carbonconfiglib.api.ConfigType;
import carbonconfiglib.config.Config;
import carbonconfiglib.config.ConfigSettings;
import carbonconfiglib.gui.api.types.EntrySettingTypes.FloatingSlider;

public class FloatingSliderTest
{
	public static void init() {
		Config config = new Config("sliderTest");
		config.add("sliders").addFloat("floatTest", 1).setRange(0.1F, 1F).addSettings(new FloatingSlider(0.01F));
		config.add("sliders").addDouble("doubleTest", 1).setRange(0.1D, 1D).addSettings(new FloatingSlider(0.01D));
		CarbonConfig.createConfig("carbonconfig", config, ConfigSettings.withConfigType(ConfigType.CLIENT)).register();

	}
}
