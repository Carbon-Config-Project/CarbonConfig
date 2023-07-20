package carbonconfiglib.networking.carbon;

import java.util.UUID;

import carbonconfiglib.gui.api.IRequestScreen;
import carbonconfiglib.networking.ICarbonPacket;
import io.netty.buffer.Unpooled;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

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
	UUID id;
	byte[] data;
	
	public ConfigAnswerPacket() {}
	
	public ConfigAnswerPacket(UUID id, byte[] data) {
		this.id = id;
		this.data = data;
	}

	@Override
	public void write(FriendlyByteBuf buffer) {
		buffer.writeUUID(id);
		buffer.writeByteArray(data);
	}
	
	@Override
	public void read(FriendlyByteBuf buffer) {
		id = buffer.readUUID();
		data = buffer.readByteArray();
	}
	
	@Override
	public void process(Player player) {
		processClient();
	}
	
	@OnlyIn(Dist.CLIENT)
	private void processClient() {
		Screen screen = Minecraft.getInstance().screen;
		if(screen instanceof IRequestScreen) {
			((IRequestScreen)screen).receiveConfigData(id, new FriendlyByteBuf(Unpooled.wrappedBuffer(data)));
		}
	}
}
