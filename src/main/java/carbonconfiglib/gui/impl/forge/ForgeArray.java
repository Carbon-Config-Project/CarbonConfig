package carbonconfiglib.gui.impl.forge;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import carbonconfiglib.api.IEntrySettings;
import carbonconfiglib.api.IRange;
import carbonconfiglib.api.ISuggestionProvider.Suggestion;
import carbonconfiglib.gui.api.node.IArrayNode;
import carbonconfiglib.gui.api.node.INode;
import carbonconfiglib.gui.api.types.DataType;
import carbonconfiglib.impl.ReloadMode;
import carbonconfiglib.utils.ParseResult;
import carbonconfiglib.utils.structure.IStructuredData.StructureType;
import net.minecraft.network.chat.Component;
import speiger.src.collections.objects.lists.ObjectArrayList;
import speiger.src.collections.objects.utils.ObjectLists;
import speiger.src.collections.utils.Stack;

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
public class ForgeArray implements IArrayNode
{
	String nodeName;
	Component name;
	Component tooltip;
	DataType type;
	IRange range;
	ReloadMode mode;
	Function<String, ParseResult<?>> isValid;
	Supplier<List<Suggestion>> suggestions;
	Consumer<List<String>> saved;
	
	List<ForgeValue> values = new ObjectArrayList<>();
	Stack<List<String>> previous = new ObjectArrayList<>();
	List<String> currentValues;
	List<String> savedValues = new ObjectArrayList<>();
	List<String> defaults;
	boolean autosave;
	
	public ForgeArray(String nodeName, Component name, Component tooltip, ReloadMode mode, DataType type, IRange range, List<String> value, List<String> defaultValue, Supplier<List<Suggestion>> suggestions, Function<String, ParseResult<?>> isValid, Consumer<List<String>> saved) {
		this.nodeName = nodeName;
		this.name = name;
		this.tooltip = tooltip;
		this.isValid = isValid;
		this.mode = mode;
		this.type = type;
		this.range = range;
		this.currentValues = value;
		this.savedValues.addAll(value);
		previous.push(new ObjectArrayList<>(currentValues));
		this.defaults = defaultValue;
		this.suggestions = suggestions;
		this.saved = saved;
		reload();
	}
	
	public ForgeArray withAutosave() {
		autosave = true;
		return this;
	}
	
	private void autosave() {
		if(!autosave) return;
		save();
	}
	
	private void save(String value, ForgeValue entry) {
		int index = values.indexOf(entry);
		if(index == -1) return;
		currentValues.set(0, value);
		autosave();
	}
	
	public void save() {
		saved.accept(currentValues); 
		savedValues.clear();
		savedValues.addAll(currentValues);
	}
	
	protected void reload() {
		values.clear();
		for(int i = 0;i<currentValues.size();i++) {
			values.add(new ForgeValue(name, tooltip, mode, type, range, currentValues.get(i), i >= defaults.size() ? null : defaults.get(i), () -> ObjectLists.empty(), isValid::apply, this::save).withAutosave());
		}
		autosave();
	}
	
	protected List<String> getPrev() {
		return previous.top();
	}
	
	@Override
	public boolean isChanged() {
		return !getPrev().equals(currentValues);
	}
	
	@Override
	public boolean isDefault() {
		return currentValues.equals(defaults);
	}
	
	@Override
	public boolean isUnsaved() {
		return !currentValues.equals(savedValues);
	}
	
	@Override
	public void setPrevious() {
		currentValues.clear();
		currentValues.addAll(getPrev());
		if(previous.size() > 1) previous.pop();
		reload();
	}
	
	@Override
	public void setDefault() {
		currentValues.clear();
		currentValues.addAll(defaults);
		reload();
	}
	
	@Override
	public void moveDown(int index) {
		swap(index, index+1);
	}
	
	@Override
	public void moveUp(int index) {
		swap(index, index-1);
	}
	
	@Override
	public void swap(int oldIndex, int newIndex) {
		if(oldIndex >= values.size() || oldIndex < 0) return;
		if(newIndex >= values.size() || newIndex < 0) return;
		int start = oldIndex < newIndex ? oldIndex : newIndex;
		int end = oldIndex < newIndex ? newIndex : oldIndex;
		boolean inverse = newIndex < oldIndex;
		Collections.rotate(currentValues.subList(start, end+1), inverse ? 1 : -1);
		Collections.rotate(values.subList(start, end+1), inverse ? 1 : -1);
		autosave();
	}
	
	@Override
	public void createTemp() {
		previous.push(new ObjectArrayList<>(currentValues));
		reload();
	}
	
	@Override
	public void deleteTempIfNeeded() {
		if(previous.size() > 1 && getPrev().equals(currentValues)) {
			previous.pop();
		}
	}
	
	@Override
	public StructureType getInnerType() { return StructureType.SIMPLE; }
	@Override
	public IEntrySettings getSettings() { return null; }
	@Override
	public StructureType getNodeType() { return StructureType.LIST; }
	@Override
	public ReloadMode getReloadState() { return mode; }
	@Override
	public String getNodeName() { return nodeName; }
	@Override
	public Component getName() { return name; }
	@Override
	public Component getTooltip() { return tooltip; }
	@Override
	public int size() { return values.size(); }
	@Override
	public INode get(int index) { return values.get(index); }
	@Override
	public boolean isForcedSuggestion() { return false; }
	@Override
	public List<Suggestion> getSuggestions() { return suggestions.get(); }
	
	@Override
	public void createNode(String value) {
		String defaultValue = defaults.isEmpty() ? type.getDefaultValue() : defaults.get(0);
		if(value == null) {
			value = defaultValue;			
		}
		currentValues.add(value);
		values.add(new ForgeValue(name, tooltip, mode, type, range, value, defaultValue, () -> ObjectLists.empty(), isValid::apply, this::save).withAutosave());
		autosave();
	}
	
	@Override
	public void removeNode(int index) {
		values.remove(index); 
		currentValues.remove(index);
		autosave();
	}
	@Override
	public int indexOf(INode value) { return values.indexOf(value); }
}
