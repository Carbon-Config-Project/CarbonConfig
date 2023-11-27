package carbonconfiglib.gui.widgets;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;

import carbonconfiglib.gui.config.IListOwner;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.button.AbstractButton;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
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
public class CarbonIconCheckbox extends AbstractButton
{
	private static final ResourceLocation TEXTURE = new ResourceLocation("textures/gui/checkbox.png");
	private boolean selected;
	Icon selectedIcon;
	Icon unselectedIcon;
	Runnable listener;
	ITextComponent tooltip;
	IListOwner owner;
	
	public CarbonIconCheckbox(int x, int y, int width, int height, Icon selectedIcon, Icon unselectedIcon, boolean selected) {
		super(x, y, width, height, "");
		this.selectedIcon = selectedIcon;
		this.unselectedIcon = unselectedIcon;
		this.selected = selected;
	}
	
	public CarbonIconCheckbox withListener(Runnable listener) {
		this.listener = listener;
		return this;
	}
	
	public CarbonIconCheckbox setTooltip(IListOwner owner, String tooltips) {
		this.owner = owner;
		tooltip = new TranslationTextComponent(tooltips);
		return this;
	}
	
	public void onPress() {
		this.selected = !this.selected;
		if(listener != null) listener.run();
	}
	
	public void setSelected(boolean value) {
		this.selected = value;
	}
	
	public boolean selected() {
		return this.selected;
	}
	
	@Override
	public void renderButton(int mouseX, int mouseY, float partialTicks) {
		Minecraft.getInstance().getTextureManager().bindTexture(TEXTURE);
		RenderSystem.enableDepthTest();
		RenderSystem.color4f(1F, 1F, 1F, 1F);
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
		
		GuiUtils.drawTextureRegion(x, y, isHovered() ? 20F : 0F, 0F, width, height, 20F, 20F, 64F, 64F);
		GuiUtils.drawTextureRegion(x+2, y+2, width-4, height-4, this.selected ? selectedIcon : unselectedIcon, 16, 16);
		if(owner != null && isMouseOver(mouseX, mouseY)) {
			owner.addTooltips(tooltip);
		}
	}

}