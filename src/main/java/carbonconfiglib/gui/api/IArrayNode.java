package carbonconfiglib.gui.api;

import java.util.List;

import carbonconfiglib.api.ISuggestionProvider.Suggestion;
import carbonconfiglib.utils.structure.IStructuredData.StructureType;

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
public interface IArrayNode extends INode
{
	public int size();
	public INode get(int index);
	public StructureType getInnerType();
	
	public List<Suggestion> getSuggestions();
	
	public void createNode(String value);
	public void removeNode(int index);
	public int indexOf(INode value);
	public default void swap(int oldIndex, int newIndex) {}
	public void moveUp(int index);
	public void moveDown(int index);
}
