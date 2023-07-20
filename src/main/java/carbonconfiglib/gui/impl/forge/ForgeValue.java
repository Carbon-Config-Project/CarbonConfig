package carbonconfiglib.gui.impl.forge;

import java.util.List;
import java.util.Objects;

import com.electronwill.nightconfig.core.CommentedConfig;

import carbonconfiglib.gui.api.IValueNode;
import carbonconfiglib.utils.ParseResult;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraftforge.common.ForgeConfigSpec.ValueSpec;

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
public class ForgeValue implements IValueNode
{
	List<String> path;
	CommentedConfig config;
	ValueSpec spec;
	ForgeDataType<?> type;
	
	ObjectArrayList<String> previous = new ObjectArrayList<>();
	String current;
	String defaultValue;
	
	public ForgeValue(List<String> path, CommentedConfig config, ValueSpec spec, ForgeDataType<?> type) {
		this.path = path;
		this.config = config;
		this.spec = spec;
		this.type = type;
		loadData(type);
	}
	
	@SuppressWarnings("unchecked")
	<T> void loadData(ForgeDataType<T> type) {
		defaultValue = type.serialize((T)spec.getDefault());
		String starting = type.serialize(config.get(path));
		this.current = starting;
		previous.add(starting);
	}
	
	public void save() {
		config.set(path, type.parse(current).getValue()); 
	}
	
	@Override
	public String get() { return current; }
	@Override
	public void set(String value) { current = value; }
	@Override
	public ParseResult<Boolean> isValid(String value) {
		ParseResult<?> result = type.parse(value);
		return result.isValid() ? ParseResult.success(true) : result.withDefault(false);
	}
	@Override
	public boolean isDefault() { return Objects.equals(defaultValue, current); }
	@Override
	public boolean isChanged() { return !Objects.equals(previous.top(), current); }
	@Override
	public boolean isCompoundNode() { return false; }
	@Override
	public void setDefault() { current = defaultValue; }
	@Override
	public void setPrevious() {
		current = previous.top();
		if(previous.size() > 1) previous.pop();
	}
	@Override
	public void createTemp() { previous.push(current); }
	@Override
	public void apply() {
		if(previous.size() > 1) previous.pop();
	}
	
}
