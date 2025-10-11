package carbonconfiglib.gui.nodes;

import carbonconfiglib.gui.api.INode;
import carbonconfiglib.gui.nodes.base.BaseElement;

public abstract class NodeElement extends BaseElement
{
	INode node;
	
	public NodeElement(INode node) {
		this.node = node;
	}
	
	protected void readValue() {}
	
	@Override
	protected boolean isChanged() {
		return node.isChanged();
	}
	
	@Override
	protected boolean isNotDefault() {
		return !node.isDefault();
	}
	
	@Override
	protected void onRevert() {
		node.setPrevious();
		readValue();
	}
	
	@Override
	protected void onReset() {
		node.setDefault();
		readValue();
	}
}
