package carbonconfiglib.gui.impl.carbon;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Predicate;

import carbonconfiglib.CarbonConfig;
import carbonconfiglib.api.ConfigType;
import carbonconfiglib.api.IConfigProxy.IPotentialTarget;
import carbonconfiglib.config.Config;
import carbonconfiglib.config.ConfigHandler;
import carbonconfiglib.gui.api.IConfigNode;
import carbonconfiglib.gui.api.IModConfig;
import carbonconfiglib.impl.PerWorldProxy.WorldTarget;
import carbonconfiglib.networking.carbon.ConfigRequestPacket;
import carbonconfiglib.networking.carbon.SaveConfigPacket;
import carbonconfiglib.utils.MultilinePolicy;
import net.minecraft.network.PacketBuffer;
import speiger.src.collections.objects.lists.ObjectArrayList;

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
public class ModConfig implements IModConfig
{
	String modId;
	ConfigHandler handler;
	Config config;
	Path path;
	
	public ModConfig(String modId, ConfigHandler handler) {
		this(modId, handler, handler.getConfig(), handler.getConfigFile());
	}

	public ModConfig(String modId, ConfigHandler handler, Config config, Path path) {
		this.modId = modId;
		this.handler = handler;
		this.config = config;
		this.path = path;
	}
	
	@Override
	public IModConfig loadFromFile(Path path) {
		if(Files.notExists(path)) return null;
		Config copy = config.copy();
		try {
			ConfigHandler.load(handler, copy, Files.readAllLines(path), false);
			return new ModConfig(modId, handler, config, path);
		}
		catch(Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	@Override
	public IModConfig loadFromNetworking(UUID requestId, Consumer<Predicate<PacketBuffer>> network) {
		NetworkModConfig config = new NetworkModConfig(modId, handler, this.config.copy());
		CarbonConfig.NETWORK.sendToServer(new ConfigRequestPacket(requestId, handler.getConfigIdentifer()));
		network.accept(config);
		return config;
	}
	
	@Override
	public String getFileName() {
		return handler.getConfig().getName().concat(".cfg");
	}
	
	@Override
	public String getConfigName() {
		return handler.getSubFolder().isEmpty() ? handler.getConfig().getName() : handler.getConfigIdentifer();
	}
	
	@Override
	public String getModId() {
		return modId;
	}
	
	@Override
	public boolean isDynamicConfig() {
		return handler.getProxy().isDynamicProxy();
	}
	
	@Override
	public boolean isLocalConfig() {
		return path == handler.getConfigFile();
	}
	
	@Override
	public List<IConfigTarget> getPotentialFiles() {
		List<IConfigTarget> result = new ObjectArrayList<>();
		for(IPotentialTarget target : handler.getProxy().getPotentialConfigs()) {
			Path file = handler.createConfigFile(target.getFolder());
			if(Files.notExists(file)) continue;
			if(target instanceof WorldTarget) result.add(new WorldConfigTarget(((WorldTarget)target), file));
			else result.add(new SimpleConfigTarget(target, file));
		}
		return result;
	}
	
	@Override
	public ConfigType getConfigType() {
		return handler.getConfigType();
	}
	
	@Override
	public IConfigNode getRootNode() {
		return new ConfigRoot(config);
	}
	
	@Override
	public boolean isDefault() {
		return config.isDefault();
	}
	
	@Override
	public void restoreDefault() {
		config.resetDefault();
	}
	
	@Override
	public void save() {
		if(config == handler.getConfig()) {
			handler.save();
		}
		else {
			try (BufferedWriter writer = Files.newBufferedWriter(path)) {
				writer.write(config.serialize(handler.getMultilinePolicy()));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	private static class NetworkModConfig extends ModConfig implements Predicate<PacketBuffer> {
		
		public NetworkModConfig(String modId, ConfigHandler handler, Config config) {
			super(modId, handler, config, null);
		}

		@Override
		public boolean test(PacketBuffer t) {
			try {
				ConfigHandler.load(handler, config,  ObjectArrayList.wrap(t.readStringFromBuffer(262144).split("\n")), false);
				return true;
			}
			catch(Exception e) {
				e.printStackTrace();
				return false;
			}
		}
		
		@Override
		public void save() {
			CarbonConfig.NETWORK.sendToServer(new SaveConfigPacket(handler.getConfigIdentifer(), config.serialize(MultilinePolicy.DISABLED)));
		}
	}
}
