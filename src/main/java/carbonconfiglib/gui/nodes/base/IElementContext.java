package carbonconfiglib.gui.nodes.base;

public interface IElementContext
{
	public boolean isElementActive(BaseElement base);
	
	public boolean isAtTop(int layer);
	public int calculateSegmentWidth();
	
	public void setTooltipFocused(BaseElement element);
}
