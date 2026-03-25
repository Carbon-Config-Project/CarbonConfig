package carbonconfiglib.impl.entries;

import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.ToIntFunction;

import net.minecraft.block.Block;
import net.minecraft.client.resources.I18n;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.Item;
import net.minecraft.potion.Potion;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.registries.ForgeRegistry;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;

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
	public static final NamedRegistry<Block> BLOCKS = NamedRegistry.ofForge(ForgeRegistries.BLOCKS, T -> T.getLocalizedName());
	public static final NamedRegistry<Item> ITEMS = NamedRegistry.ofForge(ForgeRegistries.ITEMS, T -> T.getItemStackDisplayName(T.getDefaultInstance()));
	public static final NamedRegistry<Potion> MOB_EFFECTS = NamedRegistry.ofForge(ForgeRegistries.POTIONS, T -> T.getName());
	public static final NamedRegistry<Enchantment> ENCHANTMENTS = NamedRegistry.ofForge(ForgeRegistries.ENCHANTMENTS, T -> I18n.format(T.getName()));
	
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
	
	public static <T extends IForgeRegistryEntry<T>> NamedRegistry<T> ofForge(IForgeRegistry<T> registry, Function<T, String> nameFunction) {
		ForgeRegistry<T> impl = (ForgeRegistry<T>)registry;
		return new NamedRegistry<T>(nameFunction, 
				registry::getKeys, registry::getKey, registry::containsKey, impl::getID, T -> impl.getKey(impl.getValue(T)), 
				() -> registry, registry::getValue, registry::containsValue, impl::getID, impl::getValue);
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
