package carbonconfiglib.gui.impl.forge;

import java.util.List;

import carbonconfiglib.gui.api.IConfigFolderNode;
import carbonconfiglib.gui.api.IConfigNode;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.common.config.Configuration;

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
public class ForgeRoot implements IConfigFolderNode
{
	Configuration config;
	String fileName;
	List<IConfigNode> children;
	
	public ForgeRoot(Configuration config, String fileName) {
		this.config = config;
		this.fileName = fileName;
	}

	@Override
	public List<IConfigNode> getChildren() {
		if(children == null) {
			children = new ObjectArrayList<>();
			for(String category : config.getCategoryNames()) {
				children.add(new ForgeFolder(config.getCategory(category)));
			}
		}
		return children;
	}
	
	@Override
	public boolean isRoot() { return true; }
	@Override
	public String getNodeName() { return null; }
	@Override
	public ITextComponent getName() { return new TextComponentString(fileName); }
}
