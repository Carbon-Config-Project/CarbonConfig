package carbonconfiglib.impl.internal;

import carbonconfiglib.config.ConfigHandler;
import carbonconfiglib.gui.api.DataType;
import carbonconfiglib.gui.api.ISuggestionRenderer;
import carbonconfiglib.gui.config.ColorElement;
import carbonconfiglib.gui.config.RegistryElement;
import carbonconfiglib.gui.widgets.SuggestionRenderers;
import carbonconfiglib.impl.entries.ColorValue;
import carbonconfiglib.impl.entries.ColorValue.ColorWrapper;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;

/**
 * Copyright 2025 Speiger, Meduris
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
 * 
 * 
 * Common Registries class so when people want to expand things have a bit more easy access.
 */
public class InternalFeatures
{
	
	
	public static void initMinecraftDataTypes(ConfigHandler handlers) {
		handlers.addParser('C', ColorValue::parse);
		handlers.addTempParser('R');
		handlers.addTempParser('K');
	}
	
	@Environment(EnvType.CLIENT)
	public static void loadDefaultTypes() {
		ISuggestionRenderer.SuggestionRegistry.register(Item.class, new SuggestionRenderers.ItemEntry());
		ISuggestionRenderer.SuggestionRegistry.register(Block.class, new SuggestionRenderers.ItemEntry());
		ISuggestionRenderer.SuggestionRegistry.register(Fluid.class, new SuggestionRenderers.FluidEntry());
		ISuggestionRenderer.SuggestionRegistry.register(Enchantment.class, new SuggestionRenderers.EnchantmentEntry());
		ISuggestionRenderer.SuggestionRegistry.register(ColorWrapper.class, new SuggestionRenderers.ColorEntry());
		ISuggestionRenderer.SuggestionRegistry.register(MobEffect.class, new SuggestionRenderers.PotionEntry());
		
		DataType.registerType(Item.class, RegistryElement.createForType(Item.class, "minecraft:air"));
		DataType.registerType(Block.class, RegistryElement.createForType(Block.class, "minecraft:air"));
		DataType.registerType(Fluid.class, RegistryElement.createForType(Fluid.class, "minecraft:empty"));
		DataType.registerType(Enchantment.class, RegistryElement.createForType(Enchantment.class, "minecraft:fortune"));
		DataType.registerType(MobEffect.class, RegistryElement.createForType(MobEffect.class, "minecraft:luck"));
		DataType.registerType(ColorWrapper.class, new DataType(false, "0xFFFFFFFF", ColorElement::new, ColorElement::new, ColorElement::new));
	}
}
