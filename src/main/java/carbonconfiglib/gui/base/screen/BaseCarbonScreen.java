package carbonconfiglib.gui.base.screen;

import java.util.List;
import java.util.function.Consumer;

import com.mojang.blaze3d.vertex.PoseStack;

import carbonconfiglib.gui.base.helpers.Align;
import carbonconfiglib.gui.base.helpers.GuiUtils;
import carbonconfiglib.gui.base.helpers.ITooltipProvider;
import carbonconfiglib.gui.base.widgets.CarbonList;
import carbonconfiglib.gui.base.widgets.CarbonList.ListEntry;
import carbonconfiglib.gui.base.widgets.CarbonList.ListState;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import speiger.src.collections.objects.lists.ObjectArrayList;

public class BaseCarbonScreen extends Screen
{
	public static final int DEFAULT_DELAY = 200;
	protected int centerX;
	protected int centerY;
	protected int tick;
	int lastMouseX = 0;
	int lastMouseY = 0;
	long lastCheck = 0L;
	int lastDrawnToolTipAmount = 0;
	boolean renderTooltip = false;
	
	public BaseCarbonScreen() {
		super(Component.empty());
	}
	
	@Override
	protected void init() {
		super.init();
		centerX = (this.width / 2);
		centerY = (this.height / 2);
		clearWidgets();
	}
	
	@Override
	public void tick() {
		super.tick();
		tick++;
	}
	
	@Override
	public void render(PoseStack matrix, int mouseX, int mouseY, float partialTicks) {
		renderBackground(matrix, mouseX, mouseY, partialTicks);
		renderWidgets(matrix, mouseX, mouseY, partialTicks);
		renderForeground(matrix, mouseX, mouseY, partialTicks);
		renderTooltips(matrix, mouseX, mouseY, partialTicks);
	}
	
	public void renderBackground(PoseStack matrix, int mouseX, int mouseY, float partialTicks) {
		
	}
	
	public void renderWidgets(PoseStack matrix, int mouseX, int mouseY, float partialTicks) {
		for(Widget widget : this.renderables) {
			widget.render(matrix, mouseX, mouseY, partialTicks);
		}	
	}
	
	public void renderForeground(PoseStack matrix, int mouseX, int mouseY, float partialTicks) {
		
	}
	
	public void collectTooltips(PoseStack matrix, int mouseX, int mouseY, float partialTicks, Consumer<Component> tooltips) {
		
	}
	
	public void renderTooltips(PoseStack matrix, int mouseX, int mouseY, float partialTicks) {
		List<FormattedCharSequence> tooltips = new ObjectArrayList<>();
		if(mouseX != Integer.MAX_VALUE && mouseY != Integer.MAX_VALUE) {
			for(GuiEventListener listener : children()) {
				if(listener instanceof ITooltipProvider) {
					((ITooltipProvider)listener).provideTooltips(T -> tooltips.addAll(font.split(T, Math.max(mouseX, width - mouseX) - 20)));
				}
			}
			collectTooltips(matrix, mouseX, mouseY, partialTicks, T -> tooltips.addAll(font.split(T, Math.max(mouseX, width - mouseX) - 20)));
		}
		if((!renderTooltip && (lastMouseX != mouseX || lastMouseY != mouseY)) || tooltips.isEmpty()) {
			lastCheck = System.currentTimeMillis();
			lastMouseX = mouseX;
			lastMouseY = mouseY;
			lastDrawnToolTipAmount = 0;
			if(tooltips.isEmpty()) renderTooltip = false;
			return;
		}
		else if(System.currentTimeMillis() - lastCheck >= DEFAULT_DELAY) {
			renderTooltip = true;
		}
		else {
			lastDrawnToolTipAmount = 0;
			return;
		}
		lastDrawnToolTipAmount = 0;
		renderTooltip(matrix, tooltips, mouseX, mouseY);
		lastDrawnToolTipAmount = tooltips.size();
	}
	
	public void drawText(PoseStack stack, Component text, float x, float y, Align align, int color) {
		GuiUtils.drawText(stack, font, text, x + centerX, y + centerY, align, color);
	}
	
	public void drawSplitText(PoseStack stack, Component text, float x, float y, Align align, int maxWidth, int color) {
		GuiUtils.drawSplitText(stack, font, text, x + centerX, y + centerY, align, maxWidth, color);
	}
	
	public <T extends ListEntry<T>> CarbonList<T> list(int width, int height, int startY, int endY, ListState<T> state) {
		return addRenderableWidget(new CarbonList<>(this, width, height, startY, endY, state));
	}
	
	public <T extends ListEntry<T>> CarbonList<T> list(ListState<T> state) {
		return addRenderableWidget(new CarbonList<>(this, state));
	}
	
	public <T extends ListEntry<T>> CarbonList<T> listArea(int x, int y, int width, int height, ListState<T> state) {
		CarbonList<T> list = addRenderableWidget(new CarbonList<>(this, width, height, y, y+height, state.setRowWidth(width).setScrollOffset(0)));
		list.setLeftPos(x);
		list.setRenderBackground(false);
		list.setRenderTopAndBottom(false);
		return list;
	}
	
	public <T extends ListEntry<T>> CarbonList<T> listArea(int x, int y, int width, int height, Align horizontal, Align vertical, ListState<T> state) {
		x = getAlignedX(horizontal) + x;
		y = getAlignedY(vertical) + y;
		CarbonList<T> list = addRenderableWidget(new CarbonList<>(this, width, height, y, y+height, state.setRowWidth(width).setScrollOffset(0)));
		list.setLeftPos(x);
		list.setRenderBackground(false);
		list.setRenderTopAndBottom(false);
		return list;
	}
	
	protected int getAlignedX(Align align) {
		switch(align) {
			case CENTER: return centerX;
			case END: return width;
			case START: return 0;
			default: return 0;
		}
	}
	
	protected int getAlignedY(Align align) {
		switch(align) {
			case CENTER: return centerY;
			case END: return height;
			case START: return 0;
			default: return 0;
		}
	}
	
}
