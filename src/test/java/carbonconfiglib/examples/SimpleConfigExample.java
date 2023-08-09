package carbonconfiglib.examples;

import carbonconfiglib.CarbonConfig;
import carbonconfiglib.api.ConfigType;
import carbonconfiglib.config.Config;
import carbonconfiglib.config.ConfigEntry.ArrayValue;
import carbonconfiglib.config.ConfigEntry.BoolValue;
import carbonconfiglib.config.ConfigEntry.DoubleValue;
import carbonconfiglib.config.ConfigEntry.EnumValue;
import carbonconfiglib.config.ConfigEntry.IntValue;
import carbonconfiglib.config.ConfigHandler;
import carbonconfiglib.config.ConfigSection;
import carbonconfiglib.config.ConfigSettings;
import carbonconfiglib.config.HashSetCache;
import carbonconfiglib.config.SyncedConfig;
import carbonconfiglib.impl.entries.ColorValue;
import carbonconfiglib.utils.AutomationType;
import carbonconfiglib.utils.MultilinePolicy;
import net.minecraft.ChatFormatting;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

public class SimpleConfigExample
{
	public static boolean LOAD_LATE = false;
	
	public IntValue numberExample;
	public IntValue rangeExample;
	public IntValue serverSyncedExample;
	public SyncedConfig<IntValue> clientSyncedValue;
	
	public BoolValue flagExample;
	public BoolValue suggestionExample;
	public ArrayValue stringArrayExample;
	public HashSetCache<String> cache;
	public DoubleValue percentageExample;
	public EnumValue<ChatFormatting> enumExample;
	public ColorValue colorExample;
	
	public ConfigHandler handler;
	
	public SimpleConfigExample() {
		Config config = new Config("simpleExample");
		ConfigSection section = config.add("general");
		numberExample = section.addInt("numberExample", 512, "Simple Commented Number Example"); //Comments can be multiline by using \n or making an array out of this.
		rangeExample = section.addInt("rangeExample", 10).setRange(0, 15); //Custom ranges can be used but are not required. By default it will do MAX_INT/MIN_INT
		flagExample = section.addBool("flagExample", false);	
		suggestionExample = section.addBool("suggestion_example", false).addSuggestion("Enable", true).addSuggestion("Disable", false).addSuggestion("Random?", false); //Suggestions can be used to give example values within the GUI itself. Allowing to simplify the process for you. But this ONLY APPLIES FOR THE GUI!
		stringArrayExample = section.addArray("Array example", new String[] {"First Value", "Apple", "Crazy What?"});
		
		ConfigSection subSection = section.addSubSection("Subsection"); // Sections can be cascaded as many times you want, but a section is requried!
		percentageExample = subSection.addDouble("FloatingPoint Example", 0.25); //NOTE Double.MIN_VALUE IS NOT THE NEGATIVE LIMIT BUT THE SMALLEST POSSIBLE VALUE POSSIBLE! IF you want the biggest negative value just do: -Double.MAX_VALUE;
		
		enumExample = subSection.addEnum("Enum Example", ChatFormatting.AQUA, ChatFormatting.class); //Enum Value allows to select a enum. If you need a Enum Array there is other ways to deal with that.
		
		ConfigSection synced = config.add("Synced Section");
		serverSyncedExample = synced.addInt("Server Synced Value", 0).setServerSynced(); //Syncs the config from the server to the client!
		clientSyncedValue = synced.addInt("Client Synced Value", 0).setClientSynced(); // Syncs the config from the client to the server to allow client specific configs for customization!
		
		handler = CarbonConfig.CONFIGS.createConfig(config);
		cache = HashSetCache.create(stringArrayExample, handler); //String arrays can not be really read so you can use HashSetCaches to make the tests a lot faster/simpler. Automatically updates if the config reloads. ArrayValue doesn't have to be referenced since its stored inside the cache too.
		handler.register();
	}
	
	public ConfigSettings createSettings() {
		ConfigSettings settings = ConfigSettings.withConfigType(ConfigType.SHARED);
		if(LOAD_LATE) settings.withAutomations(AutomationType.AUTO_RELOAD, AutomationType.AUTO_SYNC);
		else settings.withAutomations(AutomationType.AUTO_LOAD, AutomationType.AUTO_RELOAD, AutomationType.AUTO_SYNC);
		return settings.withMultiline(MultilinePolicy.ALWAYS_MULTILINE); //Settings are not required, by default it sets all required values, its only to OVERRIDE settings.
	}
	
	public void onCommonEvent(FMLCommonSetupEvent event) {
		if(LOAD_LATE) {
			handler.load();//If you need to load your config late just remove the AUTO_LOAD function and call load when you need it. NOTE: REGISTER HAS TO BE CALLED IN THE MOD CONSTRUCTOR! But Register doesn't load it unless auto load was being set.
		}
	}
	
	public int getClientValue(Player player) {
		return clientSyncedValue.get(player.getUUID()).get(); // Gets the Player Specific value from the config automatically. If no value is provided the server side config applies! NOTE: THIS requires AutomationType.AUTO_SYNC to be set (default) otherwise no SYNC
	}
	
	public boolean getBooleanExample() {
		return flagExample.get(); // Getting a value of a config
	}
	
	public void setBooleanExample(boolean value) {
		flagExample.set(value);
		handler.save(); // ConfigHandler is managing saving/loading. ConfigValues can't do that because configs can be copied.
	}
}
