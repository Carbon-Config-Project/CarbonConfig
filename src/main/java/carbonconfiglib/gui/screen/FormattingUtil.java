package carbonconfiglib.gui.screen;

public class FormattingUtil {
    public static String getActiveFormattingCodes(String text) {
        StringBuilder active = new StringBuilder();
        boolean resetFound = false;

        for (int i = 0; i < text.length() - 1; i++) {
            if (text.charAt(i) == '§') {
                char code = Character.toLowerCase(text.charAt(i + 1));
                if (code == 'r') {
                    active.setLength(0);
                    resetFound = true;
                } else if ("0123456789abcdefklmnor".indexOf(code) != -1) {
                    if (!resetFound && active.indexOf("§" + code) == -1) {
                        active.append('§').append(code);
                    }
                }
                i++;
            }
        }
        return active.toString();
    }
}
