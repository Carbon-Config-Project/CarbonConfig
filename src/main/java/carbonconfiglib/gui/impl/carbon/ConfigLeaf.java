package carbonconfiglib.gui.impl.carbon;

import java.util.List;

import org.apache.logging.log4j.util.Strings;

import carbonconfiglib.api.ISuggestionProvider.Suggestion;
import carbonconfiglib.config.ConfigEntry;
import carbonconfiglib.config.ConfigEntry.IArrayConfig;
import carbonconfiglib.gui.api.DataType;
import carbonconfiglib.gui.api.IArrayNode;
import carbonconfiglib.gui.api.ICompoundNode;
import carbonconfiglib.gui.api.IConfigNode;
import carbonconfiglib.gui.api.IValueNode;
import carbonconfiglib.impl.ReloadMode;
import it.unimi.dsi.fastutil.objects.ObjectLists;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;

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
public class ConfigLeaf implements IConfigNode
{
	ConfigEntry<?> entry;
	ValueNode value;
	ArrayNode arrayValue;
	
	public ConfigLeaf(ConfigEntry<?> entry) {
		this.entry = entry;
	}
	
	@Override
	public List<IConfigNode> getChildren() { return null; }
	
	@Override
	public IValueNode asValue() {
		if(isArray()) return null;
		if(value == null) value = new ValueNode(entry);
		return value;
	}
	@Override
	public IArrayNode asArray() {
		if(!isArray()) return null;
		if(arrayValue == null) arrayValue = new ArrayNode(entry, (IArrayConfig)entry, DataType.bySimple(entry.getDataType().asDataType()));
		return arrayValue;
	}
	
	@Override
	public ICompoundNode asCompound() { return null; }
	@Override
	public List<DataType> getDataType() { return ObjectLists.singleton(DataType.bySimple(entry.getDataType().asDataType())); }
	@Override
	public List<Suggestion> getValidValues() { return entry.getSuggestions(T -> true); }
	@Override
	public boolean isForcingSuggestions() { return entry.areSuggestionsForced(); }
	@Override
	public boolean isLeaf() { return true; }
	@Override
	public boolean isArray() { return entry instanceof IArrayConfig; }
	@Override
	public boolean isRoot() { return false; }
	@Override
	public boolean isChanged() { return (value != null && value.isChanged()) || (arrayValue != null && arrayValue.isChanged()); }
	
	@Override
	public void save() {
		if(value != null) value.save();
		if(arrayValue != null) arrayValue.save();
	}
	
	@Override
	public void setPrevious() {
		if(value != null) value.setPrevious();
		if(arrayValue != null) arrayValue.setPrevious();
	}
	
	@Override
	public void setDefault() {
		if(isArray()) {
			if(arrayValue == null) asArray();
			arrayValue.setDefault();
		}
		else {
			if(value == null) asValue();
			value.setDefault();
		}
	}
	
	@Override
	public boolean requiresRestart() { return entry.getReloadState() == ReloadMode.GAME; }
	@Override
	public boolean requiresReload() { return entry.getReloadState() == ReloadMode.WORLD; }
	@Override
	public String getNodeName() { return null; }
	@Override
	public Component getName() { return IConfigNode.createLabel(entry.getKey()); }
	@Override
	public Component getTooltip() {
		MutableComponent comp = new TextComponent("");
		comp.append(new TextComponent(entry.getKey()).withStyle(ChatFormatting.YELLOW));
		String[] array = entry.getComment();
		if(array != null && array.length > 0) {
			for(int i = 0;i<array.length;comp.append("\n").append(array[i++]).withStyle(ChatFormatting.GRAY));
		}
		String limit = entry.getLimitations();
		if(!Strings.isBlank(limit)) comp.append("\n").append(new TextComponent(limit).withStyle(ChatFormatting.BLUE));
		return comp;
	}
}