package carbonconfiglib.gui.menu;

import java.util.List;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Matrix4f;

import carbonconfiglib.gui.base.screen.BaseCarbonScreen;
import carbonconfiglib.gui.base.widgets.CarbonList;
import carbonconfiglib.gui.base.widgets.CarbonList.ListEntry;
import carbonconfiglib.gui.base.widgets.CarbonList.ListState;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.util.FormattedCharSequence;
import net.minecraftforge.client.ForgeHooksClient;

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
		CarbonList<?> list = listArea(menuX, menuY, realWidth+5, maxHeight, menuState);
		list.setRenderTopAndBottom(false);
		list.setRenderBackground(false);
		list.setRenderSelection(true);
	}
	
	@Override
	public void renderBackground(PoseStack matrix, int mouseX, int mouseY, float partialTicks) {
		drawArea(matrix, menuX, menuY, realWidth-1, realHeight-5);
	}
	
	public void drawArea(PoseStack matrix, float x, float y, float widht, float height) {
		Tesselator tes = Tesselator.getInstance();
		BufferBuilder buffer = tes.getBuilder();
		buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
		float minX = 4 + x;
		float maxX = minX + widht;
		float minY = 4 + y;
		float maxY = minY + height;
		
		fillGradient(matrix, minX - 3, minY - 4, maxX + 3, minY - 3, -267386864, buffer);
		fillGradient(matrix, minX - 3, maxY + 3, maxX + 3, maxY + 4, -267386864, buffer);
		fillGradient(matrix, minX - 3, minY - 3, maxX + 3, maxY + 3, -267386864, buffer);
		fillGradient(matrix, minX - 4, minY - 3, minX - 3, maxY + 3, -267386864, buffer);
		fillGradient(matrix, maxX + 3, minY - 3, maxX + 4, maxY + 3, -267386864, buffer);
		
		fillGradient(matrix, minX - 3, minY - 3 + 1, minX - 3 + 1, maxY + 3 - 1, 1347420415, buffer);
		fillGradient(matrix, maxX + 2, minY - 3 + 1, maxX + 3, maxY + 3 - 1, 1347420415, buffer);
		fillGradient(matrix, minX - 3, minY - 3, maxX + 3, minY - 3 + 1, 1347420415, buffer);
		fillGradient(matrix, minX - 3, maxY + 2, maxX + 3, maxY + 3, 1344798847, buffer);
		
		GlStateManager._disableTexture();
		GlStateManager._enableBlend();
		RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
		GlStateManager._enableDepthTest();
		RenderSystem.setShader(GameRenderer::getPositionColorShader);
		tes.end();
		GlStateManager._disableDepthTest();
		GlStateManager._disableBlend();
		GlStateManager._enableTexture();
	}
	
	protected void fillGradient(PoseStack matrix, float left, float top, float right, float bottom, int color, BufferBuilder buffer) {
		float a = (color >> 24 & 255) / 255F;
		float r = (color >> 16 & 255) / 255F;
		float g = (color >> 8 & 255) / 255F;
		float b = (color & 255) / 255F;
		Matrix4f stack = matrix.last().pose();
		buffer.vertex(stack, right, top, -10F).color(r, g, b, a).endVertex();
		buffer.vertex(stack, left, top, -10F).color(r, g, b, a).endVertex();
		buffer.vertex(stack, left, bottom, -10F).color(r, g, b, a).endVertex();
		buffer.vertex(stack, right, bottom, -10F).color(r, g, b, a).endVertex();
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
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if(super.mouseClicked(mouseX, mouseY, button)) return true;
		if(callback != null) callback.run();
		ForgeHooksClient.popGuiLayer(getMinecraft());
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
			ForgeHooksClient.popGuiLayer(mc);
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
			return height(font.split(item.name(), 120).size());
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
		public void render(PoseStack poseStack, int x, int top, int left, int width, int height, int mouseX, int mouseY, boolean selected, float partialTicks) {
			lastY = top;
			List<FormattedCharSequence> sequence = font.split(item.name(), 180);
			int baseY = top + (height >> 1) - (height(sequence.size()) >> 1);
			int entryHeight = height(1);
			for(FormattedCharSequence entry : sequence) {
				font.draw(poseStack, entry, left+1, baseY + (entryHeight >> 1) - (font.lineHeight >> 1), -1);
				baseY += entryHeight;
			}
			if(item instanceof SubMenuItem) {
				font.draw(poseStack, ">", left + width - 7, top + (height >> 1) - (font.lineHeight >> 1)+1, -1);

			}
		}
		
		@Override
		public boolean mouseClicked(double mouseX, double mouseY, int button) {
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
			if(owner.getHovered(mouseX, mouseY) == this) {
				if(owner.getSelected() != null && owner.getSelected() != this) {
					owner.getSelected().close();
				}
				owner.setSelected(this);
				if(child == null && item instanceof SubMenuItem menu) {
					int x = owner.getLeft() + owner.getWidth() - 3;
					int width = Math.min(180, Math.max(80, MenuScreen.countWidth(menu, font)));
			        int screenWidth = Minecraft.getInstance().getWindow().getGuiScaledWidth();
			        if(x + width >= screenWidth) {
			        	x = owner.getLeft() - width - 1;
			        }
					Minecraft.getInstance().pushGuiLayer((child = new MenuScreen(menu, x, lastY - 5).setParent(screen)));
				}
			}
			else if(child != null) {
				child.onClose();
				child = null;
			}
			else if(owner.getSelected() == this) {
				owner.setSelected(null);
			}
		}
	}
}
