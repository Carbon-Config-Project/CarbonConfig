package carbonconfiglib.gui.impl.carbon;

import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

import carbonconfiglib.api.IEntrySettings;
import carbonconfiglib.api.IRange;
import carbonconfiglib.api.IReloadMode;
import carbonconfiglib.api.ISuggestionProvider.Suggestion;
import carbonconfiglib.gui.api.node.IValueNode;
import carbonconfiglib.gui.api.types.DataType;
import carbonconfiglib.impl.ReloadMode;
import carbonconfiglib.utils.ParseResult;
import carbonconfiglib.utils.structure.IStructuredData;
import carbonconfiglib.utils.structure.IStructuredData.StructureType;
import net.minecraft.network.chat.Component;
import speiger.src.collections.objects.lists.ObjectArrayList;
import speiger.src.collections.utils.Stack;

public class CarbonValue implements IValueNode, IValueActions
{
	IReloadMode mode;
	Component name;
	Component tooltip;
	IEntrySettings settings;
	IRange range;
	DataType type;
	boolean forced;
	Supplier<List<Suggestion>> suggestions;
	
	Function<String, ParseResult<Boolean>> isValid;
	BiConsumer<String, IValueActions> saveAction;
	
	Stack<String> previous = new ObjectArrayList<>();
	String current;
	String defaultValue;
	String savedValue;
	boolean autosave;
	
	public CarbonValue(IReloadMode mode, Component name, Component tooltip, IEntrySettings settings, IStructuredData data, boolean forced, Supplier<List<Suggestion>> suggestions, String current, String defaultValue, Function<String, ParseResult<Boolean>> isValid, BiConsumer<String, IValueActions> saveAction) {
		this.mode = mode;
		this.name = name;
		this.tooltip = tooltip;
		this.settings = settings;
		this.type =  DataType.bySimple(data.asSimple());
		this.range = data.asSimple().getRange();
		this.forced = forced;
		this.suggestions = suggestions;
		this.isValid = isValid;
		this.saveAction = saveAction;
		this.current = current;
		this.savedValue = current;
		this.defaultValue = defaultValue;
		previous.push(current);
	}
	
	public CarbonValue setAutosave(boolean value) {
		autosave = value;
		return this;
	}

	public void save() {
		saveAction.accept(current, this);
		savedValue = current;
	}
	@Override
	public boolean isDefault() { return Objects.equals(defaultValue, current); }
	@Override
	public boolean isChanged() { return !Objects.equals(previous.top(), current); }
	@Override
	public boolean isUnsaved() { return !Objects.equals(savedValue, current); }
	@Override
	public void setDefault() {
		current = defaultValue; 
		if(autosave) save();
	}
	@Override
	public void setPrevious() {
		current = previous.top();
		if(previous.size() > 1) previous.pop();
		if(autosave) save();
	}
	@Override
	public void createTemp() { previous.push(current); }
	@Override
	public void deleteTempIfNeeded() {
		if(previous.size() > 1 && previous.top().equals(current)) {
			previous.pop();
		}
	}
	@Override
	public void apply() {
		if(previous.size() > 1) previous.pop();
	}
	
	@Override
	public StructureType getNodeType() { return StructureType.SIMPLE; }
	@Override
	public IEntrySettings getSettings() { return settings; }
	@Override
	public IRange getRange() { return range; }
	@Override
	public boolean requiresRestart() { return mode == ReloadMode.GAME; }
	@Override
	public boolean requiresReload() { return mode == ReloadMode.WORLD; }
	@Override
	public Component getName() { return name; }
	@Override
	public Component getTooltip() { return tooltip; }
	@Override
	public String getDefault() { return defaultValue; }
	@Override
	public String get() { return current; }
	@Override
	public void set(String value) {
		this.current = value;
		if(autosave) save();
	}
	@Override
	public ParseResult<Boolean> isValid(String value) { return isValid.apply(value); }
	@Override
	public DataType getDataType() { return type; }
	@Override
	public boolean isForcingSuggestions() { return forced; }
	@Override
	public List<Suggestion> getSuggestions() { return suggestions.get(); }
}
