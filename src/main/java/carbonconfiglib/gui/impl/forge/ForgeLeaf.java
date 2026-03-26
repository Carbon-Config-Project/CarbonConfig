package carbonconfiglib.gui.impl.forge;

import java.util.List;
import java.util.Locale;

import carbonconfiglib.api.IRange;
import carbonconfiglib.api.IRange.DoubleRange;
import carbonconfiglib.api.IRange.IntegerRange;
import carbonconfiglib.api.ISuggestionProvider.Suggestion;
import carbonconfiglib.gui.api.node.ConfigPath;
import carbonconfiglib.gui.api.node.IConfigNode;
import carbonconfiglib.gui.api.node.INode;
import carbonconfiglib.gui.api.types.DataType;
import carbonconfiglib.gui.base.helpers.Texts;
import carbonconfiglib.impl.ReloadMode;
import carbonconfiglib.impl.entries.ColorValue.ColorWrapper;
import carbonconfiglib.impl.internal.SettingsLoader;
import carbonconfiglib.utils.Helpers;
import carbonconfiglib.utils.ParseResult;
import carbonconfiglib.utils.structure.IStructuredData.StructureType;
import cpw.mods.fml.common.Loader;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.common.config.Property.Type;
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
	ConfigPath path;
	Property property;
	ForgeValue value;
	ForgeArray array;
	IRange range;
	
	public ForgeLeaf(Property property, ConfigPath path) {
		this.property = property;
		this.path = path;
		loadRanges();
	}
	
	private void loadRanges() {
		switch(property.getType()) {
			case DOUBLE: {
				try {
					range = new DoubleRange(Double.parseDouble(property.getMinValue()), Double.parseDouble(property.getMaxValue()));
				}
				catch(Exception e) {}
				return;
			}
			case INTEGER: {
				try {
					range = new IntegerRange(Integer.parseInt(property.getMinValue()), Integer.parseInt(property.getMaxValue()));
				}
				catch(Exception e) {}
				return;
			}
			default: return;
		}
	}
	
	@Override
	public List<IConfigNode> getChildren() { return null; }
	
	@Override
	public INode asNode() {
		if(property.isList()) {
			if(array == null) array = new ForgeArray(property.getName(), getName(), getTooltip(), SettingsLoader.INSTANCE.getOverride(path), getMode(), fromType(property.getType()), range, new ObjectArrayList<>(property.getStringList()), new ObjectArrayList<>(property.getDefaults()), () -> getValidValues(), this::isValid, T -> property.setValues(T.toArray(new String[T.size()])));
			return array;
		}
		if(value == null) value = new ForgeValue(property.getName(), getName(), getTooltip(), SettingsLoader.INSTANCE.getOverride(path), getMode(), fromType(property.getType()), range, property.getString(), property.getDefault(), () -> getValidValues(), this::isValid, (K, V) -> property.set(K));
		return value;
	}
	
	private ReloadMode getMode() {
		return property.requiresMcRestart() ? ReloadMode.GAME : (property.requiresWorldRestart() ? ReloadMode.WORLD : null);
	}
		
	public List<Suggestion> getValidValues() {
		String[] values = property.getValidValues();
		if(values == null || values.length <= 0) return ObjectLists.empty();
		List<Suggestion> suggestion = new ObjectArrayList<>();
		for(String value : values) {
			suggestion.add(Suggestion.value(value));
		}
		return suggestion;
	}
	
	public ParseResult<Boolean> isValid(String value) {
		switch(property.getType()) {
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
	public StructureType getDataStructure() { return property.isList() ? StructureType.LIST : StructureType.SIMPLE; }
	@Override
	public boolean isLeaf() { return true; }
	@Override
	public boolean isRoot() { return false; }
	@Override
	public boolean isChanged() { 
		if(value != null && value.isChanged()) return true;
		if(array != null && array.isChanged()) return true;
		return false;
	}
	
	@Override
	public boolean isDefault() {
		if(value != null && value.isDefault()) return true;
		if(array != null && array.isDefault()) return true;
		return property.isDefault();
	}

	@Override
	public boolean isUnsaved() {
		if(value != null && value.isUnsaved()) return true;
		if(array != null && array.isUnsaved()) 	return true;
		return false;
	}
	
	@Override
	public void setPrevious() {
		if(value != null) value.setPrevious();
		if(array != null) array.setPrevious();
	}
	
	@Override
	public void setDefault() {
		asNode();
		if(property.isList()) array.setDefault();
		else value.setDefault();
	}
	
	@Override
	public void save() {
		if(value != null) value.save();
		if(array != null) array.save();
	}
	
	@Override
	public ReloadMode getReloadState() { return property.requiresMcRestart() ? ReloadMode.GAME : (property.requiresWorldRestart() ? ReloadMode.WORLD : null); }
		
	@Override
	public String getNodeName() {
		return property.getName().toLowerCase(Locale.ROOT);
	}
	
	@Override
	public IChatComponent getName() { return IConfigNode.createLabel(Texts.hasKey(property.getLanguageKey()) ? I18n.format(property.getLanguageKey()) : property.getName()); }
	@Override
	public IChatComponent getTooltip() {
		IChatComponent comp = new ChatComponentText("");
		String comment = property.comment;
		if(comment != null) {
			String[] array = comment.split("\n");
			if(array != null && array.length > 0) {
				for(int i = 0;i<array.length;comp.appendText(array[i++]).setChatStyle(Texts.applyStyle(EnumChatFormatting.GRAY)).appendText("\n"));
			}
		}
		return comp;
	}
	
	private DataType fromType(Type type) {
		switch(type) {
			case BOOLEAN: return DataType.BOOLEAN;
			case COLOR: return DataType.byClass(ColorWrapper.class);
			case DOUBLE: return DataType.DOUBLE;
			case INTEGER: return DataType.INTEGER;
			case MOD_ID: return DataType.STRING;
			case STRING: return DataType.STRING;
			default: return DataType.STRING;
		}
	}
}