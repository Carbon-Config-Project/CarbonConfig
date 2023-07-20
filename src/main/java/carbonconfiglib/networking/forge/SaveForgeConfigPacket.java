package carbonconfiglib.networking.forge;

import java.io.ByteArrayInputStream;

import com.electronwill.nightconfig.toml.TomlFormat;

import carbonconfiglib.CarbonConfig;
import carbonconfiglib.gui.impl.forge.ForgeHelpers;
import carbonconfiglib.networking.ICarbonPacket;
import carbonconfiglib.utils.Helpers;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.fml.config.ConfigTracker;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.config.ModConfig.Type;
import net.minecraftforge.server.ServerLifecycleHooks;

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
public class SaveForgeConfigPacket implements ICarbonPacket
{
	ModConfig.Type type;
	String modId;
	byte[] data;
	
	public SaveForgeConfigPacket() {
	}
	
	public SaveForgeConfigPacket(Type type, String modId, byte[] data) {
		this.type = type;
		this.modId = modId;
		this.data = data;
	}

	@Override
	public void write(FriendlyByteBuf buffer) {
		buffer.writeEnum(type);
		buffer.writeUtf(modId, 32767);
		buffer.writeByteArray(data);
	}
	
	@Override
	public void read(FriendlyByteBuf buffer) {
		type = buffer.readEnum(ModConfig.Type.class);
		modId = buffer.readUtf(32767);
		data = buffer.readByteArray();
	}
	
	@Override
	public void process(Player player) {
		if(!canIgnorePermissionCheck() && !player.hasPermissions(4)) {
			return;
		}
		ModConfig config = findConfig();
		if(config == null) return;
		ForgeHelpers.saveConfig(TomlFormat.instance().createParser().parse(new ByteArrayInputStream(data)), config);
		CarbonConfig.LOGGER.info("Saved ["+modId+"] "+Helpers.firstLetterUppercase(type.extension())+" Config");
	}
	
	private ModConfig findConfig() {
		for(ModConfig config : ConfigTracker.INSTANCE.configSets().get(type)) {
			if(modId.equalsIgnoreCase(config.getModId())) return config;
		}
		return null;
	}
	
	private boolean canIgnorePermissionCheck() {
		MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
		return !server.isDedicatedServer() && (server instanceof IntegratedServer ? ((IntegratedServer)server).isPublished() : false);
	}
}
