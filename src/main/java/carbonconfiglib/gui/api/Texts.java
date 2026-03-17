package carbonconfiglib.gui.api;

import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class Texts
{
	public static IFormattableTextComponent empty() {
		return new StringTextComponent("");
	}
	
	public static IFormattableTextComponent literal(String input) {
		return new StringTextComponent(input);
	}
	
	public static IFormattableTextComponent translatable(String input) {
		return new TranslationTextComponent(input);
	}
	
	public static IFormattableTextComponent translatable(String input, Object...args) {
		return new TranslationTextComponent(input, args);
	}
}
