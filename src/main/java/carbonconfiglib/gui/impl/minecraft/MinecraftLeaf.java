package carbonconfiglib.gui.impl.minecraft;

import java.util.List;
import java.util.Objects;

import carbonconfiglib.gui.api.node.IConfigNode;
import carbonconfiglib.gui.api.node.INode;
import carbonconfiglib.impl.ReloadMode;
import carbonconfiglib.utils.structure.IStructuredData.StructureType;
import net.minecraft.ChatFormatting;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;

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
	public StructureType getDataStructure() { return StructureType.SIMPLE; }
	
	@Override
	public INode asNode() {
		if(value == null) value = new MinecraftValue(entry);
		return value;
	}
	
	@Override
	public boolean isLeaf() { return true; }
	@Override
	public boolean isRoot() { return false; }
	@Override
	public boolean isDefault() { return value == null ? Objects.equals(entry.get(), entry.getDefault()) : value.isDefault(); }
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
	public ReloadMode getReloadState() { return null; }
	@Override
	public String getNodeName() { return null; }
	@Override
	public Component getName() { return IConfigNode.createLabel(I18n.get(entry.getDescriptionId())); }
	@Override
	public Component getTooltip() {
		String id = entry.getDescriptionId()+".description";
		MutableComponent result = new TextComponent("");
		if(I18n.exists(id)) {
			result.append(new TranslatableComponent(id).withStyle(ChatFormatting.GRAY));
		}
		return result;
	}
	
}