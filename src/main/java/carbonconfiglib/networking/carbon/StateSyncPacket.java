package carbonconfiglib.networking.carbon;


import carbonconfiglib.CarbonConfig;
import carbonconfiglib.impl.internal.EventHandler;
import carbonconfiglib.networking.ICarbonPacket;
import cpw.mods.fml.relauncher.Side;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;

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
public class StateSyncPacket implements ICarbonPacket
{
	Side source;
	boolean commands;
	
	public StateSyncPacket() {}
	public StateSyncPacket(Side source, boolean commands) {
		this.source = source;
	}

	@Override
	public void write(PacketBuffer buffer) {
		buffer.writeBoolean(source.isClient());
		buffer.writeBoolean(commands);
	}
	
	@Override
	public void read(PacketBuffer buffer) {
		source = buffer.readBoolean() ? Side.CLIENT : Side.SERVER;
		commands = buffer.readBoolean();
	}
	
	@Override
	public void process(EntityPlayer player) {
		if(source.isServer()) {
			CarbonConfig.NETWORK.onPlayerJoined(player, false);
			CarbonConfig.NETWORK.setPermissions(commands);
		}
		else EventHandler.INSTANCE.onServerJoinPacket(player);
	}
	
}
