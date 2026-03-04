package carbonconfiglib.gui.nodes.base;

import java.util.Objects;

import com.mojang.blaze3d.vertex.PoseStack;

import carbonconfiglib.api.ISuggestionProvider.Suggestion;
import carbonconfiglib.gui.api.suggestion.ISuggestionRenderer;
import carbonconfiglib.gui.base.helpers.Align;
import carbonconfiglib.gui.base.helpers.GuiUtils;
import carbonconfiglib.gui.base.widgets.CarbonList.ListEntry;
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
public class SuggestionEntry extends ListEntry<SuggestionEntry> {
	Component text;
	Suggestion suggestion;
	
	public SuggestionEntry(Suggestion suggestion) {
		this.suggestion = suggestion;
		this.text = Component.literal(suggestion.getName());
	}

	@Override
	public boolean containsSearch(String searchString) {
		return false;
	}

	@Override
	public void render(PoseStack poseStack, int x, int top, int left, int width, int height, int mouseX, int mouseY, boolean selected, float partialTicks) {
		ISuggestionRenderer renderer = ISuggestionRenderer.Registry.getRendererForType(suggestion.getType());
		if(renderer != null) {
			renderer.renderSuggestion(poseStack, suggestion.getValue(), left, (int)Align.CENTER.alignStart(top, height, 16));
			left += 20;
			width -= 20;
		}
		GuiUtils.drawScrollingShadowText(poseStack, font, text, left, top, width, height, Align.CENTER, -1, Objects.hashCode(suggestion));
	}
}