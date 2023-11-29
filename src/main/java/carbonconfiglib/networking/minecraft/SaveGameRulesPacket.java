package carbonconfiglib.networking.minecraft;

import java.io.IOException;

import carbonconfiglib.CarbonConfig;
import carbonconfiglib.networking.ICarbonPacket;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.world.GameRules;
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
public class SaveGameRulesPacket implements ICarbonPacket
{
	GameRules rules;
	
	public SaveGameRulesPacket() {}
	public SaveGameRulesPacket(GameRules rules) {
		this.rules = rules;
	}

	@Override
	public void write(PacketBuffer buffer) {
		buffer.writeCompoundTag(rules.writeToNBT());
	}
	
	@Override
	public void read(PacketBuffer buffer) {
		rules = new GameRules();
		try { rules.readFromNBT(buffer.readCompoundTag()); }
		catch(IOException e) { e.printStackTrace(); }
	}
	
	@Override
	public void process(EntityPlayer player) {
		if(!canIgnorePermissionCheck() && !hasPermissions(player, 4)) {
			CarbonConfig.LOGGER.warn("Don't have Permission to Change configs");
			return;
		}
		MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
		if(server == null) return;
		GameRules rule = server.getWorld(0).getGameRules();
		rule.readFromNBT(rules.writeToNBT());
	}
	
	private boolean hasPermissions(EntityPlayer player, int value) {
		MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
		return server.getPlayerList().getOppedPlayers().getPermissionLevel(player.getGameProfile()) >= value;
	}
	
	private boolean canIgnorePermissionCheck() {
		MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
		return !server.isDedicatedServer() && (server instanceof IntegratedServer ? ((IntegratedServer)server).getPublic() : false);
	}
}
