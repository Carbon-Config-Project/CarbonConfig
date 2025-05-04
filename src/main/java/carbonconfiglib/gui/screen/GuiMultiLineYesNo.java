package carbonconfiglib.gui.screen;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.gui.GuiYesNo;
import net.minecraft.client.gui.GuiYesNoCallback;

@SideOnly(Side.CLIENT)
public class GuiMultiLineYesNo extends GuiYesNo {

    private final String multiLineMessage;

    public GuiMultiLineYesNo(GuiYesNoCallback parent, String title, String message, int id) {
        super(parent, title, "", id);
        this.multiLineMessage = message;
    }

    public GuiMultiLineYesNo(GuiYesNoCallback parent, String title, String message, String confirm, String cancel, int id) {
        super(parent, title, "", confirm, cancel, id);
        this.multiLineMessage = message;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);

        String[] rawLines = this.multiLineMessage.split("\\\\n");
        int lineHeight = this.fontRendererObj.FONT_HEIGHT + 2;
        int startY = 90;
        String activeFormatting = "";

        for (int i = 0; i < rawLines.length; ++i) {
            String line = rawLines[i];

            if (!line.matches("^§[0-9a-frk-orA-FK-OR].*")) {
                line = activeFormatting + line;
            }

            this.drawCenteredString(this.fontRendererObj, line, this.width / 2, startY + (i * lineHeight), 16777215);
            activeFormatting = FormattingUtil.getActiveFormattingCodes(line);
        }
    }
}
