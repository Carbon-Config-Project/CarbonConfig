package carbonconfiglib.gui.base.widgets;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

import com.google.common.base.Predicate;

import carbonconfiglib.gui.base.helpers.ITooltipProvider;
import carbonconfiglib.gui.base.interaction.IWidget;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.util.IChatComponent;


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
public class CarbonEditBox extends GuiTextField implements ITooltipProvider, IWidget {
	TextState state;
	boolean hovered;

	public CarbonEditBox(FontRenderer font, int x, int y, int width, int height) {
		this(font, x, y, width, height, new TextState());
	}
	
	public CarbonEditBox(FontRenderer font, int x, int y, int width, int height, TextState state) {
		super(font, x, y, width, height);
		this.state = state;
		setText(state.getValue());
		if(state.getMaxLength() > 0) setMaxStringLength(state.getMaxLength());
		state.setOwner(this);
	}
	
	public TextState getState() {
		return state;
	}
	
	@Override
	public void setActive(boolean value) {
		state.enabled = value;
	}

	@Override
	public boolean isActive() {
		return state.enabled;
	}
	
	@Override
	public void setWidth(int width) {
		this.width = width;
	}

	@Override
	public void setHeight(int height) {
		this.height = height;
	}
	
	@Override
	public void render(int mouseX, int mouseY, float partialTicks) {
		hovered = this.getVisible() && state.enabled && mouseX >= this.xPosition && mouseY >= this.yPosition && mouseX < this.xPosition + this.width && mouseY < this.yPosition + this.height;
		drawTextBox();
	}
	
	public void tick() {
		if(isFocused()) {
			updateCursorCounter();
		}
	}
	
	protected boolean canShowTooltip(double mouseX, double mouseY) {
		return this.getVisible() && mouseX >= (double)this.xPosition && mouseY >= (double)this.yPosition && mouseX < (double)(this.xPosition + this.width) && mouseY < (double)(this.yPosition + this.height);
	}
	
	@Override
	public void provideTooltips(int mouseX, int mouseY, Consumer<IChatComponent> tooltips) {
		if(state.tooltip != null && canShowTooltip(mouseX, mouseY)) {
			IChatComponent result = state.tooltip.apply(state);
			if(result == null) return;
			tooltips.accept(result);
		}
	}
	
	public boolean mouseClick(double mouseX, double mouseY, int button) {
		if(state.enabled && this.getVisible() && mouseX >= this.xPosition && mouseY >= this.yPosition && mouseX < this.xPosition + this.width && mouseY < this.yPosition + this.height) {
			mouseClicked((int)mouseX, (int)mouseY, button);
			return true;
		}
		return false;
	}
	
	@Override
	public boolean charTyped(char pCodePoint, int pModifiers) {
		return state.enabled && textboxKeyTyped(pCodePoint, pModifiers);
	}
	
	@Override
	public void writeText(String p_146191_1_) {
		String s = getText();
		super.writeText(p_146191_1_);
		if(!Objects.equals(s, getText())) {
			state.updateValue(getText());
		}
	}
	
	@Override
	public void deleteFromCursor(int p_146175_1_) {
		String s = getText();
		super.deleteFromCursor(p_146175_1_);
		if(!Objects.equals(s, getText())) {
			state.updateValue(getText());
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
	public int getWidth() { return width; }
	@Override
	public int getHeight() { return height; }
	@Override
	public boolean isHovered() { return hovered; }


	public static class TextState {
		Function<TextState, IChatComponent> tooltip;
		Consumer<String> callback;
		String value = "";
		int maxLength = -1;
		boolean silent;
		boolean enabled = true;
		CarbonEditBox owner;
		
		public TextState() {
		}
		
		public TextState(String value) {
			this.value = value;
		}
		
		void setOwner(CarbonEditBox owner) {
			this.owner = owner;
		}
		
		public TextState setCallback(Consumer<String> listener) {
			this.callback = listener;
			return this;
		}
		
		public TextState setTooltip(IChatComponent tooltip) {
			this.tooltip = T -> tooltip;
			return this;
		}
		
		public TextState withTooltip(Function<TextState, IChatComponent> tooltip) {
			this.tooltip = tooltip;
			return this;
		} 
		
		public CarbonEditBox getOwner() {
			return owner; 
		}
		
		public TextState setMaxLength(int count) {
			this.maxLength = count;
			return this;
		}
		
		public int getMaxLength() {
			return maxLength;
		}
		
		public TextState apply(Consumer<TextState> mod) {
			mod.accept(this);
			return this;
		}
		
		void updateValue(String value) {
			this.value = value;
			if(callback != null && !silent) {
				callback.accept(value);
			}
		}
		
		public String getValue() {
			return value;
		}
		
		public TextState setValue(String value) {
			if(value == null) return this;
			if(owner == null) this.value = value;
			else owner.setText(value);
			return this;
		}
		
		public TextState setSilentValue(String value) {
			if(value == null) return this;
			if(owner == null) this.value = value;
			else {
				silent = true;
				owner.setText(value);
				silent = false;
			}
			return this;
		}
		
		public static void handleRGBCorrection(TextState state) {
			state.setCallback(T -> {
				if(T.isEmpty()) return;
				try {
					int value = Integer.parseInt(T);
					if(value > 255) state.setValue("255");
				}
				catch(Exception e) {}
			});
		}
		
		public static Predicate<String> rgbFilter() {
			return T -> {
				if(T.isEmpty()) return true;
				try {
					int value = Integer.parseInt(T);
					return value >= 0 && value <= 999;
				}
				catch(Exception e) {
					return false; 
				}
			};			
		}
		
		public static Predicate<String> numberFilter(boolean positive) {
			return T -> {
				if(T.isEmpty() || (!positive && "-".equals(T))) return true;
				try {
					int value = Integer.parseInt(T);
					return !positive || value >= 0;
				}
				catch(Exception e) {
					return false; 
				}
			};
		}
	}
}
