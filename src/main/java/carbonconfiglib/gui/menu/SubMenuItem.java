package carbonconfiglib.gui.menu;

import java.util.List;
import java.util.Map;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.network.chat.Component;

public class SubMenuItem implements IMenuItem {
	
	Component name;
	List<IMenuItem> childItems = new ObjectArrayList<>();
	Map<String, SubMenuItem> subMenus = new Object2ObjectOpenHashMap<>();
	
	public SubMenuItem(String text) {
		this(Component.translatable(text));
	}
	
	public SubMenuItem(Component name) {
		this.name = name;
	}
	
	public SubMenuItem addNode(String text, Runnable action) {
		return addNode(new MenuItem(Component.translatable(text), action, true));
	}
	
	public SubMenuItem addNode(String text, Runnable action, boolean closeScreen) {
		return addNode(new MenuItem(Component.translatable(text), action, closeScreen));
	}
	
	public SubMenuItem addNode(MenuItem item) {
		childItems.add(item);
		return this;
	}
	
	public SubMenuItem addSubMenu(String id, SubMenuItem sub) {
		childItems.add(sub);
		subMenus.put(id, sub);
		return this;
	}
	
	public SubMenuItem get(String id) {
		return subMenus.get(id);
	}
	
	@Override
	public Component name() {
		return name;
	}
	
	public List<IMenuItem> children() {
		return childItems;
	}
	
}
