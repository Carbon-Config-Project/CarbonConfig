package carbonconfiglib.impl;

import carbonconfiglib.api.ILimitationSerializer;
import carbonconfiglib.api.IReloadMode;
import carbonconfiglib.gui.base.helpers.Texts;
import net.minecraft.util.IChatComponent;

/**
 * Copyright 2023 Speiger, Meduris
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
 */
public enum ReloadMode implements IReloadMode, ILimitationSerializer
{
	WORLD("gui.carbonconfig.reload.sync", "gui.carbonconfig.reload.state", "Requires Reload"),
	GAME("gui.carbonconfig.restart.sync", "gui.carbonconfig.restart.state", "Requires Restart");
	
	IChatComponent message;
	IChatComponent state;
	String comment;
	
	private ReloadMode(String message, String state, String comment) {
		this.message = Texts.translatable(message);
		this.state = Texts.translatable(state);
		this.comment = comment;
	}
	
	public IChatComponent getMessage() {
		return message;
	}
	
	public IChatComponent getState() {
		return state;
	}
	
	public static ReloadMode or(ReloadMode original, IReloadMode other) {
		return getByIndex(Math.max(getModeIndex(original), getModeIndex(other instanceof ReloadMode ? (ReloadMode)other : null)));
	}
	
	private static int getModeIndex(ReloadMode mode) {
		return mode == null ? -1 : mode.ordinal();
	}
	
	private static ReloadMode getByIndex(int index) {
		return index == 0 ? ReloadMode.WORLD : (index == 1 ? ReloadMode.GAME : null);
	}

	@Override
	public String getLimitation() {
		return comment;
	}
}
