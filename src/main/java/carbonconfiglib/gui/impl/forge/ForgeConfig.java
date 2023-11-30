package carbonconfiglib.gui.impl.forge;

import java.nio.file.Path;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Predicate;

import carbonconfiglib.api.ConfigType;
import carbonconfiglib.gui.api.IConfigNode;
import carbonconfiglib.gui.api.IModConfig;
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
	
	@Override
	public IConfigNode getRootNode() { return new ForgeRoot(config, configName); }
	@Override
	public List<IConfigTarget> getPotentialFiles() { return ObjectLists.empty(); }
	@Override
	public IModConfig loadFromFile(Path path) { return null; }
	@Override
	public IModConfig loadFromNetworking(UUID requestId, Consumer<Predicate<PacketBuffer>> network) { return null; }
	
	@Override
	public void save() {
		boolean needsRestart = scanConfigs(this::hasRestartChanged);
		config.save();
        ConfigChangedEvent event = new OnConfigChangedEvent(container.getModId(), null, Minecraft.getMinecraft().theWorld != null, needsRestart);
        MinecraftForge.EVENT_BUS.post(event);
        if (!event.getResult().equals(Result.DENY))
            MinecraftForge.EVENT_BUS.post(new PostConfigChangedEvent(container.getModId(), null, Minecraft.getMinecraft().theWorld != null, needsRestart));
	}
	
	private boolean scanConfigs(Predicate<Property> props) {
		for(String section : config.getCategoryNames()) {
			for(Property prop : config.getCategory(section).values()) {
				if(props.test(prop)) return true;
			}
		}
		return false;
	}
	
	private boolean hasRestartChanged(Property property) {
		return property.hasChanged() && property.requiresMcRestart();
	}
	
	private boolean isNotDefault(Property property) {
		return !property.isDefault();
	}
	
	public static Configuration copy(Configuration config) {
		Configuration newConfig = new Configuration();
		for(String entry : config.getCategoryNames()) {
			copy(config.getCategory(entry), newConfig.getCategory(entry));
		}
		return newConfig;
	}
	
	public static void copy(ConfigCategory category, ConfigCategory newCategory) {
		newCategory.setComment(category.getComment());
		newCategory.setLanguageKey(category.getLanguagekey());
		for(Entry<String, Property> entry : category.entrySet()) {
			newCategory.put(entry.getKey(), copy(entry.getValue()));
		}
		for(ConfigCategory child : category.getChildren()) {
			copy(child, new ConfigCategory(child.getName(), newCategory));
		}
	}
	
	public static Property copy(Property prop) {
		if(prop.isList()) {
			Property newProp = new Property(prop.getName(), prop.getStringList(), prop.getType());
			newProp.setDefaultValues(prop.getDefaults());
			newProp.comment = prop.comment;
			newProp.setLanguageKey(prop.getLanguageKey());
			newProp.setRequiresMcRestart(prop.requiresMcRestart());
			newProp.setRequiresWorldRestart(prop.requiresWorldRestart());
			newProp.setValidValues(prop.getValidValues());
			return newProp;
		}
		Property newProp = new Property(prop.getName(), prop.getStringList(), prop.getType());
		newProp.setDefaultValues(prop.getDefaults());
		newProp.comment = prop.comment;
		newProp.setLanguageKey(prop.getLanguageKey());
		newProp.setRequiresMcRestart(prop.requiresMcRestart());
		newProp.setRequiresWorldRestart(prop.requiresWorldRestart());
		newProp.setValidValues(prop.getValidValues());
		return newProp;
	}
}
