package carbonconfiglib.networking;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;

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
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.client.Minecraft;
import net.minecraft.network.Connection;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkEvent.Context;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import net.minecraftforge.server.ServerLifecycleHooks;
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
	SimpleChannel channel;
	Set<UUID> clientInstalledPlayers = new ObjectOpenHashSet<>();
	boolean serverInstalled = false;
	
	public void init() {
		channel = NetworkRegistry.newSimpleChannel(new ResourceLocation("carbonconfig", "networking"), () -> VERSION, this::acceptsConnection, this::acceptsConnection);	
		registerPacket(0, SyncPacket.class, SyncPacket::new);
		registerPacket(1, BulkSyncPacket.class, BulkSyncPacket::new);
		registerPacket(2, ConfigRequestPacket.class, ConfigRequestPacket::new);
		registerPacket(3, ConfigAnswerPacket.class, ConfigAnswerPacket::new);
		registerPacket(4, SaveConfigPacket.class, SaveConfigPacket::new);
		registerPacket(5, RequestConfigPacket.class, RequestConfigPacket::new);
		registerPacket(6, SaveForgeConfigPacket.class, SaveForgeConfigPacket::new);
		registerPacket(7, RequestGameRulesPacket.class, RequestGameRulesPacket::new);
		registerPacket(8, SaveGameRulesPacket.class, SaveGameRulesPacket::new);
		registerPacket(255, StateSyncPacket.class, StateSyncPacket::new);
	}
	
	private boolean acceptsConnection(String version) {
		return VERSION.equals(version) || NetworkRegistry.ACCEPTVANILLA.equals(version) || NetworkRegistry.ABSENT.version().equals(version);
	}
	
	private <T extends ICarbonPacket> void registerPacket(int index, Class<T> packet, Supplier<T> creator) {
		channel.registerMessage(index, packet, this::writePacket, (K) -> readPacket(K, creator), this::handlePacket);
	}
	
	protected void writePacket(ICarbonPacket packet, FriendlyByteBuf buffer) {
		try { packet.write(buffer); }
		catch(Exception e) { e.printStackTrace(); }
	}
	
	protected <T extends ICarbonPacket> T readPacket(FriendlyByteBuf buffer, Supplier<T> values) {
		try {
			T packet = values.get();
			packet.read(buffer);
			return packet;
		}
		catch(Exception e) { e.printStackTrace(); }
		return null;
	}
	
	protected void handlePacket(ICarbonPacket packet, Supplier<NetworkEvent.Context> provider) {
		try {
			Context context = provider.get();
			Player player = getPlayer(context);
			context.enqueueWork(() -> packet.process(player));
			context.setPacketHandled(true);
		}
		catch(Exception e) { e.printStackTrace(); }
	}
	
	public boolean isInWorld() {
		return getClientPlayer() != null;
	}
	
	protected Player getPlayer(Context cont) {
		Player entity = cont.getSender();
		return entity != null ? entity : getClientPlayer();
	}
	
	@OnlyIn(Dist.CLIENT)
	protected Player getClientPlayer() {
		Minecraft mc = Minecraft.getInstance();
		return mc == null ? null : mc.player;
	}
	
	public void sendToServer(ICarbonPacket packet) {
		channel.send(PacketDistributor.SERVER.noArg(), packet);
	}
	
	public void sendToAllPlayers(ICarbonPacket packet) {
		channel.send(PacketDistributor.NMLIST.with(this::getAllPlayers), packet);
	}
	
	public void onPlayerJoined(Player player, boolean server) {
		if(server) clientInstalledPlayers.add(player.getUUID());
		else serverInstalled = true;
	}
	
	public void onPlayerLeft(Player player, boolean server) {
		if(server) clientInstalledPlayers.remove(player.getUUID());
		else serverInstalled = false;
	}
	
	private List<Connection> getAllPlayers() {
		List<Connection> players = new ObjectArrayList<>();
		for(ServerPlayer player : ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayers()) {
			if(isInstalledOnClient(player)) 
				players.add(player.connection.connection);
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
		channel.send(PacketDistributor.PLAYER.with(() -> ((ServerPlayer)player)), packet);
	}
}
