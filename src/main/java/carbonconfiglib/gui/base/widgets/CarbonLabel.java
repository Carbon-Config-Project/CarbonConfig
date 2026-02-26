package carbonconfiglib.gui.base.widgets;

import java.util.function.Consumer;
import java.util.function.Function;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import carbonconfiglib.gui.base.helpers.GuiUtils;
import carbonconfiglib.gui.base.helpers.ITooltipProvider;
import carbonconfiglib.gui.base.helpers.Icon;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

public class CarbonLabel extends AbstractWidget implements ITooltipProvider
{
	protected Function<CarbonLabel, Component> tooltip;
	protected Icon icon;
	public CarbonLabel(int x, int y, int width, int height, Icon icon) {
		super(x, y, width, height, Component.empty());
		this.icon = icon;
	}
	
	@Override
	protected boolean isValidClickButton(int pButton) {
		return false;
	}
	
	@Override
	public void renderButton(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
		int j = getFGColor();
		RenderSystem.setShaderColor(((j >> 16) & 0xFF) / 255F, ((j >> 8) & 0xFF) / 255F, (j & 0xFF) / 255F, 1F);
		GuiUtils.drawTextureRegion(pPoseStack, x+2, y+2, width-4, height-4, icon, 16, 16);
		RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
	}
	
	@Override
	public void updateNarration(NarrationElementOutput pNarrationElementOutput) {}
	
	@SuppressWarnings("unchecked")
	public <T extends CarbonLabel> T withTooltip(Component tooltip) {
		this.tooltip = T -> tooltip;
		return (T)this;
	}
	
	@SuppressWarnings("unchecked")
	public <T extends CarbonLabel> T withTooltip(Function<T, Component> tooltip) {
		this.tooltip = (Function<CarbonLabel, Component>)tooltip;
		return (T)this;
	} 
	
	@Override
	public void provideTooltips(int mouseX, int mouseY, Consumer<Component> tooltips) {
		if(tooltip != null && isMouseOver(mouseX, mouseY)) {
			Component result = tooltip.apply(this);
			if(result == null) return;
			tooltips.accept(result);
		}
	}
	
}
