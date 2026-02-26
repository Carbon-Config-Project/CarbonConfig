package carbonconfiglib.gui.impl.carbon;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

import carbonconfiglib.api.IEntrySettings;
import carbonconfiglib.api.IReloadMode;
import carbonconfiglib.api.ISuggestionProvider.Suggestion;
import carbonconfiglib.gui.api.node.ICompoundNode;
import carbonconfiglib.gui.api.node.IConfigNode;
import carbonconfiglib.gui.api.node.INode;
import carbonconfiglib.impl.ReloadMode;
import carbonconfiglib.utils.Helpers;
import carbonconfiglib.utils.ParseResult;
import carbonconfiglib.utils.structure.IStructuredData;
import carbonconfiglib.utils.structure.IStructuredData.StructureType;
import carbonconfiglib.utils.structure.StructureCompound.CompoundData;
import net.minecraft.ChatFormatting;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import speiger.src.collections.objects.lists.ObjectArrayList;
import speiger.src.collections.objects.maps.interfaces.Object2ObjectMap;
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
public class CarbonCompound implements ICompoundNode, IValueActions
{
	String nodeName;
	IReloadMode mode;
	CompoundData data;
	Component name;
	Component tooltip;
	Function<String, ParseResult<Boolean>> isValid;
	Supplier<List<Suggestion>> suggestions;
	BiConsumer<String, IValueActions> saveAction;
	
	Map<String, String> validationTemp = Object2ObjectMap.builder().linkedMap();
	List<IValueActions> values = new ObjectArrayList<>();
	Stack<Map<String, String>> previous = new ObjectArrayList<>();
	Map<String, String> current = Object2ObjectMap.builder().linkedMap();
	Map<String, String> savedValues = Object2ObjectMap.builder().linkedMap();
	Map<String, String> defaultValue = Object2ObjectMap.builder().linkedMap();
	boolean autosave;
	
	public CarbonCompound(String nodeName, IReloadMode mode, CompoundData data, Component name, Component tooltip, String value, String defaultValue, Function<String, ParseResult<Boolean>> isValid, Supplier<List<Suggestion>> suggestions, BiConsumer<String, IValueActions> saveAction) {
		this.nodeName = nodeName;
		this.mode = mode;
		this.data = data;
		this.name = name;
		this.current.putAll(Helpers.splitArguments(Helpers.splitCompound(value), data.getKeys(), true));
		this.defaultValue.putAll(Helpers.splitArguments(Helpers.splitCompound(defaultValue), data.getKeys(), true));
		this.savedValues.putAll(current);
		this.previous.push(Object2ObjectMap.builder().linkedMap(current));
		this.tooltip = tooltip;
		this.isValid = isValid;
		this.suggestions = suggestions;
		this.saveAction = saveAction;
		reload();
	}
	
	public CarbonCompound setAutosave(boolean value) {
		autosave = value;
		return this;
	}
	
	private void autosave() {
		if(!autosave) return;
		save();
	}
	
	private void reload() {
		values.clear();
		Map<String, IStructuredData> structure = data.getFormat();
		for(Map.Entry<String, String> entry : current.entrySet()) {
			String key = entry.getKey();
			values.add(addEntry(Helpers.removeLayer(entry.getValue(), 0), Helpers.removeLayer(defaultValue.getOrDefault(key, ""), 0), structure.get(key), key, data.getTranslationKey(key)));
		}
		autosave();
	}
	
	protected IValueActions addEntry(String value, String defaultValue, IStructuredData type, String key, String translationKey) {
		switch(type.getDataType()) {
			case COMPOUND: return new CarbonCompound(key, mode, type.asCompound(), IConfigNode.createLabel(key, translationKey), createTooltip(key), value, defaultValue, T -> isValid(key, T), () -> data.getSuggestions(key, this::isSuggestionValid), (T, V) -> save(key, T)).setAutosave(true);
			case LIST: return new CarbonArray(key, mode, type.asList(), IConfigNode.createLabel(key, translationKey), createTooltip(key), value, defaultValue, T -> isValid(key, T), () -> data.getSuggestions(key, this::isSuggestionValid), (T, V) -> save(key, T)).setAutosave(true);
			case SIMPLE: return new CarbonValue(mode, IConfigNode.createLabel(key, translationKey), createTooltip(key), data.getEntrySetting(key), type, data.isForcedSuggestion(key), () -> data.getSuggestions(key, this::isSuggestionValid), value, defaultValue, T -> isValid(key, T), (T, V) -> save(key, T)).setAutosave(true);
			default: return null;
		}
	}
	
	private Map<String, String> getPrev() {
		return previous.top();
	}
	
	protected void save(String key, String value) {
		current.put(key, value);
		autosave();
	}
	
	protected boolean isSuggestionValid(String key, String value) {
		return isValid(key, value).getValue();
	}
	
	protected ParseResult<Boolean> isValid(String key, String value) {
		validationTemp.clear();
		validationTemp.putAll(current);
		validationTemp.put(key, value);
		return isValid.apply(Helpers.mergeCompound(validationTemp, false, 0));
	}
	
	private Component createTooltip(String key) {
		MutableComponent comp = Component.empty();
		String entryKey = data.getTranslationComment(key);
		if(entryKey != null && I18n.exists(entryKey)) {
			comp.append("\n").append(Component.translatable(entryKey).withStyle(ChatFormatting.GRAY));
		}
		else {
			String[] array = data.getComments(key);
			if(array != null && array.length > 0) {
				for(int i = 0;i<array.length;comp.append("\n").append(array[i++]).withStyle(ChatFormatting.GRAY));
			}
		}
		return comp;
	}
	
	@Override
	public void save() {
		saveAction.accept(Helpers.mergeCompound(current, false, 0), this); 
		savedValues.clear();
		savedValues.putAll(current);
	}
	@Override
	public boolean isDefault() { return Objects.equals(defaultValue, current); }
	@Override
	public boolean isChanged() { return !Objects.equals(getPrev(), current); }
	@Override
	public boolean isUnsaved() { return !Objects.equals(savedValues, current); }
	@Override
	public void setDefault() {
		current.clear();
		current.putAll(defaultValue);
		reload();
	}
	
	@Override
	public void setPrevious() {
		current.clear();
		current.putAll(getPrev());
		if(previous.size() > 1) previous.pop();
		reload();
	}
	
	@Override
	public void createTemp() {
		previous.push(Object2ObjectMap.builder().linkedMap(current));
		reload();
	}
	
	@Override
	public void deleteTempIfNeeded() {
		if(previous.size() > 1 && getPrev().equals(current)) {
			previous.pop();
		}
	}
	
	@Override
	public void apply() {
		boolean last = autosave;
		autosave = false;
		if(previous.size() > 1) previous.pop();
		for(int i = 0,m=values.size();i<m;i++) {
			values.get(i).save();
		}
		autosave = last;
		autosave();
	}
	
	@Override
	public boolean isValid() {
		return isValid.apply(get()).getValue();
	}
	
	@Override
	public String get() { return Helpers.mergeCompound(current, false, 0); }
	
	@Override
	public void set(String value) {
		current.clear();
		current.putAll(Helpers.splitArguments(Helpers.splitCompound(value), data.getKeys(), true));
		autosave();
	}

	@Override
	public List<? extends INode> getValues() { return values; }
	@Override
	public StructureType getNodeType() { return StructureType.COMPOUND; }
	@Override
	public IEntrySettings getSettings() { return data.getSettings(); }
	@Override
	public boolean requiresRestart() { return mode == ReloadMode.GAME; }
	@Override
	public boolean requiresReload() { return mode == ReloadMode.WORLD; }
	@Override
	public String getNodeName() { return nodeName; }
	@Override
	public Component getName() { return name; }
	@Override
	public Component getTooltip() { return tooltip; }
}
