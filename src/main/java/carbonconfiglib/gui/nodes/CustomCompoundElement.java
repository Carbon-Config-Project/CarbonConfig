package carbonconfiglib.gui.nodes;

import java.util.function.Function;

import com.mojang.blaze3d.vertex.PoseStack;

import carbonconfiglib.CarbonConfig;
import carbonconfiglib.gui.api.node.ICompoundNode;
import carbonconfiglib.gui.base.helpers.Align;
import carbonconfiglib.gui.base.helpers.GuiUtils;
import carbonconfiglib.gui.base.screen.BaseCarbonScreen;
import carbonconfiglib.gui.base.widgets.CarbonButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
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
public class CustomCompoundElement extends NodeElement
{
	ICompoundNode node;
	CarbonButton button;
	Function<ICompoundNode, Screen> creator;
	
	public CustomCompoundElement(ICompoundNode node, Function<ICompoundNode, Screen> creator) {
		super(node);
		this.node = node;
		this.creator = creator;
		button = addChild(new CarbonButton(0, 0, 0, 0, node.getName(), this::onClick));
	}
	
	@Override
	protected void setRightComponentsVisible(boolean value) {}
	@Override
	public void setEditable(boolean value) {}
	@Override
	protected boolean isValue() { return false; }
	
	@Override
	public void renderLeftPart(PoseStack stack, int left, int top, int width, int height, int mouseX, int mouseY, boolean selected, float partialTicks) {
		GuiUtils.drawScrollingShadowText(stack, font, shouldRenderIndex() ? Component.literal(index(node)+": ") : node.getName(), left, top, width, height, Align.START, -1, node.hashCode());
	}
	
	@Override
	public void renderRightPart(PoseStack stack, int left, int top, int width, int height, int mouseX, int mouseY, boolean selected, float partialTicks) {
		button.x = left;
		button.y = top;
		button.setWidth(width);
		button.setHeight(height);
		button.render(stack, mouseX, mouseY, partialTicks);
	}
	
	protected void onClick(Button button) {
		if(creator == null) {
			CarbonConfig.LOGGER.info("Custom Compound function isn't implemented");
			return;
		}
		Screen screen = creator.apply(node);
		if(screen == null) {
			CarbonConfig.LOGGER.info("Custom Compound function created a null Object");
			return;
		}
		BaseCarbonScreen.pushExternalScreen(screen);
	}
}
