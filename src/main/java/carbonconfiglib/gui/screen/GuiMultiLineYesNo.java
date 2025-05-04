package carbonconfiglib.gui.screen;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.gui.GuiYesNo;
import net.minecraft.client.gui.GuiYesNoCallback;

@SideOnly(Side.CLIENT)
public class GuiMultiLineYesNo extends GuiYesNo {

    private final String message;

    public GuiMultiLineYesNo(GuiYesNoCallback callback, String title, String message, int id) {
        super(callback, title, "", id);
        this.message = message;
    }

    public GuiMultiLineYesNo(GuiYesNoCallback parent, String title, String message, String confirm, String cancel, int id) {
        super(parent, title, "", confirm, cancel, id);
        this.message = message;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);

        int lineHeight = this.fontRendererObj.FONT_HEIGHT + 2;
        int startY = 90;
        String[] messages = FormattingUtil.listFormattedStringToWidthRespectingNewlines(this.fontRendererObj, message, this.width - 50);

        for (int i = 0; i < messages.length; ++i) {
            this.drawCenteredString(this.fontRendererObj, messages[i], this.width / 2, startY + (i * lineHeight), 16777215);
        }
    }
}
