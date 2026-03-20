package carbonconfiglib.plugins.jei.configs;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

import carbonconfiglib.api.ISuggestionProvider.Suggestion;
import carbonconfiglib.utils.ParseResult;
import mezz.jei.api.runtime.config.IJeiConfigListValueSerializer;
import mezz.jei.api.runtime.config.IJeiConfigValue;
import mezz.jei.api.runtime.config.IJeiConfigValueSerializer;
import mezz.jei.api.runtime.config.IJeiConfigValueSerializer.IDeserializeResult;
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
public class JEIHelpers
{
	public static <T> boolean isDefault(IJeiConfigValue<T> entry) {
		return Objects.equals(entry.getDefaultValue(), entry.getValue());
	}
	
	public static <T> void setDefault(IJeiConfigValue<T> entry) {
		if(isDefault(entry)) return;
		entry.set(entry.getDefaultValue());
	}
	
	public static <T> String getValue(IJeiConfigValue<T> entry) {
		return entry.getSerializer().serialize(entry.getValue());
	}
	
	public static <T> String getDefault(IJeiConfigValue<T> entry) {
		return entry.getSerializer().serialize(entry.getDefaultValue());
	}
	
	@SuppressWarnings("unchecked")
	public static <T> List<String> getArrayValue(IJeiConfigValue<?> entry) {
		return getValueArray((IJeiConfigValue<List<T>>)entry);
	}
	
	@SuppressWarnings("unchecked")
	public static <T> List<String> getArrayDefault(IJeiConfigValue<?> entry) {
		return getDefaultArray((IJeiConfigValue<List<T>>)entry);
	}
	
	public static <T> List<String> getValueArray(IJeiConfigValue<List<T>> entry) {
		IJeiConfigValueSerializer<T> serializer = ((IJeiConfigListValueSerializer<T>)entry.getSerializer()).getListValueSerializer();
		List<String> result = new ArrayList<>();
		for(T value : entry.getValue()) {
			result.add(serializer.serialize(value));
		}
		return result;
	}
	
	public static <T> List<String> getDefaultArray(IJeiConfigValue<List<T>> entry) {
		IJeiConfigValueSerializer<T> serializer = ((IJeiConfigListValueSerializer<T>)entry.getSerializer()).getListValueSerializer();
		List<String> result = new ArrayList<>();
		for(T value : entry.getDefaultValue()) {
			result.add(serializer.serialize(value));
		}
		return result;
	}
	
	public static <T> void save(List<String> input, IJeiConfigValue<List<T>> entry) {
		IJeiConfigValueSerializer<T> serializer = ((IJeiConfigListValueSerializer<T>)entry.getSerializer()).getListValueSerializer();
		List<T> result = new ObjectArrayList<>();
		for(String value : input) {
			parseValue(value, serializer, result::add);
		}
		entry.set(result);
	}
	
	public static <T> void parseValue(String input, IJeiConfigValueSerializer<T> serializer, Consumer<T> result) {
		IDeserializeResult<T> dataResult = serializer.deserialize(input);
		Optional<T> data = dataResult.getResult();
		if(data.isPresent() && serializer.isValid(data.get())) result.accept(data.get());
	}
	
	public static <T> ParseResult<T> parse(String input, IJeiConfigValueSerializer<T> serializer) {
		IDeserializeResult<T> result = serializer.deserialize(input);
		Optional<T> data = result.getResult();
		if(data.isEmpty()) return ParseResult.error(NoSuchElementException::new, String.join("\n", result.getErrors()));
		if(!serializer.isValid(data.get())) return ParseResult.error(input, "Not a Valid Input");
		return ParseResult.success(data.get());
	}
	
	public static <T> List<Suggestion> getSuggestions(IJeiConfigValueSerializer<T> serializer) {
		Optional<Collection<T>> result = serializer.getAllValidValues();
		if(result.isEmpty()) return ObjectLists.empty();
		List<Suggestion> suggestions = new ObjectArrayList<>();
		for(T type : result.get()) {
			suggestions.add(Suggestion.value(serializer.serialize(type)));
		}
		return suggestions;
	}
}
