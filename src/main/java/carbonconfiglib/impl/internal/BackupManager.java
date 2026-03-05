package carbonconfiglib.impl.internal;

import java.nio.file.Path;

import carbonconfiglib.api.ConfigType;
import carbonconfiglib.gui.api.IModConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import net.minecraft.client.server.IntegratedServer;
import net.minecraftforge.fml.loading.FMLPaths;

public class BackupManager {
	public void createBackup(IModConfig config) {
		if(config.isLocalConfig()) {
			if(config.getConfigType() == ConfigType.SERVER) {
				//Deal with SinglePlayer Worlds
				return;
			}
			//Handle the local Stuff
			return;
		}
		//Deal with Multiplayer Server Configs
	}
	
	private Path appendWorld(Path path) {
		return path;
	}
	
	private Path getBasePath(String modId, ConfigType type, boolean multiplayer) {
		return FMLPaths.GAMEDIR.get()
				.resolve("backup")
				.resolve("carbonconfig")
				.resolve(multiplayer ? "multiplayer" : (type == ConfigType.SERVER ? "server" : "local"))
				.resolve(modId)
				.resolve(type(type));
	}
	
	private static String type(ConfigType type) {
		return type == ConfigType.CLIENT ? "client" : (type == ConfigType.SHARED ? "common" : "server");
	}
}
