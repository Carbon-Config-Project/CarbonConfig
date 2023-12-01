package carbonconfiglib.gui.widgets;

import java.util.function.Consumer;

import carbonconfiglib.gui.config.IListOwner;
import carbonconfiglib.gui.widgets.screen.IWidget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiPageButtonList.GuiResponder;
import net.minecraft.client.gui.GuiTextField;

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
public class CarbonEditBox extends GuiTextField implements IOwnable, GuiResponder, IWidget
{
	IListOwner owner;
	boolean bordered = true;
	int innerDiff = 8;
	Consumer<String> listener;
	boolean hovered;
	
	public CarbonEditBox(FontRenderer font, int x, int y, int width, int height) {
		super(0, font, x, y, width, height);
		this.setGuiResponder(this);
	}
	
	public CarbonEditBox setInnerDiff(int innerDiff) {
		this.innerDiff = innerDiff;
		return this;
	}
	
	public CarbonEditBox setListener(Consumer<String> listener) {
		this.listener = listener;
		return this;
	}
	
	public CarbonEditBox setSuggestion(String value) {
		return this;
	}
	
	public void setOwner(IListOwner owner) {
		this.owner = owner;
	}
	
	@Override
	public void setFocused(boolean focus) {
		super.setFocused(focus);
		if(focus && owner != null) {
			owner.setActiveWidget(this);
		}
	}
	
	@Override
	public int getWidth() {
		return bordered ? this.width - innerDiff : this.width;
	}
	
	@Override
	public void setEnableBackgroundDrawing(boolean value) {
		super.setEnableBackgroundDrawing(value);
		this.bordered = value;
	}
	
	@Override
	public void render(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
		hovered = this.getVisible() && mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;
		drawTextBox();
	}
	
	@Override
	public void drawTextBox() {
		if(this.isFocused() && owner != null && !owner.isActiveWidget(this)) {
			setFocused(false);
		}
		super.drawTextBox();
	}
	
	public void tick() {
		if(isFocused()) {
			updateCursorCounter();
		}
	}
	
	@Override
	public boolean mouseClick(double mouseX, double mouseY, int button) {
		return mouseClicked((int)mouseX, (int)mouseY, button);
	}
	
	@Override
	public boolean charTyped(char character, int keyCode) {
		return textboxKeyTyped(character, keyCode);
	}
	
	@Override
	public void setEntryValue(int id, boolean value) {}
	@Override
	public void setEntryValue(int id, float value) {}
	@Override
	public void setEntryValue(int id, String value) {
		if(listener != null) {
			listener.accept(value);
		}
	}
	
	@Override
	public void setX(int x) { this.x = x; }
	@Override
	public void setY(int y) { this.y = y; }
	@Override
	public int getX() { return x; }
	@Override
	public int getY() { return y; }
	@Override
	public int getWidgetWidth() { return width; }
	@Override
	public int getWidgetHeight() { return height; }
	@Override
	public boolean isHovered() { return hovered; }
}
