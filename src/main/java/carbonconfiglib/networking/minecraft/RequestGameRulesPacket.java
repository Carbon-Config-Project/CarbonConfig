package carbonconfiglib.networking.minecraft;

import java.util.UUID;

import carbonconfiglib.CarbonConfig;
import carbonconfiglib.gui.impl.minecraft.IGameRuleValue;
import carbonconfiglib.networking.ICarbonPacket;
import carbonconfiglib.networking.carbon.ConfigAnswerPacket;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

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
    public static final StreamCodec<FriendlyByteBuf, RequestGameRulesPacket> STREAM_CODEC = CustomPacketPayload.codec(RequestGameRulesPacket::write, ICarbonPacket.readPacket(RequestGameRulesPacket::new));
	public static final CustomPacketPayload.Type<RequestGameRulesPacket> ID = ICarbonPacket.createType("carbonconfig:request_mc");
	UUID requestId;
	
	public RequestGameRulesPacket(UUID requestId) {
		this.requestId = requestId;
	}
	
	public RequestGameRulesPacket(FriendlyByteBuf buffer) {
		requestId = buffer.readUUID();
	}
	
	public void write(FriendlyByteBuf buffer) {
		buffer.writeUUID(requestId);
	}
	
	@Override
	public Type<? extends CustomPacketPayload> type() { return ID; }
	
	@Override
	public void process(Player player) {
		if(!CarbonConfig.hasPermission(player, 4)) {
			return;
		}
		MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
		if(server == null) return;
		FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
		buf.writeNbt(IGameRuleValue.write(server.getGameRules()));
		byte[] data = new byte[buf.writerIndex()];
		buf.readBytes(data);
		CarbonConfig.NETWORK.sendToPlayer(new ConfigAnswerPacket(requestId, data), player);
	}
}
