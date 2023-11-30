package carbonconfiglib.gui.impl.carbon;

import java.util.Objects;

import carbonconfiglib.config.ConfigEntry;
import carbonconfiglib.gui.api.IValueNode;
import carbonconfiglib.utils.ParseResult;
import speiger.src.collections.objects.lists.ObjectArrayList;

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
public class ValueNode implements IValueNode
{
	ObjectArrayList<String> previous = new ObjectArrayList<>();
	String current;
	String defaultValue;
	ConfigEntry<?> entry;
	
	public ValueNode(ConfigEntry<?> entry) {
		this.entry = entry;
		String start = entry.serialize();
		previous.push(start);
		current = start;
		defaultValue = entry.serializeDefault();
	}
	
	public void save() { entry.deserializeValue(current); }
	
	@Override
	public String get() { return current; }
	@Override
	public void set(String value) { current = value; }
	@Override
	public ParseResult<Boolean> isValid(String value) { return entry.canSetValue(value); }
	@Override
	public boolean isDefault() { return Objects.equals(defaultValue, current); }
	@Override
	public boolean isChanged() { return !Objects.equals(previous.top(), current); }
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