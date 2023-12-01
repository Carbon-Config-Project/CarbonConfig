package carbonconfiglib.networking.minecraft;

import com.mojang.serialization.Dynamic;

import carbonconfiglib.CarbonConfig;
import carbonconfiglib.networking.ICarbonPacket;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameRules;
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
	GameRules rules;
	
	public SaveGameRulesPacket() {}
	public SaveGameRulesPacket(GameRules rules) {
		this.rules = rules;
	}

	@Override
	public void write(FriendlyByteBuf buffer) {
		buffer.writeNbt(rules.createTag());
	}
	
	@Override
	public void read(FriendlyByteBuf buffer) {
		rules = new GameRules(new Dynamic<>(NbtOps.INSTANCE, buffer.readNbt()));
	}
	
	@Override
	public void process(Player player) {
		if(!canIgnorePermissionCheck() && !player.hasPermissions(4)) {
			CarbonConfig.LOGGER.warn("Don't have Permission to Change configs");
			return;
		}
		MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
		if(server == null) return;
		server.getGameRules().assignFrom(rules, server);
	}
	
	private boolean canIgnorePermissionCheck() {
		MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
		return !server.isDedicatedServer() && (server instanceof IntegratedServer ? ((IntegratedServer)server).isPublished() : false);
	}
}
