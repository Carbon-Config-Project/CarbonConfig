package carbonconfiglib.gui.base.widgets;

import java.util.function.Consumer;
import java.util.function.Function;

import carbonconfiglib.gui.base.helpers.ITooltipProvider;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.input.InputWithModifiers;
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
public abstract class CarbonBaseButton extends Button implements ITooltipProvider
{
	protected Function<CarbonBaseButton, Component> tooltip;
	
	public CarbonBaseButton(int x, int y, int width, int height, Component message, OnPress callback) {
		super(x, y, width, height, message, callback, DEFAULT_NARRATION);
	}
	
	@Override
	public void onPress(InputWithModifiers input) {
		if(onPress != null) {
			onPress.onPress(this);
		}
	}
	
	@SuppressWarnings("unchecked")
	public <T extends CarbonBaseButton> T withTooltip(Component tooltip) {
		this.tooltip = _ -> tooltip;
		return (T)this;
	}
	
	@SuppressWarnings("unchecked")
	public <T extends CarbonBaseButton> T withTooltip(Function<T, Component> tooltip) {
		this.tooltip = (Function<CarbonBaseButton, Component>)tooltip;
		return (T)this;
	} 
	
	protected boolean canShowTooltip(double mouseX, double mouseY) {
		return this.visible && mouseX >= (double)this.getX() && mouseY >= (double)this.getY() && mouseX < (double)(this.getX() + this.width) && mouseY < (double)(this.getY() + this.height);
	}
	
	@Override
	public void provideTooltips(int mouseX, int mouseY, Consumer<Component> tooltips) {
		if(tooltip != null && canShowTooltip(mouseX, mouseY)) {
			isHovered = false;
			Component result = tooltip.apply(this);
			if(result == null) return;
			tooltips.accept(result);
		}
	}
}
