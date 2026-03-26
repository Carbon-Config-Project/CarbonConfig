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
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;

public class ModLogo extends CarbonButton
{
	private static final IChatComponent LOG_INFO = Texts.translatable("gui.carbonconfig.logo.name").setChatStyle(new ChatStyle().setColor(EnumChatFormatting.GOLD)).appendText("\n").appendSibling(Texts.translatable("gui.carbonconfig.logo.page").setChatStyle(new ChatStyle().setColor(EnumChatFormatting.GRAY)));
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
		}, Texts.translatable("gui.carbonconfig.logo.link.title"), Texts.translatable("gui.carbonconfig.logo.link.message").setChatStyle(new ChatStyle().setColor(EnumChatFormatting.GOLD)), 
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
