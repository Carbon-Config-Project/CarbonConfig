package carbonconfiglib.gui.config;

import com.mojang.blaze3d.vertex.PoseStack;

import carbonconfiglib.gui.api.IConfigNode;
import carbonconfiglib.gui.screen.ConfigScreen;
import carbonconfiglib.gui.screen.ConfigScreen.Navigator;
import carbonconfiglib.gui.widgets.CarbonButton;
import net.minecraft.client.gui.components.Button;
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
public class FolderElement extends ConfigElement
{
	Button button = addChild(new CarbonButton(0, 0, 0, 18, new TextComponent(""), this::onPress));
	Navigator nav;
	
	public FolderElement(IConfigNode node, Navigator prev)
	{
		super(node);
		button.setMessage(node.getName());
		nav = prev.add(node.getName(), node.getNodeName());
	}
	
	public void onPress(Button button) {
		mc.setScreen(new ConfigScreen(nav, node, mc.screen, owner.getCustomTexture()));
	}
	
	@Override
	public void render(PoseStack poseStack, int x, int top, int left, int width, int height, int mouseX, int mouseY, boolean selected, float partialTicks) {
		button.x = left;
		button.y = top;
		button.setWidth(width);
		button.render(poseStack, mouseX, mouseY, partialTicks);
	}
}
