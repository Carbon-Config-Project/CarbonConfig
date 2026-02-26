package carbonconfiglib.gui.nodes;

import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;

import carbonconfiglib.api.ISuggestionProvider.Suggestion;
import carbonconfiglib.gui.api.node.IValueNode;
import carbonconfiglib.gui.base.widgets.CarbonEditBox;
import carbonconfiglib.gui.base.widgets.DropDownMenu;
import carbonconfiglib.gui.base.widgets.DropDownMenu.DropDownState;
import carbonconfiglib.gui.nodes.base.SuggestionEntry;
import carbonconfiglib.gui.nodes.base.ValueElement;
import carbonconfiglib.utils.ParseResult;
import net.minecraft.network.chat.Component;
import speiger.src.collections.objects.utils.ObjectLists;

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
public class SelectionElement extends ValueElement
{
	protected DropDownState<Suggestion> state = new DropDownState<Suggestion>(T -> Component.literal(T.getName()), ObjectLists.empty()).valueOnly(true).allowEmpty(false).withListener(this::onSelectionChanged);
	protected DropDownMenu<Suggestion> values = addChild(new DropDownMenu<>(0, 0, 0, 0, state));
	protected CarbonEditBox text = addChild(new CarbonEditBox(getFont(), 0, 0, Integer.MAX_VALUE, 0));
	protected ParseResult<Boolean> result;
	
	public SelectionElement(IValueNode node) {
		super(node);
		List<Suggestion> suggestions = node.getSuggestions();
		state.setValues(suggestions);
		state.findDefaultSelected(T -> T.getValue().equals(node.getDefault()));
		state.findSelected(T -> T.getValue().equals(node.get()));
		if(!suggestions.isEmpty() && suggestions.get(0).getType() != null) {
			state.withCustomRenderer(SuggestionEntry::new).setElementHeight(22);
		}
		readValue();
		text.getState().setCallback(this::onTextChanged);
	}
	
	@Override
	protected void readValue() {
		text.getState().setValue(node.get());		
	}
	
	@Override
	public void setEditable(boolean value) {
		text.active = value;
		values.active = value;
		if(!value) text.setFocus(false);
	}
	
	private void onTextChanged(String value) {
		text.setTextColor(0xE0E0E0);
		result = null;
		if(!value.isEmpty()) {
			result = node.isValid(value);
			if(!result.getValue()) {
				text.setTextColor(0xFF0000);
				return;
			}
			setValue(value);
			state.findSelected(T -> T.getValue().equals(value));
		}
	}
	
	private void onSelectionChanged(List<Suggestion> values) {
		if(values.isEmpty()) return;
		result = null;
		String value = values.get(0).getValue();
		if(value.isEmpty()) return;
		result = node.isValid(value);
		if(!result.getValue()) {
			text.setTextColor(0xFF0000);
			return;
		}
		setValue(value);
		text.getState().setSilentValue(value);
	}
	
	//We disable this because selection already includes suggestions
	@Override
	protected List<Suggestion> getSuggestions() {
		return ObjectLists.empty();
	}
	
	@Override
	protected void setRightComponentsVisible(boolean value) {
		values.visible = value;
		text.visible = !values.visible && value;
	}
	
	@Override
	public void renderRightPart(PoseStack stack, int left, int top, int width, int height, int mouseX, int mouseY, boolean selected, float partialTicks) {
		values.x = left;
		values.y = top;
		values.setWidth(width);
		values.setHeight(height);
		values.render(stack, mouseX, mouseY, partialTicks);
		
		text.x = left;
		text.y = top;
		text.setWidth(width);
		text.setHeight(height);
		text.render(stack, mouseX, mouseY, partialTicks);
	}
	
}
