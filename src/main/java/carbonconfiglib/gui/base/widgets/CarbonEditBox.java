package carbonconfiglib.gui.base.widgets;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

import com.google.common.base.Predicate;

import carbonconfiglib.gui.base.helpers.ITooltipProvider;
import carbonconfiglib.gui.base.interaction.IWidget;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiPageButtonList.GuiResponder;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.util.text.ITextComponent;


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
public class CarbonEditBox extends GuiTextField implements ITooltipProvider, IWidget, GuiResponder {
	TextState state;
	boolean hovered;

	public CarbonEditBox(FontRenderer font, int x, int y, int width, int height) {
		this(font, x, y, width, height, new TextState());
	}
	
	public CarbonEditBox(FontRenderer font, int x, int y, int width, int height, TextState state) {
		super(0, font, x, y, width, height);
		this.state = state;
		setText(state.getValue());
		setValidator(state.getFilter());
		if(state.getMaxLength() > 0) setMaxStringLength(state.getMaxLength());
		this.setGuiResponder(this);
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
	public void setText(String textIn) {
		super.setText(textIn);
		if(state.getFilter().apply(textIn)) {
			setResponderEntryValue(0, getText());
		}
	}

	@Override
	public void render(int mouseX, int mouseY, float partialTicks) {
		hovered = this.getVisible() && state.enabled && mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;
		drawTextBox();
	}
	
	public void tick() {
		if(isFocused()) {
			updateCursorCounter();
		}
	}
	
	protected boolean canShowTooltip(double mouseX, double mouseY) {
		return this.getVisible() && mouseX >= (double)this.x && mouseY >= (double)this.y && mouseX < (double)(this.x + this.width) && mouseY < (double)(this.y + this.height);
	}
	
	@Override
	public void provideTooltips(int mouseX, int mouseY, Consumer<ITextComponent> tooltips) {
		if(state.tooltip != null && canShowTooltip(mouseX, mouseY)) {
			ITextComponent result = state.tooltip.apply(state);
			if(result == null) return;
			tooltips.accept(result);
		}
	}
	
	public boolean mouseClick(double mouseX, double mouseY, int button) {
		return state.enabled && mouseClicked((int)mouseX, (int)mouseY, button);
	}
	
	@Override
	public boolean charTyped(char pCodePoint, int pModifiers) {
		return state.enabled && textboxKeyTyped(pCodePoint, pModifiers);
	}
	
	@Override
	public void setEntryValue(int id, boolean value) {}
	@Override
	public void setEntryValue(int id, float value) {}
	@Override
	public void setEntryValue(int id, String value) {
		state.updateValue(value);
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
	public int getWidth() { return width; }
	@Override
	public int getHeight() { return height; }
	@Override
	public boolean isHovered() { return hovered; }


	public static class TextState {
		Predicate<String> filter = Objects::nonNull;
		Function<TextState, ITextComponent> tooltip;
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
		
		public TextState(String value, Predicate<String> filter) {
			this.filter = filter;
			this.value = value;
		}
		
		void setOwner(CarbonEditBox owner) {
			this.owner = owner;
		}
		
		public TextState setCallback(Consumer<String> listener) {
			this.callback = listener;
			return this;
		}
		
		public TextState setTooltip(ITextComponent tooltip) {
			this.tooltip = T -> tooltip;
			return this;
		}
		
		public TextState withTooltip(Function<TextState, ITextComponent> tooltip) {
			this.tooltip = tooltip;
			return this;
		} 
		
		public CarbonEditBox getOwner() {
			return owner; 
		}
		
		public TextState setFilter(Predicate<String> filter, String defaultValue) {
			this.filter = filter;
			if(owner != null) {
				owner.setValidator(filter);
			}
			if(filter != null && (!filter.test(value) || value.isEmpty())) {
				setValue(defaultValue);
			}
			return this;
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
		
		public Predicate<String> getFilter() {
			return filter; 
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
