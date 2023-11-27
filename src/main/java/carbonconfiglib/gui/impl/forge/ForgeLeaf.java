package carbonconfiglib.gui.impl.forge;

import java.util.List;

import org.apache.logging.log4j.util.Strings;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.google.common.collect.Iterables;

import carbonconfiglib.api.ISuggestionProvider.Suggestion;
import carbonconfiglib.gui.api.DataType;
import carbonconfiglib.gui.api.IArrayNode;
import carbonconfiglib.gui.api.ICompoundNode;
import carbonconfiglib.gui.api.IConfigNode;
import carbonconfiglib.gui.api.IValueNode;
import carbonconfiglib.gui.impl.forge.ForgeDataType.EnumDataType;
import it.unimi.dsi.fastutil.objects.ObjectLists;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;
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
public class ForgeLeaf implements IConfigNode
{
	ConfigValue<?> data;
	CommentedConfig config;
	ValueSpec spec;
	ForgeDataType<?> type;
	boolean isArray;
	ForgeValue value;
	ForgeArrayValue array;
	ITextComponent tooltip;
	
	public ForgeLeaf(ForgeConfigSpec spec, ConfigValue<?> data, CommentedConfig config) {
		this.data = data;
		this.config = config;
		this.spec = getSpec(spec, data);
		String[] array = buildComment(spec);
		if(array != null && array.length > 0) {
			TextComponent comp = new StringTextComponent("");
			for(int i = 0;i<array.length;comp.append("\n").append(array[i++]).withStyle(TextFormatting.GRAY));
			tooltip = comp;
		}
		guessDataType();
	}
	
	private void guessDataType() {
		Class<?> clz = spec.getClazz();
		if(clz == Object.class) {
			clz = spec.getDefault().getClass();
		}
		type = ForgeDataType.getDataByType(clz);
		if(type == null && clz != null && List.class.isAssignableFrom(clz)) {
			isArray = true;
			type = ForgeDataType.STRING;
		}
	}
	
	@Override
	public List<IConfigNode> getChildren() { return null; }
	
	@Override
	public IValueNode asValue() {
		if(isArray()) return null;
		if(value == null) value = new ForgeValue(data.getPath(), config, spec, type);
		return value;
	}
	
	@Override
	public IArrayNode asArray() {
		if(!isArray()) return null;
		if(array == null) array = new ForgeArrayValue(data.getPath(), config, spec, ForgeDataType.STRING);
		return array;
	}
	
	@Override
	public ICompoundNode asCompound() { return null; }
	
	@Override
	public List<DataType> getDataType() {
		if(isArray) return ObjectLists.singleton(DataType.STRING);
		if(type == null) {
			return null;
		}
		return ObjectLists.singleton(type.getDataType());
	}
 	
	@Override
	public List<Suggestion> getValidValues() {
		return type instanceof EnumDataType ? ((EnumDataType<?>)type).getSuggestions(spec) : ObjectLists.emptyList();
	}
	
	@Override
	public boolean isForcingSuggestions() {
		return type instanceof EnumDataType;
	}
	
	@Override
	public boolean isArray() { return isArray; }
	@Override
	public boolean isLeaf() { return true; }
	@Override
	public boolean isRoot() { return false; }
	
	@Override
	public boolean isChanged() { return (value != null && value.isChanged()) || (array != null && array.isChanged()); }
	
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
		if(isArray()) {
			if(array == null) asArray();
			array.setDefault();
		}
		else {
			if(value == null) asValue();
			value.setDefault();
		}
	}
	
	@Override
	public boolean requiresRestart() { return false; }
	@Override
	public boolean requiresReload() { return spec.needsWorldRestart(); }
	@Override
	public String getNodeName() { return null; }
	@Override
	public ITextComponent getName() { return IConfigNode.createLabel(Iterables.getLast(data.getPath(), "")); }
	@Override
	public ITextComponent getTooltip() {
		TextComponent comp = new StringTextComponent("");
		comp.append(new StringTextComponent(Iterables.getLast(data.getPath(), "")).withStyle(TextFormatting.YELLOW));
		if(tooltip != null) comp.append(tooltip);
		String limit = type.getLimitations(spec);
		if(limit != null && !Strings.isBlank(limit)) comp.append("\n").append(new StringTextComponent(limit).withStyle(TextFormatting.BLUE));
		return comp;
	}
	
	private String[] buildComment(ForgeConfigSpec spec) {
		String value = this.spec.getComment();
		if(value == null) return null;
		int cutoffPoint = getSmallerOfPresent(value.indexOf("Range: "), value.indexOf("Allowed Values: "));
		return (cutoffPoint >= 0 ? value.substring(0, cutoffPoint) : value).split("\n");
	}
	
	private ValueSpec getSpec(ForgeConfigSpec spec, ConfigValue<?> value) {
		return spec.get(value.getPath());
	}
	
	/**
	 * Function that finds the Lowest "present" index of a List/String.
	 * Idea is you try to find the lowest of two "List.indexOf" results.
	 * Since the range of List.indexOf is -1 -> Integer.MAX_VALUE you want anything bigger then -1 if either of them is.
	 * If both of them are -1 means you have non found so -1 can be returned.
	 * 
	 * @author Meduris (Who found the best implementation after a small fun challenge)
	 * 
	 * @param first number to compare
	 * @param second number to compare
	 * @return the highest non -1 number if found. Otherwise it is -1
	 */
	private int getSmallerOfPresent(int first, int second) {
	    if (first == -1 || (second != -1 && first > second))
	        return second;
	    return first;
	}
}