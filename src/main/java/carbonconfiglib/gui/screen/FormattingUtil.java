package carbonconfiglib.gui.screen;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.util.IChatComponent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FormattingUtil {
    private static boolean isFormatColor(char c) {
        return c >= '0' && c <= '9' || c >= 'a' && c <= 'f' || c >= 'A' && c <= 'F';
    }

    private static boolean isFormatSpecialNoReset(char c) {
        return c >= 'k' && c <= 'o' || c >= 'K' && c <= 'O';
    }

    private static String getFormatFromString(String s) {
        String s1 = "";
        int i = -1;
        int j = s.length();

        while ((i = s.indexOf(167, i + 1)) != -1)
        {
            if (i < j - 1)
            {
                char c0 = s.charAt(i + 1);

                if (isFormatColor(c0))
                {
                    s1 = "\u00a7" + c0;
                }
                else if (isFormatSpecialNoReset(c0))
                {
                    s1 = s1 + "\u00a7" + c0;
                }
            }
        }

        return s1;
    }

    @SuppressWarnings("unchecked")
    public static String[] listFormattedStringToWidthRespectingNewlines(FontRenderer fontRenderer, String s, int width) {
        List<String> result = new ArrayList<>();
        String[] lines = s.split("\\\\n");
        String formatting = getFormatFromString(s);
        for (String line : lines) {
            if (width > 0) {
                result.addAll(fontRenderer.listFormattedStringToWidth(formatting + line, width));
            } else {
                result.add(formatting + line);
            }
        }
        return result.toArray(new String[0]);
    }

    public static String[] listFormattedStringToWidthRespectingNewlines(FontRenderer fontRenderer, String s) {
        return listFormattedStringToWidthRespectingNewlines(fontRenderer, s, -1);
    }
}
