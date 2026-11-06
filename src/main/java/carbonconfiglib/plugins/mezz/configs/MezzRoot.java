package carbonconfiglib.plugins.mezz.configs;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import carbonconfiglib.gui.api.node.IConfigFolderNode;
import carbonconfiglib.gui.api.node.IConfigNode;
import net.mezzdev.config.api.schema.IConfigSchema;
import net.mezzdev.config.api.schema.category.IConfigCategory;
import net.mezzdev.config.api.schema.category.IConfigEditorCategory;
import net.mezzdev.config.api.value.IConfigValue;
import net.minecraft.network.chat.Component;
import speiger.src.collections.objects.lists.ObjectArrayList;
import speiger.src.collections.objects.maps.impl.hash.Object2ObjectLinkedOpenHashMap;
import speiger.src.collections.objects.maps.interfaces.Object2ObjectMap;
import speiger.src.collections.objects.utils.ObjectLists;

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
public class MezzRoot implements IConfigFolderNode
{
	IConfigSchema files;
	List<IConfigNode> nodes;
	
	public MezzRoot(IConfigSchema files) {
		this.files = files;
	}

	@Override
	public List<IConfigNode> getChildren()
	{
		if(nodes == null) {
			nodes = new ObjectArrayList<>();
			Map<IConfigValue<?>, IConfigCategory> owners = new Object2ObjectLinkedOpenHashMap<>();
			for(IConfigCategory cat : files.getCategories()) {
				for(IConfigValue<?> value : cat.getConfigValues()) {
					owners.put(value, cat);
				}
			}
			Object2ObjectMap<IConfigEditorCategory, List<IConfigNode>> leafs = new Object2ObjectLinkedOpenHashMap<>();
			for(Entry<IConfigValue<?>, IConfigCategory> entry : owners.entrySet()) {
				IConfigValue<?> value = entry.getKey();
				MezzLeaf leaf = new MezzLeaf(value);
				List<? extends IConfigEditorCategory> categories = value.getEditorInfo().getEditorCategories();
				if(categories.isEmpty()) categories = ObjectLists.singleton(entry.getValue());
				for(IConfigEditorCategory cat : categories) {
					leafs.supplyIfAbsent(cat, ObjectArrayList::new).add(leaf);
				}
			}
			for(Entry<IConfigEditorCategory, List<IConfigNode>> entry : leafs.entrySet()) {
				nodes.add(new MezzNode(entry.getKey(), entry.getValue()));
			}
		}
		return nodes;
	}
	
	@Override
	public boolean isRoot() { return true; }
	@Override
	public String getNodeName() { return null; }
	@Override
	public Component getName() { return Component.literal(MezzHelpers.getConfigName(files)); }
	
}
