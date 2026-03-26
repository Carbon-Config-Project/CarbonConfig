package carbonconfiglib.impl.entries;

import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.ToIntFunction;

import cpw.mods.fml.common.registry.FMLControlledNamespacedRegistry;
import cpw.mods.fml.common.registry.GameData;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

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
public class NamedRegistry<T>
{
	public static final NamedRegistry<Block> BLOCKS = NamedRegistry.ofForge(GameData.getBlockRegistry(), T -> T.getLocalizedName());
	public static final NamedRegistry<Item> ITEMS = NamedRegistry.ofForge(GameData.getItemRegistry(), T -> T.getItemStackDisplayName(new ItemStack(T)));
	
	Function<T, String> nameFunction;
	Supplier<Iterable<ResourceLocation>> keyProvider;
	Function<T, ResourceLocation> keyGetter;
	Predicate<ResourceLocation> containsKey;
	ToIntFunction<ResourceLocation> keyToId;
	IntFunction<ResourceLocation> idToKey;
	Supplier<Iterable<T>> valueProvider;
	Function<ResourceLocation, T> valueGetter;
	Predicate<T> containsValue;
	ToIntFunction<T> valueToId;
	IntFunction<T> idToValue;
	
	public NamedRegistry(Function<T, String> nameFunction, Supplier<Iterable<ResourceLocation>> keyProvider, Function<T, ResourceLocation> keyGetter, Predicate<ResourceLocation> containsKey, ToIntFunction<ResourceLocation> keyToId, IntFunction<ResourceLocation> idToKey, Supplier<Iterable<T>> valueProvider, Function<ResourceLocation, T> valueGetter, Predicate<T> containsValue, ToIntFunction<T> valueToId, IntFunction<T> idToValue) {
		this.nameFunction = nameFunction;
		this.keyProvider = keyProvider;
		this.keyGetter = keyGetter;
		this.containsKey = containsKey;
		this.keyToId = keyToId;
		this.idToKey = idToKey;
		this.valueProvider = valueProvider;
		this.valueGetter = valueGetter;
		this.containsValue = containsValue;
		this.valueToId = valueToId;
		this.idToValue = idToValue;
	}
	
	@SuppressWarnings("unchecked")
	public static <T> NamedRegistry<T> ofForge(FMLControlledNamespacedRegistry<T> registry, Function<T, String> nameFunction) {
		return new NamedRegistry<T>(nameFunction, 
				registry::getKeys, T -> new ResourceLocation(registry.getNameForObject(T)), registry::containsKey, T -> registry.getId(T.toString()), T -> new ResourceLocation(registry.getNameForObject(registry.getObjectById(T))), 
				() -> registry, T -> registry.getObject(T.toString()), T -> registry.containsKey(registry.getNameForObject(T)), registry::getId, registry::getObjectById);
	}
	
	public ResourceLocation getKey(T value) {
		return keyGetter.apply(value);
	}
	
	public boolean containsKey(ResourceLocation key) {
		return containsKey.test(key);
	}
	
	public int getId(ResourceLocation key) {
		return keyToId.applyAsInt(key);
	}
	
	public ResourceLocation getKey(int id) {
		return idToKey.apply(id);
	}
	
	public T getValue(ResourceLocation key) {
		return valueGetter.apply(key);
	}
	
	public boolean containsValue(T value) {
		return containsValue.test(value);
	}
	
	public int getId(T value) {
		return valueToId.applyAsInt(value);
	}
	
	public T getValue(int id) {
		return idToValue.apply(id);
	}
	
	public Iterable<ResourceLocation> getKeys() {
		return keyProvider.get();
	}
	
	public Iterable<T> getValues() {
		return valueProvider.get();
	}
	
	public boolean hasNameGenerator() {
		return nameFunction != null;
	}
	
	public String getName(ResourceLocation id) {
		return nameFunction == null ? id.toString() : nameFunction.apply(getValue(id));
	}
	
	public String getName(T value) {
		return nameFunction == null ? getKey(value).toString() : nameFunction.apply(value);
	}
}
