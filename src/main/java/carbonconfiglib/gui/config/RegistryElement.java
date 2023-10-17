package carbonconfiglib.gui.config;

import carbonconfiglib.gui.api.*;
import carbonconfiglib.gui.widgets.CarbonEditBox;
import carbonconfiglib.utils.ParseResult;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;

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
public class RegistryElement extends ConfigElement
{
	EditBox edit;
	ParseResult<Boolean> result;
	ISuggestionRenderer renderer;
	
	public RegistryElement(IConfigNode node, IValueNode value, ISuggestionRenderer renderer) {
		super(node, value);
		this.renderer = renderer;
	}
	
	public RegistryElement(IConfigNode node, IArrayNode array, int index, ISuggestionRenderer renderer) {
		super(node, array, index);
		this.renderer = renderer;
	}
	
	public static DataType createForType(Class<?> clz, String defaultValue) {
		ISuggestionRenderer renderer = ISuggestionRenderer.SuggestionRegistry.getRendererForType(clz);
		return new DataType(false, defaultValue, (K, V) -> new RegistryElement(K, V, renderer), (K, V, E) -> new RegistryElement(K, V, E, renderer));
	}
	
	@Override
	public void init() {
		super.init();
		if(this.isArray()) {
			edit = addChild(new CarbonEditBox(font, 0, 0, 130, 18), GuiAlign.CENTER, 0);
			edit.setValue(value.get());
			edit.setResponder(T -> {
				edit.setTextColor(0xE0E0E0);
				result = null;
				if(!T.isEmpty() && !(result = value.isValid(T)).getValue()) {
					edit.setTextColor(0xFF0000);
					return;
				}
				value.set(T);
			});
		}
	}
	
	@Override
	protected int getMaxX(int prevMaxX) {
		return prevMaxX - (renderer == null ? 0 : 25);
	}
	
	@Override
	public void tick() {
		super.tick();
		if(edit != null) {
			edit.tick();
		}
	}
	
	@Override
	public void render(PoseStack poseStack, int x, int top, int left, int width, int height, int mouseX, int mouseY, boolean selected, float partialTicks) {
		super.render(poseStack, x, top, left, width, height, mouseX, mouseY, selected, partialTicks);
		if(renderer != null) {
			Component result = renderer.renderSuggestion(poseStack, value.get(), left + 20, top);
			if(result != null && mouseX >= left + 20 && mouseX <= left + 40 && mouseY >= top && mouseY <= top + 20) {
				owner.addTooltips(result);
			}
		}
		if(edit != null && edit.isMouseOver(mouseX, mouseY) && result != null && !result.getValue()) {
			owner.addTooltips(new TextComponent(result.getError().getMessage()).withStyle(ChatFormatting.RED));
		}
		
	}
	
	@Override
	public void updateValues() {
		if(edit != null) {
			edit.setValue(value.get());
		}
	}
}
