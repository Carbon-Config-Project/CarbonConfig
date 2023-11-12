package carbonconfiglib.networking;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import carbonconfiglib.impl.internal.EventHandler;
import carbonconfiglib.networking.carbon.ConfigAnswerPacket;
import carbonconfiglib.networking.carbon.ConfigRequestPacket;
import carbonconfiglib.networking.carbon.SaveConfigPacket;
import carbonconfiglib.networking.minecraft.RequestGameRulesPacket;
import carbonconfiglib.networking.minecraft.SaveGameRulesPacket;
import carbonconfiglib.networking.snyc.BulkSyncPacket;
import carbonconfiglib.networking.snyc.SyncPacket;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
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
public class CarbonNetwork
{
	public static final String VERSION = "1.0.0";
	Map<Class<?>, ResourceLocation> mappedPackets = new Object2ObjectOpenHashMap<>();
	
	public void init() {
		registerPacket("sync", SyncPacket.class, SyncPacket::new);
		registerPacket("bulk_sync", BulkSyncPacket.class, BulkSyncPacket::new);
		registerPacket("config_request", ConfigRequestPacket.class, ConfigRequestPacket::new);
		registerPacket("config_answer", ConfigAnswerPacket.class, ConfigAnswerPacket::new);
		registerPacket("config_save", SaveConfigPacket.class, SaveConfigPacket::new);
		registerPacket("rules_request", RequestGameRulesPacket.class, RequestGameRulesPacket::new);
		registerPacket("rules_save", SaveGameRulesPacket.class, SaveGameRulesPacket::new);
	}
	
	private <T extends ICarbonPacket> void registerPacket(String id, Class<T> packet, Supplier<T> creator) {
		if(FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
			registerClientPacket(id, creator);
		}
		registerServerPacket(id, creator);
	}
	
	private <T extends ICarbonPacket> void registerServerPacket(String id, Supplier<T> creator) {
		ServerPlayNetworking.registerGlobalReceiver(new ResourceLocation("carbonconfig", id), (server, player, handler, buf, responseSender) -> {
			T packet = creator.get();
			packet.read(buf);
			server.execute(() -> packet.process(player));
		});
	}
		
	
	private <T extends ICarbonPacket> void registerClientPacket(String id, Supplier<T> creator) {
		ClientPlayNetworking.registerGlobalReceiver(new ResourceLocation("carbonconfig", id), (client, handler, buf, responseSender) -> {
			T packet = creator.get();
			packet.read(buf);
			//Lets hope local players are covered by this, because Fabric MUST SUCK THIS BADLY
			client.execute(() -> packet.process(client.player));
		});
	}
	
	public boolean isInWorld() {
		return getClientPlayer() != null;
	}
	
	@Environment(EnvType.CLIENT)
	protected Player getClientPlayer() {
		Minecraft mc = Minecraft.getInstance();
		return mc == null ? null : mc.player;
	}
	
	protected ResourceLocation toId(ICarbonPacket packet) {
		return mappedPackets.get(packet.getClass());
	}
	
	protected FriendlyByteBuf toData(ICarbonPacket packet) {
		FriendlyByteBuf buf = PacketByteBufs.create();
		packet.write(buf);
		return buf;
	}
	
	public void sendToServer(ICarbonPacket packet) {
		ClientPlayNetworking.send(toId(packet), toData(packet));
	}
	
	public void sendToAllPlayers(ICarbonPacket packet) {
		ResourceLocation id = toId(packet);
		FriendlyByteBuf data = toData(packet);
		for(ServerPlayer player : getAllPlayers()) {
			ServerPlayNetworking.send(player, id, data);
		}
	}
	
	private List<ServerPlayer> getAllPlayers() {
		List<ServerPlayer> players = new ObjectArrayList<>();
		for(ServerPlayer player : EventHandler.getServer().getPlayerList().getPlayers()) {
			if(isInstalledOnClient(player)) players.add(player);
		}
		return players;
	}
	
	public boolean isInstalled(Player player) {
		return player instanceof ServerPlayer ? isInstalledOnClient((ServerPlayer)player) : isInstalledOnServerSafe(player);
	}
	
	public boolean isInstalledOnClient(ServerPlayer player) {
		return ServerPlayNetworking.canSend(player, new ResourceLocation("carbonconfig", "sync"));
	}
	
	@Environment(EnvType.CLIENT)
	public boolean isInstalledOnServerSafe(Player player) {
		return player instanceof LocalPlayer && isInstalledOnServer((LocalPlayer)player);
	}
	
	@Environment(EnvType.CLIENT)
	public boolean isInstalledOnServer(LocalPlayer player) {
		return ClientPlayNetworking.canSend(new ResourceLocation("carbonconfig", "sync"));
	}
	
	public void sendToPlayer(ICarbonPacket packet, Player player) {
		if(!(player instanceof ServerPlayer)) {
			throw new RuntimeException("Sending a Packet to a Player from client is not allowed");
		}
		ServerPlayNetworking.send((ServerPlayer)player, toId(packet), toData(packet));
	}
}
