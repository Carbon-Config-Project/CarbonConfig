package carbonconfiglib.gui.base.helpers;

import java.util.Objects;

import net.minecraft.client.resources.I18n;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;

public class Texts
{
	
	public static IChatComponent empty() {
		return new ChatComponentText("");
	}
	
	public static IChatComponent literal(String input) {
		return new ChatComponentText(input);
	}
	
	public static IChatComponent translatable(String input) {
		return new ChatComponentTranslation(input);
	}
	
	public static IChatComponent translatable(String input, Object...args) {
		return new ChatComponentTranslation(input, args);
	}
	
	public static boolean hasKey(String key, Object...parameters) {
		String out = I18n.format(key, parameters);
		return Objects.equals(out, key);
	}
	
	public static ChatStyle applyStyle(EnumChatFormatting... formatting) {
		ChatStyle style = new ChatStyle();
		for(EnumChatFormatting format : formatting) {
			if(format.isColor()) {
				style.setColor(format);
				continue;
			}
			else if(format == EnumChatFormatting.OBFUSCATED) {
				style.setObfuscated(true);
			}
			else if(format == EnumChatFormatting.BOLD) {
				style.setBold(true);
			}
			else if(format == EnumChatFormatting.STRIKETHROUGH) {
				style.setStrikethrough(true);
			}
			else if(format == EnumChatFormatting.UNDERLINE) {
				style.setUnderlined(true);
			}
			else if(format == EnumChatFormatting.ITALIC) {
				style.setItalic(true);
			}
			if(format == EnumChatFormatting.RESET) {
				style = new ChatStyle();
			}
		}
		return style;
	}
	
	
}
