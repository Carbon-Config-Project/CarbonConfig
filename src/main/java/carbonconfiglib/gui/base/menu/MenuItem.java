package carbonconfiglib.gui.base.menu;

import net.minecraft.network.chat.Component;

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
	
	@Override
	public boolean hoverable() {
		return true;
	}
}
