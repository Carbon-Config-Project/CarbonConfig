package carbonconfiglib.plugins.mezz.configs;

import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import carbonconfiglib.api.IEntrySettings;
import carbonconfiglib.api.ISuggestionProvider.Suggestion;
import carbonconfiglib.gui.api.node.IArrayNode;
import carbonconfiglib.gui.api.node.INode;
import carbonconfiglib.gui.api.types.DataType;
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

public class MezzArray implements IArrayNode, IValueActions {
	String nodeName;
	Component name;
	Component tooltip;
	IConfigListValueSerializer<?> serializer;
	IConfigValueSerializer<?> innerSerializer;
	StructureType inner;
	ReloadMode mode;
	boolean forced;
	Function<String, ParseResult<?>> isValid;
	BiConsumer<String, IValueActions> saved;
	
	List<IValueActions> values = new ObjectArrayList<>();
	Stack<List<JsonElement>> previous = new ObjectArrayList<>();
	List<JsonElement> currentValues;
	List<JsonElement> savedValues = new ObjectArrayList<>();
	List<JsonElement> defaults;
	boolean autosave;
	
	public MezzArray(String nodeName, Component name, Component tooltip, ReloadMode mode, IConfigListValueSerializer<?> serializer, String value, String defaultValue, Function<String, ParseResult<?>> isValid, BiConsumer<String, IValueActions> saved) {
		this.nodeName = nodeName;
		this.name = name;
		this.tooltip = tooltip;
		this.isValid = isValid;
		this.serializer = serializer;
		this.innerSerializer = serializer.getElementSerializer();
		this.inner = MezzHelpers.getType(innerSerializer);
		this.forced = MezzHelpers.getDataType(innerSerializer) == DataType.ENUM;
		this.mode = mode;
		this.currentValues = MezzHelpers.encodeList(value);
		this.savedValues.addAll(MezzHelpers.encodeList(value));
		previous.push(new ObjectArrayList<>(currentValues));
		this.defaults = MezzHelpers.encodeList(defaultValue);
		this.saved = saved;
		reload();
	}
	
	public MezzArray withAutosave() {
		autosave = true;
		return this;
	}
	
	private void autosave() {
		if(!autosave) return;
		save();
	}
	
	private void save(String value, IValueActions entry) {
		int index = values.indexOf(entry);
		if(index == -1) return;
		currentValues.set(index, MezzHelpers.encode(value));
		autosave();
	}
	
	public void save() {
		saved.accept(MezzHelpers.decodeList(currentValues), this); 
		savedValues.clear();
		savedValues.addAll(currentValues);
	}
	
	@Override
	public void set(String value) {
		currentValues.clear();
		currentValues.addAll(MezzHelpers.encodeList(value));
		autosave();
	}
	
	protected void reload() {
		values.clear();
		for(int i = 0;i<currentValues.size();i++) {
			values.add(addEntry(MezzHelpers.decode(currentValues.get(i)), i >= defaults.size() ? null : MezzHelpers.decode(defaults.get(i)), i));
		}
		autosave();
	}
	
	protected IValueActions addEntry(String value, String defaultValue, int index) {
		if(innerSerializer instanceof IConfigListValueSerializer<?> list) {
			return new MezzArray(Integer.toString(index), name, tooltip, mode, list, value, defaultValue, T -> isValid(index, T), this::save).withAutosave();
		}
		if(innerSerializer instanceof IConfigKeyValueSerializer<?, ?, ?> map) {
			return new MezzPairValue(Integer.toString(index), name, tooltip, mode, map, value, defaultValue, T -> isValid(index, T), this::save).withAutosave();
		}
		return new MezzValue(null, name, tooltip, mode, innerSerializer, value, defaultValue, T -> isValid(index, T), this::save).withAutosave();
	}
	
	private ParseResult<?> isValid(int index, String value) {
		JsonArray array = MezzHelpers.merge(currentValues);
		array.set(index, MezzHelpers.encode(value));
		return MezzHelpers.parse(MezzHelpers.decode(array), serializer);
	}
	
	protected List<JsonElement> getPrev() {
		return previous.top();
	}
	
	@Override
	public boolean isChanged() {
		return !getPrev().equals(currentValues);
	}
	
	@Override
	public boolean isDefault() {
		return currentValues.equals(defaults);
	}
	
	@Override
	public boolean isUnsaved() {
		return !currentValues.equals(savedValues);
	}
	
	@Override
	public void setPrevious() {
		currentValues.clear();
		currentValues.addAll(getPrev());
		if(previous.size() > 1) previous.pop();
		reload();
	}
	
	@Override
	public void setDefault() {
		currentValues.clear();
		currentValues.addAll(defaults);
		reload();
	}
	
	@Override
	public void moveDown(int index) {
		swap(index, index+1);
	}
	
	@Override
	public void moveUp(int index) {
		swap(index, index-1);
	}
	
	@Override
	public void swap(int oldIndex, int newIndex) {
		if(oldIndex >= values.size() || oldIndex < 0) return;
		if(newIndex >= values.size() || newIndex < 0) return;
		int start = oldIndex < newIndex ? oldIndex : newIndex;
		int end = oldIndex < newIndex ? newIndex : oldIndex;
		boolean inverse = newIndex < oldIndex;
		Collections.rotate(currentValues.subList(start, end+1), inverse ? 1 : -1);
		Collections.rotate(values.subList(start, end+1), inverse ? 1 : -1);
		autosave();
	}
	
	@Override
	public void createTemp() {
		previous.push(new ObjectArrayList<>(currentValues));
		reload();
	}
	
	@Override
	public void deleteTempIfNeeded() {
		if(previous.size() > 1 && getPrev().equals(currentValues)) {
			previous.pop();
		}
	}
	
	@Override
	public StructureType getInnerType() { return inner; }
	@Override
	public IEntrySettings getSettings() { return null; }
	@Override
	public StructureType getNodeType() { return StructureType.LIST; }
	@Override
	public ReloadMode getReloadState() { return mode; }
	@Override
	public String getNodeName() { return nodeName; }
	@Override
	public Component getName() { return name; }
	@Override
	public Component getTooltip() { return tooltip; }
	@Override
	public int size() { return values.size(); }
	@Override
	public INode get(int index) { return values.get(index); }
	@Override
	public boolean isForcedSuggestion() { return forced; }
	@Override
	public List<Suggestion> getSuggestions() { return MezzHelpers.getSuggestions(innerSerializer); }
	
	@Override
	public void createNode(String value) {
		String defaultValue = defaults.isEmpty() ? MezzHelpers.getDataType(serializer).getDefaultValue() : MezzHelpers.decode(defaults.get(0));
		if(value == null) {
			value = defaultValue;			
		}
		int index = currentValues.size();
		currentValues.add(MezzHelpers.encode(value));
		values.add(addEntry(value, defaultValue, index));
		autosave();
	}
	
	@Override
	public void removeNode(int index) {
		values.remove(index); 
		currentValues.remove(index);
		autosave();
	}
	@Override
	public int indexOf(INode value) { return values.indexOf(value); }
}
