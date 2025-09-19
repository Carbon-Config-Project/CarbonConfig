package carbonconfiglib.gui.config;

import carbonconfiglib.gui.api.IArrayNode;
import carbonconfiglib.gui.api.ICompoundNode;
import carbonconfiglib.gui.api.IValueNode;
import carbonconfiglib.gui.screen.CompoundScreen;
import carbonconfiglib.gui.widgets.CarbonButton;
import carbonconfiglib.gui.widgets.CarbonIconButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

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
public class CompoundElement extends ConfigElement
{
	Button textBox;
	ICompoundNode compound;
	
	public CompoundElement(ICompoundNode compound) {
		super(compound.getName());
		this.compound = compound;
	}
	
	public CompoundElement(IArrayNode array, ICompoundNode compound) {
		super(array, compound.getName());
		this.compound = compound;

	}
	
	public CompoundElement(ICompoundNode owner, ICompoundNode compound) {
		super(owner, compound.getName());
		this.compound = compound;
	}
	
	@Override
	public void init() {
		super.init();
		Component result = isArray() ? ConfigElement.create(array, compound) : null;
		textBox = addChild(new CarbonButton(0, 0, isArray() ? 190 : isCompound() ? 105 : 72, 18, result != null ? result : Component.translatable("gui.carbonconfig.edit"), this::onPress), isArray() ? GuiAlign.CENTER : GuiAlign.RIGHT, 0);
	}
	
	private void onPress(Button button) {
		mc.setScreen(new CompoundScreen(compound, mc.screen, owner.getCustomTexture()));
	}
	
	@Override
	protected boolean createResetButtons(IValueNode value) {
		return true;
	}
	
	@Override
	protected int indexOf() {
		return array.indexOf(compound);
	}
	
	@Override
	protected boolean isReset() {
		return isArray() || compound.isChanged();
	}
	
	@Override
	public boolean isChanged() {
		return compound.isChanged();
	}
	
	@Override
	public boolean isDefault() {
		return compound.isDefault();
	}
	
	@Override
	protected boolean requiresReload() {
		return compound.requiresReload();
	}
	
	@Override
	protected boolean requiresRestart() {
		return compound.requiresRestart();
	}
	
	@Override
	protected Component tooltip() {
		return compound.getTooltip();
	}
	
	@Override
	protected void onDefault(CarbonIconButton button) {
		compound.setDefault();
		updateValues();
	}
	
	@Override
	protected void onReset(CarbonIconButton button) {
		compound.setPrevious();
		updateValues();
	}
	
}
