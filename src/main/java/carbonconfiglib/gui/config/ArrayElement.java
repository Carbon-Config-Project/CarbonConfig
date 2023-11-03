package carbonconfiglib.gui.config;

import carbonconfiglib.gui.api.IArrayNode;
import carbonconfiglib.gui.api.IConfigNode;
import carbonconfiglib.gui.api.IValueNode;
import carbonconfiglib.gui.screen.ArrayScreen;
import carbonconfiglib.gui.widgets.CarbonButton;
import carbonconfiglib.gui.widgets.CarbonIconButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.TranslatableComponent;

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
public class ArrayElement extends ConfigElement
{
	Button textBox = addChild(new CarbonButton(0, 0, 72, 18, new TranslatableComponent("gui.carbonconfig.edit"), this::onPress));
	IArrayNode array;
	
	public ArrayElement(IConfigNode node) {
		super(node);
		array = node.asArray();
	}
	
	private void onPress(Button button) {
		mc.setScreen(new ArrayScreen(node, mc.screen, owner.getCustomTexture()));
	}
	
	@Override
	protected boolean createResetButtons(IValueNode value) {
		return true;
	}
	
	@Override
	protected boolean isReset() {
		return array.isChanged();
	}
	
	@Override
	public boolean isChanged() {
		return array.isChanged();
	}
	
	@Override
	public boolean isDefault() {
		return array.isDefault();
	}
	
	@Override
	protected void onDefault(CarbonIconButton button) {
		array.setDefault();
		updateValues();
	}
	
	@Override
	protected void onReset(CarbonIconButton button) {
		array.setPrevious();
		updateValues();
	}
}
