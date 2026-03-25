package carbonconfiglib.gui.nodes;

import java.util.function.BiFunction;

import carbonconfiglib.CarbonConfig;
import carbonconfiglib.gui.api.background.BackgroundTexture.BackgroundHolder;
import carbonconfiglib.gui.api.node.ICompoundNode;
import carbonconfiglib.gui.base.helpers.Align;
import carbonconfiglib.gui.base.helpers.GuiUtils;
import carbonconfiglib.gui.base.helpers.Texts;
import carbonconfiglib.gui.base.screen.BaseCarbonScreen;
import carbonconfiglib.gui.base.widgets.CarbonBaseButton;
import carbonconfiglib.gui.base.widgets.CarbonButton;
import carbonconfiglib.gui.nodes.base.NodeElement;
import net.minecraft.client.gui.GuiScreen;

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
	BiFunction<ICompoundNode, BackgroundHolder, GuiScreen> creator;
	
	public CustomCompoundElement(ICompoundNode node, BiFunction<ICompoundNode, BackgroundHolder, GuiScreen> creator) {
		super(node);
		this.node = node;
		this.creator = creator;
		button = addChild(new CarbonButton(0, 0, 0, 0, Texts.translatable("gui.carbonconfig.edit"), this::onClick));
	}
	
	@Override
	protected void setRightComponentsVisible(boolean value) {}
	@Override
	public void setEditable(boolean value) {}
	@Override
	protected boolean isValue() { return false; }
	
	@Override
	public void renderLeftPart(int left, int top, int width, int height, int mouseX, int mouseY, boolean selected, float partialTicks) {
		GuiUtils.drawScrollingShadowText(font, shouldRenderIndex() ? Texts.literal(index(node)+": ") : node.getName(), left, top, width, height, Align.START, -1, sinceFullyVisible);
	}
	
	@Override
	public void renderRightPart(int left, int top, int desiredWidth, int width, int height, int mouseX, int mouseY, boolean selected, float partialTicks) {
		button.x = left;
		button.y = top;
		button.setWidth(desiredWidth);
		button.setHeight(height);
		button.render(mouseX, mouseY, partialTicks);
	}
	
	protected void onClick(CarbonBaseButton button) {
		if(creator == null) {
			CarbonConfig.LOGGER.info("Custom Compound function isn't implemented");
			return;
		}
		GuiScreen screen = creator.apply(node, context.getHolder());
		if(screen == null) {
			CarbonConfig.LOGGER.info("Custom Compound function created a null Object");
			return;
		}
		BaseCarbonScreen.pushExternalScreen(screen);
	}
}
