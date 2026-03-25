package carbonconfiglib.gui.nodes;

import carbonconfiglib.gui.api.node.IValueNode;
import carbonconfiglib.gui.base.helpers.Align;
import carbonconfiglib.gui.base.helpers.Icon;
import carbonconfiglib.gui.base.widgets.CarbonCheckBox;
import carbonconfiglib.gui.base.widgets.CarbonCheckBox.CheckBoxState;
import carbonconfiglib.gui.nodes.base.ValueElement;

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
public class BooleanElement extends ValueElement
{
	CarbonCheckBox box = addChild(new CarbonCheckBox(0, 0, 14, 14, new CheckBoxState(Icon.SELECTED).setCallback(this::onCallback)));
	
	public BooleanElement(IValueNode node) {
		super(node);
		readValue();
	}
	
	@Override
	protected void readValue() {
		box.getState().setValue(Boolean.parseBoolean(node.get()));
	}
	
	@Override
	protected boolean allowSuggestions() {
		return false;
	}
	
	@Override
	public void setEditable(boolean value) {
		box.setActive(value);
	}
	
	@Override
	protected void setRightComponentsVisible(boolean value) {
		box.visible = value;
	}
	
	private void onCallback(CheckBoxState state) {
		setValue(Boolean.toString(state.getValue()));
	}
	
	@Override
	public void renderRightPart(int left, int top, int desiredWidth, int width, int height, int mouseX, int mouseY, boolean selected, float partialTicks) {
		box.x = left;
		box.y = (int)Align.CENTER.alignStart(top, height, box.getHeight());
		box.render(mouseX, mouseY, partialTicks);
	}
}
