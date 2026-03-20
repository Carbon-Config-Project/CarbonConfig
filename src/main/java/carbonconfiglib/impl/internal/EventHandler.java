package carbonconfiglib.impl.internal;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

import carbonconfiglib.CarbonConfig;
import carbonconfiglib.api.IConfigChangeListener;
import carbonconfiglib.config.ConfigHandler;
import carbonconfiglib.gui.api.IModConfigs;
import carbonconfiglib.gui.impl.carbon.ModConfigs;
import carbonconfiglib.gui.impl.minecraft.MinecraftConfigs;
import carbonconfiglib.gui.screens.ModConfigList;
import carbonconfiglib.impl.PerWorldProxy;
import carbonconfiglib.networking.snyc.BulkSyncPacket;
import carbonconfiglib.networking.snyc.SyncPacket;
import carbonconfiglib.plugins.ICarbonPlugin;
import carbonconfiglib.utils.SyncType;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.client.Minecraft;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;
import speiger.src.collections.objects.lists.ObjectArrayList;
import speiger.src.collections.objects.maps.impl.hash.Object2ObjectLinkedOpenHashMap;
import speiger.src.collections.objects.maps.interfaces.Object2ObjectMap;

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
public class EventHandler implements IConfigChangeListener
{
	public static final EventHandler INSTANCE = new EventHandler();
	public static final ThreadLocal<ModContainer> ACTIVE_MOD = new ThreadLocal<>();
	private static MinecraftServer ACTIVE_SERVER = null;
	boolean wasLoaded = false;
	Map<ModContainer, ModConfigs> configs = new Object2ObjectLinkedOpenHashMap<>();
	Map<String, IModConfigs> allKnownConfigs = new Object2ObjectLinkedOpenHashMap<>();

	public static MinecraftServer getServer() {
		return ACTIVE_SERVER;
	}
	
	public void initServerEvents(Runnable loadEvent) {
		ServerTickEvents.END_SERVER_TICK.register(T -> processEvents());
		ServerLifecycleEvents.SERVER_STARTING.register(T -> {
			if(triggerLoad()) loadEvent.run();
		});
		addSharedEvents();
	}
	
	public void initClientEvents(Runnable loadEvent, Runnable clientCallback) {
		ClientTickEvents.END_CLIENT_TICK.register(T -> processEvents());
		ClientLifecycleEvents.CLIENT_STARTED.register(T -> loadEvent.run());
		ClientPlayConnectionEvents.JOIN.register((C, K, V) -> onPlayerServerJoinEvent());
		ClientPlayConnectionEvents.DISCONNECT.register((K, V) -> onPlayerServerLeaveEvent());
		addSharedEvents();
		clientCallback.run();
	}
	
	private void addSharedEvents() {
		ServerLifecycleEvents.SERVER_STARTED.register(T -> ACTIVE_SERVER = T);
		ServerLifecycleEvents.SERVER_STOPPED.register(T -> ACTIVE_SERVER = null);
		ServerPlayConnectionEvents.JOIN.register((K, V, J) -> onPlayerServerJoinEvent(K.getPlayer()));
	}
	
	private boolean triggerLoad() {
		if(wasLoaded) return false;
		wasLoaded = true;
		return true;
	}
	
	@Override
	public void onConfigCreated(ConfigHandler config) {
		InternalFeatures.initMinecraftDataTypes(config);
		if(FabricLoader.getInstance().getEnvironmentType() == EnvType.SERVER) return;
		ModContainer active = ACTIVE_MOD.get();
		if(active == null || "minecraft".equals(active.getMetadata().getId())) {
			if(!FabricLoader.getInstance().isDevelopmentEnvironment()) return;
			throw new IllegalStateException("Mod Configs Must be created (not loaded) during a Mod Loading Phase");
		}
		configs.computeIfAbsent(active, ModConfigs::new).addConfig(config);
	}
	
	@Override
	public void onConfigAdded(ConfigHandler config) {
	}
	
	@Override
	public void onConfigChanged(ConfigHandler config) {
		if(FabricLoader.getInstance().getEnvironmentType() == EnvType.SERVER) {
			SyncPacket packet = SyncPacket.create(config, SyncType.SERVER_TO_CLIENT, false);
			if(packet != null) CarbonConfig.NETWORK.sendToAllPlayers(packet);
			return;
		}
		MinecraftServer server = ACTIVE_SERVER;
		if(server != null) {
			SyncPacket packet = SyncPacket.create(config, SyncType.SERVER_TO_CLIENT, false);
			if(packet != null) CarbonConfig.NETWORK.sendToAllPlayers(packet);
		}
		if(CarbonConfig.NETWORK.isInWorld()) {
			SyncPacket packet = SyncPacket.create(config, SyncType.CLIENT_TO_SERVER, false);
			if(packet != null) CarbonConfig.NETWORK.sendToServer(packet);
		}
	}
	
	@Override
	public void onConfigErrored(ConfigHandler configHandler) {
	}
	
	@Environment(EnvType.CLIENT)
	public void onConfigsLoaded() {
		InternalFeatures.loadDefaultTypes();
		Object2ObjectMap<ModContainer, List<IModConfigs>> mappedConfigs = new Object2ObjectLinkedOpenHashMap<>();
		configs.forEach((M, C) -> {
			if(CarbonConfig.MODS_DISABLED.contains(M.getMetadata().getId())) return;
			mappedConfigs.supplyIfAbsent(M, ObjectArrayList::new).add(C);
		});
		ICarbonPlugin.LOADED_PLUGINS.forEach((K, V) -> {
			V.applyConfigs(K, T -> {
				mappedConfigs.supplyIfAbsent(K, ObjectArrayList::new).add(T); 
			});
		});
		
		allKnownConfigs.clear();
		mappedConfigs.forEach((K, V) -> allKnownConfigs.put(K.getMetadata().getId(), ModConfigList.createMultiIfApplicable(K, V)));
		allKnownConfigs.put("minecraft", new MinecraftConfigs());
	}
	
	public List<IModConfigs> getAllConfigs() {
		List<IModConfigs> result = new ObjectArrayList<IModConfigs>(allKnownConfigs.values());
		result.sort(Comparator.comparing(IModConfigs::getModName));
		return result;
	}
	
	public IModConfigs getConfigsForMod(String id) {
		return allKnownConfigs.get(id);
	}
	
	public void forEachConfigs(BiConsumer<String, IModConfigs> configs) {
		allKnownConfigs.forEach(configs);
	}
	
	public void onPlayerServerJoinEvent(Player player) {
		BulkSyncPacket packet = BulkSyncPacket.create(CarbonConfig.getConfigs().getConfigsToSync(), SyncType.SERVER_TO_CLIENT, true);
		if(packet == null) return;
		CarbonConfig.NETWORK.sendToPlayer(packet, player);
	}
	
	@Environment(EnvType.CLIENT)
	public void onPlayerServerJoinEvent() {
		if(Minecraft.getInstance().getCurrentServer() == null) loadMPConfigs();
		BulkSyncPacket packet = BulkSyncPacket.create(CarbonConfig.getConfigs().getConfigsToSync(), SyncType.CLIENT_TO_SERVER, true);
		if(packet == null) return;
		CarbonConfig.NETWORK.sendToServer(packet);
	}
	
	@Environment(EnvType.CLIENT)
	public void onPlayerServerLeaveEvent() {
		if(!Minecraft.getInstance().isLocalServer()) {
			for(ConfigHandler handler : CarbonConfig.getConfigs().getAllConfigs()) {
				if(PerWorldProxy.isProxy(handler.getProxy())) {
					handler.unload();
				}
			}
		}
	}
	
	private void loadMPConfigs() {
		for(ConfigHandler handler : CarbonConfig.getConfigs().getAllConfigs()) {
			if(PerWorldProxy.isProxy(handler.getProxy())) {
				handler.load();
			}
		}
	}
	
	private void processEvents() {
		CarbonConfig.getConfigs().processFileSystemEvents();
	}
}
