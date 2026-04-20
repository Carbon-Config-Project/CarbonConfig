package carbonconfiglib.networking;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import carbonconfiglib.networking.carbon.ConfigAnswerPacket;
import carbonconfiglib.networking.carbon.ConfigRequestPacket;
import carbonconfiglib.networking.carbon.SaveConfigPacket;
import carbonconfiglib.networking.carbon.StateSyncPacket;
import carbonconfiglib.networking.forge.RequestConfigPacket;
import carbonconfiglib.networking.forge.SaveForgeConfigPacket;
import carbonconfiglib.networking.minecraft.RequestGameRulesPacket;
import carbonconfiglib.networking.minecraft.SaveGameRulesPacket;
import carbonconfiglib.networking.snyc.BulkSyncPacket;
import carbonconfiglib.networking.snyc.SyncPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import speiger.src.collections.objects.lists.ObjectArrayList;
import speiger.src.collections.objects.sets.ObjectOpenHashSet;

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
	Set<UUID> clientInstalledPlayers = new ObjectOpenHashSet<>();
	boolean serverInstalled = false;
	
	public void init(RegisterPayloadHandlersEvent event) {
		PayloadRegistrar type = event.registrar("carbonconfig").optional().versioned(VERSION);
		type.playBidirectional(SyncPacket.ID, SyncPacket.STREAM_CODEC, this::handlePacket, this::handlePacket);
		type.playBidirectional(BulkSyncPacket.ID, BulkSyncPacket.STREAM_CODEC, this::handlePacket, this::handlePacket);
		type.playBidirectional(ConfigRequestPacket.ID, ConfigRequestPacket.STREAM_CODEC, this::handlePacket, this::handlePacket);
		type.playBidirectional(ConfigAnswerPacket.ID, ConfigAnswerPacket.STREAM_CODEC, this::handlePacket, this::handlePacket);
		type.playBidirectional(SaveConfigPacket.ID, SaveConfigPacket.STREAM_CODEC, this::handlePacket, this::handlePacket);
		type.playBidirectional(RequestConfigPacket.ID, RequestConfigPacket.STREAM_CODEC, this::handlePacket, this::handlePacket);
		type.playBidirectional(SaveForgeConfigPacket.ID, SaveForgeConfigPacket.STREAM_CODEC, this::handlePacket, this::handlePacket);
		type.playBidirectional(RequestGameRulesPacket.ID, RequestGameRulesPacket.STREAM_CODEC, this::handlePacket, this::handlePacket);
		type.playBidirectional(SaveGameRulesPacket.ID, SaveGameRulesPacket.STREAM_CODEC, this::handlePacket, this::handlePacket);
		type.playBidirectional(StateSyncPacket.ID, StateSyncPacket.STREAM_CODEC, this::handlePacket, this::handlePacket);
	}
	
	protected void handlePacket(ICarbonPacket packet, IPayloadContext provider) {
		try {
			provider.enqueueWork(() -> packet.process(getPlayer(provider)));
		}
		catch(Exception e) { e.printStackTrace(); }
	}
	
	public boolean isInWorld() {
		return getClientPlayer() != null;
	}
	
	protected Player getPlayer(IPayloadContext cont) {
		Player entity = cont.player();
		return entity != null ? entity : getClientPlayer();
	}
	
	protected Player getClientPlayer() {
		Minecraft mc = Minecraft.getInstance();
		return mc == null ? null : mc.player;
	}
	
	public void sendToServer(ICarbonPacket packet) {
		ClientPacketDistributor.sendToServer(packet);
	}
	
	public void sendToAllPlayers(ICarbonPacket packet) {
		for(ServerPlayer player : getAllPlayers()) {
			PacketDistributor.sendToPlayer(player, packet);
		}
	}
	
	public void onPlayerJoined(Player player, boolean server) {
		if(server) clientInstalledPlayers.add(player.getUUID());
		else serverInstalled = true;
	}
	
	public void onPlayerLeft(Player player, boolean server) {
		if(server) clientInstalledPlayers.remove(player.getUUID());
		else serverInstalled = false;
	}
	
	private List<ServerPlayer> getAllPlayers() {
		List<ServerPlayer> players = new ObjectArrayList<>();
		for(ServerPlayer player : ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayers()) {
			if(isInstalledOnClient(player)) 
				players.add(player);
		}
		return players;
	}
	
	public boolean isInstalled(Player player) {
		return player instanceof ServerPlayer ? isInstalledOnClient((ServerPlayer)player) : isInstalledOnServer();
	}
	
	public boolean isInstalledOnClient(ServerPlayer player) {
		return clientInstalledPlayers.contains(player.getUUID());
	}
		
	public boolean isInstalledOnServer() {
		return serverInstalled;
	}
	
	public void sendToPlayer(ICarbonPacket packet, Player player) {
		if(!(player instanceof ServerPlayer)) {
			throw new RuntimeException("Sending a Packet to a Player from client is not allowed");
		}
		PacketDistributor.sendToPlayer((ServerPlayer)player, packet);
	}
}
