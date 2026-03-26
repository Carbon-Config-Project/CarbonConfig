package carbonconfiglib.gui.impl.forge;

import java.util.List;
import java.util.Locale;

import carbonconfiglib.gui.api.node.ConfigPath;
import carbonconfiglib.gui.api.node.IConfigFolderNode;
import carbonconfiglib.gui.api.node.IConfigNode;
import carbonconfiglib.gui.base.helpers.Texts;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;
import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.Property;
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
public class ForgeFolder implements IConfigFolderNode
{
	ConfigCategory category;
	List<IConfigNode> children;
	ConfigPath path;
	
	public ForgeFolder(ConfigCategory category, ConfigPath path) {
		this.category = category;
		this.path = path;
	}

	@Override
	public List<IConfigNode> getChildren() {
		if(children == null) {
			children = new ObjectArrayList<>();
			for(Property prop : category.values()) {
				children.add(new ForgeLeaf(prop, path.append(prop.getName())));
			}
		}
		return children;
	}
	
	@Override
	public String getNodeName() { return category.getName().toLowerCase(Locale.ROOT); }
	@Override
	public IChatComponent getName() { return IConfigNode.createLabel(Texts.hasKey(category.getLanguagekey()) ? I18n.format(category.getLanguagekey()) : category.getName()); }
	@Override
	public IChatComponent getTooltip() {
		IChatComponent comp = new ChatComponentText("");
		String comment = category.getComment();
		if(comment != null) {
			String[] array = comment.split("\n");
			if(array != null && array.length > 0) {
				for(int i = 0;i<array.length;comp.appendText(array[i++]).setChatStyle(Texts.applyStyle(EnumChatFormatting.GRAY)).appendText("\n"));
			}

		}
		return comp;
	}
	
}
