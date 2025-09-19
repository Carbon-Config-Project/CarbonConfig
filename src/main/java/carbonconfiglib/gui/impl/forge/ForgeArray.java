package carbonconfiglib.gui.impl.forge;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import carbonconfiglib.api.IEntrySettings;
import carbonconfiglib.api.ISuggestionProvider.Suggestion;
import carbonconfiglib.gui.api.DataType;
import carbonconfiglib.gui.api.IArrayNode;
import carbonconfiglib.gui.api.INode;
import carbonconfiglib.impl.ReloadMode;
import carbonconfiglib.utils.ParseResult;
import carbonconfiglib.utils.structure.IStructuredData.StructureType;
import net.minecraft.network.chat.Component;
import speiger.src.collections.objects.lists.ObjectArrayList;
import speiger.src.collections.objects.utils.ObjectLists;
import speiger.src.collections.utils.Stack;

public class ForgeArray implements IArrayNode
{
	Component name;
	Component tooltip;
	DataType type;
	ReloadMode mode;
	Function<String, ParseResult<?>> isValid;
	Supplier<List<Suggestion>> suggestions;
	Consumer<List<String>> saved;
	
	List<ForgeValue> values = new ObjectArrayList<>();
	Stack<List<String>> previous = new ObjectArrayList<>();
	List<String> currentValues;
	List<String> defaults;
	
	public ForgeArray(Component name, Component tooltip, ReloadMode mode, DataType type, List<String> value, List<String> defaultValue, Supplier<List<Suggestion>> suggestions, Function<String, ParseResult<?>> isValid, Consumer<List<String>> saved) {
		this.name = name;
		this.tooltip = tooltip;
		this.isValid = isValid;
		this.mode = mode;
		this.type = type;
		this.currentValues = value;
		previous.push(new ObjectArrayList<>(currentValues));
		this.defaults = defaultValue;
		this.suggestions = suggestions;
		this.saved = saved;
		reload();
	}
	
	public void save() { saved.accept(currentValues); }
	
	protected void reload() {
		values.clear();
		for(int i = 0;i<currentValues.size();i++) {
			values.add(new ForgeValue(name, tooltip, mode, type, currentValues.get(i), i >= defaults.size() ? null : defaults.get(i), () -> ObjectLists.empty(), isValid::apply, this::save));
		}
	}
	
	protected List<String> getPrev() {
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
	public StructureType getInnerType() { return StructureType.SIMPLE; }
	@Override
	public IEntrySettings getSettings() { return null; }
	@Override
	public StructureType getNodeType() { return StructureType.LIST; }
	@Override
	public boolean requiresRestart() { return mode == ReloadMode.GAME; }
	@Override
	public boolean requiresReload() { return mode == ReloadMode.WORLD; }
	
	@Override
	public Component getName() { return name; }
	
	@Override
	public Component getTooltip() { return tooltip; }
	@Override
	public int size() { return values.size(); }
	@Override
	public INode get(int index) { return values.get(index); }
	@Override
	public List<Suggestion> getSuggestions() { return suggestions.get(); }
	
	@Override
	public void createNode() {
		String value = defaults.isEmpty() ? type.getDefaultValue() : defaults.get(0);
		currentValues.add(value);
		values.add(new ForgeValue(name, tooltip, mode, type, value, null, () -> ObjectLists.empty(), isValid::apply, this::save));
	}
	
	private void save(String value, ForgeValue entry) {
		int index = values.indexOf(entry);
		if(index == -1) return;
		currentValues.set(0, value);
	}
	
	@Override
	public void removeNode(int index) {
		values.remove(index); 
		currentValues.remove(index);
	}
	@Override
	public int indexOf(INode value) { return values.indexOf(value); }
}
