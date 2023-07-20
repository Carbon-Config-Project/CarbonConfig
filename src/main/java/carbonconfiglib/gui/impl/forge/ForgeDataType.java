package carbonconfiglib.gui.impl.forge;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import carbonconfiglib.api.ISuggestedEnum;
import carbonconfiglib.config.ConfigEntry.Suggestion;
import carbonconfiglib.gui.api.DataType;
import carbonconfiglib.utils.Helpers;
import carbonconfiglib.utils.ParseResult;
import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraftforge.common.ForgeConfigSpec.ValueSpec;

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
public class ForgeDataType<T>
{
	private static final Map<Class<?>, ForgeDataType<?>> DATA_TYPES = Object2ObjectMaps.synchronize(new Object2ObjectOpenHashMap<>());
	public static final ForgeDataType<Boolean> BOOLEAN = new ForgeDataType<>(Boolean.class, DataType.BOOLEAN, ForgeHelpers::parseBoolean, T -> T.toString(), null);
	public static final ForgeDataType<Integer> INTEGER = new ForgeDataType<>(Integer.class, DataType.INTEGER, Helpers::parseInt, T -> T.toString(), ForgeHelpers::getIntLimit);
	public static final ForgeDataType<Long> LONG = new ForgeDataType<>(Long.class, DataType.INTEGER, ForgeHelpers::parseLong, T -> T.toString(), ForgeHelpers::getLongLimit);
	public static final ForgeDataType<Float> FLOAT = new ForgeDataType<>(Float.class, DataType.DOUBLE, ForgeHelpers::parseFloat, T -> T.toString(), ForgeHelpers::getFloatLimit);
	public static final ForgeDataType<Double> DOUBLE = new ForgeDataType<>(Double.class, DataType.DOUBLE, Helpers::parseDouble, T -> T.toString(), ForgeHelpers::getDoubleLimit);
	public static final ForgeDataType<String> STRING = new ForgeDataType<>(String.class, DataType.STRING, ForgeHelpers::parseString, Function.identity(), null);
	
	DataType type;
	Function<String, ParseResult<T>> parse;
	Function<T, String> serialize;
	Function<Object[], String> rangeInfo;
	
	ForgeDataType(Class<T> clz, DataType type, Function<String, ParseResult<T>> parse, Function<T, String> serialize, Function<Object[], String> rangeInfo) {
		this(type, parse, serialize, rangeInfo);
		DATA_TYPES.putIfAbsent(clz, this);
	}
	
	public ForgeDataType(DataType type, Function<String, ParseResult<T>> parse, Function<T, String> serialize, Function<Object[], String> rangeInfo) {
		this.type = type;
		this.parse = parse;
		this.serialize = serialize;
		this.rangeInfo = rangeInfo;
	}
	
	public DataType getDataType() {
		return type;
	}
	
	public String getLimitations(ValueSpec spec) {
		if(rangeInfo == null) return "";
		Object[] data = ForgeHelpers.getRangeInfo(spec);
		return data == null ? "" : rangeInfo.apply(ForgeHelpers.getRangeInfo(spec));
	}
	
	public ParseResult<T> parse(String input) {
		return parse.apply(input);
	}
	
	public String serialize(T value) {
		return serialize.apply(value);
	}
	
	public static void registerDataType(Class<?> clz, ForgeDataType<?> type) {
		DATA_TYPES.putIfAbsent(clz, type);
	}
	
	@SuppressWarnings("unchecked")
	public static <T> ForgeDataType<T> getDataByType(Class<T> clz) {
		if(clz == null) return null;
		ForgeDataType<T> type = (ForgeDataType<T>)DATA_TYPES.get(clz);
		return type == null && clz.isEnum() ? EnumDataType.create(clz) : type;
	}
	
	public static class EnumDataType<T extends Enum<T>> extends ForgeDataType<T> {
		Class<T> clz;
		
		EnumDataType(Class<T> clz) {
			super(clz, DataType.STRING, null, null, null);
			this.clz = clz;
		}
		
		@Override
		public ParseResult<T> parse(String input) {
			try { return ParseResult.success(Enum.valueOf(clz, input)); }
			catch (Exception e) { return ParseResult.error(input, e, "Value must be one of the following: "+Arrays.toString(toArray(null))); }
		}
		
		@Override
		public String getLimitations(ValueSpec spec) {
			return "Value must be one of the following: "+Arrays.toString(toArray(spec));
		}
		
		public List<Suggestion> getSuggestions(ValueSpec spec) {
			List<Suggestion> result = new ObjectArrayList<>();
			for(T value : clz.getEnumConstants()) {
				if(!spec.test(value)) continue;
				ISuggestedEnum<T> wrapper = ISuggestedEnum.getWrapper(value);
				if(wrapper != null) result.add(new Suggestion(wrapper.getName(value), value));
				else result.add(new Suggestion(value.toString()));
			}
			return result;
		}
		
		@Override
		public String serialize(T value) {
			return value.name();
		}
		
		@SuppressWarnings({"rawtypes", "unchecked" })
		public static <T> ForgeDataType<T> create(Class<T> clz) {
			return new EnumDataType(clz);
		}
		
		private String[] toArray(ValueSpec spec) {
			T[] array = clz.getEnumConstants();
			List<String> values = new ObjectArrayList<>();
			for(int i = 0,m=array.length;i<m;i++) {
				if(spec == null || spec.test(array[i])) {
					values.add(array[i].name());
				}
			}
			return values.toArray(new String[values.size()]);
		}
	}
}
