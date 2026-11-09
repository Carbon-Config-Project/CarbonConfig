package carbonconfiglib.plugins.mezz.configs;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import carbonconfiglib.gui.api.node.IConfigNode;
import carbonconfiglib.gui.api.node.INode;
import carbonconfiglib.gui.api.types.DataType;
import carbonconfiglib.gui.impl.carbon.IValueActions;
import carbonconfiglib.impl.ReloadMode;
import carbonconfiglib.impl.entries.ColorValue.ColorWrapper;
import carbonconfiglib.utils.structure.IStructuredData.StructureType;
import mezz.jei.library.config.serializers.ColorNameSerializer;
import net.mezzdev.config.api.value.IConfigValue;
import net.mezzdev.config.api.value.editor.ConfigValueRestartRequirement;
import net.mezzdev.config.api.value.editor.IConfigValueEditorInfo;
import net.mezzdev.config.api.value.serializer.IConfigKeyValueSerializer;
import net.mezzdev.config.api.value.serializer.IConfigListValueSerializer;
import net.mezzdev.config.api.value.serializer.IConfigValueSerializer;
import net.mezzdev.config.serializers.BooleanSerializer;
import net.mezzdev.config.serializers.ColorSerializer;
import net.mezzdev.config.serializers.DoubleSerializer;
import net.mezzdev.config.serializers.EnumSerializer;
import net.mezzdev.config.serializers.IntegerSerializer;
import net.mezzdev.config.serializers.LongSerializer;
import net.mezzdev.config.serializers.StringSerializer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.resources.language.I18n;
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
public class MezzLeaf implements IConfigNode
{
	public static final Map<Class<?>, DataType> KNOWN_TYPES = createTypes();
	IConfigValue<?> entry;
	IConfigValueEditorInfo<?> info;
	IConfigValueSerializer<?> serializer;
	StructureType structure;
	ReloadMode reloadMode;
	IValueActions node;
	
	public MezzLeaf(IConfigValue<?> leaf) {
		this.entry = leaf;
		this.info = leaf.getEditorInfo();
		this.serializer = info.getSerializer();
		reloadMode = getMode(info.getRestartRequirement());
		structure = MezzHelpers.getType(serializer);
	}
	
	@Override
	public List<IConfigNode> getChildren() { return null; }
	
	@Override
	public INode asNode() {
		if(node == null) {
			node = switch(structure) {
				case COMPOUND -> new MezzPairValue(info.getName(), getName(), getTooltip(), reloadMode, (IConfigKeyValueSerializer<?, ?, ?>)serializer, MezzHelpers.getValue(entry), MezzHelpers.getDefault(entry), T -> MezzHelpers.parse(T, serializer), this::save);
				case LIST -> new MezzArray(info.getName(), getName(), getTooltip(), reloadMode, (IConfigListValueSerializer<?>)serializer, MezzHelpers.getArrayValue(entry), MezzHelpers.getArrayDefault(entry), T -> MezzHelpers.parse(T, serializer), this::save);
				case SIMPLE -> new MezzValue(info.getName(), getName(), getTooltip(), reloadMode, serializer, MezzHelpers.getValue(entry), MezzHelpers.getDefault(entry), T -> MezzHelpers.parse(T, serializer), (K, V) -> save(K, V, entry));
			};
		}
		return node;
	}
	
	private ReloadMode getMode(ConfigValueRestartRequirement requirement) {
		return switch(requirement) {
			case GAME_RESTART -> ReloadMode.GAME;
			case WORLD_RESTART -> ReloadMode.WORLD;
			default -> null;
		};
	}
	
	@SuppressWarnings("unchecked")
	private <T> void save(String result, IValueActions actions) {
		MezzHelpers.save(result, (IConfigValue<List<T>>)entry);
	}
	
	private <T> void save(String value, IValueActions entry, IConfigValue<T> result) {
		MezzHelpers.parseValue(value, result.getEditorInfo().getSerializer(), result::set);
	}
	
	@Override
	public StructureType getDataStructure() { return structure; }
	@Override
	public boolean isLeaf() { return true; }
	@Override
	public boolean isRoot() { return false; }
	@Override
	public boolean isDefault() { return (node != null ? node.isDefault() : Objects.equals(MezzHelpers.getValue(entry), MezzHelpers.getDefault(entry))); }
	@Override
	public boolean isChanged() { return node != null && node.isChanged(); }
	@Override
	public boolean isUnsaved() { return node != null && node.isUnsaved(); }
	
	@Override
	public void save() { 
		if(node != null) node.save();
	}
	
	@Override
	public void setPrevious() {
		if(node != null) node.setPrevious();
	}
	
	@Override
	public void setDefault() {
		asNode();
		node.setDefault();
	}
	
	@Override
	public ReloadMode getReloadState() { return reloadMode; }
	@Override
	public String getNodeName() { return null; }
	@Override
	public Component getName() { return IConfigNode.createLabel(info.getName(), info.getLocalizationKey()); }
	@Override
	public Component getTooltip() {
		MutableComponent comp = Component.empty();
		String key = this.entry.getEditorInfo().getDescriptionLocalizationKey();
		if(key != null && I18n.exists(key)) {
			comp.append(Component.translatable(key).withStyle(ChatFormatting.GRAY)).append("\n");
		}
		comp.append(Component.literal(serializer.getValidValuesDescription()).withStyle(ChatFormatting.BLUE));
		return comp;
	}
	
	private static Map<Class<?>, DataType> createTypes() {
		Map<Class<?>, DataType> types = new Object2ObjectOpenHashMap<>();
		types.put(BooleanSerializer.class, DataType.BOOLEAN);
		types.put(IntegerSerializer.class, DataType.INTEGER);
		types.put(LongSerializer.class, DataType.LONG);
		types.put(DoubleSerializer.class, DataType.DOUBLE);
		types.put(ColorSerializer.class, DataType.byClass(ColorWrapper.class));
		types.put(EnumSerializer.class, DataType.ENUM);
		types.put(StringSerializer.class, DataType.STRING);
		types.put(ColorNameSerializer.class, DataType.STRING);
		return types;
	}
	
}
