package carbonconfiglib.gui.base.widgets;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import carbonconfiglib.gui.base.helpers.ITooltipProvider;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;

public class CarbonEditBox extends EditBox implements ITooltipProvider {
	TextState state;
	
	public CarbonEditBox(Font font, int x, int y, int width, int height) {
		this(font, x, y, width, height, new TextState());
	}
	
	public CarbonEditBox(Font font, int x, int y, int width, int height, TextState state) {
		super(font, x, y, width, height, Component.empty());
		this.state = state;
		setValue(state.getValue());
		setSuggestion(state.getValue().isEmpty() ? state.getSuggestion() : "");
		setFilter(state.getFilter());
		if(state.getMaxLength() > 0) setMaxLength(state.getMaxLength());
		state.setOwner(this);
	}
	
	
	@Override
	public void provideTooltips(int mouseX, int mouseY, Consumer<Component> tooltips) {
		if(state.tooltip != null && isMouseOver(mouseX, mouseY)) {
			Component result = state.tooltip.apply(state);
			if(result == null) return;
			tooltips.accept(result);
		}
	}
	
	public static class TextState {
		Predicate<String> filter = Objects::nonNull;
		Function<TextState, Component> tooltip;
		Consumer<String> callback;
		String value = "";
		String suggestion;
		int maxLength = -1;
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
		
		public TextState(String value, String suggestion) {
			this.value = value;
			this.suggestion = suggestion;
		}

		public TextState(String value, Predicate<String> filter, String suggestion) {
			this.filter = filter;
			this.value = value;
			this.suggestion = suggestion;
		}
		
		void setOwner(CarbonEditBox owner) {
			if(this.owner != null) this.owner.setResponder(null);
			this.owner = owner;
			if(owner == null) return;
			owner.setResponder(this::updateValue);
		}
		
		public TextState setCallback(Consumer<String> listener) {
			this.callback = listener;
			return this;
		}
		
		public TextState setTooltip(Component tooltip) {
			this.tooltip = T -> tooltip;
			return this;
		}
		
		public TextState withTooltip(Function<TextState, Component> tooltip) {
			this.tooltip = tooltip;
			return this;
		} 
		
		public CarbonEditBox getOwner() {
			return owner; 
		}
		
		public TextState setFilter(Predicate<String> filter, String defaultValue) {
			this.filter = filter;
			if(owner != null) {
				owner.setFilter(filter);
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
			if(callback != null) {
				callback.accept(value);
			}
			if(suggestion != null && owner != null) {
				owner.setSuggestion(value.length() > 0 ? null : suggestion);
			}
		}
		
		public String getSuggestion() {
			return suggestion;
		}
		
		public TextState setSuggestion(String suggestion) {
			this.suggestion = suggestion;
			if(owner != null) owner.setSuggestion(suggestion);
			return this;
		}
		
		public String getValue() {
			return value;
		}
		
		public TextState setValue(String value) {
			if(value == null) return this;
			if(owner == null) this.value = value;
			else owner.setValue(value);
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
