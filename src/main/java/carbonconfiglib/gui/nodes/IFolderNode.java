package carbonconfiglib.gui.nodes;

import java.util.function.ObjIntConsumer;

public interface IFolderNode
{
	public void setCallbacks(ObjIntConsumer<BaseElement> nodes);
}
