package carbonconfiglib.gui.base.widgets;

import java.util.function.Consumer;
import java.util.function.Function;

import org.lwjgl.opengl.GL11;

import carbonconfiglib.gui.base.helpers.GuiUtils;
import carbonconfiglib.gui.base.helpers.ITooltipProvider;
import carbonconfiglib.gui.base.helpers.Icon.IconPair;
import carbonconfiglib.gui.base.helpers.Texts;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.ResourceLocation;


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
public class CarbonCheckBox extends CarbonBaseButton implements ITooltipProvider {

	CheckBoxState state;
	private static final ResourceLocation TEXTURE = new ResourceLocation("carbonconfig:textures/gui/checkbox.png");

	public CarbonCheckBox(int x, int y, int width, int height, CheckBoxState state) {
		super(x, y, width, height, Texts.empty(), null);
		this.state = state;
	}

	@Override
	public void onPress() {
		state.updateValue(!state.getValue());
	}
	
	public CheckBoxState getState() {
		return state;
	}
	
	public boolean selected() {
		return state.value;
	}
	
	protected boolean canShowTooltip(double mouseX, double mouseY) {
		return this.visible && mouseX >= (double)this.xPosition && mouseY >= (double)this.yPosition && mouseX < (double)(this.xPosition + this.width) && mouseY < (double)(this.yPosition + this.height);
	}
	
	@Override
	public void provideTooltips(int mouseX, int mouseY, Consumer<IChatComponent> tooltips) {
		if (state.tooltip != null && canShowTooltip(mouseX, mouseY)) {
			IChatComponent result = state.tooltip.apply(this);
			if (result == null) return;
			tooltips.accept(result);
		}
	}
	
	@Override
	public void render(int mouseX, int mouseY, float partialTicks){
		Minecraft mc = Minecraft.getMinecraft();
		mc.getTextureManager().bindTexture(TEXTURE);
		GlStateManager.enableDepth();
		GlStateManager.color(1F, 1F, 1F, 1F);
		GlStateManager.enableBlend();
		GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
		GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		
		boolean notVanilla = state.getIcon() != null;
		GuiUtils.drawTextureRegion(xPosition, yPosition, isMouseOver(mouseX, mouseY) ? 20F : 0F, !notVanilla && selected() ? 20F : 0F, width, height, 20F, 20F, 64F, 64F);
		if(notVanilla) {
			GuiUtils.drawTextureRegion(xPosition+2, yPosition+2, width-4, height-4, selected() ? state.getIcon().active() : state.getIcon().inactive(), 16, 16);
		}
		if (state.label != null) {
			drawString(mc.fontRendererObj, state.label.getFormattedText(), this.xPosition + 24, this.yPosition + (this.height - 8) / 2, 14737632 | 255 << 24);
		}
	}

	public static class CheckBoxState {
		protected Function<CarbonCheckBox, IChatComponent> tooltip;
		Consumer<CheckBoxState> callback;
		boolean value = true;
		IChatComponent label;
		IconPair icon;
		CarbonCheckBox owner;

		public CheckBoxState() {
		}

		public CheckBoxState(boolean value) {
			this.value = value;
		}
		
		public CheckBoxState(IChatComponent label) {
			this.label = label;
		}
		
		public CheckBoxState(boolean value, IChatComponent label) {
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

		public CheckBoxState withLabel(IChatComponent label) {
			this.label = label;
			return this;
		}

		public IChatComponent getLabel() {
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
		
		public CheckBoxState setTooltip(IChatComponent tooltip) {
			this.tooltip = T -> tooltip;
			return this;
		}

		public CheckBoxState withTooltip(Function<CarbonCheckBox, IChatComponent> tooltip) {
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
