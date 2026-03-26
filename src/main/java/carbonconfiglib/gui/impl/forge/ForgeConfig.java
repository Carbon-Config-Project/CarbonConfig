package carbonconfiglib.gui.impl.forge;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Predicate;

import carbonconfiglib.api.ConfigType;
import carbonconfiglib.gui.api.IModConfig;
import carbonconfiglib.gui.api.node.ConfigPath;
import carbonconfiglib.gui.api.node.IConfigNode;
import carbonconfiglib.impl.internal.BackupManager;
import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.client.event.ConfigChangedEvent.OnConfigChangedEvent;
import net.minecraftforge.fml.client.event.ConfigChangedEvent.PostConfigChangedEvent;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.eventhandler.Event.Result;
import scala.actors.threadpool.Arrays;
import speiger.src.collections.objects.maps.impl.hash.Object2ObjectOpenHashMap;
import speiger.src.collections.objects.utils.ObjectLists;

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
public class ForgeConfig implements IModConfig
{
	ModContainer container;
	Configuration config;
	String configName;
	
	public ForgeConfig(ModContainer container, Configuration config) {
		this(container, config, config.getConfigFile().toPath().getFileName().toString());
	}
		
	public ForgeConfig(ModContainer container, Configuration config, String configName) {
		this.container = container;
		this.config = config;
		this.configName = configName;
	}
	
	@Override
	public String getFileName() { return configName; }
	@Override
	public String getConfigName() { return configName; }
	@Override
	public String getModId() { return container.getModId(); }
	@Override
	public boolean isDynamicConfig() { return false; }
	@Override
	public ConfigType getConfigType() { return ConfigType.SHARED; }
	@Override
	public boolean isLocalConfig() { return true; }
	@Override
	public boolean isDefault() { return !scanConfigs(this::isNotDefault); }
	@Override
	public void restoreDefault() {
		for(String section : config.getCategoryNames()) {
			for(Property prop : config.getCategory(section).values()) {
				prop.setToDefault();
			}
		}
	}
	
	public boolean canCreateConfigs() { return false; }
	@Override
	public boolean createConfig(Path path) { return false; }
	
	@Override
	public IConfigNode getRootNode() { return new ForgeRoot(config, configName, new ConfigPath(getModId(), configName)); }
	@Override
	public List<IConfigTarget> getPotentialFiles() { return ObjectLists.empty(); }
	@Override
	public IModConfig loadFromFile(Path path) { return null; }
	@Override
	public IModConfig loadFromNetworking(UUID requestId, Consumer<Predicate<PacketBuffer>> network) { return null; }
	
	@Override
	public void save(boolean createBackup) {
		if(createBackup) BackupManager.createBackup(this);
		boolean needsRestart = scanConfigs(this::hasRestartChanged);
		config.save();
        ConfigChangedEvent event = new OnConfigChangedEvent(container.getModId(), null, Minecraft.getMinecraft().theWorld != null, needsRestart);
        MinecraftForge.EVENT_BUS.post(event);
        if (!event.getResult().equals(Result.DENY))
            MinecraftForge.EVENT_BUS.post(new PostConfigChangedEvent(container.getModId(), null, Minecraft.getMinecraft().theWorld != null, needsRestart));
	}
	
	@Override
	public byte[] createBackup() {
		try { return Files.readAllBytes(config.getConfigFile().toPath()); }
		catch(Exception e) { e.printStackTrace(); }
		return null;
	}
	
	@Override
	public void loadBackup(byte[] data) {
		try {
			Files.write(config.getConfigFile().toPath(), data);
			Map<Property, String[]> before = serializeConfigs();
			config.load();
			boolean needsRestart = scanConfigs(T -> doPropsMatch(T, before.get(T)) && T.requiresMcRestart());
	        ConfigChangedEvent event = new OnConfigChangedEvent(container.getModId(), null, Minecraft.getMinecraft().theWorld != null, needsRestart);
	        MinecraftForge.EVENT_BUS.post(event);
	        if (!event.getResult().equals(Result.DENY))
	            MinecraftForge.EVENT_BUS.post(new PostConfigChangedEvent(container.getModId(), null, Minecraft.getMinecraft().theWorld != null, needsRestart));
		}
		catch(Exception e) { e.printStackTrace(); }
	}
	
	private boolean doPropsMatch(Property prop, String[] data) {
		return prop.isList() ? Arrays.deepEquals(prop.getStringList(), data) : Objects.equals(prop.getString(), data[0]);
	}
	
	private Map<Property, String[]> serializeConfigs() {
		Map<Property, String[]> result = new Object2ObjectOpenHashMap<>();
		for(String section : config.getCategoryNames()) {
			serializeCat(config.getCategory(section), result);
		}
		return result;
	}
	
	private void serializeCat(ConfigCategory cat, Map<Property, String[]> data) {
		for(ConfigCategory sub : cat.getChildren()) {
			serializeCat(sub, data);
		}
		for(Property prop : cat.values()) {
			data.put(prop, prop.isList() ? prop.getStringList().clone() : new String[] {prop.getString()});
		}
	}
	
	private boolean scanConfigs(Predicate<Property> props) {
		for(String section : config.getCategoryNames()) {
			if(scanCategory(props, config.getCategory(section))) return true;
		}
		return false;
	}
	
	private boolean scanCategory(Predicate<Property> props, ConfigCategory cat) {
		for(ConfigCategory sub : cat.getChildren()) {
			if(scanCategory(props, sub)) return true;
		}
		for(Property prop : cat.values()) {
			if(props.test(prop)) return true;
		}
		return false;
	}
	
	private boolean hasRestartChanged(Property property) {
		return property.hasChanged() && property.requiresMcRestart();
	}
	
	private boolean isNotDefault(Property property) {
		return !property.isDefault();
	}
}