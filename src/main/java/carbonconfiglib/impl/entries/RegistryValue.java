package carbonconfiglib.impl.entries;

import java.util.Collection;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;

import carbonconfiglib.api.ISuggestionProvider;
import carbonconfiglib.api.buffer.IReadBuffer;
import carbonconfiglib.api.buffer.IWriteBuffer;
import carbonconfiglib.config.ConfigEntry.CollectionConfigEntry;
import carbonconfiglib.config.ConfigSection;
import carbonconfiglib.utils.Helpers;
import carbonconfiglib.utils.MultilinePolicy;
import carbonconfiglib.utils.ParseResult;
import carbonconfiglib.utils.structure.IStructuredData;
import carbonconfiglib.utils.structure.IStructuredData.EntryDataType;
import carbonconfiglib.utils.structure.StructureList.ListBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSets;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistry;

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
public class RegistryValue<T> extends CollectionConfigEntry<T, Set<T>>
{
	NamedForgeRegistry<T> registry;
	Class<T> clz;
	Predicate<T> filter;
	
	protected RegistryValue(String key, NamedForgeRegistry<T> registry, Class<T> clz, Set<T> defaultValue, Predicate<T> filter, String... comment) {
		super(key, defaultValue, comment);
		this.registry = registry;
		this.clz = clz;
		this.filter = filter;
		addSuggestionProvider(new RegistrySuggestions<>(this));
	}
	
	public static <E> Builder<E> builder(String key, Class<E> clz) {
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
			result[i++] = registry.getRegistry().getKey(entry).toString();
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
			T entry = registry.getRegistry().getValue(location);
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
			if(!registry.getRegistry().containsValue(entry)) return ParseResult.partial(false, NoSuchElementException::new, "Value ["+entry+"] doesn't exist in the registry");
			if(filter != null && !filter.test(entry)) return ParseResult.partial(false, IllegalArgumentException::new, "Value ["+registry.getRegistry().getKey(entry)+"] isn't allowed");
		}
		return ParseResult.success(true);
	}
	
	private ParseResult<T> parseEntry(String value) {
		ResourceLocation location = ResourceLocation.tryParse(value);
		if(location == null) return ParseResult.error(value, "Id ["+value+"] isn't a valid resource location");
		T entry = registry.getRegistry().getValue(location);
		if(entry == null || (filter != null && !filter.test(entry))) return ParseResult.error(value, "Id ["+value+"] isn't valid");
		return ParseResult.success(entry);
	}
	
	@Override
	public IStructuredData getDataType() {
		return ListBuilder.variants(EntryDataType.CUSTOM, clz, this::parseEntry, T -> registry.getRegistry().getKey(T).toString()).addSuggestions(ISuggestionProvider.wrapper(this::getSuggestions)).build(true);
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
			buffer.writeVarInt(registry.getImplRegistry().getID(entry));
		}
	}

	@Override
	protected void deserializeValue(IReadBuffer buffer) {
		Set<T> result = new ObjectLinkedOpenHashSet<>();
		int size = buffer.readVarInt();
		for(int i = 0;i<size;i++) {
			T entry = registry.getImplRegistry().getValue(buffer.readVarInt());
			if(entry != null) {
				result.add(entry);
			}
		}
	}

	@Override
	protected Set<T> create(T value) {
		return ObjectSets.singleton(value);
	}
	
	public static class RegistrySuggestions<T> implements ISuggestionProvider {
		RegistryValue<T> value;
		
		public RegistrySuggestions(RegistryValue<T> value) {
			this.value = value;
		}
		
		@Override
		public void provideSuggestions(Consumer<Suggestion> output, Predicate<Suggestion> filter) { 
			for(T entry : value.registry.getRegistry()) {
				Suggestion suggestion = Suggestion.namedTypeValue(value.registry.getName(entry), value.registry.getRegistry().getKey(entry).toString(), value.clz);
				if(filter.test(suggestion)) output.accept(suggestion);
			}
		}
	}
	
	public static class Builder<E> {
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
			return new RegistryValue<>(key, new NamedForgeRegistry<>(registry, null), clz, values, filter, comments);
		}
		
		public RegistryValue<E> build(IForgeRegistry<E> registry, ConfigSection section) {
			return section.add(new RegistryValue<>(key, new NamedForgeRegistry<>(registry, null), clz, values, filter, comments));
		}
		
		public RegistryValue<E> build(NamedForgeRegistry<E> registry) {
			return new RegistryValue<>(key, registry, clz, values, filter, comments);
		}
		
		public RegistryValue<E> build(NamedForgeRegistry<E> registry, ConfigSection section) {
			return section.add(new RegistryValue<>(key, registry, clz, values, filter, comments));
		}
	}

}
