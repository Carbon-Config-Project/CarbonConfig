package carbonconfiglib.gui.impl.forge;

import java.util.List;
import java.util.Objects;

import org.apache.logging.log4j.util.Strings;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.google.common.collect.Iterables;

import carbonconfiglib.api.IRange;
import carbonconfiglib.api.IRange.DoubleRange;
import carbonconfiglib.api.IRange.IntegerRange;
import carbonconfiglib.api.ISuggestionProvider.Suggestion;
import carbonconfiglib.gui.api.node.ConfigPath;
import carbonconfiglib.gui.api.node.IConfigNode;
import carbonconfiglib.gui.api.node.INode;
import carbonconfiglib.impl.ReloadMode;
import carbonconfiglib.impl.internal.SettingsLoader;
import carbonconfiglib.utils.ParseResult;
import carbonconfiglib.utils.structure.IStructuredData.StructureType;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.neoforged.fml.util.ObfuscationReflectionHelper;
import net.neoforged.neoforge.common.ModConfigSpec.ConfigValue;
import net.neoforged.neoforge.common.ModConfigSpec.RestartType;
import net.neoforged.neoforge.common.ModConfigSpec.ValueSpec;
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
public class ForgeLeaf implements IConfigNode
{
	ConfigValue<?> data;
	CommentedConfig config;
	ValueSpec spec;
	ConfigPath path;
	ForgeDataType<?> type;
	IRange range;
	boolean isArray;
	ForgeValue value;
	ForgeArray array;
	Component tooltip;
	
	public ForgeLeaf(IConfigSpecProvider spec, ConfigValue<?> data, ConfigPath path, CommentedConfig config) {
		this.data = data;
		this.config = config;
		this.path = path;
		this.spec = getSpec(spec, data);
		String[] array = buildComment();
		if(array != null && array.length > 0) {
			MutableComponent comp = Component.empty();
			for(int i = 0;i<array.length;comp.append(Component.literal(array[i++]).withStyle(ChatFormatting.GRAY)).append("\n"));
			tooltip = comp;
		}
		guessDataType();
		loadRange();
	}
	
	@SuppressWarnings("unchecked")
	private void guessDataType() {
		Class<?> clz = spec.getClazz();
		if(clz == Object.class) {
			clz = spec.getDefault().getClass();
		}
		type = ForgeDataType.getDataByType(clz);
		if(type == null && clz != null && List.class.isAssignableFrom(clz)) {
			isArray = true;
			List<?> list = (List<?>)spec.getDefault();
			type = list.isEmpty() ? ForgeDataType.STRING : ForgeDataType.getDataByType(list.get(0).getClass());
		}
		if(type == ForgeDataType.STRING && ForgeHelpers.isColor(spec.getDefault())) {
			if(!isArray && ForgeHelpers.isColor(spec.getDefault())) {
				type = ForgeDataType.COLOR;
			}
			else if(isArray) {
				List<String> list = (List<String>)spec.getDefault();
				if(!list.isEmpty() && ForgeHelpers.isColor(list.get(0))) {
					type = ForgeDataType.COLOR;
				}
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	private void loadRange() {
		try {
			Object obj = spec.getRange();
			if(obj == null) return;
			Class<?> clz = ObfuscationReflectionHelper.getPrivateValue((Class<Object>)obj.getClass(), obj, "clazz");
			if(clz == Integer.class) {
				Integer min = ObfuscationReflectionHelper.getPrivateValue((Class<Object>)obj.getClass(), obj, "min");
				Integer max = ObfuscationReflectionHelper.getPrivateValue((Class<Object>)obj.getClass(), obj, "max");
				range = new IntegerRange(min, max);
			}
			else if(clz == Double.class) {
				Double min = ObfuscationReflectionHelper.getPrivateValue((Class<Object>)obj.getClass(), obj, "min");
				Double max = ObfuscationReflectionHelper.getPrivateValue((Class<Object>)obj.getClass(), obj, "max");
				range = new DoubleRange(min, max);
			}
		}
		catch(Exception e) {e.printStackTrace();}
	}
	
	public boolean isValid() { return type != null; }
	@Override
	public List<IConfigNode> getChildren() { return null; }
	
	@Override
	public INode asNode() {
		if(isArray) {
			if(array == null) array = new ForgeArray(Iterables.getLast(data.getPath(), ""), getName(), getTooltip(), SettingsLoader.INSTANCE.getOverride(path), getReloadState(), type.getDataType(), range, getCurrentList(), getDefaultList(), () -> ObjectLists.empty(), type::parse, this::save);
			return array;
		}
		if(value == null) value = new ForgeValue(Iterables.getLast(data.getPath(), ""), getName(), getTooltip(), SettingsLoader.INSTANCE.getOverride(path), getReloadState(), type.getDataType(), range, getCurrent(), getDefault(), this::getSuggestions, type::parse, this::save);
		return value;
	}
	
	private List<String> getDefaultList() {
		List<String> list = new ObjectArrayList<>();
		for(Object data : (List<?>)spec.getDefault()) {
			list.add(type.serialize(data));
		}
		return list;
	}
	
	private List<String> getCurrentList() {
		List<String> list = new ObjectArrayList<>();
		for(Object data : (List<?>)config.get(data.getPath())) {
			list.add(type.serialize(data));
		}
		return list;
	}
	
	private <T> List<T> deserialize(List<String> list, ForgeDataType<T> type) {
		List<T> result = new ObjectArrayList<>();
		for(String entry : list) {
			ParseResult<T> parse = type.parse(entry);
			if(parse.isValid())
				result.add(parse.getValue());
		}
		return result;
	}
	
	private String getDefault() { return type.serialize(spec.getDefault()); }
	private String getCurrent() {
		Object currentValue = config.get(data.getPath());
		return type.isEnum() && currentValue instanceof String ? (String)currentValue : type.serialize(currentValue);
	}
	
	private List<Suggestion> getSuggestions() {
		return type instanceof ForgeDataType.EnumDataType ? ((ForgeDataType.EnumDataType<?>)type).getSuggestions(spec) : ObjectLists.empty();
	}
	
	private void save(String value, ForgeValue entry) { config.set(data.getPath(), type.parse(value).getValue()); }
	private void save(List<String> values) { config.set(data.getPath(), deserialize(values, type)); }
	@Override
	public StructureType getDataStructure() { return isArray ? StructureType.LIST : StructureType.SIMPLE; }
	@Override
	public boolean isLeaf() { return true; }
	@Override
	public boolean isRoot() { return false; }
	@Override
	public boolean isDefault() {
		if(value != null && value.isDefault()) return true;
		if(array != null && array.isDefault()) return true;
		return Objects.equals(getDefault(), getCurrent());
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
		if(isDefault()) return;
		asNode();
		if(isArray) array.setDefault();
		else value.setDefault();
	}
	
	@Override
	public ReloadMode getReloadState() { return spec.restartType() == RestartType.GAME ? ReloadMode.GAME : spec.restartType() == RestartType.WORLD ? ReloadMode.WORLD : null; }
	@Override
	public String getNodeName() { return Iterables.getLast(data.getPath(), ""); }
	@Override
	public Component getName() { return IConfigNode.createLabel(Iterables.getLast(data.getPath(), "")); }
	@Override
	public Component getTooltip() {
		MutableComponent comp = Component.empty();
		if(tooltip != null) comp.append(tooltip);
		String limit = type.getLimitations(spec);
		if(limit != null && !Strings.isBlank(limit)) {
			comp.append(Component.literal(limit).withStyle(ChatFormatting.BLUE));
		}
		return comp;
	}
	
	private String[] buildComment() {
		String value = this.spec.getComment();
		if(value == null) return null;
		int cutoffPoint = getSmallerOfPresent(value.indexOf("Range: "), value.indexOf("Allowed Values: "));
		return (cutoffPoint >= 0 ? value.substring(0, cutoffPoint) : value).split("\n");
	}
	
	private ValueSpec getSpec(IConfigSpecProvider spec, ConfigValue<?> value) {
		return spec.getSpec().get(value.getPath());
	}
	/**
	 * Function that finds the Lowest "present" index of a List/String. Idea is
	 * you try to find the lowest of two "List.indexOf" results. Since the range
	 * of List.indexOf is -1 -> Integer.MAX_VALUE you want anything bigger then
	 * -1 if either of them is. If both of them are -1 means you have non found
	 * so -1 can be returned.
	 * 
	 * @author Meduris (Who found the best implementation after a small fun challenge)
	 * 
	 * @param first number to compare
	 * @param second number to compare
	 * @return the highest non -1 number if found. Otherwise it is -1
	 */
	private int getSmallerOfPresent(int first, int second) {
		if(first == -1 || (second != -1 && first > second))
			return second;
		return first;
	}
}
