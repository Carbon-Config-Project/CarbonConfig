package carbonconfiglib.gui.api.suggestion;

import java.nio.file.Files;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Predicate;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.authlib.GameProfile;
import com.mojang.util.UndashedUuid;

import carbonconfiglib.api.ISuggestionProvider;
import carbonconfiglib.impl.internal.EventHandler;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import speiger.src.collections.objects.lists.ObjectArrayList;

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
public class SuggestionProviders
{
	public static class ModProvider implements ISuggestionProvider {
		public static final ISuggestionProvider INSTANCE = new ModProvider();
		
		@Override
		public void provideSuggestions(Consumer<Suggestion> output, Predicate<Suggestion> filter) {
			for(ModContainer container : FabricLoader.getInstance().getAllMods()) {
				ModMetadata metadata = container.getMetadata();
				Suggestion suggestion = Suggestion.namedValue(metadata.getName(), metadata.getId());
				if(filter.test(suggestion)) output.accept(suggestion);
			}
		}
	}
	
	public static class EnchantmentProvider implements ISuggestionProvider {
		public static final ISuggestionProvider INSTANCE = new EnchantmentProvider();
		
		@Override
		public void provideSuggestions(Consumer<Suggestion> output, Predicate<Suggestion> filter) {
			for(ResourceLocation key : getEnchantments()) {
				Suggestion suggestion = Suggestion.namedTypeValue(key.toString(), key.toString(), Enchantment.class);
				if(filter.test(suggestion)) output.accept(suggestion);
			}
		}
		
		private List<ResourceLocation> getEnchantments() {
			Level level = Minecraft.getInstance().level;
			if(level == null) return getDefaults();
			Registry<Enchantment> registry = level.registryAccess().registry(Registries.ENCHANTMENT).orElse(null);
			return registry == null ? null : new ObjectArrayList<>(registry.keySet());
		}
		
		private List<ResourceLocation> getDefaults() {
			List<ResourceLocation> enchantments = new ObjectArrayList<>();
			enchantments.add(Enchantments.PROTECTION.location());
			enchantments.add(Enchantments.FIRE_PROTECTION.location());
			enchantments.add(Enchantments.FEATHER_FALLING.location());
			enchantments.add(Enchantments.BLAST_PROTECTION.location());
			enchantments.add(Enchantments.PROJECTILE_PROTECTION.location());
			enchantments.add(Enchantments.RESPIRATION.location());
			enchantments.add(Enchantments.AQUA_AFFINITY.location());
			enchantments.add(Enchantments.THORNS.location());
			enchantments.add(Enchantments.DEPTH_STRIDER.location());
			enchantments.add(Enchantments.FROST_WALKER.location());
			enchantments.add(Enchantments.BINDING_CURSE.location());
			enchantments.add(Enchantments.SOUL_SPEED.location());
			enchantments.add(Enchantments.SWIFT_SNEAK.location());
			enchantments.add(Enchantments.SHARPNESS.location());
			enchantments.add(Enchantments.SMITE.location());
			enchantments.add(Enchantments.BANE_OF_ARTHROPODS.location());
			enchantments.add(Enchantments.KNOCKBACK.location());
			enchantments.add(Enchantments.FIRE_ASPECT.location());
			enchantments.add(Enchantments.LOOTING.location());
			enchantments.add(Enchantments.SWEEPING_EDGE.location());
			enchantments.add(Enchantments.EFFICIENCY.location());
			enchantments.add(Enchantments.SILK_TOUCH.location());
			enchantments.add(Enchantments.UNBREAKING.location());
			enchantments.add(Enchantments.FORTUNE.location());
			enchantments.add(Enchantments.POWER.location());
			enchantments.add(Enchantments.PUNCH.location());
			enchantments.add(Enchantments.FLAME.location());
			enchantments.add(Enchantments.INFINITY.location());
			enchantments.add(Enchantments.LUCK_OF_THE_SEA.location());
			enchantments.add(Enchantments.LURE.location());
			enchantments.add(Enchantments.LOYALTY.location());
			enchantments.add(Enchantments.IMPALING.location());
			enchantments.add(Enchantments.RIPTIDE.location());
			enchantments.add(Enchantments.CHANNELING.location());
			enchantments.add(Enchantments.MULTISHOT.location());
			enchantments.add(Enchantments.QUICK_CHARGE.location());
			enchantments.add(Enchantments.PIERCING.location());
			enchantments.add(Enchantments.DENSITY.location());
			enchantments.add(Enchantments.BREACH.location());
			enchantments.add(Enchantments.WIND_BURST.location());
			enchantments.add(Enchantments.MENDING.location());
			enchantments.add(Enchantments.VANISHING_CURSE.location());
			return enchantments;
		}
	}
	
	public static class PlayerProvider implements ISuggestionProvider {
		public static final ISuggestionProvider INSTANCE = new PlayerProvider();

		@Override
		public void provideSuggestions(Consumer<Suggestion> output, Predicate<Suggestion> filter) {
			collectProfiles(T -> {
				Suggestion suggestion = Suggestion.namedValue(T.getName(), T.getId().toString());
				if(filter.test(suggestion)) output.accept(suggestion);
			});
		}
		
		private void collectProfiles(Consumer<GameProfile> profile) {
			MinecraftServer server = EventHandler.getServer();
			if(server != null) {
				for(Player player : server.getPlayerList().getPlayers()) {
					profile.accept(player.getGameProfile());
				}
			}
			try {
				JsonArray array = JsonParser.parseReader(Files.newBufferedReader(FabricLoader.getInstance().getGameDir().resolve("usercache.json"))).getAsJsonArray();
				for(int i = 0,m=array.size();i<m;i++) {
					try { 
						JsonObject obj = array.get(i).getAsJsonObject();
						String name = obj.get("name").getAsString();
						UUID id = UndashedUuid.fromString(obj.get("uuid").getAsString());
						if(id != null && name != null) profile.accept(new GameProfile(id, name));
					}
					catch(Exception e) {}
				}
			}
			catch(Exception e) {}
		}
	}
}
