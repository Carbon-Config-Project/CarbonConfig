package carbonconfiglib.gui.base.widgets;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.IntFunction;

import org.lwjgl.glfw.GLFW;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.client.gui.ScreenUtils;

public class CarbonSlider extends Button {
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
	
	public int get() {
		return state.get();
	}
	
	public double getProgress() {
		return (double)(state.get() - state.getMin()) / (double)state.getRange();
	}
	
	protected void setFromMouse(double mouseX) {
		double progress = Math.max(0, Math.min(1, (mouseX - (x + 4D)) / (width - 8D)));
		state.set((int)(state.getMin() + (state.getRange() * progress)));
	}
	
	@Override
	public void onPress() {}
	
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
	public boolean mouseScrolled(double mouseX, double mouseY, double scroll) {
		if(state.stepSize != 0 && active && visible) {
			state.set(get() + (int)(state.stepSize * scroll * (Screen.hasShiftDown() ? 10 : 1) * (Screen.hasControlDown() ? 100 : 1)));
			return true;
		}
		return false;
	}
	
	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if(state.stepSize != 0 && active && visible && (keyCode == GLFW.GLFW_KEY_LEFT || keyCode == GLFW.GLFW_KEY_RIGHT)) {
			state.set(get() +(state.stepSize * ((keyCode == GLFW.GLFW_KEY_LEFT ? -1 : 0) + (keyCode == GLFW.GLFW_KEY_RIGHT ? 1 : 0)) * (Screen.hasShiftDown() ? 10 : 1) * (Screen.hasControlDown() ? 100 : 1)));
			return true;
		}
		return false;
	}
	
	@Override
	protected int getYImage(boolean hovered) {
		return 0;
	}
	
	@Override
	protected void renderBg(PoseStack stack, Minecraft mc, int mouseX, int mouseY) {
		RenderSystem.setShaderTexture(0, WIDGETS_LOCATION);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		int i = (this.isHoveredOrFocused() ? 2 : 1) * 20;
		double range = getProgress();
		ScreenUtils.blitWithBorder(stack, WIDGETS_LOCATION, this.x + (int)(range * (float)(this.width - 8)), this.y, 0, 46 + i, 8, this.height, 200, 20, 2, 3, 2, 2, this.getBlitOffset());
	}
	
	public static class SliderState {
		private static final DecimalFormat NUMBERS = new DecimalFormat("###,###", DecimalFormatSymbols.getInstance(Locale.ENGLISH));
		int minValue;
		int maxValue;
		int value;
		int stepSize = 1;
		IntFunction<Component> displayText = T -> Component.literal(NUMBERS.format(T));
		Component prefix = Component.empty();
		Component suffix = Component.empty();
		Consumer<CarbonSlider> listener;
		CarbonSlider owner;
		
		public SliderState(int value, int minValue, int maxValue) {
			this.minValue = minValue;
			this.maxValue = maxValue;
			this.value = value;
		}

		public SliderState(int value, int minValue, int maxValue, IntFunction<Component> displayText) {
			this.minValue = minValue;
			this.maxValue = maxValue;
			this.value = value;
			this.displayText = Objects.requireNonNull(displayText);
		}
		
		public SliderState(int value, int minValue, int maxValue, Component prefix, Component suffix) {
			this.minValue = minValue;
			this.maxValue = maxValue;
			this.value = value;
			this.prefix = Objects.requireNonNull(prefix);
			this.suffix = Objects.requireNonNull(suffix);
		}
		
		public SliderState(int value, int minValue, int maxValue, IntFunction<Component> displayText, Component prefix, Component suffix) {
			this.minValue = minValue;
			this.maxValue = maxValue;
			this.value = value;
			this.displayText = Objects.requireNonNull(displayText);
			this.prefix = Objects.requireNonNull(prefix);
			this.suffix = Objects.requireNonNull(suffix);
		}
		
		public SliderState setListener(Consumer<CarbonSlider> listener) {
			this.listener = listener;
			return this;
		}
		
		public SliderState setStepSize(int value) {
			this.stepSize = value;
			return this;
		}
		
		public SliderState setMaxValue(int maxValue) {
			if(maxValue < minValue) return this;
			this.maxValue = maxValue;
			int oldValue = value;
			this.value = Math.min(maxValue, value);
			if(owner != null) {
				if(oldValue != value) owner.onValueChanged();
				owner.updateMessage();
			}
			return this;
		}
		
		public SliderState setMinValue(int minValue) {
			if(maxValue < minValue) return this;
			this.minValue = minValue;
			int oldValue = value;
			this.value = Math.max(minValue, value);
			if(owner != null) {
				if(oldValue != value) owner.onValueChanged();
				owner.updateMessage();
			}
			return this;
		}
		
		public SliderState set(int value) {
			int newValue = Math.max(minValue, Math.min(maxValue, value));
			if(this.value != newValue) {
				this.value = newValue;
				if(owner != null) owner.onValueChanged();
			}
			if(owner != null) owner.updateMessage();
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
		public int getMin() { return minValue; }
		public int getMax() { return maxValue; }
		public int getRange() { return maxValue - minValue; }
		public int get() { return value; }
		public CarbonSlider getOwner() { return owner; }
	}
}
