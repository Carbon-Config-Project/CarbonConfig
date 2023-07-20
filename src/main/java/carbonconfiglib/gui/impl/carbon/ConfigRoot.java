package carbonconfiglib.gui.impl.carbon;

import java.util.List;

import carbonconfiglib.config.Config;
import carbonconfiglib.config.ConfigEntry.Suggestion;
import carbonconfiglib.config.ConfigSection;
import carbonconfiglib.gui.api.DataType;
import carbonconfiglib.gui.api.IArrayNode;
import carbonconfiglib.gui.api.ICompoundNode;
import carbonconfiglib.gui.api.IConfigNode;
import carbonconfiglib.gui.api.IValueNode;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.network.chat.Component;

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
public class ConfigRoot implements IConfigNode
{
	Config config;
	List<IConfigNode> children;
	
	public ConfigRoot(Config config) {
		this.config = config;
	}

	@Override
	public List<IConfigNode> getChildren() {
		if(children == null) {
			children = new ObjectArrayList<>();
			for(ConfigSection section : config.getChildren()) {
				children.add(new ConfigNode(section));
			}
		}
		return children;
	}
	
	@Override
	public IValueNode asValue() { return null; }
	@Override
	public IArrayNode asArray() { return null; }
	@Override
	public ICompoundNode asCompound() { return null; }
	@Override
	public List<Suggestion> getValidValues() { return null; }
	@Override
	public List<DataType> getDataType() { return null; }
	@Override
	public boolean isArray() { return false; }
	@Override
	public boolean isLeaf() { return false; }
	@Override
	public boolean isRoot() { return true; }
	@Override
	public boolean isChanged() { return false; }
	@Override
	public void save() {}
	@Override
	public void setPrevious() {}
	@Override
	public void setDefault() {}
	@Override
	public boolean requiresRestart() { return false; }
	@Override
	public boolean requiresReload() { return false; }
	@Override
	public Component getName() { return IConfigNode.createLabel(config.getName()); }
	@Override
	public Component getTooltip() { return null; }
}
