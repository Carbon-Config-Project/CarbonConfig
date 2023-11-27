package carbonconfiglib.networking.carbon;

import java.util.UUID;

import carbonconfiglib.CarbonConfig;
import carbonconfiglib.config.ConfigHandler;
import carbonconfiglib.networking.ICarbonPacket;
import carbonconfiglib.utils.MultilinePolicy;
import io.netty.buffer.Unpooled;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

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
		buffer.writeUUID(id);
		buffer.writeUtf(identifier, 32767);
	}
	
	@Override
	public void read(PacketBuffer buffer) {
		id = buffer.readUUID();
		identifier = buffer.readUtf(32767);
	}
	
	@Override
	public void process(PlayerEntity player) {
		if(!canIgnorePermissionCheck() && !player.hasPermissions(4)) {
			return;
		}
		ConfigHandler handler = CarbonConfig.CONFIGS.getConfig(identifier);
		if(handler == null) return;
		PacketBuffer buf = new PacketBuffer(Unpooled.buffer());
		buf.writeUtf(handler.getConfig().serialize(MultilinePolicy.DISABLED), 262144);
		byte[] data = new byte[buf.writerIndex()];
		buf.readBytes(data);
		CarbonConfig.NETWORK.sendToPlayer(new ConfigAnswerPacket(id, data), player);
	}
	
	private boolean canIgnorePermissionCheck() {
		MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
		return !server.isDedicatedServer() && (server instanceof IntegratedServer ? ((IntegratedServer)server).isPublished() : false);
	}
}
