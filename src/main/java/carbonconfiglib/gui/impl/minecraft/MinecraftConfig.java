package carbonconfiglib.gui.impl.minecraft;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Predicate;

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
import net.minecraft.network.PacketBuffer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.GameRules;
import net.minecraft.world.GameRules.BooleanValue;
import net.minecraft.world.GameRules.IRuleEntryVisitor;
import net.minecraft.world.GameRules.IntegerValue;
import net.minecraft.world.GameRules.RuleKey;
import net.minecraft.world.GameRules.RuleType;
import net.minecraft.world.GameRules.RuleValue;
import net.minecraft.world.storage.SaveFormat;
import net.minecraft.world.storage.WorldSummary;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import speiger.src.collections.objects.lists.ObjectArrayList;
import speiger.src.collections.objects.maps.impl.hash.Object2ObjectLinkedOpenHashMap;
import speiger.src.collections.objects.maps.impl.hash.Object2ObjectOpenHashMap;

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
	private static final Map<RuleKey<?>, Category> CATEOGIRES = createCategories();
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
		GameRules.func_223590_a(new IRuleEntryVisitor() {
			@Override
			@SuppressWarnings("unchecked")
			public <T extends RuleValue<T>> void func_223481_a(RuleKey<T> key, RuleType<T> type) {
				T value = current.get(key);
				if(value instanceof BooleanValue) {
					add(key, IGameRuleValue.bool((RuleKey<BooleanValue>)key, (BooleanValue)value));
				}
				else if(value instanceof IntegerValue) {
					add(key, IGameRuleValue.ints((RuleKey<IntegerValue>)key, (IntegerValue)value));
				}
			}
		});
	}
	
	private void add(RuleKey<?> key, IGameRuleValue value) {
		keys.computeIfAbsent(CATEOGIRES.getOrDefault(key, Category.MODDED), T -> new ObjectArrayList<>()).add(value);
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
		SaveFormat storage = Minecraft.getInstance().getSaveLoader();
		List<IConfigTarget> folders = new ObjectArrayList<>();
		try {
			for(WorldSummary sum : storage.getSaveList()) {
				try {
					Path path = storage.getFile(sum.getFileName(), "level.dat").toPath();
					if(Files.notExists(path)) continue;
					folders.add(new WorldConfigTarget(new WorldTarget(sum, storage.getFile(sum.getFileName(), ".").toPath(), path), path));
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
		try(InputStream stream = Files.newInputStream(path)) { return new FileConfig(path, CompressedStreamTools.readCompressed(stream)); }
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
		current.read(current.write());
	}
	
	public static Map<RuleKey<?>, Category> createCategories() {
		EnumMap<Category, RuleKey<?>[]> keys = new EnumMap<>(Category.class);
		keys.put(Category.CHAT, new RuleKey[]{ GameRules.ANNOUNCE_ADVANCEMENTS, GameRules.LOG_ADMIN_COMMANDS, GameRules.COMMAND_BLOCK_OUTPUT, GameRules.SEND_COMMAND_FEEDBACK, GameRules.SHOW_DEATH_MESSAGES });
		keys.put(Category.DROPS, new RuleKey[]{ GameRules.DO_TILE_DROPS, GameRules.DO_ENTITY_DROPS, GameRules.DO_MOB_LOOT });
		keys.put(Category.MISC, new RuleKey[]{ GameRules.MAX_COMMAND_CHAIN_LENGTH, GameRules.REDUCED_DEBUG_INFO });
		keys.put(Category.MOBS, new RuleKey[]{ GameRules.MOB_GRIEFING, GameRules.DISABLE_RAIDS, GameRules.MAX_ENTITY_CRAMMING });
		keys.put(Category.PLAYER, new RuleKey[]{ GameRules.SPECTATORS_GENERATE_CHUNKS, GameRules.DISABLE_ELYTRA_MOVEMENT_CHECK, GameRules.KEEP_INVENTORY, GameRules.NATURAL_REGENERATION, GameRules.SPAWN_RADIUS });
		keys.put(Category.SPAWNING, new RuleKey[]{ GameRules.DO_MOB_SPAWNING});
		keys.put(Category.WORLD, new RuleKey[]{ GameRules.DO_DAYLIGHT_CYCLE, GameRules.RANDOM_TICK_SPEED, GameRules.DO_FIRE_TICK, GameRules.DO_WEATHER_CYCLE });
		Map<RuleKey<?>, Category> result = new Object2ObjectOpenHashMap<>();
		for(Entry<Category, RuleKey<?>[]> entry : keys.entrySet()) {
			Category key = entry.getKey();
			for(RuleKey<?> value : entry.getValue()) {
				result.put(value, key);
			}
		}
		return result;
	}
	
	public static enum Category {
		CHAT("gamerule.category.chat"),
		DROPS("gamerule.category.drops"),
		MISC("gamerule.category.misc"),
		MOBS("gamerule.category.mobs"),
		PLAYER("gamerule.category.player"),
		SPAWNING("gamerule.category.spawning"),
		WORLD("gamerule.category.updates"),
		MODDED("gamerule.category.modded");
		
		String key;
		
		private Category(String key) {
			this.key = key;
		}
		
		public String getDescriptionId() {
			return key;
		}
	}
	
	public static class FileConfig extends MinecraftConfig {
		Path file;
		CompoundNBT tag;
		
		public FileConfig(Path file, CompoundNBT tag) { 
			super(parse(tag.getCompound("Data").getCompound("GameRules")));
			this.file = file;
			this.tag = tag;
		}
		
		private static GameRules parse(CompoundNBT tag) {
			GameRules rules = new GameRules();
			rules.read(tag);;
			return rules;
		}
		
		@Override
		public void save() {
			tag.getCompound("Data").put("GameRules", current.write());
			try(OutputStream stream = Files.newOutputStream(file)) {
				CompressedStreamTools.writeCompressed(tag, stream);
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
			setRules(FileConfig.parse(buffer.readCompoundTag()));
			return true;
		}
	}
}
