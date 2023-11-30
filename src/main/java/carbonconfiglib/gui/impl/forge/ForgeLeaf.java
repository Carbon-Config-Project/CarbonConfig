package carbonconfiglib.gui.impl.forge;

import java.util.List;
import java.util.Locale;

import carbonconfiglib.api.ISuggestionProvider.Suggestion;
import carbonconfiglib.gui.api.DataType;
import carbonconfiglib.gui.api.IArrayNode;
import carbonconfiglib.gui.api.ICompoundNode;
import carbonconfiglib.gui.api.IConfigNode;
import carbonconfiglib.gui.api.IValueNode;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentBase;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.common.config.Property.Type;
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
	Property property;
	ForgeValue value;
	ForgeArrayValue array;
	
	public ForgeLeaf(Property property) {
		this.property = property;
	}

	@Override
	public List<IConfigNode> getChildren() { return null; }
	@Override
	public IValueNode asValue() {
		if(isArray()) return null;
		if(value == null) value = new ForgeValue(property);
		return value;
	}
	
	@Override
	public IArrayNode asArray() {
		if(!isArray()) return null;
		if(array == null) array = new ForgeArrayValue(property, fromType(property.getType()));
		return array;
	}
	
	@Override
	public ICompoundNode asCompound() { return null; }
	
	@Override
	public List<DataType> getDataType() {
		return ObjectLists.singleton(fromType(property.getType()));
	}
	
	@Override
	public List<Suggestion> getValidValues() {
		String[] values = property.getValidValues();
		if(values == null || values.length <= 0) return ObjectLists.empty();
		List<Suggestion> suggestion = new ObjectArrayList<>();
		for(String value : values) {
			suggestion.add(Suggestion.value(value));
		}
		return suggestion;
	}
	
	@Override
	public boolean isForcingSuggestions() {
		String[] value = property.getValidValues();
		return value != null && value.length > 0;
	}
	
	@Override
	public boolean isArray() {
		return property.isList();
	}
	
	@Override
	public boolean isLeaf() {
		return true;
	}
	
	@Override
	public boolean isRoot() { return false; }
	
	@Override
	public boolean isChanged() { return (value != null && value.isChanged()) || (array != null && array.isChanged()); }
	
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
	public void save() {
		if(value != null) value.save();
		if(array != null) array.save();
	}
	
	@Override
	public boolean requiresRestart() {
		return property.requiresMcRestart();
	}
	
	@Override
	public boolean requiresReload() {
		return property.requiresWorldRestart();
	}
	
	@Override
	public String getNodeName() {
		return property.getName().toLowerCase(Locale.ROOT);
	}
	
	@Override
	public ITextComponent getName() { return IConfigNode.createLabel(I18n.hasKey(property.getLanguageKey()) ? I18n.format(property.getLanguageKey()) : property.getName()); }
	@Override
	public ITextComponent getTooltip() {
		TextComponentBase comp = new TextComponentString("");
		comp.appendSibling(new TextComponentString(I18n.hasKey(property.getLanguageKey()) ? I18n.format(property.getLanguageKey()) : property.getName()).setStyle(new Style().setColor(TextFormatting.YELLOW)));
		String comment = property.getComment();
		if(comment != null) {
			String[] array = comment.split("\n");
			if(array != null && array.length > 0) {
				for(int i = 0;i<array.length;comp.appendText("\n").appendText(array[i++]).setStyle(new Style().setColor(TextFormatting.GRAY)));
			}
		}
		return comp;
	}
	
	private DataType fromType(Type type) {
		switch(type) {
			case BOOLEAN: return DataType.BOOLEAN;
			case COLOR: return DataType.INTEGER;
			case DOUBLE: return DataType.DOUBLE;
			case INTEGER: return DataType.INTEGER;
			case MOD_ID: return DataType.STRING;
			case STRING: return DataType.STRING;
			default: return DataType.STRING;
		}
	}
}
