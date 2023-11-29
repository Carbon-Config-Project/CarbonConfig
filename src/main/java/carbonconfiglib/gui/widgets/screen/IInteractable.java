package carbonconfiglib.gui.widgets.screen;

public interface IInteractable
{
	long DOUBLE_CLICK_THRESHOLD_MS = 250L;
	
	default void mouseMoved(double mouseX, double mouseY) {}
	default boolean mouseClick(double mouseX, double mouseY, int button) { return false; }
	default boolean mouseRelease(double mouseX, double mouseY, int button) { return false; }
	default boolean mouseDrag(double mouseX, double mouseY, int button, double dragX, double dragY) { return false; }
	default boolean mouseScroll(double mouseX, double mouseY, double scroll) { return false; }
	default boolean charTyped(char character, int keyCode) { return false; }
	default boolean changeFocus(boolean value) { return false; }
	default boolean isMouseOver(double mouseX, double mouseY) { return false; }
}
