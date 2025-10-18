package carbonconfiglib.gui.screens;

import java.util.Comparator;
import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;

import carbonconfiglib.gui.api.BackgroundTypes;
import carbonconfiglib.gui.api.IConfigNode;
import carbonconfiglib.gui.base.helpers.GuiUtils;
import carbonconfiglib.gui.base.screen.BaseCarbonScreen;
import carbonconfiglib.gui.base.widgets.CarbonList.ListState;
import carbonconfiglib.gui.nodes.FolderElement;
import carbonconfiglib.gui.nodes.base.BaseElement;
import carbonconfiglib.gui.nodes.base.IElementContext;
import carbonconfiglib.gui.nodes.base.IFolderNode;
import carbonconfiglib.gui.nodes.base.ISortableNode;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.network.chat.Component;
import speiger.src.collections.objects.lists.ObjectArrayList;

public class ConfigScreen extends BaseCarbonScreen implements IElementContext
{
	private static final Comparator<BaseElement> SORTER = (K, V) -> {
		int sort = (V instanceof FolderElement ? 1 : 0) - (K instanceof FolderElement ? 1 : 0);
		return sort;
//		return sort != 0 ? sort : String.CASE_INSENSITIVE_ORDER.compare(K.getName(), V.getName());
	};
	
	ListState<BaseElement> rowOne = new ListState<BaseElement>().setParentRowWidth().setDragListener(this::onSwapped);
	ListState<BaseElement> rowTwo = new ListState<BaseElement>().setParentRowWidth().setDragListener(this::onSwapped);
	ListState<BaseElement> rowThree = new ListState<BaseElement>().setParentRowWidth().setDragListener(this::onSwapped);
	@SuppressWarnings("unchecked")
	ListState<BaseElement>[] all = new ListState[] {rowOne, rowTwo, rowThree};
	List<List<BaseElement>> visibleChildren = new ObjectArrayList<>();
	List<BaseElement> pickedNode = new ObjectArrayList<>();
	boolean twoLayerMode = false;
	
	public ConfigScreen(IConfigNode root) {
		addElement(new FolderElement(root));
		recalculateNode();
	}

	@Override
	protected void init() {
		super.init();
		int minY = (int)(height * 0.15F);
		int maxY = (int)(height * 0.8F) - minY;
		int widthOne = calculateWidth(0);
		int widthTwo = calculateWidth(1);
		int widthThree = calculateWidth(2);
		listArea(0, minY, widthOne, maxY, rowOne);
		listArea(widthOne+4, minY, widthTwo, maxY, rowTwo);
		listArea(widthTwo + 8 + widthOne, minY, widthThree, maxY, rowThree);
		button(5, 10, 40, 20, Component.literal("Toggle"), T-> {
			twoLayerMode = !twoLayerMode;
			recalculateNode();
		});
		
	}
	
	private int calculateWidth(int index) {
		if(index == 2) {
			if(rowThree.isEmpty()) return (int)(width - 8);
			return (int)((width - (int)(width * 0.28F)) - 8);
		}
		if(!all[index+1].isEmpty()) return (int)(width * (0.42F / countFilledList()) - 4);
		return (int)((width - (int)(width * (index == 0 ? 0F : 0.21F))) - 8);
	}
	
	@Override
	public int calculateSegmentWidth() {
		return (int)(width * (0.42F / countFilledList()) - 4);
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
		GuiUtils.renderBackground(0, width, 0, height, 0F, background.getTexture());
		GuiUtils.renderListOverlay(0, width, (int)(height * 0.15F), (int)(height * 0.8F), width, height, BackgroundTypes.AMETHYST.getTexture());
	}
	
	@Override
	public void renderForeground(PoseStack matrix, int mouseX, int mouseY, float partialTicks) {
		GuiUtils.renderListShadow(0, width, (int)(height * 0.15F), (int)(height * 0.8F), width, height);
		int widthOne = calculateWidth(0);
		if(!rowOne.isScrollbarVisible() && !rowTwo.isEmpty()) {
			GuiComponent.fill(matrix, widthOne, (int)(height * 0.15F), widthOne+4, (int)(height * 0.8F), 0xFF000000);
		}
		int widthTwo = calculateWidth(1) + widthOne + 4;
		if(rowTwo.isVisible() && !rowTwo.isScrollbarVisible() && !rowThree.isEmpty()) {
			GuiComponent.fill(matrix, widthTwo, (int)(height * 0.15F), widthTwo+4, (int)(height * 0.8F), 0xFF000000);
		}
		int widthThree = (int)(width * 0.42F) - 4;
		if(rowThree.isVisible()) {
			GuiComponent.fill(matrix, widthThree, (int)(height * 0.15F), widthThree+4, (int)(height * 0.8F), 0xFF000000);
		}
	}
	
	public void pushNode(BaseElement node, int index) {
		if(index == 0 && pickedNode.size() > 1 && pickedNode.get(pickedNode.size()-1) == node) {
			visibleChildren.remove(visibleChildren.size()-1);
			pickedNode.remove(pickedNode.size()-1);
		}
		else {
			int max = twoLayerMode ? 1 : 2;
			if(index != max && visibleChildren.size() > 1+index) {
				for(int i = 0,m=max-index;i<m && visibleChildren.size() > 1;i++) {
					visibleChildren.remove(visibleChildren.size()-1);
					pickedNode.remove(pickedNode.size()-1);
				}
			}
			addElement(node);
		}
		recalculateNode();			
	}
	
	private void addElement(BaseElement node) {
		pickedNode.add(node);
		List<BaseElement> nodes = node.getChildNodes();
		for(int i = 0,m=nodes.size();i<m;i++) {
			BaseElement element = nodes.get(i);
			element.setContext(this);
			if(element instanceof IFolderNode) {
				((IFolderNode)element).setCallbacks(this::pushNode);
			}
		}
		visibleChildren.add(nodes);
	}
	
	protected void onSwapped(int oldIndex, int newIndex) {
		BaseElement root = pickedNode.get(pickedNode.size()-1);
		if(root instanceof ISortableNode) {
			((ISortableNode)root).onSwapped(oldIndex, newIndex);
		}
	}
	
	@Override
	public boolean mouseClicked(double pMouseX, double pMouseY, int pButton)
	{
		return super.mouseClicked(pMouseX, pMouseY, pButton);
	}
	
	private void recalculateNode() {
		for(int i = 0;i<3;i++) {
			all[i].setDraggable(false).clear();
		}
		int max = twoLayerMode ? 2 : 3;
		for(int i = 0, offset = Math.max(visibleChildren.size()-max, 0);i<max && offset < visibleChildren.size();i++) {
			all[i].replace(processElements(i, visibleChildren.get(offset)));
			offset++;
		}
		for(int i = 2;i>=0;i--) {
			if(!all[i].isEmpty()) {
				if(pickedNode.get(pickedNode.size()-1) instanceof ISortableNode) {
					all[i].setDraggable(true);
				}
				break;
			}
		}
		
		//Lets pull a Skyrim. Because reloading the GUI from scratch simply works better than applying the new Positions
		//LIKE WHAT THE FUCK....
		//But this doesn't matter as Carbons new API is state-less within the components and the state is stored in the screen itself.
		//So Unless the screen object gets unloaded there is no difference.
		init();
	}
	
	private List<BaseElement> processElements(int index, List<BaseElement> data) {
		for(int i = 0,m=data.size();i<m;i++) {
			data.get(i).setLayer(index);
		}
		data.sort(SORTER);
		return data;
	}
	
	@Override
	public boolean isElementActive(BaseElement base) {
		for(int i = pickedNode.size()-1;i>=0;i--) {
			if(pickedNode.get(i) == base) return true;
		}
		return false;
	}
	
	@Override
	public boolean isAtTop(int index) {
		return index == 2 || all[index+1].isEmpty();
	}
	
//	protected List<BaseElement> collectNodes(IConfigNode node) {
//		List<BaseElement> elements = new ObjectArrayList<>();
//		for(IConfigNode entry : node.getChildren()) {
//			elements.add(createElement(entry));
//		}
//		return elements;
//	}
	
//	protected BaseElement createElement(IConfigNode node) {
//		if(node.isLeaf()) {
//			return new TestElement(node.asNode());
//		}
//		return new FolderElement(node);
//	}
}
