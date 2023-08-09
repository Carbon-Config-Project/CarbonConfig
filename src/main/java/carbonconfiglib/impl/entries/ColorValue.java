package carbonconfiglib.impl.entries;

import carbonconfiglib.api.buffer.IReadBuffer;
import carbonconfiglib.api.buffer.IWriteBuffer;
import carbonconfiglib.config.ConfigEntry.BasicConfigEntry;
import carbonconfiglib.impl.entries.ColorValue.ColorWrapper;
import carbonconfiglib.utils.IEntryDataType;
import carbonconfiglib.utils.IEntryDataType.SimpleDataType;
import carbonconfiglib.utils.MultilinePolicy;
import carbonconfiglib.utils.ParseResult;

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
	public ColorValue(String key, int defaultValue, String... comment) {
		super(key, new ColorWrapper(defaultValue), comment);
	}
	
	@Override
	protected ColorValue copy() {
		return new ColorValue(getKey(), get(), getComment());
	}
	
	public final ColorValue addSuggestions(int... values) {
		for(int value : values) {
			addSuggestionInternal(Long.toHexString(0xFF00000000L | value).substring(2), serializedValue(MultilinePolicy.DISABLED, new ColorWrapper(value)), ColorWrapper.class);		
		}
		return this;
	}
	
	public final ColorValue addSuggestion(String name, int value) {
		return addSuggestionInternal(name, serializedValue(MultilinePolicy.DISABLED, new ColorWrapper(value)), ColorWrapper.class);
	}
	
	@Override
	public ParseResult<ColorWrapper> parseValue(String value) {
		ParseResult<Integer> result = ColorWrapper.parseInt(value);
		return result.hasError() ? result.onlyError() : ParseResult.success(new ColorWrapper(result.getValue()));
	}
	
	@Override
	public IEntryDataType getDataType() {
		return SimpleDataType.ofVariant(ColorWrapper.class);
	}
	
	public int get() {
		return getValue().getColor();
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
	
	public static class ColorWrapper {
		int color;
		
		public ColorWrapper(int color) {
			this.color = color;
		}
		
		public int getColor() {
			return color;
		}
		
		public static ParseResult<Integer> parseInt(String value) {
			try { return ParseResult.success(Long.decode(value).intValue()); }
			catch (Exception e) { return ParseResult.error(value, e, "Couldn't parse Colour"); }
		}
		
		public static String serialize(long color) {
			return "0x"+(Long.toHexString(0xFF00000000L | (color & 0xFFFFFFFFL)).substring(2));
		}
	}
}
