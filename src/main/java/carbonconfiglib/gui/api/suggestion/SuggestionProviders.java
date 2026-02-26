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
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.forgespi.language.IModInfo;
import net.minecraftforge.server.ServerLifecycleHooks;

public class SuggestionProviders
{
	public static class ModProvider implements ISuggestionProvider {
		public static final ISuggestionProvider INSTANCE = new ModProvider();
		
		@Override
		public void provideSuggestions(Consumer<Suggestion> output, Predicate<Suggestion> filter) {
			for(IModInfo info : ModList.get().getMods()) {
				Suggestion suggestion = Suggestion.namedValue(info.getDisplayName(), info.getModId());
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
			MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
			if(server != null) {
				for(Player player : server.getPlayerList().getPlayers()) {
					profile.accept(player.getGameProfile());
				}
			}
			try {
				JsonArray array = JsonParser.parseReader(Files.newBufferedReader(FMLPaths.GAMEDIR.get().resolve("usercache.json"))).getAsJsonArray();
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
