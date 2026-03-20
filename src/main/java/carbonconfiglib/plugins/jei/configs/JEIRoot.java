package carbonconfiglib.plugins.jei.configs;

import java.util.List;

import carbonconfiglib.gui.api.node.IConfigFolderNode;
import carbonconfiglib.gui.api.node.IConfigNode;
import mezz.jei.api.runtime.config.IJeiConfigCategory;
import mezz.jei.api.runtime.config.IJeiConfigFile;
import net.minecraft.network.chat.Component;
import speiger.src.collections.objects.lists.ObjectArrayList;

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
public class JEIRoot implements IConfigFolderNode
{
	IJeiConfigFile files;
	List<IConfigNode> nodes;
	
	public JEIRoot(IJeiConfigFile files) {
		this.files = files;
	}

	@Override
	public List<IConfigNode> getChildren()
	{
		if(nodes == null) {
			nodes = new ObjectArrayList<>();
			for(IJeiConfigCategory cat : files.getCategories()) {
				nodes.add(new JEINode(cat));
			}
		}
		return nodes;
	}
	
	@Override
	public boolean isRoot() { return true; }
	@Override
	public String getNodeName() { return null; }
	@Override
	public Component getName() { return Component.literal(files.getPath().getFileName().toString()); }
	
}
