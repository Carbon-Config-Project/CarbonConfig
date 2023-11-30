package carbonconfiglib.gui.impl.minecraft;

import java.util.List;

import carbonconfiglib.api.ISuggestionProvider.Suggestion;
import carbonconfiglib.gui.api.DataType;
import carbonconfiglib.gui.api.IArrayNode;
import carbonconfiglib.gui.api.ICompoundNode;
import carbonconfiglib.gui.api.IConfigNode;
import carbonconfiglib.gui.api.IValueNode;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ChatComponentStyle;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;
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
public class MinecraftLeaf implements IConfigNode
{
	IGameRuleValue entry;
	MinecraftValue value;
	
	public MinecraftLeaf(IGameRuleValue entry) {
		this.entry = entry;
	}
	
	@Override
	public List<IConfigNode> getChildren() { return null; }
	
	@Override
	public IValueNode asValue() {
		if(value == null) {
			value = new MinecraftValue(entry);
		}
		return value;
	}
	
	@Override
	public IArrayNode asArray() { return null; }
	@Override
	public ICompoundNode asCompound() { return null; }
	@Override
	public List<DataType> getDataType() { return ObjectLists.singleton(entry.getType()); }
	
	@Override
	public List<Suggestion> getValidValues() { return null; }
	@Override
	public boolean isForcingSuggestions() { return false; }
	@Override
	public boolean isArray() { return false; }
	@Override
	public boolean isLeaf() { return true; }
	@Override
	public boolean isRoot() { return false; }
	@Override
	public boolean isChanged() { return value != null && value.isChanged(); }
	@Override
	public void setPrevious() {
		if(value != null) value.setPrevious();
	}
	
	@Override
	public void setDefault() {
		if(value != null) value.setDefault();
	}
	
	@Override
	public void save() {
		if(value != null) value.save();
	}
	
	@Override
	public boolean requiresRestart() { return false; }
	@Override
	public boolean requiresReload() { return false; }
	@Override
	public String getNodeName() { return null; }
	@Override
	public IChatComponent getName() {
		return IConfigNode.createLabel(I18n.format(entry.getDescriptionId()));
	}
	
	@Override
	public IChatComponent getTooltip() {
		String id = entry.getDescriptionId();
		ChatComponentStyle result = new ChatComponentText("");
		result.appendSibling(new ChatComponentTranslation(id).setChatStyle(new ChatStyle().setColor(EnumChatFormatting.YELLOW)));
		id += ".description";
		if(I18n.format(id) != id) {
			result.appendText("\n").appendSibling(new ChatComponentTranslation(id).setChatStyle(new ChatStyle().setColor(EnumChatFormatting.GRAY)));
		}
		return result;
	}
	
}