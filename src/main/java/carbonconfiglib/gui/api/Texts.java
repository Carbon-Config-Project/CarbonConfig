package carbonconfiglib.gui.api;

import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class Texts
{
	public static ITextComponent empty() {
		return new StringTextComponent("");
	}
	
	public static ITextComponent literal(String input) {
		return new StringTextComponent(input);
	}
	
	public static ITextComponent translatable(String input) {
		return new TranslationTextComponent(input);
	}
	
	public static ITextComponent translatable(String input, Object...args) {
		return new TranslationTextComponent(input, args);
	}
}
