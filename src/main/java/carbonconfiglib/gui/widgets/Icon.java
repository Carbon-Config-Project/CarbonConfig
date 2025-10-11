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
	public static final IconSheet LOGO_SHEET = new IconSheet(new ResourceLocation("carbonconfig:textures/gui/logo.png"), 400, 400);
	public static final Icon LOGO = LOGO_SHEET.create(0, 0);
	
	public static final IconSheet ICONS = new IconSheet(new ResourceLocation("carbonconfig:textures/gui/icons.png"), 80, 80);

	public static final Icon DELETE = ICONS.create(0, 16);
	public static final Icon REVERT = ICONS.create(0, 0);
	public static final Icon SET_DEFAULT = ICONS.create(16, 0);
	public static final Icon RELOAD = ICONS.create(16, 16);
	public static final Icon RESTART = ICONS.create(32, 16);
	public static final IconPair SELECTED = ICONS.horizontalActivityIcon(0, 16, 64);
	public static final IconPair SEARCH = ICONS.verticalActivityIcon(48, 32, 16);
	public static final IconPair NOT_DEFAULT = ICONS.horizontalActivityIcon(48, 32, 0);
	public static final IconPair MOVE_DOWN = ICONS.verticalActivityIcon(64, 16, 0);
	public static final IconPair MOVE_UP = ICONS.verticalActivityIcon(64, 48, 32);
	public static final Icon SUGGESTIONS = ICONS.create(48, 48);
	public static final EnumMap<ConfigType, Icon> TYPE_ICON = create(ICONS.create(0, 32), ICONS.create(16, 32), ICONS.create(32, 32));
	public static final EnumMap<ConfigType, Icon> MULTITYPE_ICON = create(ICONS.create(0, 48), ICONS.create(16, 48), ICONS.create(32, 48));

	IconSheet sheet;
	int x;
	int y;

	public Icon(IconSheet sheet, int x, int y) {
		this.sheet = sheet;
		this.x = x;
		this.y = y;
	}

	public ResourceLocation getTexture() {
		return sheet.texture();
	}
	
	public float getX() {
		return x;
	}
	
	public float getY() {
		return y;
	}

	public float getSheetWidth() {
		return sheet.width();
	}

	public float getSheetHeight() {
		return sheet.height();
	}
	
	private static EnumMap<ConfigType, Icon> create(Icon first, Icon second, Icon third) {
		EnumMap<ConfigType, Icon> icons = new EnumMap<>(ConfigType.class);
		icons.put(ConfigType.CLIENT, first);
		icons.put(ConfigType.SHARED, second);
		icons.put(ConfigType.SERVER, third);
		return icons;
	}
	
	public static class IconSheet {
		ResourceLocation texture;
		int width;
		int height;
		
		public IconSheet(ResourceLocation texture, int width, int height) {
			this.texture = texture;
			this.width = width;
			this.height = height;
		}
		
		public Icon create(int x, int y) {
			return new Icon(this, x, y);
		}
		
		public IconPair createActivityIcon(int x, int y, int x2, int y2) {
			return new IconPair(new Icon(this, x, y), new Icon(this, x2, y2));
		}
		
		public IconPair verticalActivityIcon(int x, int y1, int y2) {
			return new IconPair(new Icon(this, x, y1), new Icon(this, x, y2));
		}
		
		public IconPair horizontalActivityIcon(int x1, int x2, int y) {
			return new IconPair(new Icon(this, x1, y), new Icon(this, x2, y));
		}
		
		public ResourceLocation texture() {
			return texture;
		}
		
		public int width() {
			return width;
		}
		
		public int height() {
			return height;
		}
	}
	
	public static class IconPair {
		Icon active;
		Icon inactive;
		
		public IconPair(Icon active, Icon inactive) {
			this.active = active;
			this.inactive = inactive;
		}
		
		public Icon active() {
			return active; 
		}
		
		public Icon inactive() {
			return inactive;
		}
		
		public IconPair invert() {
			return new IconPair(inactive, active);
		}
	}
}
