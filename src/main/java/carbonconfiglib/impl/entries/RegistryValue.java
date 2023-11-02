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
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSets;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistry;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;

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
public class RegistryValue<T extends IForgeRegistryEntry<T>> extends CollectionConfigEntry<T, Set<T>> implements IArrayConfig
{
	ForgeRegistry<T> registry;
	Class<T> clz;
	Predicate<T> filter;
	
	protected RegistryValue(String key, IForgeRegistry<T> registry, Class<T> clz, Set<T> defaultValue, Predicate<T> filter, String... comment) {
		super(key, defaultValue, comment);
		this.registry = (ForgeRegistry<T>)registry;
		this.clz = clz;
		this.filter = filter;
		addSuggestionProvider(new RegistrySuggestions<>(this));
	}
	
	public static <E extends IForgeRegistryEntry<E>> Builder<E> builder(String key, Class<E> clz) {
		return new Builder<>(key, clz);
	}
	
	@Override
	protected RegistryValue<T> copy() {
		return new RegistryValue<>(getKey(), registry, clz, getDefault(), filter, getComment());
	}
	
	@Override
	protected String serializedValue(MultilinePolicy policy, Set<T> value) {
		String[] result = new String[value.size()];
		int i = 0;
		for(T entry : value) {
			result[i] = registry.getKey(entry).toString();
		}
		return serializeArray(policy, result);
	}
	
	@Override
	public ParseResult<Set<T>> parseValue(String value) {
		String[] values = Helpers.splitArray(value, ",");
		Set<T> result = new ObjectLinkedOpenHashSet<>();
		for(int i = 0,m=values.length;i<m;i++) {
			ResourceLocation location = ResourceLocation.tryParse(values[i]);
			if(location == null) continue;
			T entry = registry.getValue(location);
			if(entry == null || (filter != null && !filter.test(entry))) continue;
			result.add(entry);
		}
		return ParseResult.success(result);
	}
	
	@Override
	public ParseResult<Boolean> canSet(Set<T> value) {
		ParseResult<Boolean> result = super.canSet(value);
		if(result.hasError()) return result;
		for(T entry : value) {
			if(!registry.containsValue(entry)) return ParseResult.partial(false, NoSuchElementException::new, "Value ["+entry+"] doesn't exist in the registry");
			if(filter != null && !filter.test(entry)) return ParseResult.partial(false, IllegalArgumentException::new, "Value ["+registry.getKey(entry)+"] isn't allowed");
		}
		return ParseResult.success(true);
	}
	
	@Override
	public List<String> getEntries() {
		List<String> result = new ObjectArrayList<>();
		for(T entry : getValue()) {
			result.add(registry.getKey(entry).toString());
		}
		return result;
	}
	
	@Override
	public List<String> getDefaults() {
		List<String> result = new ObjectArrayList<>();
		for(T entry : getDefault()) {
			result.add(registry.getKey(entry).toString());
		}
		return result;
	}
	
	@Override
	public ParseResult<Boolean> canSetArray(List<String> entries) {
		if(entries == null) return ParseResult.partial(false, NullPointerException::new, "Value isn't allowed to be null");
		for(int i = 0,m=entries.size();i<m;i++) {
			T result = registry.getValue(ResourceLocation.tryParse(entries.get(i)));
			if(result == null) return ParseResult.partial(false, NoSuchElementException::new, "Value ["+entries.get(i)+"] doesn't exist in the registry");
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
		return 'R';
	}

	@Override
	public String getLimitations() {
		return "";
	}

	@Override
	public void serialize(IWriteBuffer buffer) {
		Set<T> value = getValue();
		buffer.writeVarInt(value.size());
		for(T entry : value) {
			buffer.writeVarInt(registry.getID(entry));
		}
	}

	@Override
	protected void deserializeValue(IReadBuffer buffer) {
		Set<T> result = new ObjectLinkedOpenHashSet<>();
		int size = buffer.readVarInt();
		for(int i = 0;i<size;i++) {
			T entry = registry.getValue(buffer.readVarInt());
			if(entry != null) {
				result.add(entry);
			}
		}
	}

	@Override
	protected Set<T> create(T value) {
		return ObjectSets.singleton(value);
	}
	
	public static class RegistrySuggestions<T extends IForgeRegistryEntry<T>> implements ISuggestionProvider {
		RegistryValue<T> value;
		
		public RegistrySuggestions(RegistryValue<T> value) {
			this.value = value;
		}
		
		@Override
		public void provideSuggestions(Consumer<Suggestion> output, Predicate<Suggestion> filter) {
			for(T entry : value.registry) {
				String key = value.registry.getKey(entry).toString();
				Suggestion suggestion = Suggestion.namedTypeValue(key, key, value.clz);
				if(filter.test(suggestion)) output.accept(suggestion);
			}
		}
	}
	
	public static class Builder<E extends IForgeRegistryEntry<E>> {
		Class<E> clz;
		String key;
		Set<E> values = new ObjectLinkedOpenHashSet<>();
		Predicate<E> filter;
		String[] comments;
		
		private Builder(String key, Class<E> clz) {
			this.key = key;
			this.clz = clz;
		}
		
		@SuppressWarnings("unchecked")
		public Builder<E> addDefault(E... elements) {
			values.addAll(ObjectArrayList.wrap(elements));
			return this;
		}
		
		public Builder<E> addDefaults(Collection<E> elements) {
			values.addAll(elements);
			return this;
		}
		
		public Builder<E> withFilter(Predicate<E> filter) {
			this.filter = filter;
			return this;
		}
		
		public Builder<E> withComment(String... comments) {
			this.comments = comments;
			return this;
		}
		
		public RegistryValue<E> build(IForgeRegistry<E> registry) {
			return new RegistryValue<>(key, registry, clz, values, filter, comments);
		}
		
		public RegistryValue<E> build(IForgeRegistry<E> registry, ConfigSection section) {
			return section.add(new RegistryValue<>(key, registry, clz, values, filter, comments));
		}
	}

}
