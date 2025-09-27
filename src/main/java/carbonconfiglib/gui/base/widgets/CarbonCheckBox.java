package carbonconfiglib.gui.base.widgets;

import java.util.function.Consumer;
import java.util.function.Function;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import carbonconfiglib.gui.base.helpers.GuiUtils;
import carbonconfiglib.gui.base.helpers.ITooltipProvider;
import carbonconfiglib.gui.widgets.Icon;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class CarbonCheckBox extends Checkbox implements ITooltipProvider {

	CheckBoxState state;
	private static final ResourceLocation TEXTURE = new ResourceLocation("textures/gui/checkbox.png");

	public CarbonCheckBox(int x, int y, int width, int height, Icon icon, CheckBoxState state) {
		super(x, y, width, height, Component.empty(), state.value);
	}

	@Override
	public void onPress() {
		if (state.callback != null) {
			this.state.callback.accept(state);
		}
	}

	@Override
	public boolean selected() {
		return state.value;
	}

	@SuppressWarnings("unchecked")
	public <T extends CarbonCheckBox> T withTooltip(Component tooltip) {
		this.state.tooltip = T -> tooltip;
		return (T) this;
	}

	@SuppressWarnings("unchecked")
	public <T extends CarbonCheckBox> T withTooltip(Function<T, Component> tooltip) {
		this.state.tooltip = (Function<CarbonCheckBox, Component>) tooltip;
		return (T) this;
	}

	@Override
	public void provideTooltips(int mouseX, int mouseY, Consumer<Component> tooltips) {
		if (state.tooltip != null && isMouseOver(mouseX, mouseY)) {
			Component result = state.tooltip.apply(this);
			if (result == null)
				return;
			tooltips.accept(result);
		}
	}

	@Override
	public void renderButton(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
		Minecraft minecraft = Minecraft.getInstance();
		RenderSystem.setShaderTexture(0, TEXTURE);
		RenderSystem.enableDepthTest();
		Font font = minecraft.font;
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, this.alpha);
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
		blit(pPoseStack, this.x, this.y, this.isFocused() ? 20.0F : 0.0F,
				this.state.value && this.state.selectedIcon == null ? 20.0F : 0.0F, 20, this.height, 64, 64);
		if (this.state.selectedIcon != null) {
			GuiUtils.drawTextureRegion(pPoseStack, x + 2, y + 2, width - 4, height - 4,
					this.state.value ? state.selectedIcon : state.unselectedIcon, 16, 16);
		}

		this.renderBg(pPoseStack, minecraft, pMouseX, pMouseY);
		if (this.state.label != null) {
			drawString(pPoseStack, font, state.label, this.x + 24, this.y + (this.height - 8) / 2,
					14737632 | Mth.ceil(this.alpha * 255.0F) << 24);
		}

	}

	public static class CheckBoxState {
		protected Function<CarbonCheckBox, Component> tooltip;
		Consumer<CheckBoxState> callback;
		boolean value = true;
		Component label;
		Icon selectedIcon;
		Icon unselectedIcon;
		CarbonCheckBox owner;

		public CheckBoxState() {
		}

		public CheckBoxState(boolean value) {
			this.value = value;
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

		public CheckBoxState withIcons(Icon active, Icon inactive) {
			this.selectedIcon = active;
			this.unselectedIcon = inactive;
			return this;
		}

		public Icon getActiveIcon() {
			return selectedIcon;
		}

		public Icon getInactiveIcon() {
			return unselectedIcon;
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
