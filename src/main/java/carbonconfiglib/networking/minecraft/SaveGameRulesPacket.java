package carbonconfiglib.networking.minecraft;

import carbonconfiglib.CarbonConfig;
import carbonconfiglib.gui.impl.minecraft.IGameRuleValue;
import carbonconfiglib.networking.ICarbonPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.gamerules.GameRules;
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
public class SaveGameRulesPacket implements ICarbonPacket
{
    public static final StreamCodec<FriendlyByteBuf, SaveGameRulesPacket> STREAM_CODEC = CustomPacketPayload.codec(SaveGameRulesPacket::write, ICarbonPacket.readPacket(SaveGameRulesPacket::new));
	public static final CustomPacketPayload.Type<SaveGameRulesPacket> ID = ICarbonPacket.createType("carbonconfig:save_mc");
	GameRules rules;
	
	public SaveGameRulesPacket(GameRules rules) {
		this.rules = rules;
	}
	
	public SaveGameRulesPacket(FriendlyByteBuf buffer) {
		rules = IGameRuleValue.read(buffer.readNbt());
	}
	
	public void write(FriendlyByteBuf buffer) {
		buffer.writeNbt(IGameRuleValue.write(rules));
	}
	
	@Override
	public Type<? extends CustomPacketPayload> type() { return ID; }
	
	@Override
	public void process(Player player) {
		if(!CarbonConfig.hasPermission(player, 4)) {
			CarbonConfig.LOGGER.warn("Don't have Permission to Change configs");
			return;
		}
		MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
		if(server == null) return;
		server.getGameRules().setAll(rules, server);
	}
}
