package carbonconfiglib.gui.base.widgets;

import com.mojang.blaze3d.vertex.PoseStack;

import carbonconfiglib.gui.api.Texts;
import carbonconfiglib.gui.base.helpers.GuiUtils;
import carbonconfiglib.gui.base.helpers.Icon;
import carbonconfiglib.gui.screens.MultiChoiceScreen;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class ModLogo extends CarbonButton
{
	private static final Component LOG_INFO = Texts.translatable("gui.carbonconfig.logo.name").withStyle(ChatFormatting.GOLD).append("\n").append(Texts.translatable("gui.carbonconfig.logo.page").withStyle(ChatFormatting.GRAY));
	Minecraft minecraft = Minecraft.getInstance();
	Screen owner;
	
	public ModLogo(int pX, int pY, int pWidth, int pHeight, Screen owner) {
		super(pX, pY, pWidth, pHeight, Texts.empty(), null);
		this.owner = owner;
		withTooltip(LOG_INFO);
	}
	
	@Override
	public void renderButton(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
		GuiUtils.drawTextureRegion(pPoseStack, x, y, width, height, Icon.LOGO, 400, 400);
	}
	
	@Override
	public void onPress() {
		MultiChoiceScreen screen = new MultiChoiceScreen(T -> {
			if(T.isMain()) openURL("https://curseforge.com/minecraft/mc-mods/carbon-config");
			else if(T.isOther()) openURL("https://modrinth.com/mod/carbon-config");
			else minecraft.setScreen(owner);
		}, Texts.translatable("gui.carbonconfig.logo.link.title"), Texts.translatable("gui.carbonconfig.logo.link.message").withStyle(ChatFormatting.GRAY), 
		   Texts.translatable("gui.carbonconfig.logo.link.curseforge"), Texts.translatable("gui.carbonconfig.logo.link.modrinth"), Texts.translatable("gui.carbonconfig.reset_all.cancel"));
		minecraft.setScreen(screen);
	}
	
	private void openURL(String url) {
		minecraft.setScreen(new ConfirmLinkScreen(T -> {
            if (T) Util.getPlatform().openUri(url);
            this.minecraft.setScreen(owner);
         }, url, true));
	}
}
