package carbonconfiglib.gui.impl.minecraft;

import java.util.List;
import java.util.Objects;

import carbonconfiglib.api.IEntrySettings;
import carbonconfiglib.api.IRange;
import carbonconfiglib.api.ISuggestionProvider.Suggestion;
import carbonconfiglib.gui.api.DataType;
import carbonconfiglib.gui.api.IConfigNode;
import carbonconfiglib.gui.api.IValueNode;
import carbonconfiglib.utils.ParseResult;
import carbonconfiglib.utils.structure.IStructuredData.StructureType;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.ChatFormatting;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
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
public class MinecraftValue implements IValueNode
{
	ObjectArrayList<String> previous = new ObjectArrayList<>();
	IGameRuleValue entry;
	String defaultValue;
	String savedValue;
	String current;
	boolean autosave;
	
	public MinecraftValue(IGameRuleValue entry) {
		this.entry = entry;
		this.defaultValue = entry.getDefault(); 
		this.current = entry.get();
		this.savedValue = current;
		this.previous.push(current);
	}
	
	public MinecraftValue withAutosave() {
		autosave = true;
		return this;
	}
	
	public void save() {
		entry.set(current); 
		savedValue = current;
	}
	
	@Override
	public boolean isDefault() { return Objects.equals(current, defaultValue); }
	@Override
	public boolean isChanged() { return !Objects.equals(previous.top(), current); }
	@Override
	public boolean isUnsaved() { return !Objects.equals(current, savedValue); }
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
	public String getDefault() { return defaultValue; }
	@Override
	public String get() { return current; }
	@Override
	public void set(String value) {
		this.current = value; 
		if(autosave) save();
	}
	@Override
	public ParseResult<Boolean> isValid(String value) { return entry.isValid(value); }
	@Override
	public StructureType getNodeType() { return StructureType.SIMPLE; }
	@Override
	public IEntrySettings getSettings() { return null; }
	@Override
	public IRange getRange() { return null; }
	@Override
	public boolean requiresRestart() { return false; }
	@Override
	public boolean requiresReload() { return false; }
	@Override
	public Component getName() { return IConfigNode.createLabel(I18n.get(entry.getDescriptionId())); }
	@Override
	public Component getTooltip() {
		String id = entry.getDescriptionId();
		MutableComponent result = Component.empty();
		id += ".description";
		if(I18n.exists(id)) {
			result.append("\n").append(Component.translatable(id).withStyle(ChatFormatting.GRAY));
		}
		return result;
	}
	@Override
	public DataType getDataType() { return entry.getType(); }
	@Override
	public boolean isForcingSuggestions() { return false; }
	@Override
	public List<Suggestion> getSuggestions() { return ObjectLists.empty(); }
}
