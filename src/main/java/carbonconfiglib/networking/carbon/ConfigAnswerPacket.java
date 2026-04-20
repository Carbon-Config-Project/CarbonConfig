package carbonconfiglib.networking.carbon;

import java.util.UUID;

import carbonconfiglib.gui.api.IRequestReceiver;
import carbonconfiglib.networking.ICarbonPacket;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;

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
public class ConfigAnswerPacket implements ICarbonPacket
{
    public static final StreamCodec<FriendlyByteBuf, ConfigAnswerPacket> STREAM_CODEC = CustomPacketPayload.codec(ConfigAnswerPacket::write, ICarbonPacket.readPacket(ConfigAnswerPacket::new));
	public static final Type<ConfigAnswerPacket> ID = ICarbonPacket.createType("carbonconfig:answer");
	UUID id;
	byte[] data;
	
	public ConfigAnswerPacket(UUID id, byte[] data) {
		this.id = id;
		this.data = data;
	}
	
	public ConfigAnswerPacket(FriendlyByteBuf buffer) {
		id = buffer.readUUID();
		data = buffer.readByteArray();
	}
	
	public void write(FriendlyByteBuf buffer) {
		buffer.writeUUID(id);
		buffer.writeByteArray(data);
	}
	
	@Override
	public Type<? extends CustomPacketPayload> type() { return ID; }
	
	public void process(Player player) {
		IRequestReceiver.Impl.receiveData(id, new FriendlyByteBuf(Unpooled.wrappedBuffer(data)));
	}
}
