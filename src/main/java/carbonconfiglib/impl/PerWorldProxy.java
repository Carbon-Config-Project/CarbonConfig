package carbonconfiglib.impl;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

import carbonconfiglib.CarbonConfig;
import carbonconfiglib.api.ConfigType;
import carbonconfiglib.api.IConfigProxy;
import carbonconfiglib.api.SimpleConfigProxy.SimpleTarget;
import carbonconfiglib.config.ConfigSettings;
import carbonconfiglib.impl.internal.EventHandler;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.level.storage.LevelStorageException;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.LevelSummary;

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
public final class PerWorldProxy implements IConfigProxy
{
	public static final IConfigProxy INSTANCE = new PerWorldProxy(getGameDir("multiplayerconfigs"), getGameDir("defaultconfigs"), getGameDir("saves"));
	Path baseClientPath;
	Path baseServerPath;
	Path saveFolders;
	
	private PerWorldProxy(Path baseClientPath, Path baseServerPath, Path saveFolders) {
		this.baseClientPath = baseClientPath;
		this.baseServerPath = baseServerPath;
		this.saveFolders = saveFolders;
	}
	
	private static Path getGameDir(String path) {
		return FabricLoader.getInstance().getGameDir().resolve(path);
	}
	
	public static boolean isProxy(IConfigProxy proxy) {
		return proxy instanceof PerWorldProxy;
	}
	
	public static ConfigSettings perWorld() {
		return ConfigSettings.withFolderProxy(INSTANCE).withType(ConfigType.SERVER);
	}
	
	@Override
	public List<Path> getBasePaths() {
		List<Path> paths = new ObjectArrayList<>();
		MinecraftServer server = EventHandler.getServer();
		if(server != null) paths.add(server.getWorldPath(LevelResource.ROOT).resolve("serverconfig"));
		else if(FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) paths.add(baseClientPath);
		paths.add(baseServerPath);
		return paths;
	}
	
	@Override
	public List<? extends IPotentialTarget> getPotentialConfigs() {
		if(FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) return getLevels();
		else return Collections.singletonList(new SimpleTarget(EventHandler.getServer().getWorldPath(LevelResource.ROOT).resolve("serverconfig"), "server"));
	}
	
	@Environment(EnvType.CLIENT)
	private List<IPotentialTarget> getLevels() {
		LevelStorageSource storage = Minecraft.getInstance().getLevelSource();
		List<IPotentialTarget> folders = new ObjectArrayList<>();
		if(Files.exists(baseServerPath)) {
			folders.add(new SimpleTarget(baseServerPath, "Default Config"));
		}
		try {
			for(LevelSummary sum : storage.getLevelList()) {
				try(LevelStorageSource.LevelStorageAccess access = Minecraft.getInstance().getLevelSource().createAccess(sum.getLevelId()))
				{
					Path path = access.getLevelPath(LevelResource.ROOT).resolve("serverconfig");
					if(Files.exists(path)) folders.add(new WorldTarget(sum, access.getLevelPath(LevelResource.ROOT), path));
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
			}
		} catch (LevelStorageException e) {
			CarbonConfig.LOGGER.error("Level loading failed", e);
		}
		return folders;
	}
	
	@Override
	public boolean isDynamicProxy() {
		return true;
	}
	
	public static class WorldTarget implements IPotentialTarget {
		LevelSummary summary;
		Path worldFile;
		Path folder;
		
		public WorldTarget(LevelSummary summary, Path worldFile, Path folder) {
			this.summary = summary;
			this.worldFile = worldFile;
			this.folder = folder;
		}

		@Override
		public Path getFolder() {
			return folder;
		}

		@Override
		public String getName() {
			return summary.getLevelName();
		}
		
		public Path getWorldFile() {
			return worldFile;
		}
		
		public LevelSummary getSummary() {
			return summary;
		}
	}
}
