package carbonconfiglib.gui.api;

import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

import carbonconfiglib.gui.config.BooleanElement;
import carbonconfiglib.gui.config.ConfigElement;
import carbonconfiglib.gui.config.EnumElement;
import carbonconfiglib.gui.config.NumberElement;
import carbonconfiglib.gui.config.StringElement;
import carbonconfiglib.utils.structure.IStructuredData;
import carbonconfiglib.utils.structure.IStructuredData.EntryDataType;
import carbonconfiglib.utils.structure.IStructuredData.SimpleData;
import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import speiger.src.collections.objects.utils.ObjectIterables;
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
public class DataType
{
	public static final DataType BOOLEAN = new DataType(false, "false", BooleanElement::new, BooleanElement::new, BooleanElement::new);
	public static final DataType INTEGER = new DataType(false, "0", NumberElement::new, NumberElement::new, NumberElement::new);
	public static final DataType DOUBLE = new DataType(false, "0.0", NumberElement::new, NumberElement::new, NumberElement::new);
	public static final DataType STRING = new DataType(true, " ", StringElement::new, StringElement::new, StringElement::new);
	public static final DataType ENUM = new DataType(true, " ", EnumElement::new, EnumElement::new, EnumElement::new);
	private static final Map<Class<?>, DataType> AUTO_DATA_TYPES = Object2ObjectMaps.synchronize(new Object2ObjectOpenHashMap<>());
	
	boolean allowsEmptyValue;
	String defaultValue;
	Function<IValueNode, ConfigElement> creator;
	BiFunction<IArrayNode, IValueNode, ConfigElement> arrayCreator;
	BiFunction<ICompoundNode, IValueNode, ConfigElement> compoundCreator;
	
	public DataType(boolean allowsEmptyValue, String defaultValue, Function<IValueNode, ConfigElement> creator, BiFunction<IArrayNode, IValueNode, ConfigElement> arrayCreator, BiFunction<ICompoundNode, IValueNode, ConfigElement> compoundCreator) {
		this.allowsEmptyValue = allowsEmptyValue;
		this.defaultValue = defaultValue;
		this.creator = creator;
		this.arrayCreator = arrayCreator;
		this.compoundCreator = compoundCreator;
	}
	
	public ConfigElement create(IValueNode node) {
		return creator.apply(node);
	}
	
	public ConfigElement create(IArrayNode array, IValueNode node) {
		return arrayCreator.apply(array, node);
	}
	
	public ConfigElement create(ICompoundNode compound, IValueNode node) {
		return compoundCreator.apply(compound, node);
	}
		
	public String getDefaultValue() {
		return defaultValue;
	}
	
	public boolean isAllowEmptyValue() {
		return allowsEmptyValue;
	}
	
	public static List<DataType> resolve(IStructuredData data) {
		switch(data.getDataType()) {
			case COMPOUND: return ObjectIterables.flatMap(data.asCompound().getFormat().values(), DataType::resolve).pourAsList();
			case LIST: return resolve(data.asList().getType());
			case SIMPLE: return ObjectLists.singleton(bySimple(data.asSimple()));
			default: return ObjectLists.empty();
		}
	}
	
	public static DataType bySimple(SimpleData type) {
		return byConfig(type.getType(), type.getVariant());
	}
	
	public static DataType byConfig(EntryDataType type, Class<?> variant) {
		switch(type) {
			case BOOLEAN: return BOOLEAN;
			case INTEGER: return INTEGER;
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
	
	public static interface IArrayFunction
	{
		public ConfigElement create(IConfigNode node, IArrayNode array, int index);
	}
}
