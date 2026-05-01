package carbonconfiglib.impl.internal;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

import carbonconfiglib.CarbonConfig;
import carbonconfiglib.api.IConfigChangeListener;
import carbonconfiglib.config.ConfigHandler;
import carbonconfiglib.gui.api.IModConfigs;
import carbonconfiglib.gui.impl.carbon.ModConfigs;
import carbonconfiglib.gui.impl.forge.ForgeConfigs;
import carbonconfiglib.gui.impl.minecraft.MinecraftConfigs;
import carbonconfiglib.gui.screens.ConfigListScreen;
import carbonconfiglib.gui.screens.ModConfigList;
import carbonconfiglib.impl.PerWorldProxy;
import carbonconfiglib.networking.carbon.StateSyncPacket;
import carbonconfiglib.networking.snyc.BulkSyncPacket;
import carbonconfiglib.networking.snyc.SyncPacket;
import carbonconfiglib.plugins.ICarbonPlugin;
import carbonconfiglib.utils.SyncType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.fml.mclanguageprovider.MinecraftModContainer;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent.LoggingIn;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent.LoggingOut;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
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
	Map<ModContainer, ModConfigs> configs = new Object2ObjectLinkedOpenHashMap<ModContainer, ModConfigs>().synchronize();
	Map<String, IModConfigs> allKnownConfigs = new Object2ObjectLinkedOpenHashMap<>();
	
	@Override
	public void onConfigCreated(ConfigHandler config) {
		InternalFeatures.initMinecraftDataTypes(config);
		if(FMLEnvironment.dist.isDedicatedServer()) return;
		ModLoadingContext context = ModLoadingContext.get();
		if("minecraft".equals(context.getActiveNamespace())) {
			if(FMLEnvironment.production) return;
			throw new IllegalStateException("Mod Configs Must be created (not loaded) during a Mod Loading Phase");
		}
		configs.computeIfAbsent(context.getActiveContainer(), ModConfigs::new).addConfig(config);
	}
	
	@Override
	public void onConfigAdded(ConfigHandler config) {
	}
	
	@Override
	public void onConfigChanged(ConfigHandler config) {
		if(FMLEnvironment.dist.isDedicatedServer()) {
			if(ServerLifecycleHooks.getCurrentServer() == null) return;
			SyncPacket packet = SyncPacket.create(config, SyncType.SERVER_TO_CLIENT, false);
			if(packet != null) CarbonConfig.NETWORK.sendToAllPlayers(packet);
			return;
		}
		MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
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
	
	@SubscribeEvent
	@OnlyIn(Dist.DEDICATED_SERVER)
	public void onServerTickEvent(ServerTickEvent.Post event) {
		processEvents();
	}
	
	@SubscribeEvent
	@OnlyIn(Dist.CLIENT)
	public void onClientTickEvent(ClientTickEvent.Post event) {
		processEvents();		
	}
	
	@OnlyIn(Dist.CLIENT)
	public void onConfigsLoaded() {
		InternalFeatures.loadDefaultTypes();
		Object2ObjectMap<ModContainer, List<IModConfigs>> mappedConfigs = new Object2ObjectLinkedOpenHashMap<>();
		configs.forEach((M, C) -> {
			if(M.getCustomExtension(IConfigScreenFactory.class).isPresent()) return;
			mappedConfigs.supplyIfAbsent(M, ObjectArrayList::new).add(C);
		});
		if(CarbonConfig.FORGE_SUPPORT.get()) {
			ModList.get().forEachModInOrder(T-> {
				if(CarbonConfig.MODS_DISABLED.contains(T.getModId())) return;
				if(T.getCustomExtension(IConfigScreenFactory.class).isEmpty()) {
					ForgeConfigs configs = new ForgeConfigs(T);
					if(configs.hasConfigs()) {
						mappedConfigs.supplyIfAbsent(T, ObjectArrayList::new).add(configs);						
					}
					else if(T instanceof MinecraftModContainer) {
						mappedConfigs.supplyIfAbsent(T, ObjectArrayList::new).add(new MinecraftConfigs());
					}
				};
			});
		}
		ICarbonPlugin.LOADED_PLUGINS.forEach((K, V) -> {
			List<IModConfigs> configs = new ObjectArrayList<>();
			V.applyConfigs(K, configs::add);
			if(configs.size() > 0) mappedConfigs.computeIfAbsent(K, T -> new ObjectArrayList<>()).addAll(configs);
		});
		mappedConfigs.forEach(this::register);
		mappedConfigs.forEach((M, C) -> allKnownConfigs.put(M.getModId(), ModConfigList.createMultiIfApplicable(M, C)));
	}
	
	public List<IModConfigs> getAllConfigs() {
		List<IModConfigs> result = new ObjectArrayList<IModConfigs>(allKnownConfigs.values());
		result.sort(Comparator.comparing(IModConfigs::getModName));
		return result;
	}
	
	public IModConfigs getConfigsForMod(String id) {
		return allKnownConfigs.get(id);
	}
	
	@OnlyIn(Dist.CLIENT)
	private void register(ModContainer container, List<IModConfigs> configs) {
		container.registerExtensionPoint(IConfigScreenFactory.class, new Wrapper(container, configs));
	}
	
	public void onServerJoinPacket(Player player) {
		CarbonConfig.NETWORK.sendToPlayer(new StateSyncPacket(Dist.DEDICATED_SERVER), player);
		CarbonConfig.NETWORK.onPlayerJoined(player, true);
		BulkSyncPacket packet = BulkSyncPacket.create(CarbonConfig.CONFIGS.getConfigsToSync(), SyncType.SERVER_TO_CLIENT, true);
		if(packet == null) return;
		CarbonConfig.NETWORK.sendToPlayer(packet, player);
	}
	
	@SubscribeEvent
	public void onServerLeaveEvent(PlayerLoggedOutEvent event) {
		CarbonConfig.NETWORK.onPlayerLeft(event.getEntity(), true);		
	}
	
	@SubscribeEvent
	@OnlyIn(Dist.CLIENT)
	public void onPlayerServerJoinEvent(LoggingIn event) {
		if(Minecraft.getInstance().getCurrentServer() == null) loadMPConfigs();
		CarbonConfig.NETWORK.sendToServer(new StateSyncPacket(Dist.CLIENT));
		BulkSyncPacket packet = BulkSyncPacket.create(CarbonConfig.CONFIGS.getConfigsToSync(), SyncType.CLIENT_TO_SERVER, true);
		if(packet == null) return;
		CarbonConfig.NETWORK.sendToServer(packet);
	}
	
	@SubscribeEvent
	@OnlyIn(Dist.CLIENT)
	public void onPlayerServerLeaveEvent(LoggingOut event) {
		CarbonConfig.NETWORK.onPlayerLeft(event.getPlayer(), false);
		if(!Minecraft.getInstance().isLocalServer()) {
			for(ConfigHandler handler : CarbonConfig.CONFIGS.getAllConfigs()) {
				if(PerWorldProxy.isProxy(handler.getProxy())) {
					handler.unload();
				}
			}
		}
	}
	
	private void loadMPConfigs() {
		for(ConfigHandler handler : CarbonConfig.CONFIGS.getAllConfigs()) {
			if(PerWorldProxy.isProxy(handler.getProxy())) {
				handler.load();
			}
		}
	}
	
	private void processEvents() {
		CarbonConfig.CONFIGS.processFileSystemEvents();
	}
	
	@OnlyIn(Dist.CLIENT)
	private class Wrapper implements IConfigScreenFactory {
		ModContainer container;
		List<IModConfigs> configs;
		
		public Wrapper(ModContainer container, List<IModConfigs> configs) {
			this.container = container;
			this.configs = configs;
		}
		
		@Override
		public Screen createScreen(Minecraft minecraft, Screen screen) {
			return new ConfigListScreen(screen, ModConfigList.createMultiIfApplicable(container, configs));
		}
		
	}
}
