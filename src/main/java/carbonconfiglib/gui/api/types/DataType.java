package carbonconfiglib.gui.api.types;

import java.util.Map;
import java.util.function.Function;

import carbonconfiglib.gui.api.node.IValueNode;
import carbonconfiglib.gui.nodes.BooleanElement;
import carbonconfiglib.gui.nodes.NumberElement.DoubleElement;
import carbonconfiglib.gui.nodes.NumberElement.FloatElement;
import carbonconfiglib.gui.nodes.NumberElement.IntegerElement;
import carbonconfiglib.gui.nodes.NumberElement.LongElement;
import carbonconfiglib.gui.nodes.SelectionElement;
import carbonconfiglib.gui.nodes.StringElement;
import carbonconfiglib.gui.nodes.base.BaseElement;
import carbonconfiglib.utils.structure.IStructuredData.EntryDataType;
import carbonconfiglib.utils.structure.IStructuredData.SimpleData;
import speiger.src.collections.objects.maps.impl.hash.Object2ObjectOpenHashMap;
import speiger.src.collections.objects.utils.maps.Object2ObjectMaps;

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
	public static final DataType BOOLEAN = new DataType("false", BooleanElement::new);
	public static final DataType INTEGER = new DataType("0", IntegerElement::new);
	public static final DataType LONG = new DataType("0", LongElement::new);
	public static final DataType FLOAT = new DataType("0.0", FloatElement::new);
	public static final DataType DOUBLE = new DataType("0.0", DoubleElement::new);
	public static final DataType STRING = new DataType("", StringElement::new);
	public static final DataType ENUM = new DataType("", SelectionElement::new);
	private static final Map<Class<?>, DataType> AUTO_DATA_TYPES = Object2ObjectMaps.synchronize(new Object2ObjectOpenHashMap<>());
	
	String defaultValue;
	Function<IValueNode, BaseElement> creatorFunction; 

	
	public DataType(String defaultValue, Function<IValueNode, BaseElement> creatorFunction) {
		this.defaultValue = defaultValue;
		this.creatorFunction = creatorFunction;
	}
	
	public BaseElement createElement(IValueNode node) {
		return creatorFunction.apply(node);
	}
	
	public String getDefaultValue() {
		return defaultValue;
	}
	
	public static DataType bySimple(SimpleData type) {
		return byConfig(type.getType(), type.getVariant());
	}
	
	public static DataType byConfig(EntryDataType type, Class<?> variant) {
		switch(type) {
			case BOOLEAN: return BOOLEAN;
			case INTEGER: return INTEGER;
			case LONG: return LONG;
			case FLOAT: return FLOAT;
			case DOUBLE: return DOUBLE;
			case STRING: return STRING;
			case ENUM: return ENUM;
			case CUSTOM: return byClass(variant);
			default: throw new IllegalStateException("Undefined DataType shouldn't be used");
		}
	}
	
	public static DataType byClass(Class<?> clz) {
		DataType result = AUTO_DATA_TYPES.get(clz);
		if(result == null) throw new IllegalStateException("Custom Type ["+clz.getSimpleName()+"] was defined, but the type wasn't registered in "+DataType.class.getSimpleName()+".class");
		return result;
	}
	
	public static void registerType(Class<?> clz, DataType type) {
		AUTO_DATA_TYPES.putIfAbsent(clz, type);
	}
}
