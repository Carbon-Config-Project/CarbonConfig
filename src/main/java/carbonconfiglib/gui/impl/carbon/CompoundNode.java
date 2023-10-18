package carbonconfiglib.gui.impl.carbon;

import java.util.Arrays;
import java.util.List;

import carbonconfiglib.api.ISuggestionProvider.Suggestion;
import carbonconfiglib.config.ConfigEntry;
import carbonconfiglib.gui.api.DataType;
import carbonconfiglib.gui.api.ICompoundNode;
import carbonconfiglib.gui.api.IValueNode;
import carbonconfiglib.utils.Helpers;
import carbonconfiglib.utils.IEntryDataType.CompoundDataType;
import carbonconfiglib.utils.ParseResult;
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
public class CompoundNode implements ICompoundNode, ICompoundProvider
{
	ConfigEntry<?> entry;
	List<DataType> types;
	String[] names;
	CompoundDataType type;
	List<IValueNode> values = new ObjectArrayList<>();
	ObjectArrayList<String[]> previous = new ObjectArrayList<>();
	String[] current;
	String[] defaultValues;
	
	public CompoundNode(ConfigEntry<?> entry, List<DataType> types, String[] names, CompoundDataType type) {
		this.entry = entry;
		this.types = types;
		this.names = names;
		this.type = type;
		String[] value = Helpers.splitArray(entry.serialize(), ";");
		current = value;
		previous.push(Arrays.copyOf(value, value.length));
		defaultValues = Helpers.splitArray(entry.serializeDefault(), ";");
		reload();
	}
	
	public void save() { entry.deserializeValue(Helpers.mergeCompound(current)); }
	
	private void reload() {
		values.clear();
		for(int i = 0,m=types.size();i<m;i++) {
			values.add(new CompoundValue(types.get(i), i, this, current, i >= defaultValues.length ? "" : defaultValues[i]));
		}
	}
	
	@Override
	public Component getName(int index) {
		return Component.literal(names[index]);
	}
	
	@Override
	public boolean isForcedSuggestion(int index) {
		return type.isForcedSuggestion(names[index]);
	}

	@Override
	public List<Suggestion> getValidValues(int index) {
		return type.getSuggestions(names[index], T -> isValid(T.getValue(), index).isValid());
	}
	
	@Override
	public void set(String value) {
		current = Helpers.splitArray(value, ";");
	}
	
	@Override
	public ParseResult<Boolean> isValid(String value) {
		return entry.canSetValue(value);
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
		return entry.canSetValue(Helpers.mergeCompound(current)).getValue();
	}
	
	@Override
	public ParseResult<Boolean> isValid(String value, int index) {
		return entry.canSetValue(buildValue(value, index));
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
	}
	@Override
	public List<IValueNode> getValues() { return values; }
	
	public static class CompoundValue implements IValueNode {
		DataType type;
		int index;
		ICompoundProvider compound;
		String[] values;
		String defaultValue;
		ObjectArrayList<String> startingValue = new ObjectArrayList<>();
		
		public CompoundValue(DataType type, int index, ICompoundProvider compound, String[] values, String defaultValue) {
			this.type = type;
			this.index = index;
			this.compound = compound;
			this.values = values;
			this.startingValue.push(values[index]);
			this.defaultValue = defaultValue;
		}
		
		@Override
		public boolean isDefault() {
			return values[index].equals(defaultValue);
		}
		
		@Override
		public boolean isChanged() {
			return !values[index].equals(startingValue.top());
		}
		
		@Override
		public void setDefault() { 
			values[index] = defaultValue; 
		}
		
		@Override
		public void setPrevious() {
			values[index] = startingValue.top();
			if(startingValue.size() > 1) startingValue.pop();
		}
		
		@Override
		public void createTemp() {
			startingValue.push(values[index]);
		}
		
		@Override
		public void apply() {
			if(startingValue.size() > 1) startingValue.pop();
		}
		
		@Override
		public String get() {
			return values[index];
		}
		
		@Override
		public void set(String value) {
			values[index] = value;
		}
		
		@Override
		public ParseResult<Boolean> isValid(String value) {
			return compound.isValid(value, index);
		}
	}
}
