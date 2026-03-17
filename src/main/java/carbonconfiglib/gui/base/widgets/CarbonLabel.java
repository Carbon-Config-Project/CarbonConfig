package carbonconfiglib.gui.base.widgets;

import java.util.function.Consumer;
import java.util.function.Function;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import carbonconfiglib.gui.api.Texts;
import carbonconfiglib.gui.base.helpers.GuiUtils;
import carbonconfiglib.gui.base.helpers.ITooltipProvider;
import carbonconfiglib.gui.base.helpers.Icon;
import net.minecraft.client.gui.widget.Widget;
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
public class CarbonLabel extends Widget implements ITooltipProvider
{
	protected Function<CarbonLabel, ITextComponent> tooltip;
	protected Icon icon;
	public CarbonLabel(int x, int y, int width, int height, Icon icon) {
		super(x, y, width, height, Texts.empty());
		this.icon = icon;
	}
	
	@Override
	protected boolean isValidClickButton(int pButton) {
		return false;
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public void renderButton(MatrixStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
		int j = getFGColor();
		RenderSystem.color4f(((j >> 16) & 0xFF) / 255F, ((j >> 8) & 0xFF) / 255F, (j & 0xFF) / 255F, 1F);
		GuiUtils.drawTextureRegion(pPoseStack, x+2, y+2, width-4, height-4, icon, 16, 16);
		RenderSystem.color4f(1F, 1F, 1F, 1F);
	}
	
	@SuppressWarnings("unchecked")
	public <T extends CarbonLabel> T withTooltip(ITextComponent tooltip) {
		this.tooltip = T -> tooltip;
		return (T)this;
	}
	
	@SuppressWarnings("unchecked")
	public <T extends CarbonLabel> T withTooltip(Function<T, ITextComponent> tooltip) {
		this.tooltip = (Function<CarbonLabel, ITextComponent>)tooltip;
		return (T)this;
	} 
	
	protected boolean canShowTooltip(double mouseX, double mouseY) {
		return this.visible && mouseX >= (double)this.x && mouseY >= (double)this.y && mouseX < (double)(this.x + this.width) && mouseY < (double)(this.y + this.height);
	}
	
	@Override
	public void provideTooltips(int mouseX, int mouseY, Consumer<ITextComponent> tooltips) {
		if(tooltip != null && canShowTooltip(mouseX, mouseY)) {
			ITextComponent result = tooltip.apply(this);
			if(result == null) return;
			tooltips.accept(result);
		}
	}
	
}
