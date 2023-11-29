package carbonconfiglib.gui.widgets.screen;

public interface IWidget extends IRenderable, IInteractable
{
	public void setX(int x);
	public void setY(int y);
	public int getX();
	public int getY();
	public int getWidgetWidth();
	public int getWidgetHeight();
	public boolean isHovered();
}
