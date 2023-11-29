package carbonconfiglib.impl.internal;

import java.util.Map;

import carbonconfiglib.CarbonConfig;
import carbonconfiglib.api.IConfigChangeListener;
import carbonconfiglib.config.ConfigHandler;
import carbonconfiglib.gui.api.DataType;
import carbonconfiglib.gui.api.IModConfigs;
import carbonconfiglib.gui.api.ISuggestionRenderer;
import carbonconfiglib.gui.config.ColorElement;
import carbonconfiglib.gui.config.RegistryElement;
import carbonconfiglib.gui.screen.ConfigSelectorScreen;
import carbonconfiglib.gui.widgets.SuggestionRenderers;
import carbonconfiglib.gui.widgets.screen.CarbonScreen;
import carbonconfiglib.impl.entries.ColorValue;
import carbonconfiglib.impl.entries.ColorValue.ColorWrapper;
import carbonconfiglib.networking.carbon.StateSyncPacket;
import carbonconfiglib.networking.snyc.BulkSyncPacket;
import carbonconfiglib.networking.snyc.SyncPacket;
import carbonconfiglib.utils.SyncType;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.potion.Potion;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedOutEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.ServerTickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
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
public class EventHandler implements IConfigChangeListener
{
	public static final EventHandler INSTANCE = new EventHandler();
	Map<ModContainer, ModConfigs> configs = new Object2ObjectLinkedOpenHashMap<ModContainer, ModConfigs>().synchronize();
	
	@Override
	public void onConfigCreated(ConfigHandler config) {
		initMinecraftDataTypes(config);
		if(FMLCommonHandler.instance().getSide() == Side.SERVER) return;
		ModContainer container = Loader.instance().activeModContainer();
		if(container == null) {
			throw new IllegalStateException("Mod Configs Must be created (not loaded) during a Mod Loading Phase");
		}
		configs.computeIfAbsent(container, ModConfigs::new).addConfig(config);
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
		if(FMLCommonHandler.instance().getSide() == Side.SERVER) {
			SyncPacket packet = SyncPacket.create(config, SyncType.SERVER_TO_CLIENT, false);
			if(packet != null) CarbonConfig.NETWORK.sendToAllPlayers(packet);
			return;
		}
		MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
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
	@SideOnly(Side.CLIENT)
	public void onServerTickEvent(ServerTickEvent event) {
		if(event.phase == Phase.END) processEvents();
	}
	
	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void onClientTickEvent(ClientTickEvent event) {
		if(event.phase == Phase.END) processEvents();
		else {
			GuiScreen screen = Minecraft.getMinecraft().currentScreen;
			if(screen instanceof CarbonScreen) {
				((CarbonScreen)screen).tick();
			}
		}
	}
	
	@SideOnly(Side.CLIENT)
	public void onConfigsLoaded() {
		loadDefaultTypes();
//		Object2ObjectMap<ModContainer, List<IModConfigs>> mappedConfigs = new Object2ObjectLinkedOpenHashMap<>();
//		configs.forEach((M, C) -> {
//			if(M.getCustomExtension(ExtensionPoint.CONFIGGUIFACTORY).isPresent()) return;
//			mappedConfigs.supplyIfAbsent(M, ObjectArrayList::new).add(C);
//		});
//		if(CarbonConfig.FORGE_SUPPORT.get()) {
//			for(ModContainer container : Loader.instance().getModList()) {
//				
//			}
//			ModList.get().forEachModContainer((K, T)-> {
//				if(!T.getCustomExtension(ExtensionPoint.CONFIGGUIFACTORY).isPresent()) {
//					ForgeConfigs configs = new ForgeConfigs(T);
//					if(configs.hasConfigs()) {
//						mappedConfigs.supplyIfAbsent(T, ObjectArrayList::new).add(configs);						
//					}
//					else if(T instanceof MinecraftModContainer) {
//						mappedConfigs.supplyIfAbsent(T, ObjectArrayList::new).add(new MinecraftConfigs());
//					}
//				};
//			});
//		}
//		mappedConfigs.forEach((M, C) -> M.registerExtensionPoint(ExtensionPoint.CONFIGGUIFACTORY, () -> (E, S) -> create(S, ModConfigList.createMultiIfApplicable(M, C))));
	}
	
	@SideOnly(Side.CLIENT)
	private void loadDefaultTypes() {
		ISuggestionRenderer.Registry.register(Item.class, new SuggestionRenderers.ItemEntry());
		ISuggestionRenderer.Registry.register(Block.class, new SuggestionRenderers.ItemEntry());
		ISuggestionRenderer.Registry.register(Fluid.class, new SuggestionRenderers.FluidEntry());
		ISuggestionRenderer.Registry.register(Enchantment.class, new SuggestionRenderers.EnchantmentEntry());
		ISuggestionRenderer.Registry.register(ColorWrapper.class, new SuggestionRenderers.ColorEntry());
		ISuggestionRenderer.Registry.register(Potion.class, new SuggestionRenderers.PotionEntry());
		
		DataType.registerType(Item.class, RegistryElement.createForType(Item.class, "minecraft:air"));
		DataType.registerType(Block.class, RegistryElement.createForType(Block.class, "minecraft:air"));
		DataType.registerType(Fluid.class, RegistryElement.createForType(Fluid.class, "minecraft:water"));
		DataType.registerType(Enchantment.class, RegistryElement.createForType(Enchantment.class, "minecraft:fortune"));
		DataType.registerType(Potion.class, RegistryElement.createForType(Potion.class, "minecraft:luck"));
		DataType.registerType(ColorWrapper.class, new DataType(false, "0xFFFFFFFF", ColorElement::new, ColorElement::new));
	}
	
	@SideOnly(Side.CLIENT)
	private GuiScreen create(GuiScreen screen, IModConfigs configs) {	
		return new ConfigSelectorScreen(configs, screen);
	}
	
	public void onServerJoinPacket(EntityPlayer player) {
		CarbonConfig.NETWORK.sendToPlayer(new StateSyncPacket(Side.SERVER), player);
		CarbonConfig.NETWORK.onPlayerJoined(player, true);
		BulkSyncPacket packet = BulkSyncPacket.create(CarbonConfig.CONFIGS.getConfigsToSync(), SyncType.SERVER_TO_CLIENT, true);
		if(packet == null) return;
		CarbonConfig.NETWORK.sendToPlayer(packet, player);
	}
	
	@SubscribeEvent
	public void onServerLeaveEvent(PlayerLoggedOutEvent event) {
		CarbonConfig.NETWORK.onPlayerLeft(event.player, true);		
	}
	
//	@SubscribeEvent
//	@SideOnly(Side.CLIENT)
//	public void onPlayerServerJoinEvent(LoggedInEvent event) {
//		if(Minecraft.getMinecraft().getIntegratedServer() != null) loadMPConfigs();
//		CarbonConfig.NETWORK.sendToServer(new StateSyncPacket(Side.CLIENT));
//		BulkSyncPacket packet = BulkSyncPacket.create(CarbonConfig.CONFIGS.getConfigsToSync(), SyncType.CLIENT_TO_SERVER, true);
//		if(packet == null) return;
//		CarbonConfig.NETWORK.sendToServer(packet);
//	}
//	
//	@SubscribeEvent
//	@SideOnly(Side.CLIENT)
//	public void onPlayerServerJoinEvent(LoggedOutEvent event) {
//		if(Minecraft.getMinecraft().getIntegratedServer() != null) {
//			for(ConfigHandler handler : CarbonConfig.CONFIGS.getAllConfigs()) {
//				if(PerWorldProxy.isProxy(handler.getProxy())) {
//					handler.unload();
//				}
//			}
//		}
//	}
//	
//	private void loadMPConfigs() {
//		for(ConfigHandler handler : CarbonConfig.CONFIGS.getAllConfigs()) {
//			if(PerWorldProxy.isProxy(handler.getProxy())) {
//				handler.load();
//			}
//		}
//	}
	
	private void processEvents() {
		CarbonConfig.CONFIGS.processFileSystemEvents();
	}
}
