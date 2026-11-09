package carbonconfiglib.networking.forge;

import java.nio.file.Files;
import java.util.UUID;

import carbonconfiglib.CarbonConfig;
import carbonconfiglib.gui.impl.forge.ForgeHelpers;
import carbonconfiglib.networking.ICarbonPacket;
import carbonconfiglib.networking.carbon.ConfigAnswerPacket;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.neoforged.fml.config.ModConfig;
import speiger.src.collections.objects.utils.ObjectLists;

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
    public static final StreamCodec<FriendlyByteBuf, RequestConfigPacket> STREAM_CODEC = CustomPacketPayload.codec(RequestConfigPacket::write, ICarbonPacket.readPacket(RequestConfigPacket::new));
	public static final CustomPacketPayload.Type<RequestConfigPacket> ID = ICarbonPacket.createType("carbonconfig:request_neo");
	ModConfig.Type type;
	UUID requestId;
	String modId;
	String fileName;
	
	public RequestConfigPacket(ModConfig.Type type, UUID requestId, String modId, String fileName) {
		this.type = type;
		this.requestId = requestId;
		this.modId = modId;
		this.fileName = fileName;
	}
	
	@SuppressWarnings("deprecation")
	public RequestConfigPacket(FriendlyByteBuf buffer) {
		type = buffer.readEnum(ModConfig.Type.class);
		requestId = buffer.readUUID();
		modId = buffer.readUtf(32767);
		fileName = buffer.readUtf(32767);
	}
	
	@SuppressWarnings("deprecation")
	public void write(FriendlyByteBuf buffer) {
		buffer.writeEnum(type);
		buffer.writeUUID(requestId);
		buffer.writeUtf(modId, 32767);
		buffer.writeUtf(fileName, 32767);
	}
	
	@Override
	public Type<? extends CustomPacketPayload> type() { return ID; }
	
	@Override
	public void process(Player player) {
		if(!CarbonConfig.hasPermission(player, 4)) {
			return;
		}
		ModConfig config = findConfig();
		if(config == null) return;
		byte[] result = getData(config);
		if(result == null) return;
		FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
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
		for(ModConfig config : ForgeHelpers.getConfigs().getOrDefault(modId, ObjectLists.empty())) {
			if(config.getType() == type && fileName.equals(config.getFileName())) return config;
		}
		return null;
	}
}
