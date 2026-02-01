
package carbonconfiglib.impl.entries;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

import carbonconfiglib.api.ISuggestionProvider.Suggestion;
import carbonconfiglib.api.buffer.IReadBuffer;
import carbonconfiglib.api.buffer.IWriteBuffer;
import carbonconfiglib.config.ConfigEntry.BasicConfigEntry;
import carbonconfiglib.impl.entries.ColorValue.ColorWrapper;
import carbonconfiglib.utils.Helpers;
import carbonconfiglib.utils.MultilinePolicy;
import carbonconfiglib.utils.ParseResult;
import carbonconfiglib.utils.structure.IStructuredData;
import carbonconfiglib.utils.structure.IStructuredData.EntryDataType;
import carbonconfiglib.utils.structure.IStructuredData.SimpleData;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import speiger.src.collections.objects.lists.ObjectArrayList;

/**
 * Copyright 2023 Speiger, Meduris
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
public class ColorValue extends BasicConfigEntry<ColorWrapper>
{
	private static final String[] SIMPLE_COLOR_NAMES = new String[] {"black", "silver", "gray", "white", "maroon", "red", "purple", "fuchsia", "green", "lime", "olive", "yellow", "navy", "blue", "teal", "aqua"};
	private static final String[] SIMPLE_COLORS = new String[] {"0xff000000", "0xffc0c0c0", "0xff808080", "0xffffffff", "0xff800000", "0xffff0000", "0xff800080", "0xffff00ff", "0xff008000", "0xff00ff00", "0xff808000", "0xffffff00", "0xff000080", "0xff0000ff", "0xff008080", "0xff00ffff"};
	private static final String[] EXPANDED_COLOR_NAMES = new String[] {"aliceblue", "antiquewhite", "aqua", "aquamarine", "azure", "beige", "bisque", "black", "blanchedalmond", "blue", "blueviolet", "brown", "burlywood", "cadetblue", "chartreuse", "chocolate", "coral", "cornflowerblue", "cornsilk", "crimson", "darkblue", "darkcyan", "darkgoldenrod", "darkgray", "darkgreen", "darkgrey", "darkkhaki", "darkmagenta", "darkolivegreen", "darkorange", "darkorchid", "darkred", "darksalmon", "darkseagreen", "darkslateblue", "darkslategray", "darkslategrey", "darkturquoise", "darkviolet", "deeppink", "deepskyblue", "dimgray", "dimgrey", "dodgerblue", "firebrick", "floralwhite", "forestgreen", "fuchsia", "gainsboro", "ghostwhite", "gold", "goldenrod", "gray", "green", "greenyellow", "gray", "honeydew", "hotpink", "indianred", "indigo", "ivory", "khaki", "lavender", "lavenderblush", "lawngreen", "lemonchiffon", "lightblue", "lightcoral", "lightcyan", "lightgoldenrodyellow", "lightgray", "lightgreen", "lightgrey", "lightpink", "lightsalmon", "lightseagreen", "lightskyblue", "lightslategray", "lightslategrey", "lightsteelblue", "lightyellow", "lime", "limegreen", "linen", "maroon", "mediumaquamarine", "mediumblue", "mediumorchid", "mediumpurple", "mediumseagreen", "mediumslateblue", "mediumspringgreen", "mediumturquoise", "mediumvioletred", "midnightblue", "mintcream", "mistyrose", "moccasin", "navajowhite", "navy", "oldlace", "olive", "olivedrab", "orange", "orangered", "orchid", "palegoldenrod", "palegreen", "paleturquoise", "palevioletred", "papayawhip", "peachpuff", "peru", "pink", "plum", "powderblue", "purple", "rebeccapurple", "red", "rosybrown", "royalblue", "saddlebrown", "salmon", "sandybrown", "seagreen", "seashell", "sienna", "silver", "skyblue", "slateblue", "slategray", "slategrey", "snow", "springgreen", "steelblue", "tan", "teal", "thistle", "tomato", "turquoise", "violet", "wheat", "white", "whitesmoke", "yellow", "yellowgreen"};
	private static final String[] EXPANDED_COLORS = new String[] {"0xfff0f8ff", "0xfffaebd7", "0xff00ffff", "0xff7fffd4", "0xfff0ffff", "0xfff5f5dc", "0xffffe4c4", "0xff000000", "0xffffebcd", "0xff0000ff", "0xff8a2be2", "0xffa52a2a", "0xffdeb887", "0xff5f9ea0", "0xff7fff00", "0xffd2691e", "0xffff7f50", "0xff6495ed", "0xfffff8dc", "0xffdc143c", "0xff00008b", "0xff008b8b", "0xffb8860b", "0xffa9a9a9", "0xff006400", "0xffa9a9a9", "0xffbdb76b", "0xff8b008b", "0xff556b2f", "0xffff8c00", "0xff9932cc", "0xff8b0000", "0xffe9967a", "0xff8fbc8f", "0xff483d8b", "0xff2f4f4f", "0xff2f4f4f", "0xff00ced1", "0xff9400d3", "0xffff1493", "0xff00bfff", "0xff696969", "0xff696969", "0xff1e90ff", "0xffb22222", "0xfffffaf0", "0xff228b22", "0xffff00ff", "0xffdcdcdc", "0xfff8f8ff", "0xffffd700", "0xffdaa520", "0xff808080", "0xff008000", "0xffadff2f", "0xff808080", "0xfff0fff0", "0xffff69b4", "0xffcd5c5c", "0xff4b0082", "0xfffffff0", "0xfff0e68c", "0xffe6e6fa", "0xfffff0f5", "0xff7cfc00", "0xfffffacd", "0xffadd8e6", "0xfff08080", "0xffe0ffff", "0xfffafad2", "0xffd3d3d3", "0xff90ee90", "0xffd3d3d3", "0xffffb6c1", "0xffffa07a", "0xff20b2aa", "0xff87cefa", "0xff778899", "0xff778899", "0xffb0c4de", "0xffffffe0", "0xff00ff00", "0xff32cd32", "0xfffaf0e6", "0xff800000", "0xff66cdaa", "0xff0000cd", "0xffba55d3", "0xff9370db", "0xff3cb371", "0xff7b68ee", "0xff00fa9a", "0xff48d1cc", "0xffc71585", "0xff191970", "0xfff5fffa", "0xffffe4e1", "0xffffe4b5", "0xffffdead", "0xff000080", "0xfffdf5e6", "0xff808000", "0xff6b8e23", "0xffffa500", "0xffff4500", "0xffda70d6", "0xffeee8aa", "0xff98fb98", "0xffafeeee", "0xffdb7093", "0xffffefd5", "0xffffdab9", "0xffcd853f", "0xffffc0cb", "0xffdda0dd", "0xffb0e0e6", "0xff800080", "0xff663399", "0xffff0000", "0xffbc8f8f", "0xff4169e1", "0xff8b4513", "0xfffa8072", "0xfff4a460", "0xff2e8b57", "0xfffff5ee", "0xffa0522d", "0xffc0c0c0", "0xff87ceeb", "0xff6a5acd", "0xff708090", "0xff708090", "0xfffffafa", "0xff00ff7f", "0xff4682b4", "0xffd2b48c", "0xff008080", "0xffd8bfd8", "0xffff6347", "0xff40e0d0", "0xffee82ee", "0xfff5deb3", "0xffffffff", "0xfff5f5f5", "0xffffff00", "0xff9acd32"};
	
	public ColorValue(String key, int defaultValue, String... comment) {
		super(key, new ColorWrapper(defaultValue), comment);
	}
	
	@Override
	protected ColorValue copy() {
		return new ColorValue(getKey(), get(), getComment());
	}
	
	public ColorValue addSimpleColorSuggestions() {
		return addSuggestionProvider(ColorValue::addSimpleColors);
	}
	
	public ColorValue addExpandedColorSuggestions() {
		return addSuggestionProvider(ColorValue::addExpandedColors);
	}
	
	public ColorValue addMCChatFormatSuggestions() {
		addSuggestionProvider(ColorValue::addMCColorPalette);
		return this;
	}
	
	public static void addSimpleColors(Consumer<Suggestion> result, Predicate<Suggestion> filter) {
		for(int i = 0,m=SIMPLE_COLORS.length;i<m;i++) {
			Suggestion value = Suggestion.namedTypeValue(Helpers.toPascalCase(SIMPLE_COLOR_NAMES[i]), SIMPLE_COLORS[i], ColorWrapper.class);
			if(filter.test(value)) result.accept(value);
		}
	}
	
	public static void addExpandedColors(Consumer<Suggestion> result, Predicate<Suggestion> filter) {
		for(int i = 0,m=EXPANDED_COLORS.length;i<m;i++) {
			Suggestion value = Suggestion.namedTypeValue(Helpers.toPascalCase(EXPANDED_COLOR_NAMES[i]), EXPANDED_COLORS[i], ColorWrapper.class);
			if(filter.test(value)) result.accept(value);
		}
	}
	
	public static void addMCColorPalette(Consumer<Suggestion> result, Predicate<Suggestion> filter) {
		for(ChatFormatting formatting : ChatFormatting.values()) {
			if(!formatting.isColor()) continue;
			Suggestion value = Suggestion.namedTypeValue(Helpers.firstLetterUppercase(formatting.getName()), ColorWrapper.serializeRGB(formatting.getColor()), ColorWrapper.class);
			if(filter.test(value)) result.accept(value);
		}
	}
	
	public final ColorValue addSuggestions(int... values) {
		List<Suggestion> suggestions = new ObjectArrayList<>();
		for(int value : values) {
			suggestions.add(Suggestion.namedTypeValue(Long.toHexString(0xFF00000000L | value).substring(2), serializedValue(MultilinePolicy.DISABLED, new ColorWrapper(value)), ColorWrapper.class));
		}
		return this;
	}
	
	public final ColorValue addSuggestion(String name, int value) {
		return addSingleSuggestion(Suggestion.namedTypeValue(name, serializedValue(MultilinePolicy.DISABLED, new ColorWrapper(value)), ColorWrapper.class));
	}
	
	@Override
	public ParseResult<ColorWrapper> parseValue(String value) {
		ParseResult<Integer> result = ColorWrapper.parseInt(value);
		return result.hasError() ? result.onlyError() : ParseResult.success(new ColorWrapper(result.getValue()));
	}
	
	@Override
	public IStructuredData getDataType() {
		return SimpleData.variant(EntryDataType.CUSTOM, ColorWrapper.class);
	}
	
	public int get() {
		return getValue().getColor();
	}
	
	public int getRGB() {
		return getValue().getColor() & 0xFFFFFF;
	}
	
	public int getRGBA() {
		return getValue().getColor() & 0xFFFFFFFF;
	}
	
	public TextColor getMCColor() {
		return TextColor.fromRgb(getRGB());
	}
	
	public Style getMCStyle() {
		return Style.EMPTY.withColor(getMCColor());
	}
	
	public String toHex() {
		return ColorWrapper.serialize(getValue().getColor());
	}
	
	public String toRGBHex() {
		return ColorWrapper.serializeRGB(getValue().getColor() & 0xFFFFFF);
	}
	
	public String toRGBAHex() {
		return ColorWrapper.serialize(getValue().getColor() & 0xFFFFFFFF);
	}
	
	protected String serializedValue(MultilinePolicy policy, ColorWrapper value) {
		return ColorWrapper.serialize(value.getColor());
	}
	
	@Override
	public char getPrefix() {
		return 'C';
	}
	
	@Override
	public String getLimitations() {
		return "";
	}
	
	@Override
	public void serialize(IWriteBuffer buffer) {
		buffer.writeInt(get());
	}
	
	@Override
	protected void deserializeValue(IReadBuffer buffer) {
		set(new ColorWrapper(buffer.readInt()));
	}
	
	public static ParseResult<ColorValue> parse(String key, String value, String... comment) {
		ParseResult<Integer> result = ColorWrapper.parseInt(value);
		if (result.hasError()) return result.withDefault(new ColorValue(key, 0, comment));
		return ParseResult.success(new ColorValue(key, result.getValue(), comment));
	}
	
	public static class ColorWrapper extends Number {
		private static final long serialVersionUID = -6737187197596158253L;
		int color;
		
		public ColorWrapper(int color) {
			this.color = color;
		}
		
		@Override
		public boolean equals(Object obj) {
			return obj == this || (obj instanceof ColorWrapper && ((ColorWrapper)obj).color == color);
		}
		
		public int getColor() {
			return color;
		}
		
		public int intValue() { return color; }
		public long longValue() { return (long)color; }
		public float floatValue() { return (float)color; }
		public double doubleValue() { return (double)color; }
		
		public String serialize() {
			return serialize(color);
		}
		
		public static ParseResult<ColorWrapper> parse(String value) {
			try { return ParseResult.success(new ColorWrapper(Long.decode(value).intValue())); }
			catch (Exception e) { return ParseResult.error(value, e, "Couldn't parse Colour"); }
		}
		
		
		public static ParseResult<Integer> parseInt(String value) {
			try { return ParseResult.success(Long.decode(value).intValue()); }
			catch (Exception e) { return ParseResult.error(value, e, "Couldn't parse Colour"); }
		}
		
		public static String serializeRGB(long color) {
			return "0x"+(Long.toHexString(0xFF000000L | (color & 0xFFFFFFL)).substring(2));
		}
		
		public static String serialize(long color) {
			return "0x"+(Long.toHexString(0xFF00000000L | (color & 0xFFFFFFFFL)).substring(2));
		}
	}
}
