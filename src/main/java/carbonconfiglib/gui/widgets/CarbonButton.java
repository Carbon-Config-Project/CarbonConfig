package carbonconfiglib.gui.widgets;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;

public class CarbonButton extends Button
{
	public CarbonButton(int i, int j, int k, int l, Component component, OnPress onPress) {
		super(i, j, k, l, component, onPress);
	}
	
	@Override
	public void renderButton(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
		Minecraft mc = Minecraft.getInstance();
		int k = this.getYImage(this.isHovered);
		GuiUtils.drawTextureWithBorder(poseStack, WIDGETS_LOCATION, this.x, this.y, 0, 46 + k * 20, this.width, this.height, 200, 20, 2, 3, 2, 2, this.getBlitOffset());
		this.renderBg(poseStack, mc, mouseX, mouseY);
		FormattedText text = GuiUtils.ellipsize(getMessage(), width - 6, mc.font);
		drawCenteredString(poseStack, mc.font, Language.getInstance().getVisualOrder(text), this.x + this.width / 2, this.y + (this.height - 8) / 2, active ? 0xFFFFFF : 0xA0A0A0);
	}
}
