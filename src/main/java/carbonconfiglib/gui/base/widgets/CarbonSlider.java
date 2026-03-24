package carbonconfiglib.gui.base.widgets;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.LongFunction;

import org.lwjgl.glfw.GLFW;

import com.mojang.blaze3d.systems.RenderSystem;

import carbonconfiglib.gui.base.helpers.Align;
import carbonconfiglib.gui.base.helpers.GuiUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;


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
public class CarbonSlider extends CarbonBaseButton {
	SliderState state;
	
	public CarbonSlider(int xPos, int yPos, int width, int height, SliderState state) {
		super(xPos, yPos, width, height, state.getCurrentDisplayText(), null);
		state.owner = this;
		this.state = state;
	}
	
	protected void onValueChanged() {
		if(state.listener != null) {
			state.listener.accept(this);
		}
	}
	
	public void updateMessage() {
		setMessage(state.getCurrentDisplayText());
	}
	
	public SliderState getState() {
		return state;
	}
	
	public long get() {
		return state.get();
	}
	
	public double getProgress() {
		return (double)(state.get() - state.getMin()) / (double)state.getRange();
	}
	
	protected void setFromMouse(double mouseX) {
		double progress = Math.max(0, Math.min(1, (mouseX - (getX() + 4D)) / (width - 8D)));
		state.set((long)(state.getMin() + (state.getRange() * progress)));
	}
	
	@Override
	public void onClick(double mouseX, double mouseY) {
		setFromMouse(mouseX);
	}
	
	@Override
	protected void onDrag(double mouseX, double mouseY, double dragX, double dragY) {
		setFromMouse(mouseX);
		super.onDrag(mouseX, mouseY, dragX, dragY);
	}
	
	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
		if(state.stepSize != 0 && active && visible) {
			state.set(get() + (long)(state.stepSize * scrollY * (Screen.hasShiftDown() ? 10D : 1D) * (Screen.hasControlDown() ? 100D : 1D)));
			return true;
		}
		return false;
	}
	
	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if(state.stepSize != 0 && active && visible && (keyCode == GLFW.GLFW_KEY_LEFT || keyCode == GLFW.GLFW_KEY_RIGHT)) {
			state.set(get() + (state.stepSize * ((keyCode == GLFW.GLFW_KEY_LEFT ? -1L : 0L) + (keyCode == GLFW.GLFW_KEY_RIGHT ? 1L : 0L)) * (Screen.hasShiftDown() ? 10L : 1L) * (Screen.hasControlDown() ? 100L : 1L)));
			return true;
		}
		return false;
	}
	
	@Override
	public void renderWidget(GuiGraphics graphics, int pMouseX, int pMouseY, float pPartialTick) {
	      Minecraft minecraft = Minecraft.getInstance();
	      Font font = minecraft.font;
	      graphics.blitSprite(SPRITES.disabled(), getX(), getY(), width, height);
	      this.renderBg(graphics, minecraft, pMouseX, pMouseY);
	      int j = getFGColor();
	      GuiUtils.drawScrollingText(graphics, font, getMessage(), getX(), getY()+1, width, height, Align.CENTER, j | Mth.ceil(this.alpha * 255.0F) << 24, 0);
	}
	
	protected void renderBg(GuiGraphics graphics, Minecraft mc, int mouseX, int mouseY) {
		if(isActive()) RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		else RenderSystem.setShaderColor(0.5F, 0.5F, 0.5F, 1.0F);
		double range = getProgress();
		graphics.blitSprite(SPRITES.get(true, isActive() && isHoveredOrFocused()), this.getX() + (int)(range * (float)(this.width - 8)), getY(), 8, height);
	}
	
	public static class SliderState {
		private static final DecimalFormat NUMBERS = new DecimalFormat("###,###", DecimalFormatSymbols.getInstance(Locale.ROOT));
		long minValue;
		long maxValue;
		long value;
		long stepSize = 1L;
		LongFunction<Component> displayText = T -> Component.literal(NUMBERS.format(T));
		Component prefix = Component.empty();
		Component suffix = Component.empty();
		Consumer<CarbonSlider> listener;
		CarbonSlider owner;
		
		public SliderState(long value, long minValue, long maxValue) {
			this.minValue = minValue;
			this.maxValue = maxValue;
			this.value = value;
		}

		public SliderState(long value, long minValue, long maxValue, LongFunction<Component> displayText) {
			this.minValue = minValue;
			this.maxValue = maxValue;
			this.value = value;
			this.displayText = Objects.requireNonNull(displayText);
		}
		
		public SliderState(long value, long minValue, long maxValue, Component prefix, Component suffix) {
			this.minValue = minValue;
			this.maxValue = maxValue;
			this.value = value;
			this.prefix = Objects.requireNonNull(prefix);
			this.suffix = Objects.requireNonNull(suffix);
		}
		
		public SliderState(long value, long minValue, long maxValue, LongFunction<Component> displayText, Component prefix, Component suffix) {
			this.minValue = minValue;
			this.maxValue = maxValue;
			this.value = value;
			this.displayText = Objects.requireNonNull(displayText);
			this.prefix = Objects.requireNonNull(prefix);
			this.suffix = Objects.requireNonNull(suffix);
		}
		
		public SliderState setDisplayFunction(LongFunction<Component> displayText) {
			this.displayText = Objects.requireNonNull(displayText);
			return this;
		}
		
		public SliderState setListener(Consumer<CarbonSlider> listener) {
			this.listener = listener;
			return this;
		}
		
		public SliderState setListener(Runnable run) {
			listener = T -> run.run();
			return this;
		}
		
		public SliderState setStepSize(long value) {
			this.stepSize = value;
			return this;
		}
		
		public SliderState setMaxValue(long maxValue) {
			if(maxValue < minValue) return this;
			this.maxValue = maxValue;
			long oldValue = value;
			this.value = Math.min(maxValue, value);
			if(owner != null) {
				if(oldValue != value) owner.onValueChanged();
				owner.updateMessage();
			}
			return this;
		}
		
		public SliderState setMinValue(long minValue) {
			if(maxValue < minValue) return this;
			this.minValue = minValue;
			long oldValue = value;
			this.value = Math.max(minValue, value);
			if(owner != null) {
				if(oldValue != value) owner.onValueChanged();
				owner.updateMessage();
			}
			return this;
		}
		
		public SliderState set(long value) {
			long newValue = Math.max(minValue, Math.min(maxValue, value));
			if(this.value != newValue) {
				this.value = newValue;
				if(owner != null) {
					owner.onValueChanged();
					owner.updateMessage();
				}
			}
			return this;
		}
		
		public SliderState setSilent(long value) {
			long newValue = Math.max(minValue, Math.min(maxValue, value));
			if(this.value != newValue) {
				this.value = newValue;
				if(owner != null) owner.updateMessage();
			}
			return this;
		}
		
		public SliderState increaseOnce() { return set(value+1); }
		public SliderState decreaseOnce() { return set(value-1); }
		
		public SliderState setPrefix(Component prefix) {
			this.prefix = Objects.requireNonNull(prefix);
			if(owner != null) owner.updateMessage();
			return this;
		}
		
		public SliderState setSuffix(Component suffix) {
			this.suffix = Objects.requireNonNull(suffix);
			if(owner != null) owner.updateMessage();
			return this;
		}
		
		public Component getCurrentDisplayText() {
			return Component.empty().append(prefix).append(displayText.apply(value)).append(suffix);
		}
		public long getMin() { return minValue; }
		public long getMax() { return maxValue; }
		public long getRange() { return maxValue - minValue; }
		public long get() { return value; }
		public CarbonSlider getOwner() { return owner; }
	}
}
