package carbonconfiglib.gui.widgets;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;

import java.util.function.Supplier;

public class CarbonButton extends Button
{

	public CarbonButton(int i, int j, int k, int l, Component component, OnPress onPress, CreateNarration createNarration) {
		super(i, j, k, l, component, onPress, createNarration);
	}

	public CarbonButton(int i, int j, int k, int l, Component component, OnPress onPress) {
		this(i, j, k, l, component, onPress, Supplier::get);
	}

	@Override
	public void renderWidget(GuiGraphics poseStack, int mouseX, int mouseY, float partialTick) {
		Minecraft mc = Minecraft.getInstance();
		int k = this.getYImage(this.isHovered);
		GuiUtils.drawTextureWithBorder(poseStack, WIDGETS_LOCATION, this.getX(), this.getY(), 0, 46 + k * 20, this.width, this.height, 200, 20, 2, 3, 2, 2, this.getBlitOffset());
		this.renderBg(poseStack, mc, mouseX, mouseY);
		FormattedText text = GuiUtils.ellipsize(getMessage(), width - 6, mc.font);
		poseStack.drawString(mc.font, Language.getInstance().getVisualOrder(text), this.getX() + this.width / 2, this.getY() + (this.height - 8) / 2, active ? 0xFFFFFF : 0xA0A0A0);
	}

	protected int getYImage(boolean isHovered) {
		int i = 1;
		if (!this.active) {
			i = 0;
		} else if (isHovered) {
			i = 2;
		}

		return i;
	}
}
