package carbonconfiglib.plugins.mezz.configs;

import java.util.List;
import java.util.Objects;

import carbonconfiglib.gui.api.node.IConfigFolderNode;
import carbonconfiglib.gui.api.node.IConfigNode;
import net.mezzdev.config.api.schema.category.IConfigEditorCategory;
import net.minecraft.ChatFormatting;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

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
public class MezzNode implements IConfigFolderNode
{
	IConfigEditorCategory category;
	List<IConfigNode> children;
	
	public MezzNode(IConfigEditorCategory category, List<IConfigNode> children) {
		this.category = category;
		this.children = children;
	}

	@Override
	public List<IConfigNode> getChildren() {
		return children;
	}
	
	public String getNodeName() { return category.getName(); }
	@Override
	public Component getName() { return IConfigNode.createLabel(category.getName(), category.getLocalizationKey()); }
	@Override
	public Component getTooltip() {
		MutableComponent comp = Component.empty();
		String key = category.getDescriptionLocalizationKey();
		if(key != null && !Objects.equals(I18n.get(key), key)) {
			comp.append(Component.translatable(key).withStyle(ChatFormatting.GRAY)).append("\n");
		}
		return comp;
	}
}
