package carbonconfiglib.impl.entries;

import java.util.Collection;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.StringJoiner;
import java.util.function.Consumer;
import java.util.function.Predicate;

import carbonconfiglib.api.ISuggestionProvider;
import carbonconfiglib.api.buffer.IReadBuffer;
import carbonconfiglib.api.buffer.IWriteBuffer;
import carbonconfiglib.config.ConfigEntry.CollectionConfigEntry;
import carbonconfiglib.config.ConfigEntry.IArrayConfig;
import carbonconfiglib.config.ConfigSection;
import carbonconfiglib.utils.Helpers;
import carbonconfiglib.utils.IEntryDataType;
import carbonconfiglib.utils.IEntryDataType.SimpleDataType;
import carbonconfiglib.utils.MultilinePolicy;
import carbonconfiglib.utils.ParseResult;
import cpw.mods.fml.common.registry.FMLControlledNamespacedRegistry;
import speiger.src.collections.objects.lists.ObjectArrayList;
import speiger.src.collections.objects.sets.ObjectLinkedOpenHashSet;
import speiger.src.collections.objects.utils.ObjectSets;

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
public class RegistryKeyValue extends CollectionConfigEntry<String, Set<String>> implements IArrayConfig
{
	FMLControlledNamespacedRegistry<?> registry;
	Class<?> clz;
	Predicate<String> filter;
	
	public RegistryKeyValue(String key, FMLControlledNamespacedRegistry<?> registry, Class<?> clz, Set<String> defaultValue, Predicate<String> filter, String... comment) {
		super(key, defaultValue, comment);
		this.registry = registry;
		this.clz = clz;
		this.filter = filter;
		addSuggestionProvider(new RegistryKeySuggestions(this));
	}
	
	public static <E> Builder<E> builder(String key, Class<E> clz) {
		return new Builder<>(key, clz);
	}
	
	@Override
	protected RegistryKeyValue copy() {
		return new RegistryKeyValue(getKey(), registry, clz, getDefault(), filter, getComment());
	}
	
	@Override
	protected String serializedValue(MultilinePolicy policy, Set<String> value) {
		String[] result = new String[value.size()];
		int i = 0;
		for(String entry : value) {
			result[i] = entry.toString();
		}
		return serializeArray(policy, result);
	}
	
	@Override
	public ParseResult<Set<String>> parseValue(String value) {
		String[] values = Helpers.splitArray(value, ",");
		Set<String> result = new ObjectLinkedOpenHashSet<>();
		for(int i = 0,m=values.length;i<m;i++) {
			String location = new String(values[i]);
			if(location == null || (filter != null && !filter.test(location))) continue;
			result.add(location);
		}
		return ParseResult.success(result);
	}
	
	@Override
	public ParseResult<Boolean> canSet(Set<String> value) {
		ParseResult<Boolean> result = super.canSet(value);
		if(result.hasError()) return result;
		for(String entry : value) {
			if(!registry.containsKey(entry)) return ParseResult.partial(false, NoSuchElementException::new, "Value ["+entry+"] doesn't exist in the registry");
			if(filter != null && !filter.test(entry)) return ParseResult.partial(false, IllegalArgumentException::new, "Value ["+entry+"] isn't allowed");
		}
		return ParseResult.success(true);
	}
	
	@Override
	public List<String> getEntries() {
		List<String> result = new ObjectArrayList<>();
		for(String entry : getValue()) {
			result.add(entry.toString());
		}
		return result;
	}
	
	@Override
	public List<String> getDefaults() {
		List<String> result = new ObjectArrayList<>();
		for(String entry : getDefault()) {
			result.add(entry.toString());
		}
		return result;
	}
	
	@Override
	public ParseResult<Boolean> canSetArray(List<String> entries) {
		if(entries == null) return ParseResult.partial(false, NullPointerException::new, "Value isn't allowed to be null");
		for(int i = 0,m=entries.size();i<m;i++) {
			String result = new String(entries.get(i));
			if(result == null || !registry.containsKey(result)) return ParseResult.partial(false, NoSuchElementException::new, "Value ["+entries.get(i)+"] doesn't exist in the registry");
			if(filter != null && !filter.test(result)) return ParseResult.partial(false, IllegalArgumentException::new, "Value ["+entries.get(i)+"] isn't allowed");
		}
		return ParseResult.success(true);
	}
	
	@Override
	public void setArray(List<String> entries) {
		StringJoiner joiner = new StringJoiner(",");
		for(String s : entries) {
			joiner.add(s);
		}
		deserializeValue(joiner.toString());
	}
	
	@Override
	public IEntryDataType getDataType() {
		return SimpleDataType.ofVariant(clz);
	}
	
	@Override
	public char getPrefix() {
		return 'K';
	}
	
	@Override
	public String getLimitations() {
		return "";
	}
	
	@Override
	public void serialize(IWriteBuffer buffer) {
		Set<String> value = getValue();
		buffer.writeVarInt(value.size());
		for(String entry : value) {
			buffer.writeString(entry.toString());
		}
	}
	
	@Override
	protected void deserializeValue(IReadBuffer buffer) {
		Set<String> result = new ObjectLinkedOpenHashSet<>();
		int size = buffer.readVarInt();
		for(int i = 0;i<size;i++) {
			String entry = new String(buffer.readString());
			if(entry != null) {
				result.add(entry);
			}
		}
	}
	
	@Override
	protected Set<String> create(String value) {
		return ObjectSets.singleton(value);
	}
	
	public static class RegistryKeySuggestions implements ISuggestionProvider {
		RegistryKeyValue value;
		
		public RegistryKeySuggestions(RegistryKeyValue value) {
			this.value = value;
		}
		
		@Override
		@SuppressWarnings("unchecked")
		public void provideSuggestions(Consumer<Suggestion> output, Predicate<Suggestion> filter) {
			for(String entry : (Collection<String>)value.registry.getKeys()) {
				String key = entry.toString();
				Suggestion suggestion = Suggestion.namedTypeValue(key, key, value.clz);
				if(filter.test(suggestion)) output.accept(suggestion);
			}
		}
	}
	
	public static class Builder<E> {
		Class<E> clz;
		String key;
		Set<E> unparsedValues = new ObjectLinkedOpenHashSet<>();
		Set<String> values = new ObjectLinkedOpenHashSet<>();
		Predicate<String> filter;
		String[] comments;
		
		private Builder(String key, Class<E> clz) {
			this.key = key;
			this.clz = clz;
		}
		
		@SuppressWarnings("unchecked")
		public Builder<E> addDirectDefault(E... elements) {
			unparsedValues.addAll(ObjectArrayList.wrap(elements));
			return this;
		}
		
		public Builder<E> addDirectDefaults(Collection<E> elements) {
			unparsedValues.addAll(elements);
			return this;
		}
		
		public Builder<E> addDefault(String... elements) {
			values.addAll(ObjectArrayList.wrap(elements));
			return this;
		}
		
		public Builder<E> addDefaults(Collection<String> elements) {
			values.addAll(elements);
			return this;
		}
		
		public Builder<E> withFilter(Predicate<String> filter) {
			this.filter = filter;
			return this;
		}
		
		public Builder<E> withComment(String... comments) {
			this.comments = comments;
			return this;
		}
		
		private void parseValues(FMLControlledNamespacedRegistry<E> registry) {
			for(E entry : unparsedValues) {
				String location = registry.getNameForObject(entry);
				if(location != null) values.add(location);
			}
			unparsedValues.clear();
		}
		
		public RegistryKeyValue build(FMLControlledNamespacedRegistry<E> registry) {
			parseValues(registry);
			return new RegistryKeyValue(key, registry, clz, values, filter, comments);
		}
		
		public RegistryKeyValue build(FMLControlledNamespacedRegistry<E> registry, ConfigSection section) {
			parseValues(registry);
			return section.add(new RegistryKeyValue(key, registry, clz, values, filter, comments));
		}
	}

}
