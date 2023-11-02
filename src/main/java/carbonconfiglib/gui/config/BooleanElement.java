package carbonconfiglib.gui.config;

import carbonconfiglib.gui.api.IArrayNode;
import carbonconfiglib.gui.api.IConfigNode;
import carbonconfiglib.gui.api.IValueNode;
import carbonconfiglib.gui.widgets.CarbonButton;
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
public class BooleanElement extends ConfigElement
{
	private Button trueButton;
	private Button falseButton;
	
	public BooleanElement(IConfigNode node, IValueNode value) {
		super(node, value);
	}
	
	public BooleanElement(IConfigNode node, IArrayNode array, int index) {
		super(node, array, index);
	}
	
	@Override
	public void init() {
		super.init();
		int width = isArray() ? 72 : 36;
		trueButton = addChild(new CarbonButton(0, 0, width, 18, new TranslatableComponent("gui.carbonconfig.boolean.true"), this::onTrue), isArray() ? GuiAlign.CENTER : GuiAlign.RIGHT, isArray() ? width/2 : width);
		falseButton = addChild(new CarbonButton(0, 0, width, 18, new TranslatableComponent("gui.carbonconfig.boolean.false"), this::onFalse), isArray() ? GuiAlign.CENTER : GuiAlign.RIGHT, isArray() ? -(width/2) : 0);
		updateData();
	}
	
	private void updateData() {
		boolean isTrue = Boolean.parseBoolean(value.get());
		trueButton.active = !isTrue;
		falseButton.active = isTrue;		
	}
	
	@Override
	public void tick() {
		super.tick();
		updateData();
	}
	
	protected void onTrue(Button button) {
		value.set("true");
	}
	
	protected void onFalse(Button button) {
		value.set("false");
	}
}
