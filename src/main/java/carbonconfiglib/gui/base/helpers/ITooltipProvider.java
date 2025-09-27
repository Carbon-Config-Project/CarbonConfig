package carbonconfiglib.gui.base.helpers;

import java.util.function.Consumer;

import net.minecraft.network.chat.Component;

public interface ITooltipProvider
{
	public void provideTooltips(Consumer<Component> tooltips);
}
