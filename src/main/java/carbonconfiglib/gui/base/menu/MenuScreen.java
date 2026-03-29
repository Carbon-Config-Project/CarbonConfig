package carbonconfiglib.gui.base.menu;

import java.util.List;

import org.lwjgl.opengl.GL11;

import carbonconfiglib.gui.base.helpers.Align;
import carbonconfiglib.gui.base.helpers.GuiUtils;
import carbonconfiglib.gui.base.screen.BaseCarbonScreen;
import carbonconfiglib.gui.base.screen.LayeredScreen;
import carbonconfiglib.gui.base.widgets.CarbonList;
import carbonconfiglib.gui.base.widgets.CarbonList.ListEntry;
import carbonconfiglib.gui.base.widgets.CarbonList.ListState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
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
	public void initGui() {
		super.initGui();
		realHeight = Math.min(maxHeight, countHeight(item, fontRendererObj)+4);
		realWidth = Math.min(180, Math.max(80, countWidth(item, fontRendererObj)));
		menuState.setSelectable(true);
		CarbonList<?> list = listArea(menuX, menuY, realWidth+5, maxHeight, menuState);
		list.setRenderTopAndBottom(false);
		list.setRenderBackground(false);
		list.setRenderSelection(true);
	}
	
	@Override
	public void renderBackground(int mouseX, int mouseY, float partialTicks) {
		drawArea(menuX, menuY, realWidth-1, realHeight-5);
	}
	
	public void drawArea(float x, float y, float widht, float height) {
		Tessellator tes = Tessellator.instance;
		tes.startDrawingQuads();
		float minX = 4 + x;
		float maxX = minX + widht;
		float minY = 4 + y;
		float maxY = minY + height;
		
		fillGradient(minX - 3, minY - 4, maxX + 3, minY - 3, -267386864, tes);
		fillGradient(minX - 3, maxY + 3, maxX + 3, maxY + 4, -267386864, tes);
		fillGradient(minX - 3, minY - 3, maxX + 3, maxY + 3, -267386864, tes);
		fillGradient(minX - 4, minY - 3, minX - 3, maxY + 3, -267386864, tes);
		fillGradient(maxX + 3, minY - 3, maxX + 4, maxY + 3, -267386864, tes);
		
		fillGradient(minX - 3, minY - 3 + 1, minX - 3 + 1, maxY + 3 - 1, 1347420415, tes);
		fillGradient(maxX + 2, minY - 3 + 1, maxX + 3, maxY + 3 - 1, 1347420415, tes);
		fillGradient(minX - 3, minY - 3, maxX + 3, minY - 3 + 1, 1347420415, tes);
		fillGradient(minX - 3, maxY + 2, maxX + 3, maxY + 3, 1344798847, tes);
		
		
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL11.glEnable(GL11.GL_BLEND);
        OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
		tes.draw();
		GL11.glDisable(GL11.GL_BLEND);
		GL11.glDisable(GL11.GL_DEPTH_TEST);
		GL11.glEnable(GL11.GL_TEXTURE_2D);
	}
	
	protected void fillGradient(float left, float top, float right, float bottom, int color, Tessellator buffer) {
		float a = (color >> 24 & 255) / 255F;
		float r = (color >> 16 & 255) / 255F;
		float g = (color >> 8 & 255) / 255F;
		float b = (color & 255) / 255F;
		buffer.setColorRGBA_F(r, g, b, a);
		buffer.addVertex(right, top, -10F);
		buffer.addVertex(left, top, -10F);
		buffer.addVertex(left, bottom, -10F);
		buffer.addVertex(right, bottom, -10F);
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
	public boolean mouseClick(double mouseX, double mouseY, int button) {
		if(super.mouseClick(mouseX, mouseY, button)) return true;
		if(callback != null) callback.run();
		LayeredScreen.popGuiLayer();
		return false;
	}
	
	private static int countHeight(SubMenuItem menu, FontRenderer font) {
		int height = 0;
		for(IMenuItem item : menu.children()) {
			height += MenuEntry.height(item, font);
		}
		return height;
	}
	
	private static int countWidth(SubMenuItem menu, FontRenderer font) {
		int width = 0;
		for(IMenuItem item : menu.children()) {
			width = Math.max(MenuEntry.width(item, font), width);
		}
		return width;
	}
	
	public static void popAllMenus() {
		Minecraft mc = Minecraft.getMinecraft();
		if(mc.currentScreen instanceof MenuScreen) {
			mc.displayGuiScreen(null);
			return;
		}
		while(LayeredScreen.topScreen() instanceof MenuScreen) {
			LayeredScreen.popGuiLayer();
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
		
		private static int height(IMenuItem item, FontRenderer font) {
			return height(GuiUtils.splitLines(font, item.name(), 180).size());
		}
		
		private static int height(int count) {
			return 12 * count;
		}
		
		private static int width(IMenuItem item, FontRenderer font)  {
			int width = 0;
			for(String entry : GuiUtils.splitLines(font, item.name(), 180)) {
				width = Math.max(font.getStringWidth(entry)+5, width);
			}
			return width + (item instanceof SubMenuItem ? font.getStringWidth(">")+10 : 0);
		}
		
		@Override
		public int getItemHeight() {
			return height(item, font); 
		}
		
		@Override
		public int getSelectionBackgroundColor() { return 0x55FFFFFF; }
		@Override
		public int getSelectionColor(boolean focused) {
			return 0x55FFFFFF;
		}
		
		@Override
		public void render(int x, int top, int left, int width, int height, int mouseX, int mouseY, boolean selected, float partialTicks) {
			lastY = top;
			List<String> sequence = GuiUtils.splitLines(font, item.name(), 180);
			int baseY = Align.CENTER.alignStart(top, height, height(sequence.size()));
			int entryHeight = height(1);
			int offset = Align.CENTER.alignStart(0, entryHeight, font.FONT_HEIGHT);
			for(String entry : sequence) {
				font.drawString(entry, left+1, baseY+offset, -1);
				baseY += entryHeight;
			}
			if(item instanceof SubMenuItem) {
				font.drawString(">", left + width - 7, Align.CENTER.alignStart(top, height, font.FONT_HEIGHT)+1, -1);
			}
		}
		
		@Override
		public boolean mouseClick(double mouseX, double mouseY, int button) {
			if(item instanceof MenuItem && ((MenuItem)item).action != null) {
				MenuItem menu = (MenuItem)item;
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
				if(child == null && item instanceof SubMenuItem) {
					SubMenuItem menu = (SubMenuItem)item;
					int x = owner.getLeft() + owner.getWidth() - 3;
					int width = Math.min(180, Math.max(80, MenuScreen.countWidth(menu, font)));
					Minecraft mc = Minecraft.getMinecraft();
			        int screenWidth = new ScaledResolution(mc, mc.displayWidth, mc.displayHeight).getScaledWidth();
			        if(x + width >= screenWidth) {
			        	x = owner.getLeft() - width - 5;
			        }
					LayeredScreen.pushGuiLayer((child = new MenuScreen(menu, x, lastY - 5).setParent(screen)));
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
