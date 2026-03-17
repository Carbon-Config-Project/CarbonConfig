package carbonconfiglib.examples;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Predicate;

import org.apache.commons.lang3.mutable.MutableObject;

import carbonconfiglib.CarbonConfig;
import carbonconfiglib.api.IConfigSerializer;
import carbonconfiglib.api.ISuggestionProvider;
import carbonconfiglib.api.ISuggestionProvider.Suggestion;
import carbonconfiglib.config.Config;
import carbonconfiglib.config.ConfigSection;
import carbonconfiglib.config.ConfigSettings;
import carbonconfiglib.gui.api.Texts;
import carbonconfiglib.gui.api.types.EntrySettingTypes.ColorType;
import carbonconfiglib.gui.api.types.EntrySettingTypes.CompoundArrayNamer;
import carbonconfiglib.impl.PerWorldProxy;
import carbonconfiglib.impl.entries.ColorValue.ColorWrapper;
import carbonconfiglib.utils.ParseResult;
import carbonconfiglib.utils.ParsedCollections.ParsedMap;
import carbonconfiglib.utils.structure.IStructuredData.EntryDataType;
import carbonconfiglib.utils.structure.StructureCompound.CompoundBuilder;
import net.minecraft.world.item.DyeColor;

public class FullTestCase
{
	public void init(boolean perWorld) {
		Config config = new Config("unittest");
		ConfigSection simple = config.add("simple-entries");
		
		simple.addBool("Flag", false);
		simple.addBool("Commented Flag", false, "Multi", "Comment", "Example");
		simple.addBool("Hidden Flag", false).setHidden();
		simple.addBool("Suggested Flag", false).addSuggestions(false, true);
		simple.addBool("Forced Suggestion Flag", false).addSuggestions(false, true).forceSuggestions(true);
		simple.addBool("Named Suggestion Flag", false).addSuggestionProvider(this::suggestsFlags);
		
		simple.addInt("Simple Number", 0);
		simple.addInt("Commented Number", 0, "Multi", "Comment", "Example");
		simple.addInt("Simple Number Range", 50).setRange(0, 100);
		simple.addInt("Suggestion Number", 0).addSuggestionProvider(this::suggestIntRange);
		simple.addInt("Forced Suggestion Number", 0).addSuggestionProvider(this::suggestIntRange).forceSuggestions(true);
		
		simple.addDouble("Simple Decimal", 0.534);
		simple.addDouble("Commented Decimal", 0.1, "Multi", "Comment", "Example");
		simple.addDouble("Simple Decimal Range", 50.121).setRange(25.2323, 75.3232);
		simple.addDouble("Suggestion Decimal", 0.1212).addSuggestionProvider(this::suggestDoubleRange);
		simple.addDouble("Forced Suggestion Decimal", 0.1212).addSuggestionProvider(this::suggestDoubleRange).forceSuggestions(true);
		
		simple.addString("Simple String", "Testing");
		simple.addString("Filtered String", "Requires a . in the sentince").withFilter(T -> T.contains("."));
		simple.addString("Suggested String", "Red").addSuggestions("Green", "Blue");
		simple.addString("Forced Suggested String", "Red").addSuggestions("Green", "Blue").forceSuggestions(true);
		
		simple.addEnum("Simple Enum", DyeColor.BLACK, DyeColor.class);
		simple.addEnum("Commented Enum", DyeColor.BLACK, DyeColor.class, "Requires to be a dye Color");
		
		ConfigSection collection = config.add("collection-entries");
		collection.addArray("Simple Array", new String[] {"One", "Two", "Three", "Four", "Five", "Six"});
		collection.addArray("Commented Array", "Simple Comment");
		collection.addArray("Filtered Array", new String[] {"Requires a . To be present for each entry"}).withFilter(T -> T.contains("."));
		
		collection.addEnumList("Simple Enum List", Collections.emptyList(), DyeColor.class);
		collection.addEnumList("Commented Enum List", Collections.emptyList(), DyeColor.class, "Requires a dye Color");
		
		ConfigSection special = config.add("special-entries");
		special.addParsed("Single Example", new ExampleValue(), ExampleValue.createSerializer());
		special.addParsedArray("Array Example", ExampleValue.createExample(), ExampleValue.createSerializer());
		
		CarbonConfig.CONFIGS.createConfig(config, perWorld ? PerWorldProxy.perWorld() : ConfigSettings.of()).register();
	}
	
	private void suggestIntRange(Consumer<Suggestion> acceptor, Predicate<Suggestion> filter) {
		for(int index : new int[] {1, 5, 12, 20, 50, 100}) {
			Suggestion entry = Suggestion.value(Integer.toString(index));
			if(filter.test(entry)) acceptor.accept(entry);
		}
	}
	
	private void suggestDoubleRange(Consumer<Suggestion> acceptor, Predicate<Suggestion> filter) {
		for(double index : new double[] {1.1, 5, 12.52, 20, 50.1212121, 100}) {
			Suggestion entry = Suggestion.value(Double.toString(index));
			if(filter.test(entry)) acceptor.accept(entry);
		}
	}
	
	private void suggestsFlags(Consumer<Suggestion> acceptor, Predicate<Suggestion> filter) {
		Suggestion entry = Suggestion.namedValue("Yes", "true");
		if(filter.test(entry)) acceptor.accept(entry);
		entry = Suggestion.namedValue("No", "false");
		if(filter.test(entry)) acceptor.accept(entry);
		entry = Suggestion.namedValue("Maybe?", "true");
		if(filter.test(entry)) acceptor.accept(entry);
	}
	
	public static class ExampleValue
	{
		String name;
		int year;
		double fluffyness;
		int favoriteColour;
		DyeColor color;
		boolean value;
		
		public ExampleValue() {
			this("Testing", 2000, 512.2423, 0xFF00FF, DyeColor.BLACK, false);
		}
		
		public ExampleValue(String name, int year, double fluffyness, int favoriteColour, DyeColor color, boolean value) {
			this.name = name;
			this.year = year;
			this.fluffyness = fluffyness;
			this.favoriteColour = favoriteColour;
			this.color = color;
			this.value = value;
		}
		
		@Override
		public boolean equals(Object obj) {
			if(obj instanceof ExampleValue) {
				ExampleValue other = (ExampleValue)obj;
				return Objects.equals(other.name, name) && year == other.year && Double.compare(fluffyness, other.fluffyness) == 0 && favoriteColour == other.favoriteColour && color == other.color && value == other.value;
			}
			return false;
		}
		
		public static IConfigSerializer<ExampleValue> createSerializer() {
			MutableObject<IConfigSerializer<ExampleValue>> result = new MutableObject<>();
			CompoundBuilder builder = new CompoundBuilder().addSetting(new CompoundArrayNamer<>(result::getValue, T -> Texts.literal(T.name)))
					.simple("Name", EntryDataType.STRING).setComments("Testing my ", "New Line Comment")
					.simple("Year", EntryDataType.INTEGER).addSuggestions(ISuggestionProvider.array(Suggestion.value("2000"), Suggestion.value("2005"), Suggestion.value("2017"), Suggestion.value("2023")))
					.simple("Fluffyness", EntryDataType.DOUBLE)
					.variants("Color", EntryDataType.CUSTOM, ColorWrapper.class, ColorWrapper::parse, ColorWrapper::serialize).addEntrySetting(new ColorType(false))
						.addSuggestions(ISuggestionProvider.array(
								Suggestion.namedTypeValue("Red", "0xFF0000", ColorWrapper.class), 
								Suggestion.namedTypeValue("Green", "0x00FF00", ColorWrapper.class), 
								Suggestion.namedTypeValue("Blue", "0x0000FF", ColorWrapper.class), 
								Suggestion.namedTypeValue("Black", "0x000000", ColorWrapper.class), 
								Suggestion.namedTypeValue("White", "0xFFFFFF", ColorWrapper.class)))
					.enums("Dye", DyeColor.class).forceSuggestions(true)
					.simple("Valid", EntryDataType.BOOLEAN);
			result.setValue(IConfigSerializer.noSync(builder.build(), new ExampleValue(), ExampleValue::parse, ExampleValue::serialize));
			return result.getValue();
		}
		
		public static List<ExampleValue> createExample() {
			return Collections.singletonList(new ExampleValue());
		}
		
		/*
		 * Parse Function that parses the DataType.
		 */
		public static ParseResult<ExampleValue> parse(ParsedMap map) {
			ParseResult<String> name = map.getOrError("Name", String.class);
			if(name.hasError()) return name.onlyError();
			ParseResult<Integer> year = map.getOrError("Year", Integer.class);
			if(year.hasError()) return year.onlyError();
			ParseResult<Double> fluffyness = map.getOrError("Fluffyness", Double.class);
			if(fluffyness.hasError()) return fluffyness.onlyError();
			ParseResult<ColorWrapper> color = map.getOrError("Color", ColorWrapper.class);
			if(color.hasError()) return color.onlyError();
			ParseResult<DyeColor> dye = map.getOrError("Dye", DyeColor.class);
			if(dye.hasError()) return dye.onlyError();
			ParseResult<Boolean> valid = map.getOrError("Valid", Boolean.class);
			if(valid.hasError()) return valid.onlyError();
			return ParseResult.success(new ExampleValue(name.getValue(), year.getValue(), fluffyness.getValue(), color.map(ColorWrapper::getColor), dye.getValue(), valid.getValue()));
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
			map.put("Dye", color);
			map.put("Valid", value);
			return map;
		}
	}
}
