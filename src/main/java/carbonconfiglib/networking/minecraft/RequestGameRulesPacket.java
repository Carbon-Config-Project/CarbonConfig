package carbonconfiglib.networking.minecraft;

import java.io.IOException;
import java.util.UUID;

import carbonconfiglib.CarbonConfig;
import carbonconfiglib.networking.ICarbonPacket;
import carbonconfiglib.networking.carbon.ConfigAnswerPacket;
import cpw.mods.fml.common.FMLCommonHandler;
import io.netty.buffer.Unpooled;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.server.management.UserListOpsEntry;

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
public class RequestGameRulesPacket implements ICarbonPacket
{
	UUID requestId;
	
	public RequestGameRulesPacket() {}
	
	public RequestGameRulesPacket(UUID requestId) {
		this.requestId = requestId;
	}

	@Override
	public void write(PacketBuffer buffer) {
		buffer.writeLong(requestId.getMostSignificantBits());
		buffer.writeLong(requestId.getLeastSignificantBits());
	}
	
	@Override
	public void read(PacketBuffer buffer) {
		requestId = new UUID(buffer.readLong(), buffer.readLong());
	}
	
	@Override
	public void process(EntityPlayer player) {
		if(!canIgnorePermissionCheck() && !hasPermissions(player, 4)) {
			return;
		}
		MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
		if(server == null) return;
		PacketBuffer buf = new PacketBuffer(Unpooled.buffer());
		try { buf.writeNBTTagCompoundToBuffer(server.worldServers[0].getGameRules().writeGameRulesToNBT()); }
		catch(IOException e) { e.printStackTrace(); }
		byte[] data = new byte[buf.writerIndex()];
		buf.readBytes(data);
		CarbonConfig.NETWORK.sendToPlayer(new ConfigAnswerPacket(requestId, data), player);
	}
	
	private boolean hasPermissions(EntityPlayer player, int value) {
		MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
		UserListOpsEntry entry = (UserListOpsEntry)server.getConfigurationManager().func_152603_m().func_152683_b(player.getGameProfile());
		return entry != null && entry.func_152644_a() >= value;
	}
	
	private boolean canIgnorePermissionCheck() {
		MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
		return !server.isDedicatedServer() && (server instanceof IntegratedServer ? ((IntegratedServer)server).getPublic() : false);
	}
}
