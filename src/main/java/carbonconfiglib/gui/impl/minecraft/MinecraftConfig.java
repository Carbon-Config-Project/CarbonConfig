package carbonconfiglib.gui.impl.minecraft;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Predicate;

import com.mojang.serialization.Dynamic;

import carbonconfiglib.CarbonConfig;
import carbonconfiglib.api.ConfigType;
import carbonconfiglib.gui.api.IConfigNode;
import carbonconfiglib.gui.api.IModConfig;
import carbonconfiglib.impl.PerWorldProxy.WorldTarget;
import carbonconfiglib.networking.minecraft.RequestGameRulesPacket;
import carbonconfiglib.networking.minecraft.SaveGameRulesPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTDynamicOps;
import net.minecraft.network.PacketBuffer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.GameRules;
import net.minecraft.world.GameRules.BooleanValue;
import net.minecraft.world.GameRules.Category;
import net.minecraft.world.GameRules.IRuleEntryVisitor;
import net.minecraft.world.GameRules.IntegerValue;
import net.minecraft.world.GameRules.RuleKey;
import net.minecraft.world.GameRules.RuleType;
import net.minecraft.world.storage.FolderName;
import net.minecraft.world.storage.SaveFormat;
import net.minecraft.world.storage.SaveFormat.LevelSave;
import net.minecraft.world.storage.WorldSummary;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import speiger.src.collections.objects.lists.ObjectArrayList;
import speiger.src.collections.objects.maps.impl.hash.Object2ObjectLinkedOpenHashMap;

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
public class MinecraftConfig implements IModConfig
{
	public static final GameRules DEFAULTS = new GameRules();
	protected GameRules current;
	List<IGameRuleValue> values = new ObjectArrayList<>();
	Map<Category, List<IGameRuleValue>> keys = new Object2ObjectLinkedOpenHashMap<>();
	
	public MinecraftConfig() {
		MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
		if(server == null) return;
		setRules(server.getGameRules());
	}
	
	protected MinecraftConfig(GameRules current) {
		setRules(current);
	}
	
	protected void setRules(GameRules current) {
		this.current = current;
		collect();		
	}
	
	private void collect() {
		GameRules.visitGameRuleTypes(new IRuleEntryVisitor() {
			@Override
			public void visitBoolean(RuleKey<BooleanValue> key, RuleType<BooleanValue> type) {
				add(key, IGameRuleValue.bool(key, current.getRule(key)));
			}
			
			@Override
			public void visitInteger(RuleKey<IntegerValue> key, RuleType<IntegerValue> type) {
				add(key, IGameRuleValue.ints(key, current.getRule(key)));
			}
		});
	}
	
	private void add(RuleKey<?> key, IGameRuleValue value) {
		keys.computeIfAbsent(key.getCategory(), T -> new ObjectArrayList<>()).add(value);
		values.add(value);
	}
	
	@Override
	public String getFileName() {
		return "level.dat";
	}
	
	@Override
	public String getConfigName() {
		return "Game Rules";
	}
	
	@Override
	public String getModId() {
		return "minecraft";
	}
	
	@Override
	public boolean isDynamicConfig() {
		return true;
	}
	
	@Override
	public boolean isLocalConfig() {
		return ServerLifecycleHooks.getCurrentServer() != null;
	}
	
	@Override
	public ConfigType getConfigType() {
		return ConfigType.SERVER;
	}
	
	@Override
	public IConfigNode getRootNode() {
		return new MinecraftRoot(keys);
	}
	
	@Override
	public boolean isDefault() {
		for(IGameRuleValue value : values) {
			if(!Objects.equals(value.get(), value.getDefault())) return false;
		}
		return true;
	}
	
	@Override
	public void restoreDefault() {
		for(IGameRuleValue value : values) {
			value.set(value.getDefault());
		}
	}
	
	@Override
	public List<IConfigTarget> getPotentialFiles() {
		SaveFormat storage = Minecraft.getInstance().getLevelSource();
		List<IConfigTarget> folders = new ObjectArrayList<>();
		try {
			for(WorldSummary sum : storage.getLevelList()) {
				try(LevelSave access = Minecraft.getInstance().getLevelSource().createAccess(sum.getLevelId())) {
					Path path = access.getLevelPath(FolderName.LEVEL_DATA_FILE);
					if(Files.notExists(path)) continue;
					folders.add(new WorldConfigTarget(new WorldTarget(sum, access.getLevelPath(FolderName.ROOT), path), path));
				}
				catch(Exception e) { e.printStackTrace(); }
			}
		}
		catch(Exception e) { e.printStackTrace(); }
		return folders;
	}
	
	@Override
	public IModConfig loadFromFile(Path path) {
		if(Files.notExists(path)) return null;
		try { return new FileConfig(path, CompressedStreamTools.readCompressed(path.toFile())); }
		catch(Exception e) { e.printStackTrace(); }
		return null;
	}
	
	@Override
	public IModConfig loadFromNetworking(UUID requestId, Consumer<Predicate<PacketBuffer>> network) {
		NetworkConfig config = new NetworkConfig();
		network.accept(config);
		CarbonConfig.NETWORK.sendToServer(new RequestGameRulesPacket(requestId));
		return config;
	}
	
	@Override
	public void save() {
		if(current == null) return;
		current.assignFrom(current.copy(), ServerLifecycleHooks.getCurrentServer());
	}
	
	public static class FileConfig extends MinecraftConfig {
		Path file;
		CompoundNBT tag;
		
		public FileConfig(Path file, CompoundNBT tag) { 
			super(new GameRules(new Dynamic<>(NBTDynamicOps.INSTANCE, tag.getCompound("Data").getCompound("GameRules"))));
			this.file = file;
			this.tag = tag;
		}
		
		@Override
		public void save() {
			tag.getCompound("Data").put("GameRules", current.createTag());
			try {
				CompressedStreamTools.writeCompressed(tag, file.toFile());
			}
			catch(Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public static class NetworkConfig extends MinecraftConfig implements Predicate<PacketBuffer> {
		@Override
		public void save() {
			if(current == null) return;
			CarbonConfig.NETWORK.sendToServer(new SaveGameRulesPacket(current));
		}
		
		@Override
		public boolean test(PacketBuffer buffer) {
			setRules(new GameRules(new Dynamic<>(NBTDynamicOps.INSTANCE, buffer.readNbt())));
			return true;
		}
	}
}
