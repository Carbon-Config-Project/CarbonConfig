package carbonconfiglib.gui.screens;

import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;

import carbonconfiglib.gui.api.IConfigNode;
import carbonconfiglib.gui.base.screen.BaseCarbonScreen;
import carbonconfiglib.gui.base.widgets.CarbonList.ListEntry;
import carbonconfiglib.gui.base.widgets.CarbonList.ListState;
import carbonconfiglib.gui.nodeElements.BaseElement;
import carbonconfiglib.gui.nodeElements.FolderElement;
import carbonconfiglib.gui.nodeElements.IFolderNode;
import carbonconfiglib.gui.nodeElements.TestElement;
import net.minecraft.network.chat.Component;
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
		listArea(0, 60, 100, height - 120, rowOne);
		listArea(105, 60, 100, height - 120, rowTwo);
		listArea(210, 60, 100, height - 120, rowThree);
	}
	
	@Override
	public void tick() {
		init();
		super.tick();
	}
	
	@Override
	public void renderBackground(PoseStack matrix, int mouseX, int mouseY, float partialTicks) {
		renderDirtBackground(0);
	}
	
	@Override
	public void renderForeground(PoseStack matrix, int mouseX, int mouseY, float partialTicks) {
	}
	
	public void pushNode(IConfigNode node, int index) {
		if(index != 2 && visibleChildren.size() > 2) {
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
		for(int i = 0, offset = Math.max(pickedNode.size()-4, 0);i<3 && offset < pickedNode.size();i++) {
			all[i].replace(processElements(i, visibleChildren.get(offset)));
			offset++;
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
	
	public static List<ListTest> create() {
		List<ListTest> values = new ObjectArrayList<>();
		for(int i = 0;i<30;i++) {
			values.add(new ListTest(i));
		}
		return values;
	}
	
	public static class ListTest extends ListEntry<ListTest> {
		int index;
		
		public ListTest(int index) {
			this.index = index;
		}

		@Override
		protected boolean containsSearch(String searchString) {
			return false;
		}

		@Override
		public void render(PoseStack poseStack, int x, int top, int left, int width, int height, int mouseX, int mouseY, boolean selected, float partialTicks) {
			font.draw(poseStack, Component.literal(index+", top: "+top), left, top, -1);
		}
		
	}
}
