package carbonconfiglib.networking;

import java.util.EnumMap;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.INetHandler;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.IThreadListener;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.FMLEmbeddedChannel;
import net.minecraftforge.fml.common.network.FMLOutboundHandler;
import net.minecraftforge.fml.common.network.FMLOutboundHandler.OutboundTarget;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
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
@Sharable
public class CarbonNetwork extends SimpleChannelInboundHandler<ICarbonPacket>
{
	public static final String VERSION = "1.0.0";
	private EnumMap<Side, FMLEmbeddedChannel> channel;
	Set<UUID> clientInstalledPlayers = new ObjectOpenHashSet<>();
	boolean serverInstalled = false;
	boolean hasPermissions = false;
	
	public void init() {
		channel = NetworkRegistry.INSTANCE.newChannel("carbonconfig", new CarbonChannel(), this);
	}
	
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, ICarbonPacket msg) throws Exception {
		try {
			INetHandler netHandler = ctx.channel().attr(NetworkRegistry.NET_HANDLER).get();
	        IThreadListener thread = FMLCommonHandler.instance().getWorldThread(netHandler);
	        if(thread.isCallingFromMinecraftThread()) handlePacket(msg, netHandler);
	        else thread.addScheduledTask(() -> handlePacket(msg, netHandler));
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	protected void writePacket(ICarbonPacket packet, PacketBuffer buffer) {
		try { packet.write(buffer); }
		catch(Exception e) { e.printStackTrace(); }
	}
	
	protected <T extends ICarbonPacket> T readPacket(PacketBuffer buffer, Supplier<T> values) {
		try {
			T packet = values.get();
			packet.read(buffer);
			return packet;
		}
		catch(Exception e) { e.printStackTrace(); }
		return null;
	}
	
	protected void handlePacket(ICarbonPacket packet, INetHandler provider) {
		try {
			EntityPlayer player = getPlayer(provider);
			packet.process(player);
		}
		catch(Exception e) { e.printStackTrace(); }
	}
	
	public boolean isInWorld() {
		return getClientPlayer() != null;
	}
	
	public boolean hasPermissions() {
		return hasPermissions;
	}
	
	public void setPermissions(boolean value) {
		this.hasPermissions = value;
	}
	
	protected EntityPlayer getPlayer(INetHandler handler) {
		EntityPlayer entity = (handler instanceof NetHandlerPlayServer ? ((NetHandlerPlayServer)handler).playerEntity : null);
		return entity != null ? entity : getClientPlayer();
	}
	
	@SideOnly(Side.CLIENT)
	protected EntityPlayer getClientPlayer() {
		Minecraft mc = Minecraft.getMinecraft();
		return mc == null ? null : mc.thePlayer;
	}
	
	public void sendToServer(ICarbonPacket packet) {
		FMLEmbeddedChannel data = channel.get(Side.CLIENT);
		data.attr(FMLOutboundHandler.FML_MESSAGETARGET).set(OutboundTarget.TOSERVER);
		data.writeOutbound(packet);
	}
	
	public void sendToAllPlayers(ICarbonPacket packet) {
		for(EntityPlayer player : getAllPlayers()) {
			FMLEmbeddedChannel data = channel.get(Side.SERVER);
			data.attr(FMLOutboundHandler.FML_MESSAGETARGET).set(OutboundTarget.PLAYER);
			data.attr(FMLOutboundHandler.FML_MESSAGETARGETARGS).set(player);	
			data.writeOutbound(packet);
		}
	}
	
	public void onPlayerJoined(EntityPlayer player, boolean server) {
		if(server) clientInstalledPlayers.add(player.getUniqueID());
		else serverInstalled = true;
	}
	
	public void onPlayerLeft(EntityPlayer player, boolean server) {
		if(server) clientInstalledPlayers.remove(player.getUniqueID());
		else serverInstalled = false;
	}
	
	private List<EntityPlayerMP> getAllPlayers() {
		List<EntityPlayerMP> players = new ObjectArrayList<>();
		for(EntityPlayerMP player : FMLCommonHandler.instance().getMinecraftServerInstance().getConfigurationManager().getPlayerList()) {
			if(isInstalledOnClient(player)) 
				players.add(player);
		}
		return players;
	}
	
	public boolean isInstalled(EntityPlayer player) {
		return player instanceof EntityPlayerMP ? isInstalledOnClient((EntityPlayerMP)player) : isInstalledOnServer();
	}
	
	public boolean isInstalledOnClient(EntityPlayerMP player) {
		return clientInstalledPlayers.contains(player.getUniqueID());
	}
		
	public boolean isInstalledOnServer() {
		return serverInstalled;
	}
	
	public void sendToPlayer(ICarbonPacket packet, EntityPlayer player) {
		if(!(player instanceof EntityPlayerMP)) {
			throw new RuntimeException("Sending a Packet to a PlayerEntity from client is not allowed");
		}
		FMLEmbeddedChannel data = channel.get(Side.SERVER);
		data.attr(FMLOutboundHandler.FML_MESSAGETARGET).set(OutboundTarget.PLAYER);
		data.attr(FMLOutboundHandler.FML_MESSAGETARGETARGS).set(player);	
		data.writeOutbound(packet);
	}
}
