package carbonconfiglib.gui.base.screen;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import com.mojang.blaze3d.vertex.PoseStack;

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
import net.minecraft.client.gui.components.Button.OnPress;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
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
		clearWidgets();
	}
	
	@Override
	public void tick() {
		super.tick();
		tick++;
	}
	
	@Override
	public void onClose() {
		LayeredScreen.popGuiLayer();
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
		super.render(matrix, mouseX, mouseY, partialTicks);
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
					((ITooltipProvider)listener).provideTooltips(mouseX, mouseY, T -> tooltips.addAll(font.split(T, Math.max(mouseX, width - mouseX) - 20)));
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
	
	protected void setScreen(Screen screen) {
		Minecraft.getInstance().setScreen(screen);
	}
	
	public static void setExternalScreen(Screen screen) {
		Minecraft.getInstance().setScreen(screen);
	}
	
	public static void pushExternalScreen(Screen screen) {
		LayeredScreen.pushGuiLayer(screen);
	}
	
	protected void pushScreen(Screen screen) {
		LayeredScreen.pushGuiLayer(screen);
	}
	
	public void drawText(PoseStack stack, Component text, float x, float y, Align align, int color) {
		GuiUtils.drawText(stack, font, text, x + centerX, y + centerY, align, color);
	}
	
	public void drawUnalignedText(PoseStack stack, Component text, float x, float y, Align align, int color) {
		GuiUtils.drawText(stack, font, text, x, y, align, color);
	}
	
	public void drawSplitText(PoseStack stack, Component text, float x, float y, Align align, int maxWidth, int color) {
		GuiUtils.drawSplitText(stack, font, text, x + centerX, y + centerY, align, color, maxWidth);
	}
	
	public CarbonButton button(int x, int y, int width, int height, Component text, OnPress listener) {
		return addRenderableWidget(new CarbonButton(x, y, width, height, text, listener));
	}
	
	public CarbonButton button(int x, int y, int width, int height, Align horizontal, Align vertical, Component text, OnPress listener) {
		return addRenderableWidget(new CarbonButton(getAlignedX(horizontal) + x, getAlignedY(vertical) + y, width, height, text, listener));
	}
	
	public CarbonButton iconButton(int x, int y, int width, int height, Icon icon, OnPress listener) {
		return addRenderableWidget(new CarbonButton(x, y, width, height, Texts.empty(), listener).withIcon(Optional.of(icon)));
	}
	
	public CarbonButton iconButton(int x, int y, int width, int height, Align horizontal, Align vertical, Icon icon, OnPress listener) {
		return addRenderableWidget(new CarbonButton(getAlignedX(horizontal) + x, getAlignedY(vertical) + y, width, height, Texts.empty(), listener).withIcon(Optional.of(icon)));
	}
	
	public CarbonCheckBox checkbox(int x, int y, int width, int height, CheckBoxState state) {
		return addRenderableWidget(new CarbonCheckBox(x, y, width, height, state));
	}
	
	public CarbonCheckBox checkbox(int x, int y, int width, int height, Align horizontal, Align vertical, CheckBoxState state) {
		return addRenderableWidget(new CarbonCheckBox(getAlignedX(horizontal) + x, getAlignedY(vertical) + y, width, height, state));
	}
	
	public CarbonEditBox text(int x, int y, int width, int height, TextState state) {
		return addRenderableWidget(new CarbonEditBox(font, x, y, width, height, state));
	}
	
	public CarbonEditBox text(int x, int y, int width, int height, Align horizontal, Align vertical, TextState state) {
		return addRenderableWidget(new CarbonEditBox(font, getAlignedX(horizontal) + x, getAlignedY(vertical) + y, width, height, state));
	}
	
	public CarbonSlider slider(int x, int y, int width, int height, SliderState state) {
		return addRenderableWidget(new CarbonSlider(x, y, width, height, state));
	}
	
	public CarbonSlider slider(int x, int y, int width, int height, Align horizontal, Align vertical, SliderState state) {
		return addRenderableWidget(new CarbonSlider(getAlignedX(horizontal) + x, getAlignedY(vertical) + y, width, height, state));
	}
	
	public <T extends ListEntry<T>> CarbonList<T> list(int width, int height, int startY, int endY, ListState<T> state) {
		return addRenderableWidget(new CarbonList<>(this, width, height, startY, endY, state));
	}
	
	public <T extends ListEntry<T>> CarbonList<T> list(ListState<T> state) {
		return addRenderableWidget(new CarbonList<>(this, state));
	}
	
	public <T> DropDownMenu<T> dropDown(int x, int y, int width, int height, DropDownState<T> state) {
		return addRenderableWidget(new DropDownMenu<T>(x, y, width, height, state));
	}
	
	public <T> DropDownMenu<T> dropDown(int x, int y, int width, int height, Align horizontal, Align vertical, DropDownState<T> state) {
		return addRenderableWidget(new DropDownMenu<T>(getAlignedX(horizontal) + x, getAlignedY(vertical) + y, width, height, state));
	}
	
	public ModLogo modlogo(int x, int y, int width, int height) {
		return addRenderableWidget(new ModLogo(x, y, width, height, this));
	}
	
	public ModLogo modlogo(int x, int y, int width, int height, Align horizontal, Align vertical) {
		return addRenderableWidget(new ModLogo(getAlignedX(horizontal) + x, getAlignedY(vertical) + y, width, height, this));
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
	
	@Override
	public void mouseMoved(double mouseX, double mouseY) {
		for(GuiEventListener listener : children()) {
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
