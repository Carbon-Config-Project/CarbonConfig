package carbonconfiglib.gui.widgets;

import java.util.EnumMap;

import carbonconfiglib.api.ConfigType;
import net.minecraft.resources.ResourceLocation;

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
public class Icon
{
	private static final ResourceLocation LOGO_TEXTURE = new ResourceLocation("carbonconfig:textures/gui/logo.png");
	public static final Icon LOGO = new Icon(LOGO_TEXTURE, 0, 0, 400, 400);
	
	private static final ResourceLocation ICONS = new ResourceLocation("carbonconfig:textures/gui/icons.png");
	public static final Icon DELETE = new Icon(ICONS, 0, 16, 80, 64);
	public static final Icon REVERT = new Icon(ICONS, 0, 0, 80, 64);
	public static final Icon SET_DEFAULT = new Icon(ICONS, 16, 0, 80, 64);
	public static final Icon RELOAD = new Icon(ICONS, 16, 16, 80, 64);
	public static final Icon RESTART = new Icon(ICONS, 32, 16, 80, 64);
	public static final Icon SEARCH = new Icon(ICONS, 48, 16, 80, 64);
	public static final Icon SEARCH_SELECTED = new Icon(ICONS, 48, 32, 80, 64);
	public static final Icon NOT_DEFAULT = new Icon(ICONS, 32, 0, 80, 64);
	public static final Icon NOT_DEFAULT_SELECTED = new Icon(ICONS, 48, 0, 80, 64);
	public static final Icon MOVE_DOWN = new Icon(ICONS, 64, 0, 80, 64);
	public static final Icon MOVE_DOWN_HOVERED = new Icon(ICONS, 64, 16, 80, 64);
	public static final Icon MOVE_UP = new Icon(ICONS, 64, 32, 80, 64);
	public static final Icon MOVE_UP_HOVERED = new Icon(ICONS, 64, 48, 80, 64);
	public static final EnumMap<ConfigType, Icon> TYPE_ICON = create(new Icon(ICONS, 0, 32, 80, 64), new Icon(ICONS, 16, 32, 80, 64), new Icon(ICONS, 32, 32, 80, 64));
	public static final EnumMap<ConfigType, Icon> MULTITYPE_ICON = create(new Icon(ICONS, 0, 48, 80, 64), new Icon(ICONS, 16, 48, 80, 64), new Icon(ICONS, 32, 48, 80, 64));

	ResourceLocation texture;
	int x;
	int y;
	int sheetWidth;
	int sheetHeight;

	public Icon(ResourceLocation texture, int x, int y, int sheetWidth, int sheetHeight) {
		this.texture = texture;
		this.x = x;
		this.y = y;
		this.sheetWidth = sheetWidth;
		this.sheetHeight = sheetHeight;
	}

	public ResourceLocation getTexture() {
		return texture;
	}
	
	public float getX() {
		return x;
	}
	
	public float getY() {
		return y;
	}

	public float getSheetWidth() {
		return sheetWidth;
	}

	public float getSheetHeight() {
		return sheetHeight;
	}
	
	private static EnumMap<ConfigType, Icon> create(Icon first, Icon second, Icon third) {
		EnumMap<ConfigType, Icon> icons = new EnumMap<>(ConfigType.class);
		icons.put(ConfigType.CLIENT, first);
		icons.put(ConfigType.SHARED, second);
		icons.put(ConfigType.SERVER, third);
		return icons;
	}
}
