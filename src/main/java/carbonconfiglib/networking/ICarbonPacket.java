package carbonconfiglib.networking;

import java.util.function.Function;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamDecoder;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
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
public interface ICarbonPacket extends CustomPacketPayload
{
	public void process(Player player);
	
	public static <T extends ICarbonPacket> StreamDecoder<FriendlyByteBuf, T> readPacket(Function<FriendlyByteBuf, T> provider) {
		return B -> {
			try { return provider.apply(B); }
			catch(Exception e) { e.printStackTrace(); }
			return null;
		};
	}
	
	public static <T extends CustomPacketPayload> CustomPacketPayload.Type<T> createType(String id) {
		return new CustomPacketPayload.Type<>(Identifier.parse(id));
	}
}
