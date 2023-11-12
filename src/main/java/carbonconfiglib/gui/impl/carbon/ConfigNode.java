package carbonconfiglib.gui.impl.carbon;

import java.util.List;
import java.util.Locale;

import carbonconfiglib.config.ConfigEntry;
import carbonconfiglib.config.ConfigSection;
import carbonconfiglib.gui.api.IConfigFolderNode;
import carbonconfiglib.gui.api.IConfigNode;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
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
public class ConfigNode implements IConfigFolderNode
{
	ConfigSection section;
	List<IConfigNode> children;
	
	public ConfigNode(ConfigSection section) {
		this.section = section;
	}

	@Override
	public List<IConfigNode> getChildren() {
		if(children == null) {
			children = new ObjectArrayList<>();
			for(ConfigSection sub : section.getChildren()) {
				children.add(new ConfigNode(sub));
			}
			for(ConfigEntry<?> entry : section.getEntries()) {
				if(!entry.isNotHidden()) continue;
				if(entry.getDataType().isCompound()) {
					children.add(new ConfigCompoundLeaf(entry));
					continue;
				}
				children.add(new ConfigLeaf(entry));
			}
		}
		return children;
	}
	@Override
	public String getNodeName() { return section.getName().toLowerCase(Locale.ROOT); }
	@Override
	public Component getName() { return IConfigNode.createLabel(section.getName()); }
	@Override
	public Component getTooltip() {
		MutableComponent comp = new TextComponent("");
		comp.append(new TextComponent(section.getName()).withStyle(ChatFormatting.YELLOW));
		String[] array = section.getComment();
		if(array != null && array.length > 0) {
			for(int i = 0;i<array.length;comp.append("\n").append(array[i++]).withStyle(ChatFormatting.GRAY));
		}
		return comp;
	}
}