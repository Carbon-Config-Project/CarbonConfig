package carbonconfiglib.gui.impl.carbon;

import java.util.List;

import com.google.common.base.Objects;

import carbonconfiglib.config.ConfigEntry;
import carbonconfiglib.config.ConfigEntry.IArrayConfig;
import carbonconfiglib.gui.api.DataType;
import carbonconfiglib.gui.api.IArrayNode;
import carbonconfiglib.gui.api.INode;
import carbonconfiglib.gui.api.IValueNode;
import carbonconfiglib.utils.ParseResult;
import speiger.src.collections.objects.lists.ObjectArrayList;
import speiger.src.collections.objects.utils.ObjectLists;

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
public class ArrayNode implements IArrayNode
{
	List<WrappedEntry> values = new ObjectArrayList<>();
	ConfigEntry<?> entry;
	IArrayConfig config;
	DataType type;
	ObjectArrayList<List<String>> previous = new ObjectArrayList<>();
	List<String> currentValues;
	List<String> defaults;
	
	public ArrayNode(ConfigEntry<?> entry, IArrayConfig config, DataType type) {
		this.entry = entry;
		this.config = config;
		this.type = type;
		previous.push(config.getEntries());
		currentValues = config.getEntries();
		defaults = config.getDefaults();
		reload();
	}
	
	public void save() { config.setArray(currentValues); }
	
	protected void reload() {
		values.clear();
		for(int i = 0;i<currentValues.size();i++) {
			values.add(new WrappedEntry(this, config, currentValues, i, i >= defaults.size() ? null : defaults.get(i)));
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
	public IValueNode get(int index) {
		return values.get(index);
	}
	
	@Override
	public int indexOf(INode value) {
		return values.indexOf(value);
	}
	
	@Override
	public void createNode() {
		currentValues.add(type.getDefaultValue());
		values.add(new WrappedEntry(this, config, currentValues, values.size(), null));
	}
	
	@Override
	public void removeNode(int index) {
		values.remove(index);
		currentValues.remove(index);
	}
	
	public static class WrappedEntry implements IValueNode {
		IArrayNode owner;
		IArrayConfig array;
		List<String> values;
		String defaultValue;
		ObjectArrayList<String> previous = new ObjectArrayList<>();
		
		public WrappedEntry(IArrayNode owner, IArrayConfig array, List<String> values, int index, String defaultValue) {
			this.owner = owner;
			this.array = array;
			this.values = values;
			this.defaultValue = defaultValue;
			this.previous.add(values.get(index));
		}
		
		private int getIndex() {
			return owner.indexOf(this);
		}
		
		@Override
		public String get() {
			return values.get(getIndex());
		}
		
		@Override
		public void set(String value) {
			values.set(getIndex(), value);
		}

		@Override
		public ParseResult<Boolean> isValid(String value) {
			return array.canSetArray(ObjectLists.singleton(value));
		}
		
		@Override
		public boolean isDefault() {
			return defaultValue == null || Objects.equal(defaultValue, values.get(getIndex()));
		}

		@Override
		public boolean isChanged() {
			return !Objects.equal(previous.top(), values.get(getIndex()));
		}
		
		@Override
		public void setDefault() {
			values.set(getIndex(), defaultValue == null ? "" : defaultValue);
		}
		
		@Override
		public void setPrevious() {
			values.set(getIndex(), previous.top());
			if(previous.size() > 1) previous.pop();
		}

		@Override
		public void createTemp() {
			previous.push(values.get(getIndex()));
		}

		@Override
		public void apply() {
			if(previous.size() > 1) previous.pop();
		}
	}
}