package carbonconfiglib.gui.nodes;

import java.util.function.Consumer;

import carbonconfiglib.gui.api.node.IValueNode;
import carbonconfiglib.gui.api.suggestion.ISuggestionRenderer;
import carbonconfiglib.gui.api.types.DataType;
import carbonconfiglib.gui.base.helpers.Align;
import net.minecraft.util.text.ITextComponent;

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
public class RegistryElement extends SelectionElement
{
	ISuggestionRenderer renderer;
	int[] bounds = new int[2];
	ITextComponent last;
	public RegistryElement(IValueNode node, ISuggestionRenderer renderer) {
		super(node);
		this.renderer = renderer;
	}
	
	@Override
	public void renderRightPart(int left, int top, int desiredWidth, int width, int height, int mouseX, int mouseY, boolean selected, float partialTicks) {
		bounds[0] = left;
		bounds[1] = top;
		if(renderer != null) {
			last = renderer.renderSuggestion(node.get(), left, (int)Align.CENTER.alignStart(top, height, 16));
			left += 20;
		}
		super.renderRightPart(left, top, desiredWidth, width, height, mouseX, mouseY, selected, partialTicks);
	}
	
	@Override
	public void provideTooltips(int mouseX, int mouseY, Consumer<ITextComponent> tooltips) {
		if(last != null && mouseX >= bounds[0] && mouseX < bounds[0] + 20 && mouseY >= bounds[1] && mouseY < bounds[1] + 20) {
			tooltips.accept(last);
		}
		super.provideTooltips(mouseX, mouseY, tooltips);
	}
	
	
	public static DataType createForType(Class<?> clz, String defaultValue) {
		ISuggestionRenderer renderer = ISuggestionRenderer.Registry.getRendererForType(clz);
		return new DataType(defaultValue, T -> new RegistryElement(T, renderer));
	}
}
