package carbonconfiglib.gui.impl.forge;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Predicate;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.file.FileNotFoundAction;
import com.electronwill.nightconfig.toml.TomlFormat;

import carbonconfiglib.CarbonConfig;
import carbonconfiglib.api.ConfigType;
import carbonconfiglib.gui.api.IConfigNode;
import carbonconfiglib.gui.api.IModConfig;
import carbonconfiglib.impl.PerWorldProxy;
import carbonconfiglib.impl.PerWorldProxy.WorldTarget;
import carbonconfiglib.networking.forge.RequestConfigPacket;
import carbonconfiglib.networking.forge.SaveForgeConfigPacket;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectLists;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.LevelSummary;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.config.ModConfig.Type;
import net.minecraftforge.fml.event.config.ModConfigEvent;

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
public class ForgeConfig implements IModConfig
{
	ModConfig config;
	ForgeConfigSpec spec;
	CommentedConfig data;
	List<ConfigValue<?>> entries;
	Path path;
	
	public ForgeConfig(ModConfig config) {
		this.config = config;
		spec = (ForgeConfigSpec)config.getSpec();
		data = config.getConfigData();
		entries = collect();
	}
	
	public ForgeConfig(ModConfig config, CommentedConfig data, Path path) {
		this.config = config;
		this.data = data;
		this.path = path;
		spec = (ForgeConfigSpec)config.getSpec();
		entries = collect();
	}
	
	@Override
	public String getFileName() {
		return config.getFileName();
	}
	
	@Override
	public String getConfigName() {
		return config.getFileName();
	}
	
	@Override
	public String getModId() {
		return config.getModId();
	}
	
	@Override
	public boolean isDynamicConfig() {
		return config.getType() == Type.SERVER;
	}
	
	@Override
	public boolean isLocalConfig() {
		return path == null;
	}
	
	@Override
	public ConfigType getConfigType() {
		switch(config.getType()) {
			case CLIENT: return ConfigType.CLIENT;
			case COMMON: return ConfigType.SHARED;
			case SERVER: return ConfigType.SERVER;
			default: throw new IllegalArgumentException("Type: "+config.getType()+", not supported");
		}
	}
	
	@Override
	public IConfigNode getRootNode() {
		return new ForgeNode(new ObjectArrayList<>(), data, spec);
	}
	
	@Override
	public boolean isDefault() {
		if(data == null) return true;
		for(int i = 0,m=entries.size();i<m;i++) {
			ConfigValue<?> entry = entries.get(i);
			if(!Objects.equals(entry.getDefault(), data.get(entry.getPath()))) return false;
		}
		return true;
	}
	
	@Override
	public void restoreDefault() {
		if(data == null) return;
		for(int i = 0,m=entries.size();i<m;i++) {
			ConfigValue<?> entry = entries.get(i);
			this.data.set(entry.getPath(), entry.getDefault());
		}
	}
	
	@Override
	public List<IConfigTarget> getPotentialFiles() {
		if(getConfigType() == ConfigType.SERVER) {
			return getLevels();
		}
		return ObjectLists.emptyList();
	}
	
	@Override
	public IModConfig loadFromFile(Path path) {
		try {
			return new ForgeConfig(config, TomlFormat.instance().createParser().parse(path, FileNotFoundAction.THROW_ERROR), path);
		}
		catch(Exception e) { e.printStackTrace(); }
		return null;
	}
	
	@Override
	public IModConfig loadFromNetworking(UUID requestId, Consumer<Predicate<FriendlyByteBuf>> network) {
		NetworkForgeConfig config = new NetworkForgeConfig(this.config);
		CarbonConfig.NETWORK.sendToServer(new RequestConfigPacket(this.config.getType(), requestId, this.config.getModId()));
		network.accept(config);
		return config;
	}
	
	@Override
	public void save() {
		if(path != null) {
			ForgeHelpers.saveConfig(path, data);
			return;
		}
		config.save();
        config.getSpec().afterReload();
        ModList.get().getModContainerById(config.getModId()).get().dispatchConfigEvent(new ModConfigEvent.Reloading(this.config));
	}
	
	private List<ConfigValue<?>> collect() {
		List<ConfigValue<?>> values = new ObjectArrayList<>();
		iterate(spec.getValues().valueMap().values(), values::add);
		return values;
	}
	
	private void iterate(Iterable<Object> source, Consumer<ConfigValue<?>> result) {
		for(Object entry : source) {
			if(entry instanceof ConfigValue) {
				result.accept((ConfigValue<?>)entry);
			}
			else if(entry instanceof Config) {
				iterate(((Config)entry).valueMap().values(), result);
			}
		}
	}
	
	private List<IConfigTarget> getLevels() {
		LevelStorageSource storage = Minecraft.getInstance().getLevelSource();
		List<IConfigTarget> folders = new ObjectArrayList<>();
		for(LevelSummary sum : storage.loadLevelSummaries(storage.findLevelCandidates()).join()) {
			try(LevelStorageSource.LevelStorageAccess access = Minecraft.getInstance().getLevelSource().createAccess(sum.getLevelId())) {
				Path path = access.getLevelPath(PerWorldProxy.SERVERCONFIG);
				if(Files.notExists(path)) continue;
				Path file = path.resolve(config.getFileName());
				if(Files.notExists(file)) continue;
				folders.add(new WorldConfigTarget(new WorldTarget(sum, access.getLevelPath(LevelResource.ROOT), path), file));
			}
			catch(Exception e) { e.printStackTrace(); }
		}
		return folders;
	}
	
	public static class NetworkForgeConfig extends ForgeConfig implements Predicate<FriendlyByteBuf> {

		public NetworkForgeConfig(ModConfig config) {
			super(config, null, null);
		}

		@Override
		public boolean test(FriendlyByteBuf t) {
			try {
				this.data = TomlFormat.instance().createParser().parse(new ByteArrayInputStream(t.readByteArray()));
				return true;
			}
			catch(Exception e) {
				e.printStackTrace();
				return false;
			}
		}
		
		@Override
		public void save() {
			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			data.configFormat().createWriter().write(data, stream);
			CarbonConfig.NETWORK.sendToServer(new SaveForgeConfigPacket(config.getType(), config.getModId(), stream.toByteArray()));
		}
		
		@Override
		public boolean isLocalConfig() { return false; }
	}
}
