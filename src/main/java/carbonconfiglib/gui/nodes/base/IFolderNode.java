package carbonconfiglib.gui.nodes.base;

import java.util.function.ObjIntConsumer;

public interface IFolderNode
{
	public void setCallbacks(ObjIntConsumer<BaseElement> listener);
}
