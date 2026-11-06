package carbonconfiglib.plugins.mezz.configs;

import java.nio.file.Path;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.function.Consumer;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import carbonconfiglib.api.ConfigType;
import carbonconfiglib.api.IRange;
import carbonconfiglib.api.IRange.DoubleRange;
import carbonconfiglib.api.IRange.IntegerRange;
import carbonconfiglib.api.IRange.LongRange;
import carbonconfiglib.api.ISuggestionProvider.Suggestion;
import carbonconfiglib.gui.api.types.DataType;
import carbonconfiglib.impl.entries.ColorValue.ColorWrapper;
import carbonconfiglib.utils.ParseResult;
import carbonconfiglib.utils.structure.IStructuredData.StructureType;
import net.mezzdev.config.api.schema.ConfigSchemaType;
import net.mezzdev.config.api.schema.IConfigSchema;
import net.mezzdev.config.api.value.IConfigValue;
import net.mezzdev.config.api.value.serializer.IConfigKeyValueSerializer;
import net.mezzdev.config.api.value.serializer.IConfigListValueSerializer;
import net.mezzdev.config.api.value.serializer.IConfigValueSerializer;
import net.mezzdev.config.api.value.serializer.IDeserializeResult;
import net.mezzdev.config.serializers.BooleanSerializer;
import net.mezzdev.config.serializers.ColorSerializer;
import net.mezzdev.config.serializers.DoubleSerializer;
import net.mezzdev.config.serializers.EnumSerializer;
import net.mezzdev.config.serializers.IntegerSerializer;
import net.mezzdev.config.serializers.LongSerializer;
import speiger.src.collections.objects.lists.ObjectArrayList;
import speiger.src.collections.objects.utils.ObjectLists;

/**
 * Copyright 2026 Speiger, Meduris
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
public class MezzHelpers
{
	public static String getConfigName(IConfigSchema schema) {
		return schema.getPath().map(Path::getFileName).map(Path::toString).orElse("<Unknown>");
	}
	
	public static ConfigType toType(ConfigSchemaType type) {
		return switch(type) {
			case CLIENT -> ConfigType.CLIENT;
			case CLIENT_PER_WORLD -> ConfigType.SHARED;
			case SERVER -> ConfigType.SERVER;
		};
	}
	
	public static <T> boolean isDefault(IConfigValue<T> entry) {
		return Objects.equals(entry.getEditorInfo().getDefaultValue(), entry.get());
	}
	
	public static <T> void setDefault(IConfigValue<T> entry) {
		if(isDefault(entry)) return;
		entry.set(entry.getEditorInfo().getDefaultValue());
	}
	
	public static <T> String getValue(IConfigValue<T> entry) {
		return entry.getEditorInfo().getSerializer().serialize(entry.get());
	}
	
	public static <T> String getDefault(IConfigValue<T> entry) {
		return entry.getEditorInfo().getSerializer().serialize(entry.getEditorInfo().getDefaultValue());
	}
	
	public static <T, K, V> String getKey(String compound, IConfigKeyValueSerializer<T, K, V> serializer) {
		IDeserializeResult<T> entry = serializer.deserialize(compound);
		return entry.getResult().map(serializer::getKey).map(serializer.getKeySerializer()::serialize).orElseThrow();
	}
	
	public static <T, K, V> String getValue(String compound, IConfigKeyValueSerializer<T, K, V> serializer) {
		IDeserializeResult<T> entry = serializer.deserialize(compound);
		return entry.getResult().map(serializer::getValue).map(serializer.getValueSerializer()::serialize).orElseThrow();
	}
	
	public static <T, K, V> String createEntry(String key, String value, IConfigKeyValueSerializer<T, K, V> serializer) {
		IDeserializeResult<K> parsedKey = serializer.getKeySerializer().deserialize(key);
		IDeserializeResult<V> parsedValue = serializer.getValueSerializer().deserialize(value);
		return parsedKey.getResult().flatMap(KV -> parsedValue.getResult().map(VV -> serializer.createEntry(KV, VV))).map(serializer::serialize).orElseThrow();
	}
	
	public static <T, K, V> ParseResult<?> tryCreateEntry(String keyData, String valueData, IConfigKeyValueSerializer<T, K, V> serializer) {
		IDeserializeResult<K> parsedKey = serializer.getKeySerializer().deserialize(keyData);
		IDeserializeResult<V> parsedValue = serializer.getValueSerializer().deserialize(valueData);
		Optional<K> key = parsedKey.getResult();
		Optional<V> value = parsedValue.getResult();
		if(key.isEmpty() || value.isEmpty()) {
			StringJoiner joiner = new StringJoiner("\n");
			if(key.isEmpty()) {
				joiner.add("Key Invalid");
				parsedKey.getDiagnostics().forEach(joiner::add);
			}
			if(value.isEmpty()) {
				joiner.add("Value Invalid");
				parsedValue.getDiagnostics().forEach(joiner::add);
			}
			return ParseResult.error(NoSuchElementException::new, joiner.toString());
		}
		boolean keyValid = serializer.getKeySerializer().isValid(key.get());
		boolean valueValid = serializer.getValueSerializer().isValid(value.get());
		if(!keyValid || !valueValid) {
			StringBuilder builder = new StringBuilder();
			if(keyValid) builder.append("Parsed Key is not valid!").append("\n").append(keyData).append("\n");
			if(valueValid) builder.append("Parsed Value is not valid!").append("\n").append(valueData).append("\n");
			return ParseResult.error(NoSuchElementException::new, builder.toString());
		}
		T entry = serializer.createEntry(key.get(), value.get());
		if(!serializer.isValid(entry)) {
			return ParseResult.error(NoSuchElementException::new, "Key & Value are Valid, but the Entry is Invalid!");
		}
		return ParseResult.success(entry);
	}
	
	
	public static JsonArray merge(List<JsonElement> elements) {
		JsonArray array = new JsonArray();
		elements.forEach(array::add);
		return array;
	}
	
	public static JsonElement encode(String input) {
		return JsonParser.parseString(input);
	}
	
	public static String decode(JsonElement element) {
		return element.toString();
	}
	
	public static String decodeList(List<JsonElement> elements) {
		return merge(elements).toString();
	}
	
	public static List<JsonElement> encodeList(String input) {
		return input == null ? new ObjectArrayList<>() : new ObjectArrayList<>(encode(input).getAsJsonArray().asList());
	}
	
	@SuppressWarnings("unchecked")
	public static <T> String getArrayValue(IConfigValue<?> entry) {
		return getValueArray((IConfigValue<List<T>>)entry);
	}
	
	@SuppressWarnings("unchecked")
	public static <T> String getArrayDefault(IConfigValue<?> entry) {
		return getDefaultArray((IConfigValue<List<T>>)entry);
	}
	
	public static <T> String getValueArray(IConfigValue<List<T>> entry) {
		IConfigValueSerializer<T> serializer = ((IConfigListValueSerializer<T>)entry.getEditorInfo().getSerializer()).getElementSerializer();
		JsonArray array = new JsonArray();
		for(T value : entry.get()) {
			array.add(serializer.serialize(value));
		}
		return decode(array);
	}
	
	public static <T> String getDefaultArray(IConfigValue<List<T>> entry) {
		IConfigValueSerializer<T> serializer = ((IConfigListValueSerializer<T>)entry.getEditorInfo().getSerializer()).getElementSerializer();
		JsonArray array = new JsonArray();
		for(T value : entry.getEditorInfo().getDefaultValue()) {
			array.add(serializer.serialize(value));
		}
		return decode(array);
	}
	
	public static <T> void save(String input, IConfigValue<List<T>> entry) {
		IConfigValueSerializer<T> serializer = ((IConfigListValueSerializer<T>)entry.getEditorInfo().getSerializer()).getElementSerializer();
		List<T> result = new ObjectArrayList<>();
		for(JsonElement value : encodeList(input)) {
			parseValue(decode(value), serializer, result::add);
		}
		entry.set(result);
	}
	
	public static <T> void parseValue(String input, IConfigValueSerializer<T> serializer, Consumer<T> result) {
		IDeserializeResult<T> dataResult = serializer.deserialize(input);
		Optional<T> data = dataResult.getResult();
		if(data.isPresent() && serializer.isValid(data.get())) result.accept(data.get());
	}
	
	
	public static <T> ParseResult<T> parse(String input, IConfigValueSerializer<T> serializer) {
		IDeserializeResult<T> result = serializer.deserialize(input);
		Optional<T> data = result.getResult();
		if(data.isEmpty()) return ParseResult.error(NoSuchElementException::new, String.join("\n", result.getDiagnostics()));
		if(!serializer.isValid(data.get())) return ParseResult.error(input, "Not a Valid Input");
		return ParseResult.success(data.get());
	}
	
	public static StructureType getType(IConfigValueSerializer<?> serializer) {
		if(serializer instanceof IConfigListValueSerializer) return StructureType.LIST;
		else if(serializer instanceof IConfigKeyValueSerializer) return StructureType.COMPOUND;
		return StructureType.SIMPLE;
	}
	
	public static IRange getRange(IConfigValueSerializer<?> serializer) {
		if(serializer instanceof IRange range) return range;
		return switch(serializer) {
			case IntegerSerializer ints -> ints.getRange().map(T -> new IntegerRange(T.min(), T.max())).orElse(null);
			case LongSerializer longs -> longs.getRange().map(T -> new LongRange(T.min(), T.max())).orElse(null);
			case DoubleSerializer doubles -> doubles.getRange().map(T -> new DoubleRange(T.min(), T.max())).orElse(null);
			default -> null;
		};
	}
	
	public static <T> DataType getDataType(IConfigValueSerializer<T> serializer) {
		return switch(serializer) {
			case BooleanSerializer B -> DataType.BOOLEAN;
			case IntegerSerializer I -> DataType.INTEGER;
			case LongSerializer L -> DataType.LONG;
			case DoubleSerializer D -> DataType.DOUBLE;
			case ColorSerializer C -> DataType.byClass(ColorWrapper.class);
			case EnumSerializer<?> E -> DataType.ENUM;
			default -> DataType.STRING;
		};
	}
	
	public static <T> List<Suggestion> getSuggestions(IConfigValueSerializer<T> serializer) {
		Optional<List<T>> result = serializer.getAllValidValues();
		if(result.isEmpty()) return ObjectLists.empty();
		List<Suggestion> suggestions = new ObjectArrayList<>();
		for(T type : result.get()) {
			suggestions.add(Suggestion.value(serializer.serialize(type)));
		}
		return suggestions;
	}
}
