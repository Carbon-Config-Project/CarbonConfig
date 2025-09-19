package carbonconfiglib.examples;

import carbonconfiglib.CarbonConfig;
import carbonconfiglib.api.IConfigSerializer;
import carbonconfiglib.config.Config;
import carbonconfiglib.config.ConfigEntry.ParsedValue;
import carbonconfiglib.config.ConfigHandler;
import carbonconfiglib.config.ConfigSection;
import carbonconfiglib.impl.entries.ColorValue.ColorWrapper;
import carbonconfiglib.utils.ParseResult;
import carbonconfiglib.utils.ParsedCollections.ParsedMap;
import carbonconfiglib.utils.structure.IStructuredData.EntryDataType;
import carbonconfiglib.utils.structure.StructureCompound.CompoundBuilder;

public class ParsedValueConfigExample
{
	ParsedValue<TestValue> value;
	public ConfigHandler handler;
	
	
	public ParsedValueConfigExample()
	{
		Config config = new Config("customDataExample");
		ConfigSection section = config.add("parsedValue");
		
		// Structure information for the Gui/Config that is used for config comments and for the layout in the config Gui!
		CompoundBuilder builder = new CompoundBuilder()
				.simple("Name", EntryDataType.STRING)
				.simple("Year", EntryDataType.INTEGER)
				.variants("Favorite Color", EntryDataType.INTEGER, ColorWrapper.class, ColorWrapper::parse, ColorWrapper::serialize);
		
		// serializing helper that turns the String into the actual value, Which requires the struct, a example, parser, serializer.
		// Note that the parser is expected to catch all crashes and send "exceptions" through the ParseResult instead.
		// This one doesn't support networking, if networking is used you get also a sender/receiver function.
		// If a config tries to sync a parsed value without sync support it will CRASH. (Intentionally)
		// The Example is mainly used for the config comment that will display it, which is especially useful for arrays where no default could be a thing.
		IConfigSerializer<TestValue> serializer = IConfigSerializer.noSync(builder.build(), new TestValue(), TestValue::parse, TestValue::serialize);
		value = section.addParsed("testExample", new TestValue(), serializer);
		handler = CarbonConfig.createConfig("carbonconfig", config);
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
		public static ParseResult<TestValue> parse(ParsedMap map) {
			ParseResult<String> name = map.getOrError("Name", String.class);
			if(name.hasError()) return name.onlyError();
			ParseResult<Integer> year = map.getOrError("Year", Integer.class);
			if(year.hasError()) return year.onlyError();
			ParseResult<Double> fluffyness = map.getOrError("Fluffyness", Double.class);
			if(fluffyness.hasError()) return fluffyness.onlyError();
			ParseResult<ColorWrapper> color = map.getOrError("Favorite Color", ColorWrapper.class);
			if(color.hasError()) return color.onlyError();
			return ParseResult.success(new TestValue(name.getValue(), year.getValue(), fluffyness.getValue(), color.map(ColorWrapper::getColor)));
		}
		
		/*
		 * Serialization function that turns the DataType into a string. 
		 */
		public ParsedMap serialize() {
			ParsedMap map = new ParsedMap();
			map.put("Name", name);
			map.put("Year", year);
			map.put("Fluffyness", fluffyness);
			map.put("Color", new ColorWrapper(favoriteColour));
			return map;
		}
	}
}
