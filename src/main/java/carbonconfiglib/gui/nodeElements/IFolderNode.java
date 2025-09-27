package carbonconfiglib.gui.nodeElements;

import java.util.function.ObjIntConsumer;

import carbonconfiglib.gui.api.IConfigNode;

public interface IFolderNode
{
	public void setCallbacks(int index, ObjIntConsumer<IConfigNode> nodes);
}
