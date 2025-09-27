package carbonconfiglib.gui.base.widgets;

import java.util.function.Consumer;
import java.util.function.Function;

import carbonconfiglib.gui.base.helpers.ITooltipProvider;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

public class CarbonBaseButton extends Button implements ITooltipProvider
{
	protected Function<CarbonBaseButton, Component> tooltip;
	
	public CarbonBaseButton(int x, int y, int width, int height, Component message, OnPress callback) {
		super(x, y, width, height, message, callback);
	}
	
	@Override
	public void onPress() {
		if(onPress != null) {
			onPress.onPress(this);
		}
	}
	
	@SuppressWarnings("unchecked")
	public <T extends CarbonBaseButton> T withTooltip(Component tooltip) {
		this.tooltip = T -> tooltip;
		return (T)this;
	}
	
	@SuppressWarnings("unchecked")
	public <T extends CarbonBaseButton> T withTooltip(Function<T, Component> tooltip) {
		this.tooltip = (Function<CarbonBaseButton, Component>)tooltip;
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
