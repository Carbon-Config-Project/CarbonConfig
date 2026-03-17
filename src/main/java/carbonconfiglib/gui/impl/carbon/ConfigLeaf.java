	package carbonconfiglib.gui.impl.carbon;

import java.util.List;

import org.apache.logging.log4j.util.Strings;

import carbonconfiglib.api.IEntrySettings;
import carbonconfiglib.config.ConfigEntry;
import carbonconfiglib.config.ConfigEntry.ParsedArray;
import carbonconfiglib.gui.api.Texts;
import carbonconfiglib.gui.api.node.ConfigPath;
import carbonconfiglib.gui.api.node.IConfigNode;
import carbonconfiglib.gui.api.node.INode;
import carbonconfiglib.impl.ReloadMode;
import carbonconfiglib.impl.internal.SettingsLoader;
import carbonconfiglib.utils.Helpers;
import carbonconfiglib.utils.structure.IStructuredData;
import carbonconfiglib.utils.structure.IStructuredData.StructureType;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;

/**
 * Copyright 2026 Speiger, Meduris
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
public class ConfigLeaf implements IConfigNode
{
	ConfigEntry<?> entry;
	IStructuredData data;
	StructureType type;
	ReloadMode mode;
	IValueActions value;
	ConfigPath path;
	
	public ConfigLeaf(ConfigEntry<?> entry, ConfigPath path) {
		this.entry = entry;
		this.path = path;
		this.data = entry.getDataType();
		this.type = data.getDataType();
		mode = entry.getReloadState() instanceof ReloadMode? (ReloadMode)entry.getReloadState() : null;
	}
	
	@Override
	public INode asNode() {
		if(value == null) {
			switch(type) {
				case COMPOUND:
					value = new CarbonCompound(entry.getKey(), path, mode, data.asCompound(), getName(), getTooltip(), entry.serialize(), entry.serializeDefault(), entry::canSetValue, () -> entry.getSuggestions(T -> true), this::save);
					break;
				case LIST:
					value = new CarbonArray(entry.getKey(), path, mode, data.asList(), getName(), getTooltip(), entry.serialize(), entry.serializeDefault(), entry::canSetValue, () -> entry.getSuggestions(T -> true), this::save);
					break;
				case SIMPLE:
					value = new CarbonValue(entry.getKey(), mode, getName(), getTooltip(), IEntrySettings.copyMerge(entry.getSettings(), SettingsLoader.INSTANCE.getOverride(path)), entry.getDataType(), entry.areSuggestionsForced(), () -> entry.getSuggestions(T -> true), entry.serialize(), entry.serializeDefault(), entry::canSetValue, this::save);
					break;
			}
		}
		return value;
	}
	
	private void save(String value, IValueActions actions) {
		if(entry instanceof ParsedArray) {
			entry.deserializeValue(Helpers.removeLayer(value, 0));
			return;
		}
		entry.deserializeValue(value);
	}
	@Override
	public List<IConfigNode> getChildren() { return null; }
	@Override
	public boolean isLeaf() { return true; }
	@Override
	public boolean isRoot() { return false; }
	@Override
	public boolean isDefault() { return value == null ? entry.isDefault() : value.isDefault(); }
	@Override
	public boolean isChanged() { return value != null && value.isChanged(); }
	@Override
	public boolean isUnsaved() { return value != null && value.isUnsaved(); }
	@Override
	public void setPrevious() {
		if(value != null) value.setPrevious();
	}
	@Override
	public void setDefault() {
		if(!isDefault()) asNode().setDefault();
	}
	@Override
	public void save() {
		if(value != null) value.save();
	}
	@Override
	public ReloadMode getReloadState() { return mode; }
	@Override
	public String getNodeName() { return null; }
	@Override
	public ITextComponent getName() { return IConfigNode.createLabel(entry.getKey(), entry.getTranslationKey()); }
	@Override
	public ITextComponent getTooltip() {
		IFormattableTextComponent comp = Texts.empty();
		String key = entry.getTranslationComment();
		if(key != null && I18n.exists(key)) {
			comp.append("\n").append(Texts.translatable(key).withStyle(TextFormatting.GRAY));
		}
		else {
			String[] array = entry.getComment();
			if(array != null && array.length > 0) {
				for(int i = 0;i<array.length;comp.append(array[i++]).append("\n").withStyle(TextFormatting.GRAY));
			}
		}
		
		String limit = entry.getLimitations();
		if(!Strings.isBlank(limit)) {
			if(limit.contains("\nExample:")) {
				IFormattableTextComponent result = Texts.empty();
				TextFormatting current = TextFormatting.DARK_GREEN;
				for(String entry : limit.split("\n")) {
					if(current == TextFormatting.DARK_GREEN && entry.startsWith("Example")) {
						current = TextFormatting.BLUE;
					}
					result.append(Texts.literal(entry).withStyle(current)).append("\n");
				}
				comp.append(result);
			}
			else comp.append(Texts.literal(limit).withStyle(TextFormatting.BLUE));
		}
		return comp;
	}
	@Override
	public StructureType getDataStructure() { return type; }
}
