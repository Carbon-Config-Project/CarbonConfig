package carbonconfiglib.gui.impl.forge;

import java.util.Objects;

import carbonconfiglib.gui.api.IValueNode;
import carbonconfiglib.impl.entries.ColorValue.ColorWrapper;
import carbonconfiglib.utils.Helpers;
import carbonconfiglib.utils.ParseResult;
import cpw.mods.fml.common.Loader;
import net.minecraftforge.common.config.Property;
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
public class ForgeValue implements IValueNode
{
	ObjectArrayList<String> previous = new ObjectArrayList<>();
	String current;
	String defaultValue;
	Property entry;
	
	public ForgeValue(Property entry) {
		this.entry = entry;
		String start = entry.getString();
		previous.push(start);
		current = start;
		defaultValue = entry.getDefault();
	}

	public void save() { entry.set(current); }
	
	@Override
	public String get() { return current; }
	@Override
	public void set(String value) { current = value; }
	@Override
	public ParseResult<Boolean> isValid(String value) {
		switch(entry.getType()) {
			case BOOLEAN: return ParseResult.success(true);
			case COLOR: return validate(ColorWrapper.parseInt(value));
			case DOUBLE: return validate(Helpers.parseDouble(value));
			case INTEGER: return validate(Helpers.parseInt(value));
			case MOD_ID: return ParseResult.result(Loader.instance().getIndexedModList().containsKey(value), NullPointerException::new, "Mod ["+value+"] isn't a thing");
			case STRING: return ParseResult.success(true);
			default: return ParseResult.success(true);
		}
	}
	private ParseResult<Boolean> validate(ParseResult<?> value) {
		return value.hasError() ? value.withDefault(false) : ParseResult.success(true);
	}
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
