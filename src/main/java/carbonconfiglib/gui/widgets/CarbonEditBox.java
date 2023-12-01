package carbonconfiglib.gui.widgets;

import java.util.Objects;
import java.util.function.Consumer;

import carbonconfiglib.gui.config.IListOwner;
import carbonconfiglib.gui.widgets.screen.IWidget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
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
public class CarbonEditBox extends GuiTextField implements IOwnable, IWidget
{
	IListOwner owner;
	boolean bordered = true;
	int innerDiff = 8;
	Consumer<String> listener;
	boolean field_146123_n;
	
	public CarbonEditBox(FontRenderer font, int x, int y, int width, int height) {
		super(font, x, y, width, height);
		setCanLoseFocus(true);
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
		//TODO implement me?
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
		field_146123_n = this.getVisible() && mouseX >= this.xPosition && mouseY >= this.yPosition && mouseX < this.xPosition + this.width && mouseY < this.yPosition + this.height;
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
		if(this.getVisible() && mouseX >= this.xPosition && mouseY >= this.yPosition && mouseX < this.xPosition + this.width && mouseY < this.yPosition + this.height) {
			mouseClicked((int)mouseX, (int)mouseY, button);
			return true;
		}
		return false;
	}
	
	@Override
	public boolean charTyped(char character, int keyCode) {
		return textboxKeyTyped(character, keyCode);
	}
	
	@Override
	public void writeText(String p_146191_1_) {
		String s = getText();
		super.writeText(p_146191_1_);
		if(!Objects.equals(s, getText())) {
			if(listener != null) {
				listener.accept(getText());
			}
		}
	}
	
	@Override
	public void deleteFromCursor(int p_146175_1_) {
		String s = getText();
		super.deleteFromCursor(p_146175_1_);
		if(!Objects.equals(s, getText())) {
			if(listener != null) {
				listener.accept(getText());
			}
		}
	}
		
	@Override
	public void setX(int x) { this.xPosition = x; }
	@Override
	public void setY(int y) { this.yPosition = y; }
	@Override
	public int getX() { return xPosition; }
	@Override
	public int getY() { return yPosition; }
	@Override
	public int getWidgetWidth() { return width; }
	@Override
	public int getWidgetHeight() { return height; }
	@Override
	public boolean isHovered() { return field_146123_n; }
}
