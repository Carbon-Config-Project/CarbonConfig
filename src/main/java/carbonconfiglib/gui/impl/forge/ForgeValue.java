package carbonconfiglib.gui.impl.forge;

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

public class ForgeValue implements IValueNode
{
	Component name;
	Component tooltip;
	DataType type;
	IRange range;
	ReloadMode mode;
	Function<String, ParseResult<?>> isValid;
	Supplier<List<Suggestion>> suggestions;
	BiConsumer<String, ForgeValue> saved;
	
	Stack<String> previous = new ObjectArrayList<>();
	String current;
	String defaultValue;
	boolean autosave;
	
	public ForgeValue(Component name, Component tooltip, ReloadMode mode, DataType type, IRange range, String value, String defaultValue, Supplier<List<Suggestion>> suggestions, Function<String, ParseResult<?>> isValid, BiConsumer<String, ForgeValue> saved) {
		this.name = name;
		this.tooltip = tooltip;
		this.isValid = isValid;
		this.mode = mode;
		this.range = range;
		this.type = type;
		this.current = value;
		previous.push(current);
		this.defaultValue = defaultValue;
		this.suggestions = suggestions;
		this.saved = saved;
	}
	public ForgeValue withAutosave() {
		autosave = true;
		return this;
	}
	
	public void save() { saved.accept(current, this); }
	@Override
	public boolean isDefault() { return Objects.equals(defaultValue, current); }
	@Override
	public boolean isChanged() { return !Objects.equals(previous.top(), current); }
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
	public IRange getRange() { return range; }
	@Override
	public IEntrySettings getSettings() { return null; }
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
		if(autosave) save();
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
