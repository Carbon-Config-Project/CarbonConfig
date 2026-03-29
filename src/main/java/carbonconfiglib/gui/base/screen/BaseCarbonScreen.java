package carbonconfiglib.gui.base.screen;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import carbonconfiglib.gui.api.Texts;
import carbonconfiglib.gui.base.helpers.Align;
import carbonconfiglib.gui.base.helpers.GuiUtils;
import carbonconfiglib.gui.base.helpers.ITooltipProvider;
import carbonconfiglib.gui.base.helpers.Icon;
import carbonconfiglib.gui.base.widgets.CarbonButton;
import carbonconfiglib.gui.base.widgets.CarbonCheckBox;
import carbonconfiglib.gui.base.widgets.CarbonCheckBox.CheckBoxState;
import carbonconfiglib.gui.base.widgets.CarbonEditBox;
import carbonconfiglib.gui.base.widgets.CarbonEditBox.TextState;
import carbonconfiglib.gui.base.widgets.CarbonList;
import carbonconfiglib.gui.base.widgets.CarbonList.ListEntry;
import carbonconfiglib.gui.base.widgets.CarbonList.ListState;
import carbonconfiglib.gui.base.widgets.CarbonSlider;
import carbonconfiglib.gui.base.widgets.CarbonSlider.SliderState;
import carbonconfiglib.gui.base.widgets.DropDownMenu;
import carbonconfiglib.gui.base.widgets.DropDownMenu.DropDownState;
import carbonconfiglib.gui.base.widgets.ModLogo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.IRenderable;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button.IPressable;
import net.minecraft.util.text.ITextComponent;
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
 */
public class BaseCarbonScreen extends Screen
{
	public static final int DEFAULT_DELAY = 200;
	protected List<IRenderable> renderables = new ObjectArrayList<>();
	protected boolean renderBackground = true;
	protected int centerX;
	protected int centerY;
	protected int tick;
	int lastMouseX = 0;
	int lastMouseY = 0;
	long lastCheck = 0L;
	int lastDrawnToolTipAmount = 0;
	boolean renderTooltip = false;
	
	public BaseCarbonScreen() {
		super(Texts.empty());
	}
	
	@Override
	protected void init() {
		super.init();
		centerX = (this.width / 2);
		centerY = (this.height / 2);
		buttons.clear();
		children.clear();
		renderables.clear();
	}
	
	protected <T extends IGuiEventListener & IRenderable> T addRenderable(T widget) {
		renderables.add(widget);
		children.add(widget);
		return widget;
	}
	
	@Override
	public void tick() {
		super.tick();
		tick++;
	}
	
	@Override
	public void render(int mouseX, int mouseY, float partialTicks) {
		if(renderBackground) renderBackground();
		renderBackground(mouseX, mouseY, partialTicks);
		renderWidgets(mouseX, mouseY, partialTicks);
		renderForeground(mouseX, mouseY, partialTicks);
		renderTooltips(mouseX, mouseY, partialTicks);
	}
	
	public void renderBackground(int mouseX, int mouseY, float partialTicks) {
		
	}
	
	public void renderWidgets(int mouseX, int mouseY, float partialTicks) {
		for(IRenderable widget : this.renderables) {
			widget.render(mouseX, mouseY, partialTicks);
		}	
	}
	
	public void renderForeground(int mouseX, int mouseY, float partialTicks) {
		
	}
	
	public void collectTooltips(int mouseX, int mouseY, float partialTicks, Consumer<ITextComponent> tooltips) {
		
	}
	
	public void renderTooltips(int mouseX, int mouseY, float partialTicks) {
		List<String> tooltips = new ObjectArrayList<>();
		if(mouseX != Integer.MAX_VALUE && mouseY != Integer.MAX_VALUE) {
			for(IGuiEventListener listener : children()) {
				if(listener instanceof ITooltipProvider) {
					((ITooltipProvider)listener).provideTooltips(mouseX, mouseY, T -> tooltips.addAll(font.listFormattedStringToWidth(T.getFormattedText(), Math.max(mouseX, width - mouseX) - 20)));
				}
			}
			collectTooltips(mouseX, mouseY, partialTicks, T -> tooltips.addAll(font.listFormattedStringToWidth(T.getFormattedText(), Math.max(mouseX, width - mouseX) - 20)));
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
		renderTooltip(tooltips, mouseX, mouseY);
		lastDrawnToolTipAmount = tooltips.size();
	}
	
	protected void setScreen(Screen screen) {
		Minecraft.getInstance().displayGuiScreen(screen);
	}
	
	public static void setExternalScreen(Screen screen) {
		Minecraft.getInstance().displayGuiScreen(screen);
	}
	
	public static void pushExternalScreen(Screen screen) {
		LayeredScreen.pushGuiLayer(screen);
	}
	
	protected void pushScreen(Screen screen) {
		LayeredScreen.pushGuiLayer(screen);
	}
	
	@Override
	public void onClose() {
		LayeredScreen.popGuiLayer();
	}
	
	public void drawText(ITextComponent text, float x, float y, Align align, int color) {
		GuiUtils.drawText(font, text, x + centerX, y + centerY, align, color);
	}
	
	public void drawUnalignedText(ITextComponent text, float x, float y, Align align, int color) {
		GuiUtils.drawText(font, text, x, y, align, color);
	}
	
	public void drawSplitText(ITextComponent text, float x, float y, Align align, int maxWidth, int color) {
		GuiUtils.drawSplitText(font, text, x + centerX, y + centerY, align, color, maxWidth);
	}
	
	public CarbonButton button(int x, int y, int width, int height, ITextComponent text, IPressable listener) {
		return addRenderable(new CarbonButton(x, y, width, height, text, listener));
	}
	
	public CarbonButton button(int x, int y, int width, int height, Align horizontal, Align vertical, ITextComponent text, IPressable listener) {
		return addRenderable(new CarbonButton(getAlignedX(horizontal) + x, getAlignedY(vertical) + y, width, height, text, listener));
	}
	
	public CarbonButton iconButton(int x, int y, int width, int height, Icon icon, IPressable listener) {
		return addRenderable(new CarbonButton(x, y, width, height, Texts.empty(), listener).withIcon(Optional.of(icon)));
	}
	
	public CarbonButton iconButton(int x, int y, int width, int height, Align horizontal, Align vertical, Icon icon, IPressable listener) {
		return addRenderable(new CarbonButton(getAlignedX(horizontal) + x, getAlignedY(vertical) + y, width, height, Texts.empty(), listener).withIcon(Optional.of(icon)));
	}
	
	public CarbonCheckBox checkbox(int x, int y, int width, int height, CheckBoxState state) {
		return addRenderable(new CarbonCheckBox(x, y, width, height, state));
	}
	
	public CarbonCheckBox checkbox(int x, int y, int width, int height, Align horizontal, Align vertical, CheckBoxState state) {
		return addRenderable(new CarbonCheckBox(getAlignedX(horizontal) + x, getAlignedY(vertical) + y, width, height, state));
	}
	
	public CarbonEditBox text(int x, int y, int width, int height, TextState state) {
		return addRenderable(new CarbonEditBox(font, x, y, width, height, state));
	}
	
	public CarbonEditBox text(int x, int y, int width, int height, Align horizontal, Align vertical, TextState state) {
		return addRenderable(new CarbonEditBox(font, getAlignedX(horizontal) + x, getAlignedY(vertical) + y, width, height, state));
	}
	
	public CarbonSlider slider(int x, int y, int width, int height, SliderState state) {
		return addRenderable(new CarbonSlider(x, y, width, height, state));
	}
	
	public CarbonSlider slider(int x, int y, int width, int height, Align horizontal, Align vertical, SliderState state) {
		return addRenderable(new CarbonSlider(getAlignedX(horizontal) + x, getAlignedY(vertical) + y, width, height, state));
	}
	
	public <T extends ListEntry<T>> CarbonList<T> list(int width, int height, int startY, int endY, ListState<T> state) {
		return addRenderable(new CarbonList<>(this, width, height, startY, endY, state));
	}
	
	public <T extends ListEntry<T>> CarbonList<T> list(ListState<T> state) {
		return addRenderable(new CarbonList<>(this, state));
	}
	
	public <T> DropDownMenu<T> dropDown(int x, int y, int width, int height, DropDownState<T> state) {
		return addRenderable(new DropDownMenu<T>(x, y, width, height, state));
	}
	
	public <T> DropDownMenu<T> dropDown(int x, int y, int width, int height, Align horizontal, Align vertical, DropDownState<T> state) {
		return addRenderable(new DropDownMenu<T>(getAlignedX(horizontal) + x, getAlignedY(vertical) + y, width, height, state));
	}
	
	public ModLogo modlogo(int x, int y, int width, int height) {
		return addRenderable(new ModLogo(x, y, width, height, this));
	}
	
	public ModLogo modlogo(int x, int y, int width, int height, Align horizontal, Align vertical) {
		return addRenderable(new ModLogo(getAlignedX(horizontal) + x, getAlignedY(vertical) + y, width, height, this));
	}
	
	public <T extends ListEntry<T>> CarbonList<T> listArea(int x, int y, int width, int height, ListState<T> state) {
		CarbonList<T> list = addRenderable(new CarbonList<>(this, width, height, y, y+height, state.setRowWidth(width).setScrollOffset(0)));
		list.setLeftPos(x);
		list.setRenderBackground(false);
		list.setRenderTopAndBottom(false);
		return list;
	}
	
	public <T extends ListEntry<T>> CarbonList<T> listArea(int x, int y, int width, int height, Align horizontal, Align vertical, ListState<T> state) {
		x = getAlignedX(horizontal) + x;
		y = getAlignedY(vertical) + y;
		CarbonList<T> list = addRenderable(new CarbonList<>(this, width, height, y, y+height, state.setRowWidth(width).setScrollOffset(0)));
		list.setLeftPos(x);
		list.setRenderBackground(false);
		list.setRenderTopAndBottom(false);
		return list;
	}
	
	@Override
	public void mouseMoved(double mouseX, double mouseY) {
		for(IGuiEventListener listener : children()) {
			listener.mouseMoved(mouseX, mouseY);
		}
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
