package carbonconfiglib.gui.api;

import java.util.Map;
import java.util.function.BiFunction;

import carbonconfiglib.gui.config.BooleanElement;
import carbonconfiglib.gui.config.ConfigElement;
import carbonconfiglib.gui.config.NumberElement;
import carbonconfiglib.gui.config.StringElement;
import carbonconfiglib.utils.IEntryDataType.EntryDataType;
import carbonconfiglib.utils.IEntryDataType.SimpleDataType;
import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

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
public class DataType
{
	public static final DataType BOOLEAN = new DataType(false, "false", BooleanElement::new, BooleanElement::new);
	public static final DataType INTEGER = new DataType(false, "0", NumberElement::new, NumberElement::new);
	public static final DataType DOUBLE = new DataType(false, "0.0", NumberElement::new, NumberElement::new);
	public static final DataType STRING = new DataType(true, " ", StringElement::new, StringElement::new);
	private static final Map<Class<?>, DataType> AUTO_DATA_TYPES = Object2ObjectMaps.synchronize(new Object2ObjectOpenHashMap<>());
	
	boolean allowsEmptyValue;
	String defaultValue;
	BiFunction<IConfigNode, IValueNode, ConfigElement> creator;
	IArrayFunction arrayCreator;
	
	public DataType(boolean allowsEmptyValue, String defaultValue, BiFunction<IConfigNode, IValueNode, ConfigElement> creator, IArrayFunction arrayCreator) {
		this.allowsEmptyValue = allowsEmptyValue;
		this.defaultValue = defaultValue;
		this.creator = creator;
		this.arrayCreator = arrayCreator;
	}
	
	public ConfigElement create(IConfigNode node) {
		return creator.apply(node, node.asValue());
	}
	
	public ConfigElement create(IConfigNode node, IValueNode value) {
		return creator.apply(node, value);
	}
	
	public ConfigElement create(IConfigNode node, IArrayNode array, int index) {
		return arrayCreator.create(node, array, index);
	}
	
	public String getDefaultValue() {
		return defaultValue;
	}
	
	public boolean isAllowEmptyValue() {
		return allowsEmptyValue;
	}
	
	public static DataType bySimple(SimpleDataType type) {
		return byConfig(type.getType(), type.getVariant());
	}
	
	public static DataType byConfig(EntryDataType type, Class<?> variant) {
		switch(type) {
			case BOOLEAN: return BOOLEAN;
			case INTEGER: return INTEGER;
			case DOUBLE: return DOUBLE;
			case STRING: return STRING;
			case CUSTOM: return byClass(variant);
			default: throw new IllegalStateException("Undefined DataType shouldn't be used");
		}
	}
	
	public static DataType byClass(Class<?> clz) {
		return AUTO_DATA_TYPES.getOrDefault(clz, STRING);
	}
	
	public static void registerType(Class<?> clz, DataType type) {
		AUTO_DATA_TYPES.putIfAbsent(clz, type);
	}
	
	public static interface IArrayFunction
	{
		public ConfigElement create(IConfigNode node, IArrayNode array, int index);
	}
}
