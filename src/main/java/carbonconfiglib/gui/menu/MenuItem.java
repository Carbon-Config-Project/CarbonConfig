package carbonconfiglib.gui.menu;

import net.minecraft.network.chat.Component;

public class MenuItem implements IMenuItem {
	
	boolean closeScreen;
	Runnable action;
	Component name;
	
	public MenuItem(Component name, Runnable action, boolean closeScreen) {
		this.action = action;
		this.name = name;
		this.closeScreen = closeScreen;
	}
	
	@Override
	public Component name() {
		return name;
	}
	
	public void click() {
		if(action == null) return;
		action.run();
	}
}
