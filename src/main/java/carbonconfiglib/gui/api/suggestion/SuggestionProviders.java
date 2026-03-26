package carbonconfiglib.gui.api.suggestion;

import java.nio.file.Files;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Predicate;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.authlib.GameProfile;
import com.mojang.util.UUIDTypeAdapter;

import carbonconfiglib.api.ISuggestionProvider;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;

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
			for(ModContainer container : Loader.instance().getActiveModList()) {
				Suggestion suggestion = Suggestion.namedValue(container.getName(), container.getModId());
				if(filter.test(suggestion)) output.accept(suggestion);
			}
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
			MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
			if(server != null) {
				for(EntityPlayerMP player : server.getPlayerList().getPlayerList()) {
					profile.accept(player.getGameProfile());
				}
			}
			try {
				JsonArray array = new JsonParser().parse(Files.newBufferedReader(Loader.instance().getConfigDir().getParentFile().toPath().resolve("usercache.json"))).getAsJsonArray();
				for(int i = 0,m=array.size();i<m;i++) {
					try { 
						JsonObject obj = array.get(i).getAsJsonObject();
						String name = obj.get("name").getAsString();
						UUID id = UUIDTypeAdapter.fromString(obj.get("uuid").getAsString());
						if(id != null && name != null) profile.accept(new GameProfile(id, name));
					}
					catch(Exception e) {}
				}
			}
			catch(Exception e) {}
		}
	}
}
