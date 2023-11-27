package carbonconfiglib.gui.widgets;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import carbonconfiglib.gui.api.ISuggestionRenderer;
import it.unimi.dsi.fastutil.objects.ObjectLists;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentData;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.inventory.container.PlayerContainer;
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
		public ITextComponent renderSuggestion(MatrixStack stack, String value, int x, int y) {
			ResourceLocation id = ResourceLocation.tryParse(value);
			if(id == null) return null;
			Item item = ForgeRegistries.ITEMS.getValue(id);
			if(item == Items.AIR || item == null) return null;
			ItemStack itemStack = new ItemStack(item);
			Minecraft.getInstance().getItemRenderer().renderAndDecorateItem(itemStack, x, y);
			return itemStack.getHoverName().copy().withStyle(TextFormatting.YELLOW).append("\n").append(new StringTextComponent(id.toString()).withStyle(TextFormatting.GRAY));			
		}
	}
	
	public static class FluidEntry implements ISuggestionRenderer {
		@Override
		@SuppressWarnings("deprecation")
		public ITextComponent renderSuggestion(MatrixStack stack, String value, int x, int y) {
			ResourceLocation id = ResourceLocation.tryParse(value);
			if(id == null) return null;
			Fluid fluid = ForgeRegistries.FLUIDS.getValue(id);
			if(fluid == Fluids.EMPTY || fluid == null) return null;
			TextureAtlasSprite sprite = getSprite(fluid);
			if(sprite == null) return null;
			Minecraft.getInstance().getTextureManager().bind(PlayerContainer.BLOCK_ATLAS);
			int color = fluid.getAttributes().getColor();
			RenderSystem.color4f((color >> 16 & 255) / 255F, (color >> 8 & 255) / 255F, (color & 255) / 255F, 1F);
			AbstractGui.blit(stack, x, y, 0, 18, 18, sprite);
			RenderSystem.color4f(1F, 1F, 1F, 1F);
			return fluid.getAttributes().getDisplayName(new FluidStack(fluid, 1)).copy().withStyle(TextFormatting.YELLOW).append("\n").append(new StringTextComponent(id.toString()).withStyle(TextFormatting.GRAY));
		}
		
		private TextureAtlasSprite getSprite(Fluid fluid) {
			return Minecraft.getInstance().getTextureAtlas(PlayerContainer.BLOCK_ATLAS).apply(fluid.getAttributes().getStillTexture());
		}
	}
	
	public static class EnchantmentEntry implements ISuggestionRenderer {
		@Override
		public ITextComponent renderSuggestion(MatrixStack stack, String value, int x, int y) {
			ResourceLocation id = ResourceLocation.tryParse(value);
			if(id == null) return null;
			Enchantment ench = ForgeRegistries.ENCHANTMENTS.getValue(id);
			if(ench == null) return null;
			Minecraft.getInstance().getItemRenderer().renderAndDecorateItem(EnchantedBookItem.createForEnchantment(new EnchantmentData(ench, ench.getMinLevel())), x, y);
			return ench.getFullname(ench.getMinLevel()).copy().withStyle(TextFormatting.YELLOW).append("\n").append(new StringTextComponent(id.toString()).withStyle(TextFormatting.GRAY));
		}
	}
	
	public static class PotionEntry implements ISuggestionRenderer {
		@Override
		public ITextComponent renderSuggestion(MatrixStack stack, String value, int x, int y) {
			ResourceLocation id = ResourceLocation.tryParse(value);
			if(id == null) return null;
			Effect potion = ForgeRegistries.POTIONS.getValue(id);
			if(potion == null) return null;
			ItemStack item = new ItemStack(Items.POTION);
			PotionUtils.setCustomEffects(item, ObjectLists.singleton(new EffectInstance(potion)));
			item.addTagElement("CustomPotionColor", IntNBT.valueOf(potion.getColor()));
			Minecraft.getInstance().getItemRenderer().renderAndDecorateItem(item, x, y);
			return potion.getDisplayName().copy().withStyle(TextFormatting.YELLOW).append("\n").append(new StringTextComponent(id.toString()).withStyle(TextFormatting.GRAY));
		}
	}
	
	public static class ColorEntry implements ISuggestionRenderer {
		@Override
		public ITextComponent renderSuggestion(MatrixStack stack, String value, int x, int y) {
			try {
				AbstractGui.fill(stack, x+1, y+1, x+18, y+19, 0xFFA0A0A0);
				AbstractGui.fill(stack, x+2, y+2, x+17, y+18, Long.decode(value).intValue() | 0xFF000000);
			}
			catch(Exception e) {
			}
			return null;
		}
	}
}
