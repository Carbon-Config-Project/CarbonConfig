package carbonconfiglib.gui.api.suggestion;

import java.util.List;
import java.util.Optional;

import carbonconfiglib.CarbonConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.block.FluidModel;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

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
public class SuggestionRenderers
{
	public static class ItemEntry implements ISuggestionRenderer {
		@Override
		public Component renderSuggestion(GuiGraphicsExtractor graphics, String value, int x, int y) {
			Identifier id = Identifier.tryParse(value);
			if(id == null) return null;
			Item item = BuiltInRegistries.ITEM.getValue(id);
			if(item == Items.AIR || item == null) return null;
			ItemStack itemStack = new ItemStack(item);
			graphics.fakeItem(itemStack, x, y);
			return itemStack.getHoverName().copy().withStyle(ChatFormatting.YELLOW).append("\n").append(Component.literal(id.toString()).withStyle(ChatFormatting.GRAY));			
		}
	}
	
	public static class FluidEntry implements ISuggestionRenderer {
		@Override
		public Component renderSuggestion(GuiGraphicsExtractor graphics, String value, int x, int y) {
			Identifier id = Identifier.tryParse(value);
			if(id == null) return null;
			Fluid fluid = BuiltInRegistries.FLUID.getValue(id);
			if(fluid == Fluids.EMPTY || fluid == null) return null;
			FluidModel fluidModel = Minecraft.getInstance().getModelManager().getFluidStateModelSet().get(fluid.defaultFluidState());
			if(fluidModel == null) return null;
			TextureAtlasSprite sprite = fluidModel.stillMaterial().sprite();
			if(sprite == null) return null;
			graphics.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, x, y, 18, 18, fluidModel.fluidTintSource().color(fluid.defaultFluidState()));
			return fluid.getFluidType().getDescription().copy().withStyle(ChatFormatting.YELLOW).append("\n").append(Component.literal(id.toString()).withStyle(ChatFormatting.GRAY));
		}
	}
	
	public static class EnchantmentEntry implements ISuggestionRenderer {
		@Override
		public Component renderSuggestion(GuiGraphicsExtractor graphics, String value, int x, int y) {
			Identifier id = Identifier.tryParse(value);
			if(id == null) return null;
			ClientLevel level = Minecraft.getInstance().level;
			if(level == null) {
				if(CarbonConfig.SHOW_MISSING_ENCHANTMENT_TEXTURE.getValue()) {
					graphics.blitSprite(RenderPipelines.GUI_TEXTURED, MissingTextureAtlasSprite.getLocation(), x, y, 18, 18);
					return Component.translatable("gui.carbonconfig.enchantment.missing").withStyle(ChatFormatting.RED);
				}
				return null;
			}
			Holder<Enchantment> holder = getEnchantmnetById(level, id);
			if(holder == null) return null;
			Enchantment ench = holder.value();
			if(ench == null) return null;
			graphics.fakeItem(EnchantmentHelper.createBook(new EnchantmentInstance(holder, ench.getMinLevel())), x, y);
			return Enchantment.getFullname(holder, ench.getMinLevel()).copy().withStyle(ChatFormatting.YELLOW).append("\n").append(Component.literal(id.toString()).withStyle(ChatFormatting.GRAY));
		}
		
		private Holder<Enchantment> getEnchantmnetById(ClientLevel level, Identifier id) {
			try { return level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(ResourceKey.create(Registries.ENCHANTMENT, id)); }
			catch(Exception e) { return null; }
		}
	}
	
	public static class PotionEntry implements ISuggestionRenderer {
		@Override
		public Component renderSuggestion(GuiGraphicsExtractor graphics, String value, int x, int y) {
			Identifier id = Identifier.tryParse(value);
			if(id == null) return null;
			MobEffect potion = BuiltInRegistries.MOB_EFFECT.getValue(id);
			if(potion == null) return null;
			ItemStack item = new ItemStack(Items.POTION);
			item.set(DataComponents.POTION_CONTENTS, new PotionContents(Optional.empty(), Optional.of(potion.getColor()), List.of(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(potion))), Optional.empty()));
			graphics.fakeItem(item, x, y);
			return potion.getDisplayName().copy().withStyle(ChatFormatting.YELLOW).append("\n").append(Component.literal(id.toString()).withStyle(ChatFormatting.GRAY));
		}
	}
	
	public static class ColorEntry implements ISuggestionRenderer {
		@Override
		public Component renderSuggestion(GuiGraphicsExtractor graphics, String value, int x, int y) {
			try {
				graphics.fill(x+1, y+-1, x+18, y+17, 0xFFA0A0A0);
				graphics.fill(x+2, y, x+17, y+16, Long.decode(value).intValue() | 0xFF000000);
			}
			catch(Exception e) {
			}
			return null;
		}
	}
}