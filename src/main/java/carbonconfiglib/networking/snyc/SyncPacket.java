package carbonconfiglib.networking.snyc;

import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import carbonconfiglib.CarbonConfig;
import carbonconfiglib.api.buffer.IWriteBuffer;
import carbonconfiglib.config.ConfigEntry;
import carbonconfiglib.config.ConfigHandler;
import carbonconfiglib.impl.ReloadMode;
import carbonconfiglib.networking.ICarbonPacket;
import carbonconfiglib.networking.buffer.ReadBuffer;
import carbonconfiglib.networking.buffer.WriteBuffer;
import carbonconfiglib.utils.SyncType;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import net.minecraft.network.FriendlyByteBuf;
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
public class SyncPacket implements ICarbonPacket
{
	String identifier;
	SyncType type;
	Map<String, byte[]> entries = new Object2ObjectLinkedOpenHashMap<>();
	
	public SyncPacket() {
	}
	
	public SyncPacket(String identifier, SyncType type, Map<String, byte[]> entries) {
		this.identifier = identifier;
		this.type = type;
		this.entries = entries;
	}
	
	public static SyncPacket create(ConfigHandler handler, SyncType type, boolean forceSync) {
		if(!handler.isLoaded()) return null;
		Map<String, byte[]> data = new Object2ObjectLinkedOpenHashMap<>();
		ByteBuf buf = Unpooled.buffer();
		IWriteBuffer buffer = new WriteBuffer(new FriendlyByteBuf(buf));
		for(Map.Entry<String, ConfigEntry<?>> entry : handler.getConfig().getSyncedEntries(type).entrySet()) {
			ConfigEntry<?> value = entry.getValue();
			if(forceSync || value.hasChanged()) {
				buf.clear();
				value.serialize(buffer);
				byte[] configData = new byte[buf.writerIndex()];
				buf.readBytes(configData);
				data.put(entry.getKey(), configData);
				value.onSynced();
			}
		}
		return data.isEmpty() ? null : new SyncPacket(handler.getConfigIdentifer(), type, data);
	}
	
	@Override
	public void write(FriendlyByteBuf buffer) {
		buffer.writeUtf(identifier);
		buffer.writeEnum(type);
		buffer.writeVarInt(entries.size());
		for(Map.Entry<String, byte[]> entry : entries.entrySet()) {
			buffer.writeUtf(entry.getKey());
			buffer.writeByteArray(entry.getValue());
		}
	}
	
	@Override
	public void read(FriendlyByteBuf buffer) {
		identifier = buffer.readUtf(32767);
		type = buffer.readEnum(SyncType.class);
		int size = buffer.readVarInt();
		for(int i = 0;i<size;i++) {
			entries.put(buffer.readUtf(32767), buffer.readByteArray());
		}
	}
	
	@Override
	public void process(Player player) {
		ReloadMode mode = processEntry(player);
		if(mode != null) {
			player.sendSystemMessage(mode.getMessage());
		}
	}
	
	public ReloadMode processEntry(Player player) {
		if(entries.isEmpty()) return null;
		ConfigHandler cfg = CarbonConfig.CONFIGS.getConfig(identifier);
		if(cfg == null) {
			CarbonConfig.LOGGER.warn("Received packet for ["+identifier+"] which didn't exist!");
			return null;
		}
		Map<String, ConfigEntry<?>> mapped = cfg.getConfig().getSyncedEntries(type);
		boolean hasChanged = false;
		UUID owner = player.getUUID();
		ReloadMode mode = null;
		for(Entry<String, byte[]> dataEntry : entries.entrySet())
		{
			ConfigEntry<?> entry = mapped.get(dataEntry.getKey());
			if(entry != null)
			{
				entry.deserialize(new ReadBuffer(new FriendlyByteBuf(Unpooled.wrappedBuffer(dataEntry.getValue()))), owner);
				if(entry.hasChanged()) {
					hasChanged = true;
					mode = ReloadMode.or(mode, entry.getReloadState());
				}
				entry.onSynced();
			}
		}
		if(hasChanged) {
			cfg.onSynced();
			return mode;
		}
		return null;
	}
}