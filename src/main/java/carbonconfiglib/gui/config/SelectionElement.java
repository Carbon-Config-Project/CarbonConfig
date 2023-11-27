package carbonconfiglib.gui.config;

import carbonconfiglib.gui.api.IConfigNode;
import carbonconfiglib.gui.api.IValueNode;
import carbonconfiglib.gui.screen.ListSelectionScreen;
import carbonconfiglib.gui.widgets.CarbonButton;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.text.TranslationTextComponent;

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
public class SelectionElement extends ConfigElement
{
	Button textBox = addChild(new CarbonButton(0, 0, 72, 18, new TranslationTextComponent("gui.carbonconfig.edit"), this::onPress));
	
	public SelectionElement(IConfigNode node) {
		super(node);
	}
	
	public SelectionElement(IConfigNode node, IValueNode value) {
		super(node, value);
	}
	
	private void onPress(Button button) {
		mc.displayGuiScreen(ListSelectionScreen.ofValue(mc.currentScreen, node, value, owner.getCustomTexture()));
	}
}
