package carbonconfiglib.examples;

import carbonconfiglib.CarbonConfig;
import carbonconfiglib.api.IConfigSerializer;
import carbonconfiglib.config.Config;
import carbonconfiglib.config.ConfigEntry.ParsedValue;
import carbonconfiglib.config.ConfigHandler;
import carbonconfiglib.config.ConfigSection;
import carbonconfiglib.impl.entries.ColorValue.ColorWrapper;
import carbonconfiglib.utils.Helpers;
import carbonconfiglib.utils.IEntryDataType.CompoundDataType;
import carbonconfiglib.utils.IEntryDataType.EntryDataType;
import carbonconfiglib.utils.ParseResult;

public class ParsedValueConfigExample
{
	ParsedValue<TestValue> value;
	public ConfigHandler handler;
	
	
	public ParsedValueConfigExample()
	{
		Config config = new Config("customDataExample");
		ConfigSection section = config.add("parsedValue");
		
		// Structure information for the Gui/Config that is used for config comments and for the layout in the config Gui!
		CompoundDataType type = new CompoundDataType().with("Name", EntryDataType.STRING).with("Year", EntryDataType.INTEGER).with("Fluffyness", EntryDataType.DOUBLE).withCustom("Favorite Color", ColorWrapper.class, EntryDataType.INTEGER);
		
		// serializing helper that turns the String into the actual value, Which requires the struct, a example, parser, serializer.
		// Note that the parser is expected to catch all crashes and send "exceptions" through the ParseResult instead.
		// This one doesn't support networking, if networking is used you get also a sender/receiver function.
		// If a config tries to sync a parsed value without sync support it will CRASH. (Intentionally)
		// The Example is mainly used for the config comment that will display it, which is especially useful for arrays where no default could be a thing.
		IConfigSerializer<TestValue> serializer = IConfigSerializer.noSync(type, new TestValue(), TestValue::parse, TestValue::serialize);
		value = section.addParsed("testExample", new TestValue(), serializer);
		handler = CarbonConfig.CONFIGS.createConfig(config);
		handler.register();
	}
	
	
	
	public static class TestValue
	{
		String name;
		int year;
		double fluffyness;
		int favoriteColour;
		
		public TestValue() {
			this("Testing", 2000, 512.2423, 0xFF00FF);
		}
		
		public TestValue(String name, int year, double fluffyness, int favoriteColour) {
			this.name = name;
			this.year = year;
			this.fluffyness = fluffyness;
			this.favoriteColour = favoriteColour;
		}
		
		/*
		 * Parse Function that parses the DataType.
		 */
		public static ParseResult<TestValue> parse(String[] value) {
			if(value.length != 4) return ParseResult.error(Helpers.mergeCompound(value), "4 Elements are required");
			if(value[0] == null || value[0].trim().isEmpty()) return ParseResult.error(value[0], "Value [Name] is not allowed to be null/empty");
			ParseResult<Integer> year = Helpers.parseInt(value[1]);
			if(year.hasError()) return year.onlyError("Couldn't parse [Year] argument");
			ParseResult<Double> fluffyness = Helpers.parseDouble(value[2]);
			if(fluffyness.hasError()) return fluffyness.onlyError("Couldn't parse [Fluffyness] argument");
			ParseResult<Integer> color = ColorWrapper.parseInt(value[3]);
			if(color.hasError()) return color.onlyError("Couldn't parse [Colour] argument");
			return ParseResult.success(new TestValue(value[0], year.getValue(), fluffyness.getValue(), color.getValue()));
		}
		
		/*
		 * Serialization function that turns the DataType into a string. 
		 */
		public String[] serialize() {
			return new String[] {name, Integer.toString(year), Double.toString(fluffyness), ColorWrapper.serialize(favoriteColour)};
		}
	}
}
