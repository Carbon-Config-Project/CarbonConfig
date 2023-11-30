package carbonconfiglib.gui.widgets;

import carbonconfiglib.gui.api.ISuggestionRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentData;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

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
		public IChatComponent renderSuggestion(String value, int x, int y) {
			Item item = Item.itemRegistry.getObject(new ResourceLocation(value));
			if(item == null) return null;
			ItemStack itemStack = new ItemStack(item);
			Minecraft.getMinecraft().getRenderItem().renderItemAndEffectIntoGUI(itemStack, x, y);
			return new ChatComponentText(itemStack.getDisplayName()).setChatStyle(new ChatStyle().setColor(EnumChatFormatting.YELLOW)).appendText("\n").appendSibling(new ChatComponentText(value).setChatStyle(new ChatStyle().setColor(EnumChatFormatting.GRAY)));			
		}
	}
	
	public static class FluidEntry implements ISuggestionRenderer {
		@Override
		public IChatComponent renderSuggestion(String value, int x, int y) {
			Fluid fluid = FluidRegistry.getFluid(value);
			if(fluid == null) return null;
			TextureAtlasSprite sprite = getSprite(fluid);
			if(sprite == null) return null;
			Minecraft.getMinecraft().getTextureManager().bindTexture(TextureMap.locationBlocksTexture);
			int color = fluid.getColor();
			GlStateManager.color((color >> 16 & 255) / 255F, (color >> 8 & 255) / 255F, (color & 255) / 255F, 1F);
			GUI.drawTexturedModalRect(0, 0, sprite, 18, 18);
			GlStateManager.color(1F, 1F, 1F, 1F);
			return new ChatComponentText(fluid.getLocalizedName(new FluidStack(fluid, 1))).setChatStyle(new ChatStyle().setColor(EnumChatFormatting.YELLOW)).appendText("\n").appendSibling(new ChatComponentText(value).setChatStyle(new ChatStyle().setColor(EnumChatFormatting.GRAY)));
		}
		
		private TextureAtlasSprite getSprite(Fluid fluid) {
			return Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(fluid.getStill().toString());
		}
	}
	
	public static class EnchantmentEntry implements ISuggestionRenderer {
		@Override
		public IChatComponent renderSuggestion(String value, int x, int y) {
			Enchantment ench = Enchantment.getEnchantmentByLocation(value);
			if(ench == null) return null;
			Minecraft.getMinecraft().getRenderItem().renderItemAndEffectIntoGUI(Items.enchanted_book.getEnchantedItemStack(new EnchantmentData(ench, ench.getMinLevel())), x, y);
			return new ChatComponentText(ench.getTranslatedName(ench.getMinLevel())).setChatStyle(new ChatStyle().setColor(EnumChatFormatting.YELLOW)).appendText("\n").appendSibling(new ChatComponentText(value).setChatStyle(new ChatStyle().setColor(EnumChatFormatting.GRAY)));
		}
	}
	
	public static class ColorEntry implements ISuggestionRenderer {
		@Override
		public IChatComponent renderSuggestion(String value, int x, int y) {
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
