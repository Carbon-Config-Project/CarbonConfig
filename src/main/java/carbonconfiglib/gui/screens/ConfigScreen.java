package carbonconfiglib.gui.screens;

import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;

import carbonconfiglib.gui.api.BackgroundTypes;
import carbonconfiglib.gui.api.IConfigNode;
import carbonconfiglib.gui.base.screen.BaseCarbonScreen;
import carbonconfiglib.gui.base.widgets.CarbonList;
import carbonconfiglib.gui.base.widgets.CarbonList.ListState;
import carbonconfiglib.gui.config.ElementList;
import carbonconfiglib.gui.nodeElements.BaseElement;
import carbonconfiglib.gui.nodeElements.FolderElement;
import carbonconfiglib.gui.nodeElements.IFolderNode;
import carbonconfiglib.gui.nodeElements.TestElement;
import speiger.src.collections.objects.lists.ObjectArrayList;

public class ConfigScreen extends BaseCarbonScreen
{
	ListState<BaseElement> rowOne = new ListState<BaseElement>().setScrollOffset(0);
	ListState<BaseElement> rowTwo = new ListState<BaseElement>().setScrollOffset(0);
	ListState<BaseElement> rowThree = new ListState<BaseElement>().setScrollOffset(0);
	@SuppressWarnings("unchecked")
	ListState<BaseElement>[] all = new ListState[] {rowOne,rowTwo, rowThree};
	List<List<BaseElement>> visibleChildren = new ObjectArrayList<>();
	List<IConfigNode> pickedNode = new ObjectArrayList<>();
	
	public ConfigScreen(IConfigNode root) {
		pickedNode.add(root);
		visibleChildren.add(collectNodes(root));
		recalculateNode();
	}

	@Override
	protected void init() {
		super.init();
		int minY = (int)(height * 0.15F);
		int maxY = (int)(height * 0.8F) - minY;
		listArea(0, minY, 100, maxY, rowOne);
		listArea(105, minY, 100, maxY, rowTwo);
		listArea(210, minY, 100, maxY, rowThree);
	}
	
	@Override
	public void tick() {
//		init();
		super.tick();
	}
	
	private int calculateWidth(int index) {
		if(index == 2) {
			if(rowThree.isEmpty()) return 0;
			return width - (int)(width * 0.28F);
		}
		if(!all[index+1].isEmpty()) return (int)(width * (0.42 / countFilledList()));
		return width - (int)(width * (index == 0 ? 0F : 0.21F));
	}
	
	private int countFilledList() {
		int counted = 0;
		for(int i = 0;i<3;i++) {
			if(!all[i].isEmpty()) counted++;
		}
		return counted;
	}
	
	@Override
	public void renderBackground(PoseStack matrix, int mouseX, int mouseY, float partialTicks) {
		BackgroundTypes background = BackgroundTypes.AMETHYST;
		ElementList.renderBackground(0, width, 0, height, 0F, background.getTexture());
		ElementList.renderListOverlay(0, width, (int)(height * 0.15F), (int)(height * 0.8F), width, height, background.getTexture());
	}
	
	@Override
	public void renderForeground(PoseStack matrix, int mouseX, int mouseY, float partialTicks) {
	}
	
	public void pushNode(IConfigNode node, int index) {
		if(index != 2 && visibleChildren.size() > 1+index) {
			for(int i = 0,m=2-index;i<m && visibleChildren.size() > 1;i++) {
				visibleChildren.remove(visibleChildren.size()-1);
				pickedNode.remove(pickedNode.size()-1);
			}
		}
		pickedNode.add(node);
		visibleChildren.add(collectNodes(node));
		recalculateNode();
	}
	
	private void recalculateNode() {
		for(int i = 0;i<3;i++) {
			all[i].clear();
		}
		for(int i = 0, offset = Math.max(pickedNode.size()-3, 0);i<3 && offset < pickedNode.size();i++) {
			all[i].replace(processElements(i, visibleChildren.get(offset)));
			offset++;
		}
		for(int i = 0;i<3;i++) {
			CarbonList<BaseElement> list = all[i].getOwner();
			if(list != null) list.setWidth(this.calculateWidth(i));
		}
	}
	
	private List<BaseElement> processElements(int index, List<BaseElement> data) {
		for(int i = 0,m=data.size();i<m;i++) {
			BaseElement element = data.get(i);
			if(element instanceof IFolderNode) {
				((IFolderNode)element).setCallbacks(index, this::pushNode);
			}
		}
		return data;
	}
	
	protected List<BaseElement> collectNodes(IConfigNode node) {
		List<BaseElement> elements = new ObjectArrayList<>();
		for(IConfigNode entry : node.getChildren()) {
			elements.add(createElement(entry));
		}
		return elements;
	}
	
	protected BaseElement createElement(IConfigNode node) {
		if(node.isLeaf()) {
			return new TestElement(node);
		}
		return new FolderElement(node);
	}
}
