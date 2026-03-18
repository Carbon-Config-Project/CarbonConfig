package carbonconfiglib.gui.impl.carbon;

import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

import carbonconfiglib.api.IEntrySettings;
import carbonconfiglib.api.IReloadMode;
import carbonconfiglib.api.ISuggestionProvider.Suggestion;
import carbonconfiglib.gui.api.node.ConfigPath;
import carbonconfiglib.gui.api.node.IArrayNode;
import carbonconfiglib.gui.api.node.INode;
import carbonconfiglib.gui.api.types.DataType;
import carbonconfiglib.impl.ReloadMode;
import carbonconfiglib.impl.internal.SettingsLoader;
import carbonconfiglib.utils.Helpers;
import carbonconfiglib.utils.ParseResult;
import carbonconfiglib.utils.structure.IStructuredData;
import carbonconfiglib.utils.structure.IStructuredData.SimpleData;
import carbonconfiglib.utils.structure.IStructuredData.StructureType;
import carbonconfiglib.utils.structure.StructureList.ListData;
import net.minecraft.util.text.ITextComponent;
import speiger.src.collections.objects.lists.ObjectArrayList;
import speiger.src.collections.objects.lists.ObjectList;
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
public class CarbonArray implements IArrayNode, IValueActions
{
	String nodeName;
	ConfigPath path;
	IReloadMode mode;
	ListData data;
	IStructuredData inner;
	ITextComponent name;
	ITextComponent tooltip;
	Function<String, ParseResult<Boolean>> isValid;
	Supplier<List<Suggestion>> suggestions;
	BiConsumer<String, IValueActions> saveAction;
	
	List<IValueActions> values = new ObjectArrayList<>();
	Stack<List<String>> previous = new ObjectArrayList<>();
	ObjectList<String> currentValues;
	ObjectList<String> savedValues;
	List<String> defaults;
	boolean autoSave = false;
	
	public CarbonArray(String nodeName, ConfigPath path, IReloadMode mode, ListData data, ITextComponent name, ITextComponent tooltip, String currentValue, String defaultValue, Function<String, ParseResult<Boolean>> isValid, Supplier<List<Suggestion>> suggestions, BiConsumer<String, IValueActions> saveAction) {
		this.nodeName = nodeName;
		this.path = path;
		this.mode = mode;
		this.data = data;
		this.inner = data.getType();
		this.name = name;
		this.tooltip = tooltip;
		this.currentValues = ObjectArrayList.wrap(Helpers.splitCompoundArray(currentValue));
		this.defaults = ObjectArrayList.wrap(Helpers.splitCompoundArray(defaultValue));
		this.savedValues = ObjectArrayList.wrap(Helpers.splitCompoundArray(currentValue));
		this.previous.push(new ObjectArrayList<>(currentValues));
		this.isValid = isValid;
		this.suggestions = suggestions;
		this.saveAction = saveAction;
		reload();
	}
	
	public CarbonArray setAutosave(boolean value) {
		autoSave = value;
		return this;
	}
	
	public void reload() {
		values.clear();
		for(int i = 0,m=currentValues.size();i<m;i++) {
			values.add(addEntry(Helpers.removeLayer(currentValues.get(i), 0), i >= defaults.size() ? "" : Helpers.removeLayer(defaults.get(i), 0), i));
		}
		autosave();
	}
	
	protected IValueActions addEntry(String value, String defaultValue, int index) {
		switch(inner.getDataType()) {
			case COMPOUND: return new CarbonCompound(Integer.toString(index), path.append("array"), mode, inner.asCompound(), name.deepCopy().appendText(index+": "), tooltip, value, defaultValue, this::isValid, () -> data.getSuggestions(T -> true), this::save).setAutosave(true);
			case LIST: return new CarbonArray(Integer.toString(index), path.append("array"), mode, inner.asList(), name.deepCopy().appendText(index+": "), tooltip, value, defaultValue, this::isValid, () -> data.getSuggestions(T -> true), this::save).setAutosave(true);
			case SIMPLE: return new CarbonValue(null, mode, name.deepCopy().appendText(index+": "), tooltip, null, inner, data.isForced(), () -> data.getSuggestions(T -> true), value, defaultValue, this::isValid, this::save).setAutosave(true);
			default: return null;
		}
	}
	
	protected ParseResult<Boolean> isValid(String value) {
		return isValid.apply(value);
	}
	
	private void autosave() {
		if(!autoSave) return;
		save();
	}
	
	protected void save(String value, IValueActions actions) {
		int index = values.indexOf(actions);
		if(index == -1) return;
		currentValues.set(index, value);
		autosave();
	}
	
	protected List<String> getPrev() {
		return previous.top();
	}
	
	@Override
	public void set(String value) {
		currentValues.clear();
		currentValues.addAll(Helpers.splitCompoundArray(value));
		autosave();
	}
	
	@Override
	public void save() {
		saveAction.accept(inner.getDataType() == StructureType.COMPOUND ? Helpers.mergeCompoundArray(currentValues, false, 0) : String.join(", ", currentValues), this);
		savedValues.clear();
		savedValues.addAll(currentValues);
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
	public void createNode(String value) {
		String defaultValue = defaults.isEmpty() ? value != null && isValid(value).getValue() ? value : inner.generateDefaultValue(this::getDefaultValue) : defaults.get(0);
		if(value == null) {
			value = defaultValue;
		}
		int index = currentValues.size();
		currentValues.add(value);
		values.add(addEntry(value, defaultValue, index));
		autosave();
	}
	
	private String getDefaultValue(SimpleData data) {
		return DataType.bySimple(data).getDefaultValue();
	}

	@Override
	public void removeNode(int index) {
		values.remove(index);
		currentValues.remove(index);
		autosave();
	}
	
	@Override
	public int indexOf(INode value) { return values.indexOf(value); }
	@Override
	public int size() { return values.size(); }
	@Override
	public INode get(int index) { return values.get(index); }
	@Override
	public StructureType getInnerType() { return inner.getDataType(); }
	@Override
	public StructureType getNodeType() { return StructureType.LIST; }
	@Override
	public IEntrySettings getSettings() { return IEntrySettings.copyMerge(data.getSettings(), SettingsLoader.INSTANCE.getOverride(path)); }
	@Override
	public ReloadMode getReloadState() { return mode instanceof ReloadMode ? (ReloadMode)mode : null; }
	@Override
	public String getNodeName() { return nodeName; }
	@Override
	public ITextComponent getName() { return name; }
	@Override
	public ITextComponent getTooltip() { return tooltip; }
	@Override
	public boolean isForcedSuggestion() { return data.isForced(); }
	@Override
	public List<Suggestion> getSuggestions() { return suggestions.get(); }


}
