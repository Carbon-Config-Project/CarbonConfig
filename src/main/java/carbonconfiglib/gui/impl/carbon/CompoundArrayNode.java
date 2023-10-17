package carbonconfiglib.gui.impl.carbon;

import java.util.Arrays;
import java.util.List;

import carbonconfiglib.api.ISuggestionProvider.Suggestion;
import carbonconfiglib.config.ConfigEntry;
import carbonconfiglib.config.ConfigEntry.IArrayConfig;
import carbonconfiglib.gui.api.*;
import carbonconfiglib.gui.impl.carbon.CompoundNode.CompoundValue;
import carbonconfiglib.utils.Helpers;
import carbonconfiglib.utils.IEntryDataType.CompoundDataType;
import carbonconfiglib.utils.ParseResult;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectLists;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;

import java.util.Arrays;
import java.util.List;

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
public class CompoundArrayNode implements IArrayNode
{
	ConfigEntry<?> entry;
	IArrayConfig config;
	String[] names;
	List<DataType> dataTypes;
	CompoundDataType type;
	List<ICompoundNode> values = new ObjectArrayList<>();
	ObjectArrayList<List<String>> previous = new ObjectArrayList<>();
	List<String> currentValues;
	List<String> defaults;
	String defaultEmptyValue;
	
	public CompoundArrayNode(ConfigEntry<?> entry, IArrayConfig config, List<DataType> dataTypes, String[] names, CompoundDataType type) {
		this.entry = entry;
		this.config = config;
		this.dataTypes = dataTypes;
		this.names = names;
		this.type = type;
		previous.push(config.getEntries());
		currentValues = config.getEntries();
		defaults = config.getDefaults();
		String[] data = new String[dataTypes.size()];
		for(int i = 0,m=dataTypes.size();i<m;i++) {
			data[i] = dataTypes.get(i).getDefaultValue();
		}
		defaultEmptyValue = Helpers.mergeCompound(data);
	}
	
	public void save() { config.setArray(currentValues); }

	
	protected void reload() {
		values.clear();
		for(int i = 0;i<currentValues.size();i++) {
			values.add(new CompoundEntry(this, config, currentValues, i >= defaults.size() ? defaultEmptyValue : defaults.get(i), i, dataTypes, names, type));
		}
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
		swapValues(index, index+1);
	}
	
	@Override
	public void moveUp(int index) {
		swapValues(index, index-1);
	}
	
	private void swapValues(int from, int to) {
		if(from >= values.size() || from < 0) return;
		if(to >= values.size() || to < 0) return;
		currentValues.set(from, currentValues.set(to, currentValues.get(from)));
		reload();
	}
	
	@Override
	public void createTemp() {
		previous.push(new ObjectArrayList<>(currentValues));
		reload();
	}

	@Override
	public void apply() {
		if(previous.size() > 1) previous.pop();
	}
	
	@Override
	public int size() {
		return values.size();
	}
	
	@Override
	public ICompoundNode get(int index) {
		return values.get(index);
	}
	
	@Override
	public int indexOf(INode value) {
		return values.indexOf(value);
	}
	
	@Override
	public void createNode() {
		currentValues.add(defaultEmptyValue);
		values.add(new CompoundEntry(this, config, currentValues, defaultEmptyValue, values.size(), dataTypes, names, type));
	}
	
	@Override
	public void removeNode(int index) {
		values.remove(index);
		currentValues.remove(index);
	}
	
	public static class CompoundEntry implements ICompoundNode, ICompoundProvider {
		IArrayConfig config;
		IArrayNode node;
		List<DataType> types;
		String[] names;
		CompoundDataType type;
		List<IValueNode> values = new ObjectArrayList<>();
		
		List<String> results;
		ObjectArrayList<String[]> previous = new ObjectArrayList<>();
		String[] current;
		String[] defaultValues;
		
		public CompoundEntry(IArrayNode node, IArrayConfig config, List<String> values, String defaultValue, int index, List<DataType> types, String[] names, CompoundDataType type) {
			this.node = node;
			this.config = config;
			this.types = types;
			this.results = values;
			this.names = names;
			this.type = type;
			String[] value = Helpers.splitArray(values.get(index), ";");
			current = value;
			previous.push(Arrays.copyOf(value, value.length));
			this.defaultValues = Helpers.splitArray(defaultValue, ";");
			reload();
		}
		
		public void reload() {
			values.clear();
			for(int i = 0,m=types.size();i<m;i++) {
				values.add(new CompoundValue(types.get(i), i, this, current, i >= defaultValues.length ? "" : defaultValues[i]));
			}
		}
		
		@Override
		public void set(String value) {
			current = Helpers.splitArray(value, ";");
		}
		
		@Override
		public ParseResult<Boolean> isValid(String value) {
			return config.canSetArray(ObjectLists.singleton(value));
		}
		
		public String get() {
			return Helpers.mergeCompound(current);
		}
		
		private String[] getPrev() {
			return previous.top();
		}
		
		@Override
		public boolean isDefault() {
			return Arrays.equals(defaultValues, current);
		}
		
		@Override
		public boolean isChanged() {
			return !Arrays.equals(getPrev(), current);
		}
		
		@Override
		public void setDefault() {
			current = Arrays.copyOf(defaultValues, defaultValues.length);
		}
		
		@Override
		public void setPrevious() {
			String[] prev = getPrev();
			current = Arrays.copyOf(prev, prev.length);
			if(previous.size() > 1) previous.pop();
		}
		
		@Override
		public boolean isValid() {
			return config.canSetArray(ObjectLists.singleton(Helpers.mergeCompound(current))).getValue();
		}
		
		@Override
		public ParseResult<Boolean> isValid(String value, int index) {
			return canSkip(value, index) ? ParseResult.success(true) : config.canSetArray(ObjectLists.singleton(buildValue(value, index)));
		}
		
		private boolean canSkip(String newValue, int index) {
			if(!types.get(index).isAllowEmptyValue() && newValue.trim().isEmpty()) return true;
			for(int i = 0,m=current.length;i<m;i++) {
				if(i == index) continue;
				if(!types.get(index).isAllowEmptyValue() && current[i].trim().isEmpty()) return true;
			}
			return false;
		}
		
		private String buildValue(String value, int index) {
			String[] copy = Arrays.copyOf(current, current.length);
			copy[index] = value;
			return Helpers.mergeCompound(copy);
		}
		
		@Override
		public void createTemp() {
			previous.push(Arrays.copyOf(current, current.length));
			reload();
		}
		
		@Override
		public void apply() {
			if(previous.size() > 1) previous.pop();
			results.set(this.node.indexOf(this), Helpers.mergeCompound(current));
		}
		
		@Override
		public List<IValueNode> getValues() {
			return values;
		}
		
		@Override
		public Component getName(int index) {
			return new TextComponent(names[index]);
		}

		@Override
		public boolean isForcedSuggestion(int index) {
			return type.isForcedSuggestion(names[index]);
		}

		@Override
		public List<Suggestion> getValidValues(int index) {
			return type.getSuggestions(names[index], T -> isValid(T.getValue(), index).isValid());
		}
		
	}
}