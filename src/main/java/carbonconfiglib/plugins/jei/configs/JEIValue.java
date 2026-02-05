package carbonconfiglib.plugins.jei.configs;

import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

import carbonconfiglib.api.IEntrySettings;
import carbonconfiglib.api.IRange;
import carbonconfiglib.api.ISuggestionProvider.Suggestion;
import carbonconfiglib.gui.api.DataType;
import carbonconfiglib.gui.api.IValueNode;
import carbonconfiglib.impl.ReloadMode;
import carbonconfiglib.utils.ParseResult;
import carbonconfiglib.utils.structure.IStructuredData.StructureType;
import net.minecraft.network.chat.Component;
import speiger.src.collections.objects.lists.ObjectArrayList;
import speiger.src.collections.utils.Stack;

public class JEIValue implements IValueNode
{
	Component name;
	Component tooltip;
	DataType type;
	ReloadMode mode;
	IRange range;
	Function<String, ParseResult<?>> isValid;
	Supplier<List<Suggestion>> suggestions;
	BiConsumer<String, JEIValue> saved;
	
	Stack<String> previous = new ObjectArrayList<>();
	String current;
	String savedValue;
	String defaultValue;
	boolean autosave;
	
	public JEIValue(Component name, Component tooltip, ReloadMode mode, IRange range, DataType type, String value, String defaultValue, Supplier<List<Suggestion>> suggestions, Function<String, ParseResult<?>> isValid, BiConsumer<String, JEIValue> saved) {
		this.name = name;
		this.tooltip = tooltip;
		this.isValid = isValid;
		this.mode = mode;
		this.range = range;
		this.type = type;
		this.current = value;
		this.savedValue = value;
		previous.push(current);
		this.defaultValue = defaultValue;
		this.suggestions = suggestions;
		this.saved = saved;
	}
	
	public JEIValue withAutosave() {
		autosave = true;
		return this;
	}
	
	private void autosave() {
		if(!autosave) return;
		save();
	}
	
	public void save() {
		saved.accept(current, this);
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
		autosave();
	}
	@Override
	public void setPrevious() {
		current = previous.top();
		if(previous.size() > 1) previous.pop();
		autosave();
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
	public IEntrySettings getSettings() { return null; }
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
		current = value; 
		autosave();
	}
	@Override
	public ParseResult<Boolean> isValid(String value) {
		ParseResult<?> parse = isValid.apply(value); 
		return parse.hasError() ? parse.withDefault(false) : ParseResult.success(true); 
	}
	@Override
	public DataType getDataType() { return type; }
	@Override
	public boolean isForcingSuggestions() { return type == DataType.ENUM; }
	@Override
	public List<Suggestion> getSuggestions() { return suggestions.get(); }
	
}
