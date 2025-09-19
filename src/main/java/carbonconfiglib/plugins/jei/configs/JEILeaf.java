package carbonconfiglib.plugins.jei.configs;

import java.util.List;
import java.util.Map;

import carbonconfiglib.gui.api.DataType;
import carbonconfiglib.gui.api.IConfigNode;
import carbonconfiglib.gui.api.INode;
import carbonconfiglib.utils.structure.IStructuredData.StructureType;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import mezz.jei.api.runtime.config.IJeiConfigListValueSerializer;
import mezz.jei.api.runtime.config.IJeiConfigValue;
import mezz.jei.api.runtime.config.IJeiConfigValueSerializer;
import mezz.jei.common.config.file.serializers.BooleanSerializer;
import mezz.jei.common.config.file.serializers.EnumSerializer;
import mezz.jei.library.config.serializers.ChatFormattingSerializer;
import mezz.jei.library.config.serializers.ColorNameSerializer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

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
			if(array == null) array = new JEIArray(getName(), getTooltip(), null, type, JEIHelpers.getArrayValue(entry), JEIHelpers.getArrayDefault(entry), () -> JEIHelpers.getSuggestions(serializer), T -> JEIHelpers.parse(T, serializer), this::save);
			return array;
		}
		if(value == null) value = new JEIValue(getName(), getTooltip(), null, type, JEIHelpers.getValue(entry), JEIHelpers.getDefault(entry), () -> JEIHelpers.getSuggestions(serializer), T -> JEIHelpers.parse(T, serializer), (K, V) -> save(K, V, entry));
		return value;
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
	public boolean isChanged() {
		if(value != null && value.isChanged()) return true;
		if(array != null && array.isChanged()) 	return true;
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
	public boolean requiresRestart() { return false; }
	@Override
	public boolean requiresReload() { return false; }
	@Override
	public String getNodeName() { return null; }
	@Override
	public Component getName() { return entry.getLocalizedName(); }
	@Override
	public Component getTooltip() {
		MutableComponent comp = Component.empty();
		comp.append(entry.getLocalizedName().copy().withStyle(ChatFormatting.YELLOW)).append("\n");
		if(entry.getLocalizedDescription() != null) comp.append(entry.getLocalizedDescription()).append("\n");
		IJeiConfigValueSerializer<?> serializer = isArray ? ((IJeiConfigListValueSerializer<?>)this.serializer).getListValueSerializer() : this.serializer;
		comp.append(Component.literal(serializer.getValidValuesDescription()).withStyle(ChatFormatting.BLUE));
		return comp;
	}
	
	private static Map<Class<?>, DataType> createTypes() {
		Map<Class<?>, DataType> types = new Object2ObjectOpenHashMap<>();
		types.put(BooleanSerializer.class, DataType.BOOLEAN);
		types.put(ChatFormattingSerializer.INSTANCE.getListValueSerializer().getClass(), DataType.STRING);
		types.put(ColorNameSerializer.class, DataType.STRING);
		types.put(EnumSerializer.class, DataType.ENUM);
		return types;
	}
	
}
