package carbonconfiglib.gui.api.node;

import carbonconfiglib.gui.base.helpers.Texts;
import carbonconfiglib.impl.ReloadMode;
import carbonconfiglib.utils.structure.IStructuredData.StructureType;
import net.minecraft.util.text.ITextComponent;

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
public interface IConfigFolderNode extends IConfigNode
{
	@Override
	default INode asNode() { return null; }
	@Override
	default StructureType getDataStructure() { return null; }
	@Override
	public default boolean isLeaf() { return false; }
	@Override
	public default boolean isRoot() { return false; }
	@Override
	public default boolean isChanged() {
		for(IConfigNode node : getChildren()) {
			if(node.isChanged()) return true;
		}
		return false; 
	}
	@Override
	public default boolean isDefault() {
		for(IConfigNode node : getChildren()) {
			if(!node.isDefault()) return false;
		}
		return true;
	}
	
	@Override
	public default boolean isUnsaved() {
		for(IConfigNode node : getChildren()) {
			if(node.isUnsaved()) return true;
		}
		return false;
	}
	
	@Override
	public default void save() {
		getChildren().forEach(IConfigNode::save);
	}
	@Override
	public default void setPrevious() {
		getChildren().forEach(IConfigNode::setPrevious);
	}
	@Override
	public default void setDefault() {
		getChildren().forEach(IConfigNode::setDefault);
	}
	@Override
	public default ReloadMode getReloadState() { return null; }
	@Override
	public default ITextComponent getTooltip() { return Texts.empty(); }
}
