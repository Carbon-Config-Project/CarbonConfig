package carbonconfiglib.plugins.jei.configs;

import java.util.List;

import carbonconfiglib.gui.api.IConfigFolderNode;
import carbonconfiglib.gui.api.IConfigNode;
import mezz.jei.api.runtime.config.IJeiConfigCategory;
import mezz.jei.api.runtime.config.IJeiConfigValue;
import net.minecraft.network.chat.Component;
import speiger.src.collections.objects.lists.ObjectArrayList;

public class JEINode implements IConfigFolderNode
{
	IJeiConfigCategory category;
	List<IConfigNode> children;
	
	public JEINode(IJeiConfigCategory category) {
		this.category = category;
	}

	@Override
	public List<IConfigNode> getChildren() {
		if(children == null) {
			children = new ObjectArrayList<>();
			for(IJeiConfigValue<?> entry : category.getConfigValues()) {
				children.add(new JEILeaf(entry));
			}
		}
		return children;
	}
	
	public String getNodeName() { return category.getName(); }
	@Override
	public Component getName() { return IConfigNode.createLabel(category.getName()); }
}
