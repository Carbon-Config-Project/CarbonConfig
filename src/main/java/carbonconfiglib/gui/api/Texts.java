package carbonconfiglib.gui.api;

import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;

public class Texts
{
	public static MutableComponent empty() {
		return new TextComponent("");
	}
	
	public static MutableComponent literal(String input) {
		return new TextComponent(input);
	}
	
	public static MutableComponent translatable(String input) {
		return new TranslatableComponent(input);
	}
	
	public static MutableComponent translatable(String input, Object...args) {
		return new TranslatableComponent(input, args);
	}
}
