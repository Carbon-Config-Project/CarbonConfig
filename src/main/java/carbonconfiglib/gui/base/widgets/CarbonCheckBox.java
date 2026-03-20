package carbonconfiglib.gui.base.widgets;

import java.util.function.Consumer;
import java.util.function.Function;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;

import carbonconfiglib.gui.base.helpers.GuiUtils;
import carbonconfiglib.gui.base.helpers.ITooltipProvider;
import carbonconfiglib.gui.base.helpers.Icon.IconPair;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;


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
public class CarbonCheckBox extends Checkbox implements ITooltipProvider {
	private static final ResourceLocation SELECTED_HIGHLIGHTED = new ResourceLocation("widget/checkbox_selected_highlighted");
	private static final ResourceLocation SELECTED = new ResourceLocation("widget/checkbox_selected");
	private static final ResourceLocation HIGLIGHTED = new ResourceLocation("widget/checkbox_highlighted");
	private static final ResourceLocation NORMAL = new ResourceLocation("widget/checkbox");
	CheckBoxState state;

	public CarbonCheckBox(int x, int y, int width, int height, CheckBoxState state) {
		super(x, y, width, height, Component.empty(), state.value);
		this.state = state;
	}

	@Override
	public void onPress() {
		state.updateValue(!state.getValue());
	}
	
	public void setHeight(int height) {
		this.height = height;
	}
	
	public CheckBoxState getState() {
		return state;
	}

	@Override
	public boolean selected() {
		return state.value;
	}

	public CarbonCheckBox withTooltip(Component tooltip) {
		this.state.tooltip = T -> tooltip;
		return this;
	}

	public CarbonCheckBox withTooltip(Function<CarbonCheckBox, Component> tooltip) {
		this.state.tooltip = (Function<CarbonCheckBox, Component>) tooltip;
		return this;
	}
	
	protected boolean canShowTooltip(double mouseX, double mouseY) {
		return this.visible && mouseX >= (double)this.getX() && mouseY >= (double)this.getY() && mouseX < (double)(this.getX() + this.width) && mouseY < (double)(this.getY() + this.height);
	}
	
	@Override
	public void provideTooltips(int mouseX, int mouseY, Consumer<Component> tooltips) {
		if (state.tooltip != null && canShowTooltip(mouseX, mouseY)) {
			Component result = state.tooltip.apply(this);
			if (result == null) return;
			tooltips.accept(result);
		}
	}

	@Override
	public void renderWidget(GuiGraphics graphics, int pMouseX, int pMouseY, float pPartialTick) {
		RenderSystem.enableDepthTest();
		RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
		
		boolean notVanilla = state.getIcon() != null;
		ResourceLocation location = isMouseOver(pMouseX, pMouseY) ? (!notVanilla && selected() ? SELECTED_HIGHLIGHTED : HIGLIGHTED) : (!notVanilla && selected() ? SELECTED : NORMAL);
		graphics.blitSprite(location, getX(), getY(), width, height);
		if(notVanilla) {
			GuiUtils.drawTextureRegion(graphics, getX()+2, getY()+2, width-4, height-4, selected() ? state.getIcon().active() : state.getIcon().inactive(), 16, 16);
		}
		if (state.label != null) {
			graphics.drawString(Minecraft.getInstance().font, state.label, this.getX() + 24, this.getY() + (this.height - 8) / 2, 14737632 | Mth.ceil(this.alpha * 255.0F) << 24);
		}
	}

	public static class CheckBoxState {
		protected Function<CarbonCheckBox, Component> tooltip;
		Consumer<CheckBoxState> callback;
		boolean value = true;
		Component label;
		IconPair icon;
		CarbonCheckBox owner;

		public CheckBoxState() {
		}

		public CheckBoxState(boolean value) {
			this.value = value;
		}
		
		public CheckBoxState(Component label) {
			this.label = label;
		}
		
		public CheckBoxState(boolean value, Component label) {
			this.value = value;
			this.label = label;
		}
		
		public CheckBoxState(IconPair icon) {
			withIcons(icon);
		}
		
		public CheckBoxState(boolean value, IconPair icon) {
			this.value = value;
			withIcons(icon);
		}

		void setOwner(CarbonCheckBox owner) {
			this.owner = owner;
		}

		public CheckBoxState setCallback(Consumer<CheckBoxState> listener) {
			this.callback = listener;
			return this;
		}

		public CheckBoxState withLabel(Component label) {
			this.label = label;
			return this;
		}

		public Component getLabel() {
			return label;
		}

		public CheckBoxState withIcons(IconPair pair) {
			this.icon = pair;
			if(icon != null && (icon.active() == null || icon.inactive() == null)) throw new IllegalStateException("You need to set the state properly");
			return this;
		}
		
		public IconPair getIcon() {
			return icon;
		}
		
		public CheckBoxState setTooltip(Component tooltip) {
			this.tooltip = T -> tooltip;
			return this;
		}

		public CheckBoxState withTooltip(Function<CarbonCheckBox, Component> tooltip) {
			this.tooltip = tooltip;
			return this;
		}

		public CarbonCheckBox getOwner() {
			return owner;
		}

		public CheckBoxState apply(Consumer<CheckBoxState> mod) {
			mod.accept(this);
			return this;
		}

		void updateValue(boolean value) {
			this.value = value;
			if (callback != null) {
				callback.accept(this);
			}
		}

		public boolean getValue() {
			return value;
		}

		public CheckBoxState setValue(boolean value) {
			this.value = value;
			return this;
		}
	}

}
