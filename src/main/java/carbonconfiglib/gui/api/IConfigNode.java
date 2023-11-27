package carbonconfiglib.gui.api;

import java.util.List;

import carbonconfiglib.api.ISuggestionProvider.Suggestion;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextComponent;

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
public interface IConfigNode
{
	public List<IConfigNode> getChildren();
	public IValueNode asValue();
	public IArrayNode asArray();
	public ICompoundNode asCompound();
	public List<DataType> getDataType();
	public List<Suggestion> getValidValues();
	public boolean isForcingSuggestions();
	
	public boolean isArray();
	public boolean isLeaf();
	public boolean isRoot();
	
	public boolean isChanged();
	public void setPrevious();
	public void setDefault();
	public void save();
	
	public boolean requiresRestart();
	public boolean requiresReload();
	
	public String getNodeName();
	public ITextComponent getName();
	public ITextComponent getTooltip();
	
	
	public static TextComponent createLabel(String name) {
		TextComponent comp = new StringTextComponent("");
		for(String s : name.split("\\-|\\_|(?<!^)(?=[A-Z][a-z])|(?<!(^|[A-Z]))(?=[A-Z])")) {
			String first = Character.toString(s.charAt(0));
			comp.append(s.replaceFirst(first, first.toUpperCase())).append(" ");
		}
		return comp;
	}
}
