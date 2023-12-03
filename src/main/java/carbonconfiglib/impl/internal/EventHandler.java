package carbonconfiglib.impl.internal;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.google.common.collect.BiMap;

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
import carbonconfiglib.gui.screen.ConfigScreenFactory;
import carbonconfiglib.gui.widgets.SuggestionRenderers;
import carbonconfiglib.gui.widgets.screen.CarbonScreen;
import carbonconfiglib.impl.PerWorldProxy;
import carbonconfiglib.impl.Reflects;
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
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.GuiScreenEvent.ActionPerformedEvent;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.client.GuiModList;
import net.minecraftforge.fml.client.IModGuiFactory;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedOutEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.ServerTickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
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
	boolean loaded = false;
	Map<ModContainer, ModConfigs> configs = new Object2ObjectLinkedOpenHashMap<ModContainer, ModConfigs>().synchronize();
	Object2ObjectMap<ModContainer, List<Configuration>> forgeConfigs = new Object2ObjectLinkedOpenHashMap<ModContainer, List<Configuration>>().synchronize();
	
	public static void registerConfig(Configuration config) {
		if(config == null || config.getConfigFile() == null || FMLCommonHandler.instance().getSide().isServer()) return;
		ModContainer container = Loader.instance().activeModContainer();
		if(container == null) return;
		INSTANCE.forgeConfigs.supplyIfAbsent(container, ObjectArrayList::new).add(config);
	}
	
	public static void onPlayerClientJoin() {
		INSTANCE.onPlayerClientJoinEvent();
	}
	
	public static void onPlayerClientLeave(IntegratedServer server) {
		INSTANCE.onPlayerClientLeaveEvent(server);
	}
	
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
	@SideOnly(Side.SERVER)
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
	
	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void onGuiOpenedEvent(GuiOpenEvent event) {
		if(event.getGui() instanceof GuiModList) {
			registerConfigs();
		}
	}
	
	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void onGuiButtonClicked(ActionPerformedEvent.Pre event) {
		if(event.getGui() instanceof GuiModList && event.getButton().id == 20) {
			ModContainer container = Reflects.getSelectedMod((GuiModList)event.getGui());
			if(container == null) return;
			IModGuiFactory factory = FMLClientHandler.instance().getGuiFactoryFor(container);
			if(factory instanceof ConfigScreenFactory) {
				event.setCanceled(true);
				Minecraft.getMinecraft().displayGuiScreen(((ConfigScreenFactory)factory).createConfigGui(event.getGui()));
			}
		}
	}
	
	@SideOnly(Side.CLIENT)
	private void registerConfigs() {
		if(loaded) return;
		loaded = true;
		BiMap<ModContainer, IModGuiFactory> factory = ObfuscationReflectionHelper.getPrivateValue(FMLClientHandler.class, FMLClientHandler.instance(), "guiFactories");
		Object2ObjectMap<ModContainer, List<IModConfigs>> mappedConfigs = new Object2ObjectLinkedOpenHashMap<>();
		configs.forEach((M, C) -> {
			if(factory.containsKey(M)) return;
			mappedConfigs.supplyIfAbsent(M, ObjectArrayList::new).add(C);
		});
		if(CarbonConfig.FORGE_SUPPORT.get()) {
			forgeConfigs.forEach((M, C) -> {
				if(factory.containsKey(M) && !CarbonConfig.FORCE_FORGE_SUPPORT.get()) return;
				mappedConfigs.supplyIfAbsent(M, ObjectArrayList::new).add(new ForgeConfigs(M, C));
			});
		}
		//This has to be done because otherwise MinecraftServer crashes during startup. I assume forges code deleter in 1.12.2 is as shitty as fabrics was in 1.19.2 xD
		Optional.of(Loader.instance().getIndexedModList().get("carbonconfig")).ifPresent(T -> {
			mappedConfigs.supplyIfAbsent(T, ObjectArrayList::new).add(new MinecraftConfigs());
		});
		mappedConfigs.forEach((M, C) -> factory.put(M, new ConfigScreenFactory(ModConfigList.createMultiIfApplicable(M, C))));
	}
	
	@SideOnly(Side.CLIENT)
	public void onConfigsLoaded() {
		loadDefaultTypes();
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
	
	@SideOnly(Side.CLIENT)
	public void onPlayerClientJoinEvent() {
		if(Minecraft.getMinecraft().getIntegratedServer() == null) loadMPConfigs();
		CarbonConfig.NETWORK.sendToServer(new StateSyncPacket(Side.CLIENT));
		BulkSyncPacket packet = BulkSyncPacket.create(CarbonConfig.CONFIGS.getConfigsToSync(), SyncType.CLIENT_TO_SERVER, true);
		if(packet == null) return;
		CarbonConfig.NETWORK.sendToServer(packet);
	}
	
	@SideOnly(Side.CLIENT)
	public void onPlayerClientLeaveEvent(IntegratedServer server) {
		CarbonConfig.NETWORK.onPlayerLeft(null, false);
		if(server == null) {
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
