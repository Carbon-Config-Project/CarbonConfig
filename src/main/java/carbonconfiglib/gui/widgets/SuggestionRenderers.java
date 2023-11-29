package carbonconfiglib.gui.widgets;

import carbonconfiglib.gui.api.ISuggestionRenderer;
import it.unimi.dsi.fastutil.objects.ObjectLists;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentData;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemEnchantedBook;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.potion.PotionUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

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
	private static final Gui GUI = new Gui();
	
	public static class ItemEntry implements ISuggestionRenderer {
		@Override
		public ITextComponent renderSuggestion(String value, int x, int y) {
			Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(value));
			if(item == Items.AIR || item == null) return null;
			ItemStack itemStack = new ItemStack(item);
			Minecraft.getMinecraft().getRenderItem().renderItemAndEffectIntoGUI(itemStack, x, y);
			return new TextComponentString(itemStack.getDisplayName()).setStyle(new Style().setColor(TextFormatting.YELLOW)).appendText("\n").appendSibling(new TextComponentString(value).setStyle(new Style().setColor(TextFormatting.GRAY)));			
		}
	}
	
	public static class FluidEntry implements ISuggestionRenderer {
		@Override
		public ITextComponent renderSuggestion(String value, int x, int y) {
			Fluid fluid = FluidRegistry.getFluid(value);
			if(fluid == null) return null;
			TextureAtlasSprite sprite = getSprite(fluid);
			if(sprite == null) return null;
			Minecraft.getMinecraft().getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
			int color = fluid.getColor();
			GlStateManager.color((color >> 16 & 255) / 255F, (color >> 8 & 255) / 255F, (color & 255) / 255F, 1F);
			GUI.drawTexturedModalRect(0, 0, sprite, 18, 18);
			GlStateManager.color(1F, 1F, 1F, 1F);
			return new TextComponentString(fluid.getLocalizedName(new FluidStack(fluid, 1))).setStyle(new Style().setColor(TextFormatting.YELLOW)).appendText("\n").appendSibling(new TextComponentString(value).setStyle(new Style().setColor(TextFormatting.GRAY)));
		}
		
		private TextureAtlasSprite getSprite(Fluid fluid) {
			return Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(fluid.getStill().toString());
		}
	}
	
	public static class EnchantmentEntry implements ISuggestionRenderer {
		@Override
		public ITextComponent renderSuggestion(String value, int x, int y) {
			Enchantment ench = ForgeRegistries.ENCHANTMENTS.getValue(new ResourceLocation(value));
			if(ench == null) return null;
			Minecraft.getMinecraft().getRenderItem().renderItemAndEffectIntoGUI(ItemEnchantedBook.getEnchantedItemStack(new EnchantmentData(ench, ench.getMinLevel())), x, y);
			return new TextComponentString(ench.getTranslatedName(ench.getMinLevel())).setStyle(new Style().setColor(TextFormatting.YELLOW)).appendText("\n").appendSibling(new TextComponentString(value).setStyle(new Style().setColor(TextFormatting.GRAY)));
		}
	}
	
	public static class PotionEntry implements ISuggestionRenderer {
		@Override
		public ITextComponent renderSuggestion(String value, int x, int y) {
			Potion potion = ForgeRegistries.POTIONS.getValue(new ResourceLocation(value));
			if(potion == null) return null;
			ItemStack item = new ItemStack(Items.POTIONITEM);
			PotionUtils.appendEffects(item, ObjectLists.singleton(new PotionEffect(potion)));
			NBTTagCompound nbt = item.getTagCompound();
			if(nbt == null) {
				nbt = new NBTTagCompound();
				item.setTagCompound(nbt);
			}
			nbt.setInteger("CustomPotionColor", potion.getLiquidColor());
			Minecraft.getMinecraft().getRenderItem().renderItemAndEffectIntoGUI(item, x, y);
			return new TextComponentString(potion.getName()).setStyle(new Style().setColor(TextFormatting.YELLOW)).appendText("\n").appendSibling(new TextComponentString(value).setStyle(new Style().setColor(TextFormatting.GRAY)));
		}
	}
	
	public static class ColorEntry implements ISuggestionRenderer {
		@Override
		public ITextComponent renderSuggestion(String value, int x, int y) {
			try {
				Gui.drawRect(x+1, y+1, x+18, y+19, 0xFFA0A0A0);
				Gui.drawRect(x+2, y+2, x+17, y+18, Long.decode(value).intValue() | 0xFF000000);
			}
			catch(Exception e) {
			}
			return null;
		}
	}
}
