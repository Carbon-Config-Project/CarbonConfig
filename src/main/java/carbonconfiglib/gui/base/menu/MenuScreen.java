package carbonconfiglib.gui.base.menu;

import java.util.List;

import carbonconfiglib.gui.base.helpers.Align;
import carbonconfiglib.gui.base.screen.BaseCarbonScreen;
import carbonconfiglib.gui.base.widgets.CarbonList.ListEntry;
import carbonconfiglib.gui.base.widgets.CarbonList.ListState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.util.FormattedCharSequence;
import net.neoforged.neoforge.client.ClientHooks;
import speiger.src.collections.objects.lists.ObjectArrayList;

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
public class MenuScreen extends BaseCarbonScreen {
	ListState<MenuEntry> menuState = new ListState<MenuEntry>(10).setScrollOffset(-1);
	SubMenuItem item;
	MenuScreen parent;
	Runnable callback;
	int menuX;
	int menuY;
	int maxHeight = 200;
	
	int realWidth;
	int realHeight;
	
	public MenuScreen(SubMenuItem item, int menuX, int menuY) {
		this.item = item;
		this.menuX = menuX;
		this.menuY = menuY;
		menuState.add(MenuEntry.create(this, item));
		renderBackground = false;
	}
	
	public MenuScreen setParent(MenuScreen parent) {
		this.parent = parent;
		return this;
	}
	
	public MenuScreen setCloseCallback(Runnable run) {
		callback = run;
		return this;
	}
	
	@Override
	protected void init() {
		super.init();
		realHeight = Math.min(maxHeight, countHeight(item, font)+4);
		realWidth = Math.min(180, Math.max(80, countWidth(item, font)));
		menuState.setSelectable(true);
		listArea(menuX, menuY, realWidth+5, maxHeight, menuState);
	}
	
	@Override
	public void drawBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
		drawArea(graphics, menuX, menuY-2, realWidth-1, realHeight-5);
	}
	
	public void drawArea(GuiGraphicsExtractor graphics, int x, int y, int widht, int height) {
		int minX = 4 + x;
		int maxX = minX + widht;
		int minY = 4 + y;
		int maxY = minY + height;
		
		graphics.fill(minX - 3, minY - 4, maxX + 3, minY - 3, -267386864);
		graphics.fill(minX - 3, maxY + 3, maxX + 3, maxY + 4, -267386864);
		graphics.fill(minX - 3, minY - 3, maxX + 3, maxY + 3, -267386864);
		graphics.fill(minX - 4, minY - 3, minX - 3, maxY + 3, -267386864);
		graphics.fill(maxX + 3, minY - 3, maxX + 4, maxY + 3, -267386864);
		
		graphics.fill(minX - 3, minY - 3 + 1, minX - 3 + 1, maxY + 3 - 1, 1347420415);
		graphics.fill(maxX + 2, minY - 3 + 1, maxX + 3, maxY + 3 - 1, 1347420415);
		graphics.fill(minX - 3, minY - 3, maxX + 3, minY - 3 + 1, 1347420415);
		graphics.fill(minX - 3, maxY + 2, maxX + 3, maxY + 3, 1344798847);
	}
	
	@Override
	public void mouseMoved(double mouseX, double mouseY) {
		if(parent != null) {
			if(!menuState.getOwner().isMouseOver(mouseX, mouseY)) {
				parent.mouseMoved(mouseX, mouseY);
			}
		}
		super.mouseMoved(mouseX, mouseY);
	}
	
	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
		if(super.mouseClicked(event, doubleClick)) return true;
		if(callback != null) callback.run();
		ClientHooks.popGuiLayer(getMinecraft());
		return false;
	}
		
	private static int countHeight(SubMenuItem menu, Font font) {
		int height = 0;
		for(IMenuItem item : menu.children()) {
			height += MenuEntry.height(item, font);
		}
		return height;
	}
	
	private static int countWidth(SubMenuItem menu, Font font) {
		int width = 0;
		for(IMenuItem item : menu.children()) {
			width = Math.max(MenuEntry.width(item, font), width);
		}
		return width;
	}
	
	public static void popAllMenus() {
		Minecraft mc = Minecraft.getInstance();
		while(mc.screen instanceof MenuScreen) {
			ClientHooks.popGuiLayer(mc);
		}
	}
	
	public static class MenuEntry extends ListEntry<MenuEntry> {
		int lastY;
		MenuScreen screen;
		MenuScreen child = null;
		IMenuItem item;
		
		public MenuEntry(MenuScreen screen, IMenuItem item) {
			this.screen = screen;
			this.item = item;
		}

		private static List<MenuEntry> create(MenuScreen screen, SubMenuItem items) {
			List<MenuEntry> entries = new ObjectArrayList<>();
			for(IMenuItem item : items.children()) {
				entries.add(new MenuEntry(screen, item));
			}
			return entries;
		}
		
		@Override
		protected boolean containsSearch(String searchString) {
			return false;
		}
		
		private static int height(IMenuItem item, Font font) {
			return height(font.split(item.name(), 180).size());
		}
		
		private static int height(int count) {
			return 12 * count;
		}
		
		private static int width(IMenuItem item, Font font)  {
			int width = 0;
			for(FormattedCharSequence entry : font.split(item.name(), 180)) {
				width = Math.max(font.width(entry)+5, width);
			}
			return width + (item instanceof SubMenuItem ? font.width(">")+10 : 0);
		}
		
		@Override
		public int getHeight() {
			return height(item, font); 
		}
		
		@Override
		public int getSelectionBackgroundColor() { return 0x55FFFFFF; }
		@Override
		public int getSelectionColor(boolean focused) {
			return 0x55FFFFFF;
		}
		
		@Override
		public void extractContent(GuiGraphicsExtractor graphics, int mouseX, int mouseY, boolean selected, float partialTicks) {
			lastY = getContentY();
			List<FormattedCharSequence> sequence = font.split(item.name(), 180);
			int baseY = Align.CENTER.alignStart(getContentY(), getContentHeight(), height(sequence.size()));
			int entryHeight = height(1);
			int offset = Align.CENTER.alignStart(0, entryHeight, font.lineHeight);
			for(FormattedCharSequence entry : sequence) {
				graphics.text(font, entry, getContentX(), baseY+offset, -1);
				baseY += entryHeight;
			}
			if(item instanceof SubMenuItem) {
				graphics.text(font, ">", getContentX() + getWidth() - 7, Align.CENTER.alignStart(getY(), getHeight(), font.lineHeight)+1, -1);
			}
		}
		
		@Override
		public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
			if(item instanceof MenuItem menu && menu.action != null) {
				menu.action.run();
				if(menu.closeScreen) MenuScreen.popAllMenus();
				return true;
			}
			return false;
		}
		
		private void close() {
			if(child == null) return;
			child.onClose();
			child = null;
		}
		
		@Override
		public void mouseMoved(double mouseX, double mouseY) {
			if(owner.getHovered(mouseX, mouseY) == this && item.hoverable()) {
				if(owner.getSelected() != null && owner.getSelected() != this) {
					owner.getSelected().close();
				}
				owner.setSelected(this);
				if(child == null && item instanceof SubMenuItem menu) {
					int x = owner.getX() + owner.getWidth() - 3;
					int width = Math.min(180, Math.max(80, MenuScreen.countWidth(menu, font)));
			        int screenWidth = Minecraft.getInstance().getWindow().getGuiScaledWidth();
			        if(x + width >= screenWidth) {
			        	x = owner.getX() - width - 5;
			        }
					Minecraft.getInstance().pushGuiLayer((child = new MenuScreen(menu, x, lastY - 5).setParent(screen)));
				}
			}
			else if(child != null && item.hoverable()) {
				child.onClose();
				child = null;
			}
			else if(owner.getSelected() == this) {
				owner.setSelected(null);
			}
		}
	}
}
