package carbonconfiglib.gui.impl.carbon;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

import carbonconfiglib.api.IEntrySettings;
import carbonconfiglib.api.IReloadMode;
import carbonconfiglib.api.ISuggestionProvider.Suggestion;
import carbonconfiglib.gui.api.DataType;
import carbonconfiglib.gui.api.IArrayNode;
import carbonconfiglib.gui.api.INode;
import carbonconfiglib.impl.ReloadMode;
import carbonconfiglib.utils.Helpers;
import carbonconfiglib.utils.ParseResult;
import carbonconfiglib.utils.structure.IStructuredData;
import carbonconfiglib.utils.structure.IStructuredData.SimpleData;
import carbonconfiglib.utils.structure.IStructuredData.StructureType;
import carbonconfiglib.utils.structure.StructureList.ListData;
import net.minecraft.network.chat.Component;
import speiger.src.collections.objects.lists.ObjectArrayList;
import speiger.src.collections.objects.lists.ObjectList;
import speiger.src.collections.utils.Stack;

public class CarbonArray implements IArrayNode, IValueActions
{
	IReloadMode mode;
	IStructuredData inner;
	ListData data;
	Component name;
	Component tooltip;
	Function<String, ParseResult<Boolean>> isValid;
	Supplier<List<Suggestion>> suggestions;
	BiConsumer<String, IValueActions> saveAction;
	
	List<IValueActions> values = new ObjectArrayList<>();
	Stack<List<String>> previous = new ObjectArrayList<>();
	ObjectList<String> currentValues;
	List<String> defaults;
	
	public CarbonArray(IReloadMode mode, ListData data, Component name, Component tooltip, String currentValue, String defaultValue, Function<String, ParseResult<Boolean>> isValid, Supplier<List<Suggestion>> suggestions, BiConsumer<String, IValueActions> saveAction) {
		this.mode = mode;
		this.data = data;
		this.inner = data.getType();
		this.name = name;
		this.tooltip = tooltip;
		this.currentValues = ObjectArrayList.wrap(Helpers.splitCompoundArray(currentValue));
		this.defaults = ObjectArrayList.wrap(Helpers.splitCompoundArray(defaultValue));
		this.previous.push(new ObjectArrayList<>(currentValues));
		this.isValid = isValid;
		this.suggestions = suggestions;
		this.saveAction = saveAction;
		reload();
	}

	public void reload() {
		values.clear();
		for(int i = 0,m=currentValues.size();i<m;i++) {
			values.add(addEntry(Helpers.removeLayer(currentValues.get(i), 0), i >= defaults.size() ? "" : Helpers.removeLayer(defaults.get(i), 0), i));
		}
	}
	
	protected IValueActions addEntry(String value, String defaultValue, int index) {
		switch(inner.getDataType()) {
			case COMPOUND: return new CarbonCompound(mode, inner.asCompound(), name.copy().append(" "+index+":"), tooltip, value, defaultValue, this::isValid, () -> data.getSuggestions(T -> true), this::save);
			case LIST: return new CarbonArray(mode, inner.asList(), name.copy().append(" "+index+":"), tooltip, value, defaultValue, this::isValid, () -> data.getSuggestions(T -> true), this::save);
			case SIMPLE: return new CarbonValue(mode, name.copy().append(" "+index+":"), tooltip, null, DataType.bySimple(inner.asSimple()), false, () -> data.getSuggestions(T -> true), value, defaultValue, this::isValid, this::save);
			default: return null;
		}
	}
	
	protected ParseResult<Boolean> isValid(String value) {
		return isValid.apply(value);
	}
	
	protected void save(String value, IValueActions actions) {
		int index = values.indexOf(actions);
		if(index == -1) return;
		currentValues.set(index, value);
	}
	
	protected List<String> getPrev() {
		return previous.top();
	}
	
	@Override
	public void set(String value) {
		currentValues.clear();
		currentValues.addAll(Helpers.splitCompoundArray(value));
	}
	
	@Override
	public void save() {
		saveAction.accept(inner.getDataType() == StructureType.COMPOUND ? Helpers.mergeCompoundArray(currentValues, false, 0) : String.join(", ", currentValues), this);
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
		swapValues(index, index+1);
	}
	
	@Override
	public void moveUp(int index) {
		swapValues(index, index-1);
	}
	
	private void swapValues(int from, int to) {
		if(from >= values.size() || from < 0) return;
		if(to >= values.size() || to < 0) return;
		currentValues.set(from, currentValues.set(to, currentValues.get(from)));
		values.get(from).set(currentValues.get(from));
		values.get(to).set(currentValues.get(to));
	}
	
	@Override
	public void createTemp() {
		previous.push(new ObjectArrayList<>(currentValues));
		reload();
	}

	@Override
	public void apply() {
		if(previous.size() > 1) previous.pop();
		for(int i = 0,m=currentValues.size();i<m;i++) {
			values.get(i).save();
		}
	}
	

	@Override
	public void createNode() {
		String defaultString = defaults.isEmpty() ? inner.generateDefaultValue(this::getDefaultValue) : defaults.get(0);
		int index = currentValues.size();
		currentValues.add(defaultString);
		values.add(addEntry(defaultString, "", index));
	}
	
	private String getDefaultValue(SimpleData data) {
		return DataType.bySimple(data).getDefaultValue();
	}

	@Override
	public void removeNode(int index) {
		values.remove(index);
		currentValues.remove(index);
	}
	
	@Override
	public int indexOf(INode value) { return values.indexOf(value); }
	@Override
	public int size() { return values.size(); }
	@Override
	public INode get(int index) { return values.get(index); }
	@Override
	public StructureType getInnerType() { return inner.getDataType(); }
	@Override
	public StructureType getNodeType() { return StructureType.LIST; }
	@Override
	public IEntrySettings getSettings() { return data.getSettings(); }
	@Override
	public boolean requiresRestart() { return mode == ReloadMode.GAME; }
	@Override
	public boolean requiresReload() { return mode == ReloadMode.WORLD; }
	@Override
	public Component getName() { return name; }
	@Override
	public Component getTooltip() { return tooltip; }
	@Override
	public List<Suggestion> getSuggestions() { return suggestions.get(); }


}
