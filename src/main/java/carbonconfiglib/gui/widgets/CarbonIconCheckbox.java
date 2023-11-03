package carbonconfiglib.gui.widgets;

import carbonconfiglib.gui.config.IListOwner;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;

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
	Component tooltip;
	IListOwner owner;
	
	public CarbonIconCheckbox(int x, int y, int width, int height, Icon selectedIcon, Icon unselectedIcon, boolean selected) {
		super(x, y, width, height, new TextComponent(""));
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
		tooltip = new TranslatableComponent(tooltips);
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
	
	public void renderButton(PoseStack stack, int mouseX, int mouseY, float partialTicks) {
		RenderSystem.setShaderTexture(0, TEXTURE);
		RenderSystem.enableDepthTest();
		RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
		
		GuiUtils.drawTextureRegion(stack, x, y, isHoveredOrFocused() ? 20F : 0F, 0F, width, height, 20F, 20F, 64F, 64F);
		GuiUtils.drawTextureRegion(stack, x+2, y+2, width-4, height-4, this.selected ? selectedIcon : unselectedIcon, 16, 16);
		if(owner != null && isMouseOver(mouseX, mouseY)) {
			owner.addTooltips(tooltip);
		}
	}

	@Override
	public void updateNarration(NarrationElementOutput output) {
		output.add(NarratedElementType.TITLE, this.createNarrationMessage());
		if(!this.active) return;
		output.add(NarratedElementType.USAGE, new TranslatableComponent(isFocused() ? "narration.checkbox.usage.hovered" : "narration.checkbox.usage.focused"));		
	}

}