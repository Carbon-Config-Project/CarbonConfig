package carbonconfiglib.gui.base.widgets;

import java.awt.Desktop;
import java.net.URL;

import carbonconfiglib.gui.base.helpers.GuiUtils;
import carbonconfiglib.gui.base.helpers.Icon;
import carbonconfiglib.gui.base.helpers.Texts;
import carbonconfiglib.gui.screens.MultiChoiceScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiConfirmOpenLink;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;

public class ModLogo extends CarbonButton
{
	private static final ITextComponent LOG_INFO = Texts.translatable("gui.carbonconfig.logo.name").setStyle(new Style().setColor(TextFormatting.GOLD)).appendText("\n").appendSibling(Texts.translatable("gui.carbonconfig.logo.page").setStyle(new Style().setColor(TextFormatting.GRAY)));
	Minecraft minecraft = Minecraft.getMinecraft();
	GuiScreen owner;
	
	public ModLogo(int pX, int pY, int pWidth, int pHeight, GuiScreen owner) {
		super(pX, pY, pWidth, pHeight, Texts.empty(), null);
		this.owner = owner;
		withTooltip(LOG_INFO);
	}
	
	@Override
	public void render(int pMouseX, int pMouseY, float pPartialTick) {
		GuiUtils.drawTextureRegion(xPosition, yPosition, width, height, Icon.LOGO, 400, 400);
	}
	
	@Override
	public void onPress() {
		MultiChoiceScreen screen = new MultiChoiceScreen(T -> {
			if(T.isMain()) openURL("https://curseforge.com/minecraft/mc-mods/carbon-config");
			else if(T.isOther()) openURL("https://modrinth.com/mod/carbon-config");
			else minecraft.displayGuiScreen(owner);
		}, Texts.translatable("gui.carbonconfig.logo.link.title"), Texts.translatable("gui.carbonconfig.logo.link.message").setStyle(new Style().setColor(TextFormatting.GOLD)), 
		   Texts.translatable("gui.carbonconfig.logo.link.curseforge"), Texts.translatable("gui.carbonconfig.logo.link.modrinth"), Texts.translatable("gui.carbonconfig.reset_all.cancel"));
		minecraft.displayGuiScreen(screen);
	}
	
	private void openURL(String url) {
		minecraft.displayGuiScreen(new GuiConfirmOpenLink((T, U) -> {
            if (T) {
				try { Desktop.getDesktop().browse(new URL(url).toURI()); }
				catch(Exception e) { e.printStackTrace(); }
            }
            this.minecraft.displayGuiScreen(owner);
         }, url, 0, true));
	}
}
