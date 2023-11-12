package carbonconfiglib.gui.impl.minecraft;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import carbonconfiglib.gui.api.IConfigFolderNode;
import carbonconfiglib.gui.api.IConfigNode;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.level.GameRules.Category;
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
public class MinecraftFolder implements IConfigFolderNode {
	Category cat;
	List<IGameRuleValue> values;
	List<IConfigNode> children;

	public MinecraftFolder(Map.Entry<Category, List<IGameRuleValue>> entry) {
		this.cat = entry.getKey();
		this.values = entry.getValue();
	}

	@Override
	public List<IConfigNode> getChildren() {
		if(children == null) {
			children = new ObjectArrayList<>();
			for(IGameRuleValue value : values) {
				children.add(new MinecraftLeaf(value));
			}
		}
		return children;
	}
	@Override
	public String getNodeName() { return cat.name().toLowerCase(Locale.ROOT); }
	@Override
	public Component getName() { return new TranslatableComponent(cat.getDescriptionId()); }
}