package carbonconfiglib.gui.widgets.screen;

import net.minecraft.client.Minecraft;

public interface IRenderable
{
	public void render(Minecraft mc, int mouseX, int mouseY, float partialTicks);
}
