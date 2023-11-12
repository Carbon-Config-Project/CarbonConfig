package carbonconfiglib.gui.api;

import java.nio.file.Path;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Predicate;

import carbonconfiglib.api.ConfigType;
import carbonconfiglib.api.IConfigProxy.IPotentialTarget;
import carbonconfiglib.config.ConfigHandler;
import carbonconfiglib.gui.impl.carbon.ModConfig;
import carbonconfiglib.gui.impl.minecraft.MinecraftConfig;
import carbonconfiglib.impl.PerWorldProxy.WorldTarget;
import net.minecraft.network.FriendlyByteBuf;
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
public interface IModConfig
{
	public String getFileName();
	public String getConfigName();
	public String getModId();
	public boolean isDynamicConfig();
	public boolean isLocalConfig();
	public ConfigType getConfigType();
	public IConfigNode getRootNode();
	public boolean isDefault();
	public void restoreDefault();
	public List<IConfigTarget> getPotentialFiles();
	public IModConfig loadFromFile(Path path);
	public IModConfig loadFromNetworking(UUID requestId, Consumer<Predicate<FriendlyByteBuf>> network);
	public void save();
	
	public static IModConfig carbon(String modId, ConfigHandler handler) {
		return new ModConfig(modId, handler);
	}
	
	public static IModConfig minecraft() {
		return new MinecraftConfig();
	}
	
	public static interface IConfigTarget extends IPotentialTarget {
		public Path getConfigFile();
	}
	
	public static class SimpleConfigTarget implements IConfigTarget {
		Path folder;
		Path file;
		String name;
		
		public SimpleConfigTarget(IPotentialTarget target, Path file) {
			this(target.getFolder(), file, target.getName());
		}
		
		public SimpleConfigTarget(Path folder, Path file, String name) {
			this.folder = folder;
			this.file = file;
			this.name = name;
		}

		@Override
		public Path getFolder() {
			return folder;
		}

		@Override
		public String getName() {
			return name;
		}

		@Override
		public Path getConfigFile() {
			return file;
		}
	}
	
	public static class WorldConfigTarget implements IConfigTarget {
		LevelSummary summary;
		Path folder;
		Path file;
		String name;
		
		public WorldConfigTarget(WorldTarget target, Path file) {
			this(target.getSummary(), target.getFolder(), file, target.getName());
		}
		
		public WorldConfigTarget(LevelSummary summary, Path folder, Path file, String name) {
			this.summary = summary;
			this.folder = folder;
			this.file = file;
			this.name = name;
		}

		@Override
		public Path getFolder() {
			return folder;
		}

		@Override
		public String getName() {
			return name;
		}

		@Override
		public Path getConfigFile() {
			return file;
		}
		
		public LevelSummary getSummary() {
			return summary;
		}
		
	}
}
