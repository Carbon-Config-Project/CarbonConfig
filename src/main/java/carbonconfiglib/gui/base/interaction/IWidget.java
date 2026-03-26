package carbonconfiglib.gui.base.interaction;

public interface IWidget extends IRenderable, IInteractable
{
	public void setX(int x);
	public void setY(int y);
	public int getX();
	public int getY();
	public void setWidth(int width);
	public void setHeight(int height);
	public int getWidth();
	public int getHeight();
	public boolean isHovered();
	public void setActive(boolean value);
	public boolean isActive();
}
