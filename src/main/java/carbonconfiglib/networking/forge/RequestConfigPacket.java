package carbonconfiglib.networking.forge;

import java.nio.file.Files;
import java.util.UUID;

import carbonconfiglib.CarbonConfig;
import carbonconfiglib.impl.Reflects;
import carbonconfiglib.networking.ICarbonPacket;
import carbonconfiglib.networking.carbon.ConfigAnswerPacket;
import io.netty.buffer.Unpooled;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraftforge.fml.config.ConfigTracker;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.config.ModConfig.Type;
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
public class RequestConfigPacket implements ICarbonPacket
{
	ModConfig.Type type;
	UUID requestId;
	String modId;
	
	public RequestConfigPacket() {
	}
	
	public RequestConfigPacket(Type type, UUID requestId, String modId) {
		this.type = type;
		this.requestId = requestId;
		this.modId = modId;
	}
	
	@Override
	public void write(PacketBuffer buffer) {
		buffer.writeEnumValue(type);
		buffer.writeUniqueId(requestId);
		buffer.writeString(modId, 32767);
	}
	
	@Override
	public void read(PacketBuffer buffer) {
		type = buffer.readEnumValue(ModConfig.Type.class);
		requestId = buffer.readUniqueId();
		modId = buffer.readString(32767);
	}
	
	@Override
	public void process(PlayerEntity player) {
		if(!canIgnorePermissionCheck() && !player.hasPermissionLevel(4)) {
			return;
		}
		ModConfig config = findConfig();
		if(config == null) return;
		byte[] result = getData(config);
		if(result == null) return;
		PacketBuffer buf = new PacketBuffer(Unpooled.buffer());
		buf.writeByteArray(result);
		byte[] data = new byte[buf.writerIndex()];
		buf.readBytes(data);
		CarbonConfig.NETWORK.sendToPlayer(new ConfigAnswerPacket(requestId, data), player);
	}
	
	private byte[] getData(ModConfig config) {
		try {
			return Files.readAllBytes(config.getFullPath());
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private ModConfig findConfig() {
		for(ModConfig config : Reflects.getConfigs(ConfigTracker.INSTANCE).get(type)) {
			if(modId.equalsIgnoreCase(config.getModId())) return config;
		}
		return null;
	}
	
	private boolean canIgnorePermissionCheck() {
		MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
		return !server.isDedicatedServer() && (server instanceof IntegratedServer ? ((IntegratedServer)server).getPublic() : false);
	}
}
