package carbonconfiglib.gui.widgets;

import com.mojang.blaze3d.platform.GlStateManager;

import carbonconfiglib.gui.api.ISuggestionRenderer;
import it.unimi.dsi.fastutil.objects.ObjectLists;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentData;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.EnchantedBookItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.IntNBT;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.PotionUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;

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
		public ITextComponent renderSuggestion(String value, int x, int y) {
			ResourceLocation id = ResourceLocation.tryCreate(value);
			if(id == null) return null;
			Item item = ForgeRegistries.ITEMS.getValue(id);
			if(item == Items.AIR || item == null) return null;
			ItemStack itemStack = new ItemStack(item);
			Minecraft.getInstance().getItemRenderer().renderItemAndEffectIntoGUI(itemStack, x, y);
			return itemStack.getDisplayName().deepCopy().applyTextStyle(TextFormatting.YELLOW).appendText("\n").appendSibling(new StringTextComponent(id.toString()).applyTextStyle(TextFormatting.GRAY));			
		}
	}
	
	public static class FluidEntry implements ISuggestionRenderer {
		@Override
		public ITextComponent renderSuggestion(String value, int x, int y) {
			ResourceLocation id = ResourceLocation.tryCreate(value);
			if(id == null) return null;
			Fluid fluid = ForgeRegistries.FLUIDS.getValue(id);
			if(fluid == Fluids.EMPTY || fluid == null) return null;
			TextureAtlasSprite sprite = getSprite(fluid);
			if(sprite == null) return null;
			Minecraft.getInstance().getTextureManager().bindTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE);
			int color = fluid.getAttributes().getColor();
			GlStateManager.color4f((color >> 16 & 255) / 255F, (color >> 8 & 255) / 255F, (color & 255) / 255F, 1F);
			AbstractGui.blit(x, y, 0, 18, 18, sprite);
			GlStateManager.color4f(1F, 1F, 1F, 1F);
			return fluid.getAttributes().getDisplayName(new FluidStack(fluid, 1)).deepCopy().applyTextStyle(TextFormatting.YELLOW).appendText("\n").appendSibling(new StringTextComponent(id.toString()).applyTextStyle(TextFormatting.GRAY));
		}
		
		private TextureAtlasSprite getSprite(Fluid fluid) {
			return Minecraft.getInstance().getTextureMap().getSprite(fluid.getAttributes().getStillTexture());
		}
	}
	
	public static class EnchantmentEntry implements ISuggestionRenderer {
		@Override
		public ITextComponent renderSuggestion(String value, int x, int y) {
			ResourceLocation id = ResourceLocation.tryCreate(value);
			if(id == null) return null;
			Enchantment ench = ForgeRegistries.ENCHANTMENTS.getValue(id);
			if(ench == null) return null;
			Minecraft.getInstance().getItemRenderer().renderItemAndEffectIntoGUI(EnchantedBookItem.getEnchantedItemStack(new EnchantmentData(ench, ench.getMinLevel())), x, y);
			return ench.getDisplayName(ench.getMinLevel()).deepCopy().applyTextStyle(TextFormatting.YELLOW).appendText("\n").appendSibling(new StringTextComponent(id.toString()).applyTextStyle(TextFormatting.GRAY));
		}
	}
	
	public static class PotionEntry implements ISuggestionRenderer {
		@Override
		public ITextComponent renderSuggestion(String value, int x, int y) {
			ResourceLocation id = ResourceLocation.tryCreate(value);
			if(id == null) return null;
			Effect potion = ForgeRegistries.POTIONS.getValue(id);
			if(potion == null) return null;
			ItemStack item = new ItemStack(Items.POTION);
			PotionUtils.appendEffects(item, ObjectLists.singleton(new EffectInstance(potion)));
			item.getOrCreateTag().put("CustomPotionColor", new IntNBT(potion.getLiquidColor()));
			Minecraft.getInstance().getItemRenderer().renderItemAndEffectIntoGUI(item, x, y);
			return potion.getDisplayName().deepCopy().applyTextStyle(TextFormatting.YELLOW).appendText("\n").appendSibling(new StringTextComponent(id.toString()).applyTextStyle(TextFormatting.GRAY));
		}
	}
	
	public static class ColorEntry implements ISuggestionRenderer {
		@Override
		public ITextComponent renderSuggestion(String value, int x, int y) {
			try {
				AbstractGui.fill(x+1, y+1, x+18, y+19, 0xFFA0A0A0);
				AbstractGui.fill(x+2, y+2, x+17, y+18, Long.decode(value).intValue() | 0xFF000000);
			}
			catch(Exception e) {
			}
			return null;
		}
	}
}
