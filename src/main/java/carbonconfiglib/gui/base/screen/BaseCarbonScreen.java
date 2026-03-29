package carbonconfiglib.gui.base.screen;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import org.lwjgl.input.Mouse;

import carbonconfiglib.gui.base.helpers.Align;
import carbonconfiglib.gui.base.helpers.GuiUtils;
import carbonconfiglib.gui.base.helpers.ITooltipProvider;
import carbonconfiglib.gui.base.helpers.Icon;
import carbonconfiglib.gui.base.helpers.Texts;
import carbonconfiglib.gui.base.interaction.IInteractable;
import carbonconfiglib.gui.base.interaction.IInteractableContainer;
import carbonconfiglib.gui.base.interaction.IRenderable;
import carbonconfiglib.gui.base.interaction.IWidget;
import carbonconfiglib.gui.base.widgets.CarbonBaseButton;
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
import net.minecraft.client.gui.GuiScreen;
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
public class BaseCarbonScreen extends GuiScreen implements IInteractableContainer, ITickableScreen
{
	public static final int DEFAULT_DELAY = 200;
	private IInteractable focused;
	private boolean isDragging;
	protected List<IInteractable> interactables = new ObjectArrayList<>();
	protected List<IRenderable> renderable = new ObjectArrayList<>();
	protected boolean renderBackground = true;
	protected int centerX;
	protected int centerY;
	protected int tick;
	int lastMouseX = 0;
	int lastMouseY = 0;
	double lastX = -1;
	double lastY = -1;
	long lastCheck = 0L;
	int lastDrawnToolTipAmount = 0;
	boolean renderTooltip = false;
	
	
	private int eventButton;
	private long lastMouseEvent;
	private int touchValue;
	
	public BaseCarbonScreen() {
	}
	
	@Override
	public void initGui() {
		super.initGui();
		centerX = (this.width / 2);
		centerY = (this.height / 2);
		interactables.clear();
		renderable.clear();
	}
	
	public <T extends IWidget> T addWidget(T widget) {
		interactables.add(widget);
		renderable.add(widget);
		return widget;
	}
	
	@Override
	public void tick() {
		tick++;
	}
	
	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		if(mouseX != Integer.MAX_VALUE && mouseY != Integer.MAX_VALUE) {
			double weel = Mouse.getDWheel() / 120D;
			if(((int)weel) != 0 && mouseScroll(mouseX, mouseY, weel));
		}
		if(renderBackground) drawDefaultBackground();
		renderBackground(mouseX, mouseY, partialTicks);
		renderWidgets(mouseX, mouseY, partialTicks);
		renderForeground(mouseX, mouseY, partialTicks);
		renderTooltips(mouseX, mouseY, partialTicks);
	}
	
	public void renderBackground(int mouseX, int mouseY, float partialTicks) {
		
	}
	
	public void renderWidgets(int mouseX, int mouseY, float partialTicks) {
		for(IRenderable widget : this.renderable) {
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
			for(IInteractable listener : children()) {
				if(listener instanceof ITooltipProvider) {
					((ITooltipProvider)listener).provideTooltips(mouseX, mouseY, T -> tooltips.addAll(GuiUtils.splitLines(fontRendererObj, T, Math.max(mouseX, width - mouseX) - 20)));
				}
			}
			collectTooltips(mouseX, mouseY, partialTicks, T -> tooltips.addAll(GuiUtils.splitLines(fontRendererObj, T, Math.max(mouseX, width - mouseX) - 20)));
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
		drawHoveringText(tooltips, mouseX, mouseY);
		lastDrawnToolTipAmount = tooltips.size();
	}
	
	public final boolean isDragging() {
		return this.isDragging;
	}
	
	public final void setDragging(boolean value) {
		this.isDragging = value;
	}
	
	public IInteractable getFocused() {
		return this.focused;
	}
	
	public void setFocused(IInteractable interact) {
		this.focused = interact;
	}

	@Override
	public List<? extends IInteractable> children() {
		return interactables;
	}
	
	protected void setScreen(GuiScreen screen) {
		Minecraft.getMinecraft().displayGuiScreen(screen);
	}
	
	public static void setExternalScreen(GuiScreen screen) {
		Minecraft.getMinecraft().displayGuiScreen(screen);
	}
	
	public static void pushExternalScreen(GuiScreen screen) {
		LayeredScreen.pushGuiLayer(screen);
	}
	
	protected void pushScreen(GuiScreen screen) {
		LayeredScreen.pushGuiLayer(screen);
	}
	
	public void onClose() {
		LayeredScreen.popGuiLayer();
		if(this.mc.currentScreen == null){
			this.mc.setIngameFocus();
		}
	}
	
	public void drawText(ITextComponent text, float x, float y, Align align, int color) {
		GuiUtils.drawText(fontRendererObj, text, x + centerX, y + centerY, align, color);
	}
	
	public void drawUnalignedText(ITextComponent text, float x, float y, Align align, int color) {
		GuiUtils.drawText(fontRendererObj, text, x, y, align, color);
	}
	
	public void drawSplitText(ITextComponent text, float x, float y, Align align, int maxWidth, int color) {
		GuiUtils.drawSplitText(fontRendererObj, text, x + centerX, y + centerY, align, color, maxWidth);
	}
	
	public CarbonButton button(int x, int y, int width, int height, ITextComponent text, Consumer<CarbonBaseButton> listener) {
		return addWidget(new CarbonButton(x, y, width, height, text, listener));
	}
	
	public CarbonButton button(int x, int y, int width, int height, Align horizontal, Align vertical, ITextComponent text, Consumer<CarbonBaseButton> listener) {
		return addWidget(new CarbonButton(getAlignedX(horizontal) + x, getAlignedY(vertical) + y, width, height, text, listener));
	}
	
	public CarbonButton iconButton(int x, int y, int width, int height, Icon icon, Consumer<CarbonBaseButton> listener) {
		return addWidget(new CarbonButton(x, y, width, height, Texts.empty(), listener).withIcon(Optional.of(icon)));
	}
	
	public CarbonButton iconButton(int x, int y, int width, int height, Align horizontal, Align vertical, Icon icon, Consumer<CarbonBaseButton> listener) {
		return addWidget(new CarbonButton(getAlignedX(horizontal) + x, getAlignedY(vertical) + y, width, height, Texts.empty(), listener).withIcon(Optional.of(icon)));
	}
	
	public CarbonCheckBox checkbox(int x, int y, int width, int height, CheckBoxState state) {
		return addWidget(new CarbonCheckBox(x, y, width, height, state));
	}
	
	public CarbonCheckBox checkbox(int x, int y, int width, int height, Align horizontal, Align vertical, CheckBoxState state) {
		return addWidget(new CarbonCheckBox(getAlignedX(horizontal) + x, getAlignedY(vertical) + y, width, height, state));
	}
	
	public CarbonEditBox text(int x, int y, int width, int height, TextState state) {
		return addWidget(new CarbonEditBox(fontRendererObj, x, y, width, height, state));
	}
	
	public CarbonEditBox text(int x, int y, int width, int height, Align horizontal, Align vertical, TextState state) {
		return addWidget(new CarbonEditBox(fontRendererObj, getAlignedX(horizontal) + x, getAlignedY(vertical) + y, width, height, state));
	}
	
	public CarbonSlider slider(int x, int y, int width, int height, SliderState state) {
		return addWidget(new CarbonSlider(x, y, width, height, state));
	}
	
	public CarbonSlider slider(int x, int y, int width, int height, Align horizontal, Align vertical, SliderState state) {
		return addWidget(new CarbonSlider(getAlignedX(horizontal) + x, getAlignedY(vertical) + y, width, height, state));
	}
	
	public <T> DropDownMenu<T> dropDown(int x, int y, int width, int height, DropDownState<T> state) {
		return addWidget(new DropDownMenu<T>(x, y, width, height, state));
	}
	
	public <T> DropDownMenu<T> dropDown(int x, int y, int width, int height, Align horizontal, Align vertical, DropDownState<T> state) {
		return addWidget(new DropDownMenu<T>(getAlignedX(horizontal) + x, getAlignedY(vertical) + y, width, height, state));
	}
	
	public ModLogo modlogo(int x, int y, int width, int height) {
		return addWidget(new ModLogo(x, y, width, height, this));
	}
	
	public ModLogo modlogo(int x, int y, int width, int height, Align horizontal, Align vertical) {
		return addWidget(new ModLogo(getAlignedX(horizontal) + x, getAlignedY(vertical) + y, width, height, this));
	}
	
	public <T extends ListEntry<T>> CarbonList<T> listArea(int x, int y, int width, int height, ListState<T> state) {
		CarbonList<T> list = addWidget(new CarbonList<>(width, height, y, y+height, state.setRowWidth(width).setScrollOffset(0)));
		list.setLeftPos(x);
		list.setRenderBackground(false);
		list.setRenderTopAndBottom(false);
		return list;
	}
	
	public <T extends ListEntry<T>> CarbonList<T> listArea(int x, int y, int width, int height, Align horizontal, Align vertical, ListState<T> state) {
		x = getAlignedX(horizontal) + x;
		y = getAlignedY(vertical) + y;
		CarbonList<T> list = addWidget(new CarbonList<>(width, height, y, y+height, state.setRowWidth(width).setScrollOffset(0)));
		list.setLeftPos(x);
		list.setRenderBackground(false);
		list.setRenderTopAndBottom(false);
		return list;
	}

	
	@Override
	public void handleMouseInput() throws IOException {
		int i = Mouse.getEventX() * this.width / this.mc.displayWidth;
		int j = this.height - Mouse.getEventY() * this.height / this.mc.displayHeight - 1;
		int k = Mouse.getEventButton();
		if (Mouse.getEventButtonState()) {
			if (this.mc.gameSettings.touchscreen && this.touchValue++ > 0) return;
			this.eventButton = k;
			this.lastMouseEvent = Minecraft.getSystemTime();
			this.mouseClicked(i, j, this.eventButton);
		}
		else if (k != -1) {
			if (this.mc.gameSettings.touchscreen && --this.touchValue > 0) return;
			this.eventButton = -1;
			this.mouseReleased(i, j, k);
		}
		else if (this.lastMouseEvent > 0L) {
			if(this.eventButton != -1) this.mouseClickMove(i, j, this.eventButton, Minecraft.getSystemTime() - this.lastMouseEvent);
			else this.mouseMoved(i, j);
		}
		else this.mouseMoved(i, j);
	}
	
	@Override
	public void mouseMoved(double mouseX, double mouseY) {
		for(IInteractable listener : children()) {
			listener.mouseMoved(mouseX, mouseY);
		}
	}
	
	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
		lastX = mouseX;
		lastY = mouseY;
		if(mouseClick(mouseX, mouseY, mouseButton)) return;
		super.mouseClicked(mouseX, mouseY, mouseButton);
	}

	@Override
	protected void mouseReleased(int mouseX, int mouseY, int state) {
		if(mouseRelease(mouseX, mouseY, state)) return;
		super.mouseReleased(mouseX, mouseY, state);
	}

	@Override
	protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
		double diffX = mouseX - lastX;
		double diffY = mouseY - lastY;
		lastX = mouseX;
		lastY = mouseY;
		if(mouseDrag(mouseX, mouseY, clickedMouseButton, diffX, diffY)) return;
		super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
	}
	
	@Override
	protected void keyTyped(char typedChar, int keyCode) throws IOException {
		if(this.charTyped(typedChar, keyCode)) return;
		if (keyCode == 1 && shouldCloseOnEsc()) {
			onClose();
		}
	}
	
	protected boolean shouldCloseOnEsc() {
		return true;
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
