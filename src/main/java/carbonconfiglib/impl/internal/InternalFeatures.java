package carbonconfiglib.impl.internal;

import java.util.List;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import carbonconfiglib.api.IEntrySettings.TranslatedComment;
import carbonconfiglib.api.IEntrySettings.TranslatedKey;
import carbonconfiglib.api.ISuggestionProvider.Suggestion;
import carbonconfiglib.config.ConfigHandler;
import carbonconfiglib.gui.api.suggestion.ISuggestionRenderer;
import carbonconfiglib.gui.api.suggestion.SuggestionRenderers;
import carbonconfiglib.gui.api.types.DataType;
import carbonconfiglib.gui.api.types.EntrySettingTypes.ColorType;
import carbonconfiglib.gui.api.types.EntrySettingTypes.FloatingSlider;
import carbonconfiglib.gui.api.types.EntrySettingTypes.ForceMode;
import carbonconfiglib.gui.api.types.EntrySettingTypes.ForcedSelection;
import carbonconfiglib.gui.nodes.ColorElement;
import carbonconfiglib.gui.nodes.RegistryElement;
import carbonconfiglib.impl.entries.ColorValue;
import carbonconfiglib.impl.entries.ColorValue.ColorWrapper;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;
import speiger.src.collections.objects.lists.ObjectArrayList;

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
		handlers.addTempParser('r');
		handlers.addTempParser('K');
	}
	
	public static void loadDefaultTypes() {
		ISuggestionRenderer.Registry.register(Item.class, new SuggestionRenderers.ItemEntry());
		ISuggestionRenderer.Registry.register(Block.class, new SuggestionRenderers.ItemEntry());
		ISuggestionRenderer.Registry.register(Fluid.class, new SuggestionRenderers.FluidEntry());
		ISuggestionRenderer.Registry.register(Enchantment.class, new SuggestionRenderers.EnchantmentEntry());
		ISuggestionRenderer.Registry.register(ColorWrapper.class, new SuggestionRenderers.ColorEntry());
		ISuggestionRenderer.Registry.register(MobEffect.class, new SuggestionRenderers.PotionEntry());
		
		DataType.registerType(Item.class, RegistryElement.createForType(Item.class, "minecraft:air"));
		DataType.registerType(Block.class, RegistryElement.createForType(Block.class, "minecraft:air"));
		DataType.registerType(Fluid.class, RegistryElement.createForType(Fluid.class, "minecraft:empty"));
		DataType.registerType(Enchantment.class, RegistryElement.createForType(Enchantment.class, "minecraft:fortune"));
		DataType.registerType(MobEffect.class, RegistryElement.createForType(MobEffect.class, "minecraft:luck"));
		DataType.registerType(ColorWrapper.class, new DataType("0xFFFFFFFF", ColorElement::new));
	}
	
	public static void loadDefaultSettings() {
		SettingsLoader.INSTANCE.registerParser(Identifier.fromNamespaceAndPath("carbonconfig", "translation_key"), T -> T.has("key") ? new TranslatedKey(T.get("key").getAsString()) : null);
		SettingsLoader.INSTANCE.registerParser(Identifier.fromNamespaceAndPath("carbonconfig", "translation_comment"), T -> T.has("comment") ? new TranslatedComment(T.get("comment").getAsString()) : null);
		SettingsLoader.INSTANCE.registerParser(Identifier.fromNamespaceAndPath("carbonconfig", "color_type"), T -> T.has("hasAlpha") ? new ColorType(T.get("hasAlpha").getAsBoolean()) : null);
		SettingsLoader.INSTANCE.registerParser(Identifier.fromNamespaceAndPath("carbonconfig", "slider"), T -> T.has("stepSize") ? new FloatingSlider(T.get("stepSize").getAsDouble()) : null);
		SettingsLoader.INSTANCE.registerParser(Identifier.fromNamespaceAndPath("carbonconfig", "force_mode"), T -> T.has("forceText") ? new ForceMode(T.get("forceText").getAsBoolean()) : null);
		SettingsLoader.INSTANCE.registerParser(Identifier.fromNamespaceAndPath("carbonconfig", "force_selection"), T -> {
			if(!T.has("selection")) return null;
			List<Suggestion> suggestions = new ObjectArrayList<>();
			for(JsonElement element : T.getAsJsonArray("selection")) {
				JsonObject obj = element.getAsJsonObject();
				if(!obj.has("name") || !obj.has("value")) continue;
				suggestions.add(Suggestion.namedValue(obj.get("name").getAsString(), obj.get("value").getAsString()));
			}
			return new ForcedSelection(suggestions);
		});
	}
}
