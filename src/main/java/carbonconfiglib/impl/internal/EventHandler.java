package carbonconfiglib.impl.internal;

import java.util.List;
import java.util.Map;

import carbonconfiglib.CarbonConfig;
import carbonconfiglib.api.IConfigChangeListener;
import carbonconfiglib.config.ConfigHandler;
import carbonconfiglib.gui.api.DataType;
import carbonconfiglib.gui.api.IModConfigs;
import carbonconfiglib.gui.api.ISuggestionRenderer;
import carbonconfiglib.gui.config.ColorElement;
import carbonconfiglib.gui.config.RegistryElement;
import carbonconfiglib.gui.impl.forge.ForgeConfigs;
import carbonconfiglib.gui.impl.minecraft.MinecraftConfigs;
import carbonconfiglib.gui.screen.ConfigSelectorScreen;
import carbonconfiglib.gui.widgets.SuggestionRenderers;
import carbonconfiglib.impl.PerWorldProxy;
import carbonconfiglib.impl.entries.ColorValue;
import carbonconfiglib.impl.entries.ColorValue.ColorWrapper;
import carbonconfiglib.networking.carbon.StateSyncPacket;
import carbonconfiglib.networking.snyc.BulkSyncPacket;
import carbonconfiglib.networking.snyc.SyncPacket;
import carbonconfiglib.utils.SyncType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.ConfigGuiHandler.ConfigGuiFactory;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent.LoggedInEvent;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent.LoggedOutEvent;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.ServerTickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.mclanguageprovider.MinecraftModContainer;
import net.minecraftforge.server.ServerLifecycleHooks;
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
	
	@Override
	public void onConfigCreated(ConfigHandler config) {
		initMinecraftDataTypes(config);
		if(FMLEnvironment.dist.isDedicatedServer()) return;
		ModLoadingContext context = ModLoadingContext.get();
		if("minecraft".equals(context.getActiveNamespace())) {
			if(FMLEnvironment.production) return;
			throw new IllegalStateException("Mod Configs Must be created (not loaded) during a Mod Loading Phase");
		}
		configs.computeIfAbsent(context.getActiveContainer(), ModConfigs::new).addConfig(config);
	}
	
	public void initMinecraftDataTypes(ConfigHandler config) {
		config.addParser('C', ColorValue::parse);
		config.addTempParser('R');
		config.addTempParser('K');
	}
	
	@Override
	public void onConfigAdded(ConfigHandler config) {
	}
	
	@Override
	public void onConfigChanged(ConfigHandler config) {
		if(FMLEnvironment.dist.isDedicatedServer()) {
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
	public void onServerTickEvent(ServerTickEvent event) {
		if(event.phase == Phase.END) processEvents();
	}
	
	@SubscribeEvent
	@OnlyIn(Dist.CLIENT)
	public void onClientTickEvent(ClientTickEvent event) {
		if(event.phase == Phase.END) processEvents();		
	}
	
	@OnlyIn(Dist.CLIENT)
	public void onConfigsLoaded() {
		loadDefaultTypes();
		Object2ObjectMap<ModContainer, List<IModConfigs>> mappedConfigs = new Object2ObjectLinkedOpenHashMap<>();
		configs.forEach((M, C) -> {
			if(M.getCustomExtension(ConfigGuiFactory.class).isPresent()) return;
			mappedConfigs.supplyIfAbsent(M, ObjectArrayList::new).add(C);
		});
		if(CarbonConfig.FORGE_SUPPORT.get()) {
			ModList.get().forEachModContainer((K, T)-> {
				if(T.getCustomExtension(ConfigGuiFactory.class).isEmpty()) {
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
		mappedConfigs.forEach((M, C) -> M.registerExtensionPoint(ConfigGuiFactory.class, () -> new ConfigGuiFactory((U, S) -> create(S, ModConfigList.createMultiIfApplicable(M, C)))));
	}
	
	@OnlyIn(Dist.CLIENT)
	private void loadDefaultTypes() {
		ISuggestionRenderer.Registry.register(Item.class, new SuggestionRenderers.ItemEntry());
		ISuggestionRenderer.Registry.register(Block.class, new SuggestionRenderers.ItemEntry());
		ISuggestionRenderer.Registry.register(Fluid.class, new SuggestionRenderers.FluidEntry());
		ISuggestionRenderer.Registry.register(Enchantment.class, new SuggestionRenderers.EnchantmentEntry());
		ISuggestionRenderer.Registry.register(ColorWrapper.class, new SuggestionRenderers.ColorEntry());
		ISuggestionRenderer.Registry.register(MobEffect.class, new SuggestionRenderers.PotionEntry());
		
		DataType.registerType(Item.class, RegistryElement.createForType(Item.class, "minecraft:air"));
		DataType.registerType(Block.class, RegistryElement.createForType(Block.class, "minecraft:air"));
		DataType.registerType(Fluid.class, RegistryElement.createForType(Fluid.class, "minecraft:empty"));
		DataType.registerType(Enchantment.class, RegistryElement.createForType(Enchantment.class, "minecraft:fortune"));
		DataType.registerType(MobEffect.class, RegistryElement.createForType(MobEffect.class, "minecraft:luck"));
		DataType.registerType(ColorWrapper.class, new DataType(false, "0xFFFFFFFF", ColorElement::new, ColorElement::new));
	}
	
	@OnlyIn(Dist.CLIENT)
	private Screen create(Screen screen, IModConfigs configs) {	
		return new ConfigSelectorScreen(configs, screen);
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
		CarbonConfig.NETWORK.onPlayerLeft(event.getPlayer(), true);		
	}
	
	@SubscribeEvent
	@OnlyIn(Dist.CLIENT)
	public void onPlayerServerJoinEvent(LoggedInEvent event) {
		if(Minecraft.getInstance().getCurrentServer() != null) loadMPConfigs();
		CarbonConfig.NETWORK.sendToServer(new StateSyncPacket(Dist.CLIENT));
		BulkSyncPacket packet = BulkSyncPacket.create(CarbonConfig.CONFIGS.getConfigsToSync(), SyncType.CLIENT_TO_SERVER, true);
		if(packet == null) return;
		CarbonConfig.NETWORK.sendToServer(packet);
	}
	
	@SubscribeEvent
	@OnlyIn(Dist.CLIENT)
	public void onPlayerServerJoinEvent(LoggedOutEvent event) {
		if(Minecraft.getInstance().getCurrentServer() != null) {
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
}
