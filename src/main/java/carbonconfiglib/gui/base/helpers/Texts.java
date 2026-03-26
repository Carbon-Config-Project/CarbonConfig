package carbonconfiglib.gui.base.helpers;

import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;

public class Texts
{
	public static ITextComponent empty() {
		return new TextComponentString("");
	}
	
	public static ITextComponent literal(String input) {
		return new TextComponentString(input);
	}
	
	public static ITextComponent translatable(String input) {
		return new TextComponentTranslation(input);
	}
	
	public static ITextComponent translatable(String input, Object...args) {
		return new TextComponentTranslation(input, args);
	}
	
	public static Style applyStyle(TextFormatting... formatting) {
		Style style = new Style();
		for(TextFormatting format : formatting) {
			if(format.isColor()) {
				style.setColor(format);
				continue;
			}
			else if(format == TextFormatting.OBFUSCATED) {
				style.setObfuscated(true);
			}
			else if(format == TextFormatting.BOLD) {
				style.setBold(true);
			}
			else if(format == TextFormatting.STRIKETHROUGH) {
				style.setStrikethrough(true);
			}
			else if(format == TextFormatting.UNDERLINE) {
				style.setUnderlined(true);
			}
			else if(format == TextFormatting.ITALIC) {
				style.setItalic(true);
			}
			if(format == TextFormatting.RESET) {
				style = new Style();
			}
		}
		return style;
	}
	
	
}
