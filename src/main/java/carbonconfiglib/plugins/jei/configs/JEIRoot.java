package carbonconfiglib.plugins.jei.configs;

import java.util.List;

import carbonconfiglib.gui.api.IConfigFolderNode;
import carbonconfiglib.gui.api.IConfigNode;
import mezz.jei.api.runtime.config.IJeiConfigCategory;
import mezz.jei.api.runtime.config.IJeiConfigFile;
import net.minecraft.network.chat.Component;
import speiger.src.collections.objects.lists.ObjectArrayList;

public class JEIRoot implements IConfigFolderNode
{
	IJeiConfigFile files;
	List<IConfigNode> nodes;
	
	public JEIRoot(IJeiConfigFile files) {
		this.files = files;
	}

	@Override
	public List<IConfigNode> getChildren()
	{
		if(nodes == null) {
			nodes = new ObjectArrayList<>();
			for(IJeiConfigCategory cat : files.getCategories()) {
				nodes.add(new JEINode(cat));
			}
		}
		return nodes;
	}
	
	@Override
	public boolean isRoot() { return true; }
	@Override
	public String getNodeName() { return null; }
	@Override
	public Component getName() { return Component.literal(files.getPath().getFileName().toString()); }
	
}
