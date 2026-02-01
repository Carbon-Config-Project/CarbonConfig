package carbonconfiglib.gui.nodes;

import java.util.List;

import carbonconfiglib.api.ISuggestionProvider.Suggestion;
import carbonconfiglib.gui.api.IArrayNode;
import carbonconfiglib.gui.api.INode;
import carbonconfiglib.gui.api.IValueNode;
import carbonconfiglib.gui.nodes.base.BaseElement;
import net.minecraft.network.chat.Component;
import speiger.src.collections.objects.utils.ObjectLists;

public abstract class NodeElement extends BaseElement
{
	INode node;
	
	public NodeElement(INode node) {
		this.node = node;
	}
	
	@Override
	protected List<Suggestion> getSuggestions() {
		if(node instanceof IArrayNode) return ((IArrayNode)node).getSuggestions();
		if(node instanceof IValueNode && !((IValueNode)node).isForcingSuggestions()) return ((IValueNode)node).getSuggestions();
		return ObjectLists.empty();
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
	protected void createTemp() {
		node.createTemp();
	}

	@Override
	protected void deleteTempIfNeeded() {
		node.deleteTempIfNeeded();
	}

	@Override
	protected void onRevert() {
		node.setPrevious();
		readValue();
		onValueChanged();
	}
	
	@Override
	protected void onReset() {
		node.setDefault();
		readValue();
		onValueChanged();
	}
	
	@Override
	protected void onArrayDelete() {
		deleteNode(node);
	}
	
	protected void onValueChanged() {
		context.onNodeChanged();
	}
	
	@Override
	public Component getName() {
		return node.getName();
	}
	
	@Override
	public Component getTooltip() {
		return node.getTooltip();
	}
}
