package carbonconfiglib.plugins.mezz.configs;

import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

import carbonconfiglib.api.IEntrySettings;
import carbonconfiglib.api.ISuggestionProvider.Suggestion;
import carbonconfiglib.gui.api.node.ICompoundNode;
import carbonconfiglib.gui.api.node.IConfigNode;
import carbonconfiglib.gui.api.node.INode;
import carbonconfiglib.gui.impl.carbon.IValueActions;
import carbonconfiglib.impl.ReloadMode;
import carbonconfiglib.utils.ParseResult;
import carbonconfiglib.utils.structure.IStructuredData.StructureType;
import net.mezzdev.config.api.value.serializer.IConfigKeyValueSerializer;
import net.mezzdev.config.api.value.serializer.IConfigListValueSerializer;
import net.mezzdev.config.api.value.serializer.IConfigValueSerializer;
import net.minecraft.network.chat.Component;
import speiger.src.collections.objects.lists.ObjectArrayList;
import speiger.src.collections.utils.Stack;

public class MezzPairValue implements ICompoundNode, IValueActions {
	String nodeName;
	Component name;
	Component tooltip;
	ReloadMode mode;
	Function<String, ParseResult<?>> isValid;
	BiConsumer<String, IValueActions> saved;
	
	IConfigKeyValueSerializer<?, ?, ?> serializer;
	IConfigValueSerializer<?> keySerializer;
	IConfigValueSerializer<?> valueSerializer;
	
	List<IValueActions> values = new ObjectArrayList<>();
	Stack<String[]> previous = new ObjectArrayList<>();
	String[] currentValues = new String[3];
	String[] savedValues = new String[3];
	String[] defaults = new String[3];
	boolean autosave;
	
	public MezzPairValue(String nodeName, Component name, Component tooltip, ReloadMode mode, IConfigKeyValueSerializer<?, ?, ?> serializer, String value, String defaultValue, Function<String, ParseResult<?>> isValid, BiConsumer<String, IValueActions> saved) {
		this.nodeName = nodeName;
		this.name = name;
		this.tooltip = tooltip;
		this.isValid = isValid;
		this.mode = mode;
		this.serializer = serializer;
		this.keySerializer = serializer.getKeySerializer();
		this.valueSerializer = serializer.getValueSerializer();
		this.currentValues[0] = value;
		this.savedValues[0] = value;
		this.currentValues[1] = this.savedValues[1] = MezzHelpers.getKey(value, serializer);
		this.currentValues[2] = this.savedValues[2] = MezzHelpers.getValue(value, serializer);
		this.previous.push(Arrays.copyOf(currentValues, currentValues.length));
		this.defaults[0] = defaultValue;
		this.defaults[1] = MezzHelpers.getKey(defaultValue, serializer);
		this.defaults[2] = MezzHelpers.getValue(defaultValue, serializer);
		reload();
	}

	public MezzPairValue withAutosave() {
		autosave = true;
		return this;
	}
	
	private void autosave() {
		if(!autosave) return;
		save();
	}
	
	public void save() {
		saved.accept(currentValues[0], this); 
		System.arraycopy(currentValues, 0, savedValues, 0, 3);
	}
	
	private void reload() {
		values.clear();
		values.add(addEntry(currentValues[1], defaults[1], "Key", 1, keySerializer, () -> MezzHelpers.getSuggestions(keySerializer)));
		values.add(addEntry(currentValues[2], defaults[2], "Value", 2, valueSerializer, () -> MezzHelpers.getSuggestions(valueSerializer)));
		autosave();
	}
	
	private IValueActions addEntry(String value, String defaultValue, String name, int index, IConfigValueSerializer<?> serializer, Supplier<List<Suggestion>> suggestions) {
		if(serializer instanceof IConfigListValueSerializer<?> list) {
			return new MezzArray(name, IConfigNode.createLabel(name).append(this.name), tooltip, mode, list, value, defaultValue, T -> isValid(index, T), (K, V) -> save(index, K)).withAutosave();
		}
		if(serializer instanceof IConfigKeyValueSerializer<?, ?, ?> map) {
			return new MezzPairValue(name, IConfigNode.createLabel(name).append(this.name), tooltip, mode, map, value, defaultValue, T -> isValid(index, T), (K, V) -> save(index, K)).withAutosave();
		}
		return new MezzValue(name, IConfigNode.createLabel(name), tooltip, mode, serializer, value, defaultValue, T -> isValid(index, T), (K, V) -> save(index, K)).withAutosave();
	}
	
	private void save(int index, String value) {
		currentValues[index] = value;
		currentValues[0] = MezzHelpers.createEntry(currentValues[1], currentValues[2], serializer);
	}
	
	private ParseResult<?> isValid(int index, String data) {
		String key = index == 1 ? data : currentValues[1];
		String value = index == 2 ? data : currentValues[2];
		return MezzHelpers.tryCreateEntry(key, value, serializer);
	}
	
	protected String[] getPrev() {
		return previous.top();
	}
	
	@Override
	public boolean isDefault() { return Arrays.equals(getPrev(), defaults); }
	
	@Override
	public boolean isChanged() { return !Arrays.equals(getPrev(), currentValues); }
	
	@Override
	public boolean isUnsaved() { return !Arrays.equals(currentValues, savedValues); }
	
	@Override
	public void setPrevious() {
		System.arraycopy(getPrev(), 0, currentValues, 0, 3);
		if(previous.size() > 1) previous.pop();
		reload();
	}
	
	@Override
	public void setDefault() {
		System.arraycopy(defaults, 0, currentValues, 0, 3);
		reload();
	}
	
	@Override
	public void createTemp() {
		previous.push(Arrays.copyOf(currentValues, currentValues.length));
		reload();
	}
	
	@Override
	public void deleteTempIfNeeded() {
		if(previous.size() > 1 && Arrays.equals(getPrev(), currentValues)) {
			previous.pop();
		}
	}
	
	@Override
	public boolean isValid() {
		return isValid.apply(get()).getValue() != null;
	}
	
	@Override
	public String get() {
		return currentValues[0];
	}
	
	@Override
	public void set(String value) {
		currentValues[0] = value;
		currentValues[1] = MezzHelpers.getKey(value, serializer);
		currentValues[2] = MezzHelpers.getValue(value, serializer);
	}
	
	@Override
	public List<? extends INode> getValues() { return values; }
	@Override
	public StructureType getNodeType() { return StructureType.COMPOUND; }
	@Override
	public IEntrySettings getSettings() { return null; }
	@Override
	public ReloadMode getReloadState() { return mode; }
	@Override
	public String getNodeName() { return nodeName; }
	@Override
	public Component getName() { return name; }
	@Override
	public Component getTooltip() { return tooltip; }
	
}
