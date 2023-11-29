package carbonconfiglib.gui.widgets.screen;

import java.util.List;
import java.util.ListIterator;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

import javax.annotation.Nullable;

public interface IInteractableContainer extends IInteractable
{
	List<? extends IInteractable> children();
	
	default IInteractable getChildAt(double mouseX, double mouseY) {
		for(IInteractable interact : this.children()) {
			if (interact.isMouseOver(mouseX, mouseY)) {
				return interact;
			}
		}
		return null;
	}
	
	default boolean mouseClick(double mouseX, double mouseY, int button) {
		for(IInteractable interact : this.children()) {
			if (interact.mouseClick(mouseX, mouseY, button)) {
				this.setFocused(interact);
				if (button == 0) {
					this.setDragging(true);
				}
				
				return true;
			}
		}
		return false;
	}

	default boolean mouseRelease(double mouseX, double mouseY, int button) {
		this.setDragging(false);
		IInteractable act = getChildAt(mouseX, mouseY);
		return act != null && act.mouseRelease(mouseX, mouseY, button);
	}

	default boolean mouseDrag(double mouseX, double mouseY, int button, double dragX, double dragY) {
		return this.getFocused() != null && this.isDragging() && button == 0 && this.getFocused().mouseDrag(mouseX, mouseY, button, dragX, dragY);
	}
	
	boolean isDragging();
	void setDragging(boolean p_94720_);
	
	default boolean mouseScroll(double mouseX, double mouseY, double scroll) {
		IInteractable act = getChildAt(mouseX, mouseY);
		return act != null && act.mouseScroll(mouseX, mouseY, scroll);
	}
	
	default boolean charTyped(char character, int keyCode) {
		return this.getFocused() != null && this.getFocused().charTyped(character, keyCode);
	}
	
	@Nullable
	IInteractable getFocused();

	void setFocused(@Nullable IInteractable interact);
	
	default void setInitialFocus(@Nullable IInteractable interact) {
		this.setFocused(interact);
		interact.changeFocus(true);
	}
	
	default void magicalSpecialHackyFocus(@Nullable IInteractable interact) {
		this.setFocused(interact);
	}
	
	default boolean changeFocus(boolean value) {
		IInteractable interact = this.getFocused();
		boolean flag = interact != null;
		if (flag && interact.changeFocus(value)) {
			return true;
		} else {
			List<? extends IInteractable> list = this.children();
			int j = list.indexOf(interact);
			int i;
			if (flag && j >= 0) {
				i = j + (value ? 1 : 0);
			} else if (value) {
				i = 0;
			} else {
				i = list.size();
			}
			
			ListIterator<? extends IInteractable> iter = list.listIterator(i);
			BooleanSupplier hasNext = value ? iter::hasNext : iter::hasPrevious;
			Supplier<? extends IInteractable> next = value ? iter::next : iter::previous;
			while(hasNext.getAsBoolean()) {
				IInteractable listener = next.get();
				if (listener.changeFocus(value)) {
					this.setFocused(listener);
					return true;
				}
			}
			this.setFocused((IInteractable)null);
			return false;
		}
	}
}
