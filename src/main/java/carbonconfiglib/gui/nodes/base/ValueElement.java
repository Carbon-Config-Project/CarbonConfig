package carbonconfiglib.gui.nodes.base;

import carbonconfiglib.api.ISuggestionProvider.Suggestion;
import carbonconfiglib.gui.api.node.IValueNode;
import carbonconfiglib.gui.api.types.EntrySettingTypes.ForceMode;
import carbonconfiglib.gui.base.helpers.Align;
import carbonconfiglib.gui.base.helpers.GuiUtils;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

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
public abstract class ValueElement extends NodeElement
{
	protected IValueNode node;

	public ValueElement(IValueNode node) {
		super(node);
		this.node = node;
		if(suggestionState != null) {
			String value = node.get();
			suggestionState.findSelected(T -> value.equals(T.getValue()));
		}
	}
	
	@Override
	protected boolean isValue() { return true; }
	@Override
	protected abstract void readValue();
	
	protected boolean isForcingText() {
		ForceMode mode = node.getSetting(ForceMode.class);
		return mode != null && mode.isForcingText();
	}
	
	protected boolean isForcingAlternative() {
		ForceMode mode = node.getSetting(ForceMode.class);
		return mode != null && !mode.isForcingText();
	}
	
	protected void setValue(String value) {
		node.set(value);
		onValueChanged();
	}
	
	@Override
	public void extractLeftPart(GuiGraphicsExtractor graphics, int left, int top, int width, int height, int mouseX, int mouseY, boolean selected, float partialTicks) {
		GuiUtils.drawScrollingShadowText(graphics, font, shouldRenderIndex() ? Component.literal(index(node)+": ") : node.getName(), left, top, width, height, Align.START, -1, sinceFullyVisible);
	}
	
	@Override
	public void extractRightPart(GuiGraphicsExtractor graphics, int left, int top, int desiredWidth, int width, int height, int mouseX, int mouseY, boolean selected, float partialTicks) {
		
	}
	
	@Override
	protected void setSuggestion(Suggestion suggestion) {
		node.set(suggestion.getValue());
		readValue();
	}
	
	@Override
	protected void onValueChanged() {
		super.onValueChanged();
		String value = node.get();
		suggestionState.findSelected(T -> value.equals(T.getValue()));
	}
}
