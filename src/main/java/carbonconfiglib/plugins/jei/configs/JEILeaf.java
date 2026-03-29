package carbonconfiglib.plugins.jei.configs;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import carbonconfiglib.api.IRange;
import carbonconfiglib.api.IRange.IntegerRange;
import carbonconfiglib.gui.api.node.IConfigNode;
import carbonconfiglib.gui.api.node.INode;
import carbonconfiglib.gui.api.types.DataType;
import carbonconfiglib.impl.ReloadMode;
import carbonconfiglib.utils.structure.IStructuredData.StructureType;
import mezz.jei.api.runtime.config.IJeiConfigListValueSerializer;
import mezz.jei.api.runtime.config.IJeiConfigValue;
import mezz.jei.api.runtime.config.IJeiConfigValueSerializer;
import mezz.jei.common.config.file.serializers.BooleanSerializer;
import mezz.jei.common.config.file.serializers.EnumSerializer;
import mezz.jei.common.config.file.serializers.IntegerSerializer;
import mezz.jei.library.config.serializers.ChatFormattingSerializer;
import mezz.jei.library.config.serializers.ColorNameSerializer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import speiger.src.collections.objects.maps.impl.hash.Object2ObjectOpenHashMap;

/**
 * Copyright 2026 Speiger, Meduris
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
public class JEILeaf implements IConfigNode
{
	public static final Map<Class<?>, DataType> KNOWN_TYPES = createTypes();
	IJeiConfigValue<?> entry;
	IJeiConfigValueSerializer<?> serializer;
	DataType type;
	boolean isArray;
	JEIArray array;
	JEIValue value;

	public JEILeaf(IJeiConfigValue<?> leaf) {
		this.entry = leaf;
		this.serializer = leaf.getSerializer();
		this.isArray = serializer instanceof IJeiConfigListValueSerializer;
		this.type = KNOWN_TYPES.getOrDefault(isArray ? ((IJeiConfigListValueSerializer<?>)serializer).getListValueSerializer().getClass() : serializer.getClass(), DataType.STRING);
	}
	
	@Override
	public List<IConfigNode> getChildren() { return null; }
	
	@Override
	public INode asNode() {
		if(isArray) {
			if(array == null) array = new JEIArray(entry.getName(), getName(), getTooltip(), null, getRange(), type, JEIHelpers.getArrayValue(entry), JEIHelpers.getArrayDefault(entry), () -> JEIHelpers.getSuggestions(serializer), T -> JEIHelpers.parse(T, serializer), this::save);
			return array;
		}
		if(value == null) value = new JEIValue(entry.getName(), getName(), getTooltip(), null, getRange(), type, JEIHelpers.getValue(entry), JEIHelpers.getDefault(entry), () -> JEIHelpers.getSuggestions(serializer), T -> JEIHelpers.parse(T, serializer), (K, V) -> save(K, V, entry));
		return value;
	}
	
	private IRange getRange() {
		if(serializer instanceof IRange range) {
			return range;
		}
		if(type == DataType.INTEGER && serializer instanceof IntegerSerializer integer) {
			try
			{
				int min = getValue(IntegerSerializer.class, integer, "min");
				int max = getValue(IntegerSerializer.class, integer, "max");
				return new IntegerRange(min, max);
			}
			catch(Exception e) { e.printStackTrace(); }
		}
		return null;
	}
	
	private <T> int getValue(Class<T> clz, T instance, String fieldName) throws Exception{
		Field field = clz.getDeclaredField(fieldName);
		field.setAccessible(true);
		return field.getInt(instance);
	}
	
	@SuppressWarnings("unchecked")
	private <T> void save(List<String> result) {
		JEIHelpers.save(result, (IJeiConfigValue<List<T>>)entry);
	}
	
	private <T> void save(String value, JEIValue entry, IJeiConfigValue<T> result) {
		JEIHelpers.parseValue(value, result.getSerializer(), result::set);
	}
	
	@Override
	public StructureType getDataStructure() { return isArray ? StructureType.LIST : StructureType.SIMPLE; }
	@Override
	public boolean isLeaf() { return true; }
	@Override
	public boolean isRoot() { return false; }
	@Override
	public boolean isDefault() {
		if(value != null && value.isDefault()) return true;
		if(array != null && array.isDefault()) 	return true;
		return Objects.equals(JEIHelpers.getValue(entry), JEIHelpers.getDefault(entry));
	}
	
	@Override
	public boolean isChanged() {
		if(value != null && value.isChanged()) return true;
		if(array != null && array.isChanged()) 	return true;
		return false;
	}
	
	@Override
	public boolean isUnsaved() {
		if(value != null && value.isUnsaved()) return true;
		if(array != null && array.isUnsaved()) 	return true;
		return false;
	}
	
	@Override
	public void save() {
		if(value != null) value.save();
		if(array != null) array.save();
	}
	
	@Override
	public void setPrevious() {
		if(value != null) value.setPrevious();
		if(array != null) array.setPrevious();
	}
	
	@Override
	public void setDefault() {
		asNode();
		if(isArray) array.setDefault();
		else value.setDefault();
	}
	
	@Override
	public ReloadMode getReloadState() { return null; }
	@Override
	public String getNodeName() { return null; }
	@Override
	public Component getName() { return entry.getLocalizedName(); }
	@Override
	public Component getTooltip() {
		MutableComponent comp = Component.empty();
		if(entry.getLocalizedDescription() != null) comp.append(entry.getLocalizedDescription().copy().withStyle(ChatFormatting.GRAY)).append("\n");
		IJeiConfigValueSerializer<?> serializer = isArray ? ((IJeiConfigListValueSerializer<?>)this.serializer).getListValueSerializer() : this.serializer;
		comp.append(Component.literal(serializer.getValidValuesDescription()).withStyle(ChatFormatting.BLUE));
		return comp;
	}
	
	private static Map<Class<?>, DataType> createTypes() {
		Map<Class<?>, DataType> types = new Object2ObjectOpenHashMap<>();
		types.put(BooleanSerializer.class, DataType.BOOLEAN);
		types.put(IntegerSerializer.class, DataType.INTEGER);
		types.put(ChatFormattingSerializer.INSTANCE.getListValueSerializer().getClass(), DataType.STRING);
		types.put(ColorNameSerializer.class, DataType.STRING);
		types.put(EnumSerializer.class, DataType.ENUM);
		return types;
	}
	
}
