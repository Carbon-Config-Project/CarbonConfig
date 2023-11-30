package carbonconfiglib.gui.impl.forge;

import java.util.List;
import java.util.Locale;

import carbonconfiglib.gui.api.IConfigFolderNode;
import carbonconfiglib.gui.api.IConfigNode;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ChatComponentStyle;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
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
	
	public ForgeFolder(ConfigCategory category) {
		this.category = category;
	}

	@Override
	public List<IConfigNode> getChildren() {
		if(children == null) {
			children = new ObjectArrayList<>();
			for(Property prop : category.values()) {
				children.add(new ForgeLeaf(prop));
			}
		}
		return children;
	}
	
	@Override
	public String getNodeName() { return category.getName().toLowerCase(Locale.ROOT); }
	@Override
	public IChatComponent getName() { return IConfigNode.createLabel(ForgeLeaf.hasKey(category.getLanguagekey()) ? I18n.format(category.getLanguagekey()) : category.getName()); }
	@Override
	public IChatComponent getTooltip() {
		ChatComponentStyle comp = new ChatComponentText("");
		comp.appendSibling(new ChatComponentText(ForgeLeaf.hasKey(category.getLanguagekey()) ? I18n.format(category.getLanguagekey()) : category.getName()).setChatStyle(new ChatStyle().setColor(EnumChatFormatting.YELLOW)));
		String comment = category.getComment();
		if(comment != null) {
			String[] array = comment.split("\n");
			if(array != null && array.length > 0) {
				for(int i = 0;i<array.length;comp.appendText("\n").appendText(array[i++]).setChatStyle(new ChatStyle().setColor(EnumChatFormatting.GRAY)));
			}

		}
		return comp;
	}
	
}
