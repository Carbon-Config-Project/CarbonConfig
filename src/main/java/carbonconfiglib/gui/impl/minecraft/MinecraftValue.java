package carbonconfiglib.gui.impl.minecraft;

import java.util.Objects;

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
public class MinecraftValue implements IValueNode
{
	ObjectArrayList<String> previous = new ObjectArrayList<>();
	IGameRuleValue entry;
	String defaultValue;
	String current;
	
	public MinecraftValue(IGameRuleValue entry) {
		this.entry = entry;
		this.defaultValue = entry.getDefault(); 
		this.current = entry.get();
		this.previous.push(current);
	}
	
	public void save() { entry.set(current); }
	
	@Override
	public boolean isDefault() { return Objects.equals(current, defaultValue); }
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
	
	@Override
	public String get() { return current; }
	@Override
	public void set(String value) { this.current = value; }
	@Override
	public ParseResult<Boolean> isValid(String value) { return entry.isValid(value); }
}
