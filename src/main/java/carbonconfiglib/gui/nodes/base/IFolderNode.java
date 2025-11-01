package carbonconfiglib.gui.nodes.base;

public interface IFolderNode
{
	public void setCallbacks(IFolderController listener);
	
	public static interface IFolderController {
		public void pushNode(BaseElement element, int index, boolean reload);
		public void pushChild(BaseElement element, int index, int childIndex, boolean reverse);
	}
}
