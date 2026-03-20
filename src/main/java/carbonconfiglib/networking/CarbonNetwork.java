package carbonconfiglib.networking;

import java.util.List;
import java.util.Map;

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
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.Context;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.PlayPayloadHandler;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
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
		registerPacket(SyncPacket.ID, SyncPacket.STREAM_CODEC);
		registerPacket(BulkSyncPacket.ID, BulkSyncPacket.STREAM_CODEC);
		registerPacket(ConfigRequestPacket.ID, ConfigRequestPacket.STREAM_CODEC);
		registerPacket(ConfigAnswerPacket.ID, ConfigAnswerPacket.STREAM_CODEC);
		registerPacket(SaveConfigPacket.ID, SaveConfigPacket.STREAM_CODEC);
		registerPacket(RequestGameRulesPacket.ID, RequestGameRulesPacket.STREAM_CODEC);
		registerPacket(SaveGameRulesPacket.ID, SaveGameRulesPacket.STREAM_CODEC);
	}
	
	private <T extends ICarbonPacket> void registerPacket(CustomPacketPayload.Type<T> type, StreamCodec<FriendlyByteBuf, T> codec) {
		PayloadTypeRegistry.playS2C().register(type, codec);
		PayloadTypeRegistry.playC2S().register(type, codec);
		if(FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
			registerClientPacket(type);
		}
		registerServerPacket(type);
	}
	
	private <T extends ICarbonPacket> void registerServerPacket(CustomPacketPayload.Type<T> type) {
		ServerPlayNetworking.registerGlobalReceiver(type, (packet, context) -> {
			ServerPlayer player = context.player();
			player.server.execute(() -> packet.process(player));
		});
	}
	
	@Environment(EnvType.CLIENT)
	private <T extends ICarbonPacket> void registerClientPacket(CustomPacketPayload.Type<T> type) {
		//Have to use wrapper because fabric doesn't delete subclasses properly...
		ClientPlayNetworking.registerGlobalReceiver(type, new ClientReceiver<T>());
	}
	
	public boolean isInWorld() {
		return getClientPlayer() != null;
	}
	
	@Environment(EnvType.CLIENT)
	protected Player getClientPlayer() {
		Minecraft mc = Minecraft.getInstance();
		return mc == null ? null : mc.player;
	}
	
	public void sendToServer(ICarbonPacket packet) {
		ClientPlayNetworking.send(packet);
	}
	
	public void sendToAllPlayers(ICarbonPacket packet) {
		for(ServerPlayer player : getAllPlayers()) {
			ServerPlayNetworking.send(player, packet);
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
		return player instanceof LocalPlayer && isInstalledOnServer();
	}
	
	@Environment(EnvType.CLIENT)
	public boolean isInstalledOnServer() {
		return ClientPlayNetworking.canSend(new ResourceLocation("carbonconfig", "sync"));
	}
	
	public void sendToPlayer(ICarbonPacket packet, Player player) {
		if(!(player instanceof ServerPlayer)) {
			throw new RuntimeException("Sending a Packet to a Player from client is not allowed");
		}
		ServerPlayNetworking.send((ServerPlayer)player, packet);
	}
	
	@Environment(EnvType.CLIENT)
	private static class ClientReceiver<T extends ICarbonPacket> implements PlayPayloadHandler<T> {
		
		@Override
		public void receive(T payload, Context context) {
			LocalPlayer player = context.player();
			context.client().execute(() -> payload.process(player));
		}
	}
}
