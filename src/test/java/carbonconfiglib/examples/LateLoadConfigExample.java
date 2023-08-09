package carbonconfiglib.examples;

import carbonconfiglib.CarbonConfig;
import carbonconfiglib.config.Config;
import carbonconfiglib.config.ConfigEntry.IntValue;
import carbonconfiglib.config.ConfigHandler;

public class LateLoadConfigExample
{
	IntValue value;
	Config config;
	ConfigHandler handler;
	
	public LateLoadConfigExample() {
		config = new Config("lateLoadExample");
		handler = CarbonConfig.CONFIGS.createConfig(config);
		handler.register(); //Registers the config and loads the config file
		loadLateConfigExample();
	}
	
	/**
	 * Called for example during world load
	 */
	public void loadLateConfigExample() {
		value = config.add("lateSection").addInt("LateExample", 0); //This will load just fine because the config caches "unused" entries. Though unused entries won't be saved until they were added.
		handler.save(); //Has to be saved now because if the entry doesn't exist it has to save it.
	}
}
