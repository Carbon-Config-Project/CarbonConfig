package carbonconfiglib.gui.impl.carbon;

import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

import carbonconfiglib.api.IEntrySettings;
import carbonconfiglib.api.IRange;
import carbonconfiglib.api.IReloadMode;
import carbonconfiglib.api.ISuggestionProvider.Suggestion;
import carbonconfiglib.gui.api.node.IValueNode;
import carbonconfiglib.gui.api.types.DataType;
import carbonconfiglib.impl.ReloadMode;
import carbonconfiglib.utils.ParseResult;
import carbonconfiglib.utils.structure.IStructuredData;
import carbonconfiglib.utils.structure.IStructuredData.StructureType;
import net.minecraft.util.text.ITextComponent;
import speiger.src.collections.objects.lists.ObjectArrayList;
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
public class CarbonValue implements IValueNode, IValueActions
{
	String nodeName;
	IReloadMode mode;
	ITextComponent name;
	ITextComponent tooltip;
	IEntrySettings settings;
	IRange range;
	DataType type;
	boolean forced;
	Supplier<List<Suggestion>> suggestions;
	
	Function<String, ParseResult<Boolean>> isValid;
	BiConsumer<String, IValueActions> saveAction;
	
	Stack<String> previous = new ObjectArrayList<>();
	String current;
	String defaultValue;
	String savedValue;
	boolean autosave;
	
	public CarbonValue(String nodeName, IReloadMode mode, ITextComponent name, ITextComponent tooltip, IEntrySettings settings, IStructuredData data, boolean forced, Supplier<List<Suggestion>> suggestions, String current, String defaultValue, Function<String, ParseResult<Boolean>> isValid, BiConsumer<String, IValueActions> saveAction) {
		this.nodeName = nodeName;
		this.mode = mode;
		this.name = name;
		this.tooltip = tooltip;
		this.settings = settings;
		this.type =  DataType.bySimple(data.asSimple());
		this.range = data.asSimple().getRange();
		this.forced = forced;
		this.suggestions = suggestions;
		this.isValid = isValid;
		this.saveAction = saveAction;
		this.current = current;
		this.savedValue = current;
		this.defaultValue = defaultValue;
		previous.push(current);
	}
	
	public CarbonValue setAutosave(boolean value) {
		autosave = value;
		return this;
	}

	public void save() {
		saveAction.accept(current, this);
		savedValue = current;
	}
	@Override
	public boolean isDefault() { return Objects.equals(defaultValue, current); }
	@Override
	public boolean isChanged() { return !Objects.equals(previous.top(), current); }
	@Override
	public boolean isUnsaved() { return !Objects.equals(savedValue, current); }
	@Override
	public void setDefault() {
		current = defaultValue; 
		if(autosave) save();
	}
	@Override
	public void setPrevious() {
		current = previous.top();
		if(previous.size() > 1) previous.pop();
		if(autosave) save();
	}
	@Override
	public void createTemp() { previous.push(current); }
	@Override
	public void deleteTempIfNeeded() {
		if(previous.size() > 1 && previous.top().equals(current)) {
			previous.pop();
		}
	}
	
	@Override
	public StructureType getNodeType() { return StructureType.SIMPLE; }
	@Override
	public IEntrySettings getSettings() { return settings; }
	@Override
	public IRange getRange() { return range; }
	@Override
	public ReloadMode getReloadState() { return mode instanceof ReloadMode ? (ReloadMode)mode : null; }
	@Override
	public String getNodeName() { return nodeName; }
	@Override
	public ITextComponent getName() { return name; }
	@Override
	public ITextComponent getTooltip() { return tooltip; }
	@Override
	public String getDefault() { return defaultValue; }
	@Override
	public String get() { return current; }
	@Override
	public void set(String value) {
		this.current = value;
		if(autosave) save();
	}
	@Override
	public ParseResult<Boolean> isValid(String value) { return isValid.apply(value); }
	@Override
	public DataType getDataType() { return type; }
	@Override
	public boolean isForcingSuggestions() { return forced; }
	@Override
	public List<Suggestion> getSuggestions() { return suggestions.get(); }
}
