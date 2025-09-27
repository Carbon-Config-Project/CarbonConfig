package carbonconfiglib.gui.base.widgets;

import java.util.Optional;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import carbonconfiglib.gui.base.helpers.GuiUtils;
import carbonconfiglib.gui.config.ConfigElement.GuiAlign;
import carbonconfiglib.gui.widgets.Icon;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraftforge.client.gui.ScreenUtils;

public class CarbonButton extends CarbonBaseButton {

	Optional<Icon> icon = Optional.empty();
	int hash;

	public CarbonButton(int pX, int pY, int pWidth, int pHeight, Component pMessage, OnPress listener) {
		super(pX, pY, pWidth, pHeight, pMessage, listener);
		this.hash = pMessage.getString().hashCode();
	}
	
	public CarbonButton withIcon(Optional<Icon> icon) {
		this.icon = icon;
		return this;
	}

	@Override
	public void updateNarration(NarrationElementOutput pNarrationElementOutput) {
		defaultButtonNarrationText(pNarrationElementOutput);
	}

	public void renderIcon(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
		int j = getFGColor();
		RenderSystem.setShaderColor(((j >> 16) & 0xFF) / 255F, ((j >> 8) & 0xFF) / 255F, (j & 0xFF) / 255F, 1F);
		GuiUtils.drawTextureRegion(pPoseStack, x + (width / 2) - 5.5F, y + height / 2 - 5.5F, 11, 11, icon.get(), 16, 16);
		RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
	}

	@Override
	public void renderButton(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
		int k = this.getYImage(this.isHoveredOrFocused());
		ScreenUtils.blitWithBorder(pPoseStack, WIDGETS_LOCATION, x, y, 0, 46 + k * 20, this.width, this.height, 200, 20, 2, 3, 2, 2, this.getBlitOffset());
		if (icon.isPresent()) {
			renderIcon(pPoseStack, pMouseX, pMouseY, pPartialTick);
		}
		Minecraft minecraft = Minecraft.getInstance();
		Font font = minecraft.font;
		int width = font.width(getMessage()) + 21;
		float minX = x + 4 + (this.width / 2) - (width / 2);
		int j = getFGColor();
		RenderSystem.setShaderColor(((j >> 16) & 0xFF) / 255F, ((j >> 8) & 0xFF) / 255F, (j & 0xFF) / 255F, 1F);
		GuiUtils.drawTextureRegion(pPoseStack, minX, y + (height - 8) / 2, 11, 11, icon.get(), 16, 16);
		RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
		GuiUtils.drawScrollingShadowText(pPoseStack, font, getMessage(), minX + 15, y, width, height - 2, GuiAlign.CENTER, getFGColor(), hash);
	}
}
