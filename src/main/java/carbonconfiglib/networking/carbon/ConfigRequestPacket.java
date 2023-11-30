package carbonconfiglib.networking.carbon;

import java.util.UUID;

import carbonconfiglib.CarbonConfig;
import carbonconfiglib.config.ConfigHandler;
import carbonconfiglib.networking.ICarbonPacket;
import carbonconfiglib.utils.MultilinePolicy;
import io.netty.buffer.Unpooled;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.server.management.UserListOpsEntry;
import net.minecraftforge.fml.common.FMLCommonHandler;

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
public class ConfigRequestPacket implements ICarbonPacket
{
	UUID id;
	String identifier;
	
	public ConfigRequestPacket() {}
	
	public ConfigRequestPacket(UUID id, String identifier) {
		this.id = id;
		this.identifier = identifier;
	}

	@Override
	public void write(PacketBuffer buffer) {
		buffer.writeUuid(id);
		buffer.writeString(identifier);
	}
	
	@Override
	public void read(PacketBuffer buffer) {
		id = buffer.readUuid();
		identifier = buffer.readStringFromBuffer(32767);
	}
	
	@Override
	public void process(EntityPlayer player) {
		if(!canIgnorePermissionCheck() && !hasPermissions(player, 4)) {
			return;
		}
		ConfigHandler handler = CarbonConfig.CONFIGS.getConfig(identifier);
		if(handler == null) return;
		PacketBuffer buf = new PacketBuffer(Unpooled.buffer());
		buf.writeString(handler.getConfig().serialize(MultilinePolicy.DISABLED));
		byte[] data = new byte[buf.writerIndex()];
		buf.readBytes(data);
		CarbonConfig.NETWORK.sendToPlayer(new ConfigAnswerPacket(id, data), player);
	}
	
	private boolean hasPermissions(EntityPlayer player, int value) {
		MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
		UserListOpsEntry entry = server.getConfigurationManager().getOppedPlayers().getEntry(player.getGameProfile());
		return entry != null && entry.getPermissionLevel() >= value;
	}
	
	private boolean canIgnorePermissionCheck() {
		MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
		return !server.isDedicatedServer() && (server instanceof IntegratedServer ? ((IntegratedServer)server).getPublic() : false);
	}
}
