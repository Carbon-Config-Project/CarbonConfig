package carbonconfiglib.gui.nodes;

import carbonconfiglib.gui.api.node.IValueNode;
import carbonconfiglib.gui.base.widgets.CarbonEditBox;
import carbonconfiglib.gui.base.widgets.CarbonEditBox.TextState;
import carbonconfiglib.gui.nodes.base.ValueElement;
import carbonconfiglib.utils.ParseResult;
import net.minecraft.client.gui.GuiGraphicsExtractor;

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
public class StringElement extends ValueElement
{
	CarbonEditBox text = addChild(new CarbonEditBox(getFont(), 0, 0, Integer.MAX_VALUE, 0, new TextState().setMaxLength(Integer.MAX_VALUE)));
	ParseResult<Boolean> result;

	public StringElement(IValueNode node) {
		super(node);
		readValue();
		text.moveCursorToStart(false);
		text.getState().setCallback(this::onTextChanged);
	}
	
	@Override
	protected void readValue() {
		text.getState().setValue(node.get());
	}
	
	@Override
	public void setEditable(boolean value) {
		text.active = value;
		if(!value) text.setFocused(false);
	}
	
	@Override
	protected void setRightComponentsVisible(boolean value) {
		text.visible = value;
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
		}
	}
	
	@Override
	public void extractRightPart(GuiGraphicsExtractor graphics, int left, int top, int desiredWidth, int width, int height, int mouseX, int mouseY, boolean selected, float partialTicks) {
		text.setX(left);
		text.setY(top);
		text.setWidth(desiredWidth);
		text.setHeight(height);
		text.extractRenderState(graphics, mouseX, mouseY, partialTicks);
	}
}
