package carbonconfiglib.gui.base.widgets;

import carbonconfiglib.gui.api.Texts;
import carbonconfiglib.gui.base.helpers.GuiUtils;
import carbonconfiglib.gui.base.helpers.Icon;
import carbonconfiglib.gui.screens.MultiChoiceScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.ConfirmOpenLinkScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.Util;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;

public class ModLogo extends CarbonButton
{
	private static final ITextComponent LOG_INFO = Texts.translatable("gui.carbonconfig.logo.name").applyTextStyle(TextFormatting.GOLD).appendText("\n").appendSibling(Texts.translatable("gui.carbonconfig.logo.page").applyTextStyle(TextFormatting.GRAY));
	Minecraft minecraft = Minecraft.getInstance();
	Screen owner;
	
	public ModLogo(int pX, int pY, int pWidth, int pHeight, Screen owner) {
		super(pX, pY, pWidth, pHeight, Texts.empty(), null);
		this.owner = owner;
		withTooltip(LOG_INFO);
	}
	
	@Override
	public void renderButton(int pMouseX, int pMouseY, float pPartialTick) {
		GuiUtils.drawTextureRegion(x, y, width, height, Icon.LOGO, 400, 400);
	}
	
	@Override
	public void onPress() {
		MultiChoiceScreen screen = new MultiChoiceScreen(T -> {
			if(T.isMain()) openURL("https://curseforge.com/minecraft/mc-mods/carbon-config");
			else if(T.isOther()) openURL("https://modrinth.com/mod/carbon-config");
			else minecraft.displayGuiScreen(owner);
		}, Texts.translatable("gui.carbonconfig.logo.link.title"), Texts.translatable("gui.carbonconfig.logo.link.message").applyTextStyle(TextFormatting.GOLD), 
		   Texts.translatable("gui.carbonconfig.logo.link.curseforge"), Texts.translatable("gui.carbonconfig.logo.link.modrinth"), Texts.translatable("gui.carbonconfig.reset_all.cancel"));
		minecraft.displayGuiScreen(screen);
	}
	
	private void openURL(String url) {
		minecraft.displayGuiScreen(new ConfirmOpenLinkScreen(T -> {
            if (T) Util.getOSType().openURI(url);
            this.minecraft.displayGuiScreen(owner);
         }, url, true));
	}
}
