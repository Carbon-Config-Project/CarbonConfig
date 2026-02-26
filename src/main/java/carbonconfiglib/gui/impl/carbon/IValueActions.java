package carbonconfiglib.gui.impl.carbon;

import carbonconfiglib.gui.api.node.INode;

public interface IValueActions extends INode
{
	public void set(String value);
	public void save();
}
