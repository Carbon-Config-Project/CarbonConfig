package carbonconfiglib.gui.base.menu;

import java.util.List;
import java.util.Map;

import carbonconfiglib.gui.base.helpers.Texts;
import net.minecraft.util.text.ITextComponent;
import speiger.src.collections.objects.lists.ObjectArrayList;
import speiger.src.collections.objects.maps.impl.hash.Object2ObjectOpenHashMap;

/**
 * Copyright 2026 Speiger, Meduris
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */
public class SubMenuItem implements IMenuItem {
	
	ITextComponent name;
	List<IMenuItem> childItems = new ObjectArrayList<>();
	Map<String, SubMenuItem> subMenus = new Object2ObjectOpenHashMap<>();
	
	public SubMenuItem(String text) {
		this(Texts.translatable(text));
	}
	
	public SubMenuItem(ITextComponent name) {
		this.name = name;
	}
	
	public SubMenuItem addNode(String text, Runnable action) {
		return addNode(new MenuItem(Texts.translatable(text), action, true));
	}
	
	public SubMenuItem addNode(String text, Runnable action, boolean closeScreen) {
		return addNode(new MenuItem(Texts.translatable(text), action, closeScreen));
	}
	
	public SubMenuItem addLabel(String text) {
		childItems.add(new MenuLabel(Texts.literal(text)));
		return this;
	}

	public SubMenuItem addLabel(ITextComponent text) {
		childItems.add(new MenuLabel(text));
		return this;
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
	public ITextComponent name() {
		return name;
	}
	
	public List<IMenuItem> children() {
		return childItems;
	}
	
	@Override
	public boolean hoverable() {
		return true;
	}
}
