package carbonconfiglib.gui.impl.carbon;

import java.util.List;
import java.util.Map;

import carbonconfiglib.api.ISuggestionProvider.Suggestion;
import carbonconfiglib.config.ConfigEntry;
import carbonconfiglib.config.ConfigEntry.IArrayConfig;
import carbonconfiglib.gui.api.DataType;
import carbonconfiglib.gui.api.IArrayNode;
import carbonconfiglib.gui.api.ICompoundNode;
import carbonconfiglib.gui.api.IConfigNode;
import carbonconfiglib.gui.api.IValueNode;
import carbonconfiglib.impl.ReloadMode;
import carbonconfiglib.utils.Helpers;
import carbonconfiglib.utils.IEntryDataType.CompoundDataType;
import carbonconfiglib.utils.IEntryDataType.EntryDataType;
import net.minecraft.util.ChatComponentStyle;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;
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
public class ConfigCompoundLeaf implements IConfigNode
{
	ConfigEntry<?> entry;
	CompoundNode value;
	CompoundArrayNode array;
	
	public ConfigCompoundLeaf(ConfigEntry<?> entry) {
		this.entry = entry;
	}
	
	@Override
	public List<IConfigNode> getChildren() { return null; }
	@Override
	public IValueNode asValue() { return null; }
	
	@Override
	public IArrayNode asArray() {
		if(!isArray()) return null;
		if(array == null) array = new CompoundArrayNode(entry, (IArrayConfig)entry, getDataType(), generateNames(), entry.getDataType().asCompound());
		return array;
	}
	
	@Override
	public ICompoundNode asCompound() {
		if(isArray()) return null;
		if(value == null) value = new CompoundNode(entry, getDataType(), generateNames(), entry.getDataType().asCompound());
		return value;
	}
	
	private String[] generateNames() {
		List<Map.Entry<String, EntryDataType>> types = entry.getDataType().asCompound().getCompound();
		String[] names = new String[types.size()];
		for(int i = 0,m=types.size();i<m;i++) {
			names[i] = types.get(i).getKey();
		}
		return names;
	}
	
	@Override
	public List<DataType> getDataType() {
		CompoundDataType compound = entry.getDataType().asCompound();
		List<DataType> result = new ObjectArrayList<>();
		for(Map.Entry<String, EntryDataType> entry : compound.getCompound()) {
			result.add(DataType.byConfig(entry.getValue(), compound.getVariant(entry.getKey())));
		}
		return result;
	}
	
	@Override
	public List<Suggestion> getValidValues() { return entry.getSuggestions(T -> true); }
	@Override
	public boolean isForcingSuggestions() { return entry.areSuggestionsForced(); }
	@Override
	public boolean isArray() { return entry instanceof IArrayConfig; }
	@Override
	public boolean isLeaf() { return true; }
	@Override
	public boolean isRoot() { return false; }
	@Override
	public boolean isChanged() { return (value != null && value.isChanged()) || (array != null && array.isChanged()); }
	@Override
	public boolean requiresRestart() { return entry.getReloadState() == ReloadMode.GAME; }
	@Override
	public boolean requiresReload() { return entry.getReloadState() == ReloadMode.WORLD; }
	@Override
	public void save() {
		if(value != null) value.save();
		if(array != null) array.save();
	}
	
	@Override
	public void setPrevious() {
		if(value != null) value.setPrevious();
		if(array != null) array.setPrevious();
	}
	@Override
	public void setDefault() {
		if(isArray()) {
			if(array == null) asArray();
			array.setDefault();
		}
		else {
			if(value == null) asCompound();
			value.setDefault();
		}
	}
	@Override
	public String getNodeName() { return null; }
	@Override
	public IChatComponent getName() { return IConfigNode.createLabel(entry.getKey()); }
	@Override
	public IChatComponent getTooltip() {
		ChatComponentStyle comp = new ChatComponentText("");
		comp.appendSibling(new ChatComponentText(entry.getKey()).setChatStyle(new ChatStyle().setColor(EnumChatFormatting.YELLOW)));
		String[] array = entry.getComment();
		if(array != null && array.length > 0) {
			comp.appendText("\n");
			for(int i = 0;i<array.length;comp.appendText(array[i++]).appendText("\n"));
		}
		String limit = entry.getLimitations();
		if(limit != null && !limit.trim().isEmpty()) {
			String[] split = Helpers.splitArray(limit, ",");
			for(int i = 0,m=split.length;i<m;comp.appendText("\n").appendSibling(new ChatComponentText(split[i++]).setChatStyle(new ChatStyle().setColor(EnumChatFormatting.GRAY))));
		}
		return comp;
	}
}
