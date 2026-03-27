package carbonconfiglib.gui.screens;

import java.util.ArrayDeque;
import java.util.Comparator;
import java.util.Deque;
import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;

import carbonconfiglib.CarbonConfig;
import carbonconfiglib.gui.api.IModConfig;
import carbonconfiglib.gui.api.background.BackgroundTexture.BackgroundHolder;
import carbonconfiglib.gui.base.helpers.Align;
import carbonconfiglib.gui.base.helpers.GuiUtils;
import carbonconfiglib.gui.base.helpers.Icon;
import carbonconfiglib.gui.base.screen.BaseCarbonScreen;
import carbonconfiglib.gui.base.widgets.CarbonButton;
import carbonconfiglib.gui.base.widgets.CarbonCheckBox.CheckBoxState;
import carbonconfiglib.gui.base.widgets.CarbonEditBox.TextState;
import carbonconfiglib.gui.base.widgets.CarbonList.ListState;
import carbonconfiglib.gui.nodes.FolderElement;
import carbonconfiglib.gui.nodes.base.BaseElement;
import carbonconfiglib.gui.nodes.base.IElementContext;
import carbonconfiglib.gui.nodes.base.IFolderNode;
import carbonconfiglib.gui.nodes.base.IFolderNode.IFolderController;
import carbonconfiglib.gui.nodes.base.ISortableNode;
import carbonconfiglib.impl.ReloadMode;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.forgespi.language.IModInfo;
import speiger.src.collections.objects.lists.ObjectArrayList;
import speiger.src.collections.utils.Stack;

/**
 * Copyright 2026 Speiger, Meduris
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
public class ConfigScreen extends BaseCarbonScreen implements IElementContext, IFolderController
{
	private static final Comparator<BaseElement> SORTER = (K, V) -> (V instanceof FolderElement ? 1 : 0) - (K instanceof FolderElement ? 1 : 0);
	private static final Comparator<BaseElement> SPECIAL_SORTER = (K, V) -> {
		int sort = (V instanceof FolderElement ? 1 : 0) - (K instanceof FolderElement ? 1 : 0);
		return sort != 0 ? sort : String.CASE_INSENSITIVE_ORDER.compare(K.getName().getString(), V.getName().getString());
	};
	
	BackgroundHolder holder;
	Component display;
	IModConfig configs;
	FolderElement rootElement;
	Screen parent;
	TextState searchState = new TextState().setCallback(this::applySearch).setMaxLength(255).setSuggestion(I18n.get("gui.carbonconfig.search"));
	ListState<BaseElement> rowOne = new ListState<BaseElement>().setParentRowWidth().setDragListener(this::onSwapped);
	ListState<BaseElement> rowTwo = new ListState<BaseElement>().setParentRowWidth().setDragListener(this::onSwapped);
	ListState<BaseElement> rowThree = new ListState<BaseElement>().setParentRowWidth().setDragListener(this::onSwapped);
	@SuppressWarnings("unchecked")
	ListState<BaseElement>[] all = new ListState[] {rowOne, rowTwo, rowThree};
	CheckBoxState bulkEdit = new CheckBoxState(false, Icon.NOT_DEFAULT).setCallback(T -> onBulkEdit(T.getValue())).setTooltip(Component.translatable("gui.carbonconfig.bulkedit"));
	CheckBoxState autoSave = new CheckBoxState(CarbonConfig.AUTO_SAVE.get(), Icon.AUTO_SAVE).setCallback(T -> onNodeChanged()).setTooltip(Component.translatable("gui.carbonconfig.autosave"));
	CheckBoxState layerMode = new CheckBoxState(true, Icon.PAGE_MODE).setCallback(T -> recalculateNode()).withTooltip(T -> Component.translatable(T.getState().getValue() ? "gui.carbonconfig.layout.normal" : "gui.carbonconfig.layout.wide"));
	CarbonButton save;
	Stack<List<BaseElement>> visibleChildren = new ObjectArrayList<>();
	Stack<BaseElement> pickedNode = new ObjectArrayList<>();
	Stack<String> search = new ObjectArrayList<>();
	List<String> walker = null;
	BaseElement tooltipFocused;
	ReloadMode notifiedMode;
	
	public ConfigScreen(IModConfig configs, BackgroundHolder holder, Screen parent) {
		this.parent = parent;
		this.holder = holder;
		this.configs = configs;
		display = Component.literal(ModList.get().getModContainerById(configs.getModId()).map(ModContainer::getModInfo).map(IModInfo::getDisplayName).orElse(configs.getModId())).append(" -> ").append(configs.getConfigName());
		addElement(rootElement = new FolderElement(configs.getRootNode()));
		recalculateNode();
	}

	@Override
	protected void init() {
		super.init();
		int minY = (int)(height * 0.15F);
		int maxY = (int)(height * 0.8F) - minY;
		
		int searchWidth = (int)(width * 0.3F);
		
		int widthOne = calculateWidth(0);
		int widthTwo = calculateWidth(1);
		int widthThree = calculateWidth(2);
		modlogo(2, 2, minY - 4, minY - 4);
		listArea(0, minY, widthOne, maxY, rowOne);
		listArea(widthOne+4, minY, widthTwo, maxY, rowTwo);
		listArea(widthTwo + 8 + widthOne, minY, widthThree, maxY, rowThree);
		iconButton(minY, minY - 22, 20, 20, Align.START, Align.START, Icon.HOME, T -> onClose()).withTooltip(Component.translatable("gui.carbonconfig.home"));
		checkbox(minY + 22, minY - 22, 20, 20, layerMode);
		text(-(searchWidth >> 1), minY - 20, searchWidth, 16, Align.CENTER, Align.START, searchState);
		checkbox(-89, minY - 20, 18, 18, Align.END, Align.START, bulkEdit);
		checkbox(-69, minY - 20, 18, 18, Align.END, Align.START, autoSave);
		save = iconButton(-49, minY - 20, 18, 18, Align.END, Align.START, Icon.SAVE, T -> save()).setPadding(0).withTooltip(Component.translatable("gui.carbonconfig.save"));
		
		if(walker != null) {
			Deque<String> dequeue = new ArrayDeque<String>(walker);
			while(!dequeue.isEmpty()) {
				String current = dequeue.poll();
				boolean found = false;
				for(BaseElement element : visibleChildren.top()) {
					if(element instanceof IFolderNode && current.equalsIgnoreCase(((IFolderNode)element).getNodeName())) {
						addElement(element);
						found = true;
						break;
					}
				}
				if(found) continue;
				break;
			}
			recalculateNode();
		}
	}
	
	public ConfigScreen withWalker(List<String> walker) {
		this.walker = walker;
		return this;
	}
	
	private float totalWidth() {
		return 0.42F;
	}
	
	private float columnWidth(int index, int size) {
		switch(size) {
			case 2: return totalWidth() * (index == 0 ? 0.4F : 0.6F);
			case 3: return totalWidth() / size;
			default: return totalWidth();
		}
	}
	
	private int calculateWidth(int index) {
		if(index == 2) {
			if(rowThree.isEmpty()) return (int)(width - 8);
			return (int)((width - (int)(width * (columnWidth(index-2, 3) + columnWidth(index-2, 3)))) - 8);
		}
		if(!all[index+1].isEmpty()) return (int)(width * columnWidth(index, countFilledList()) - 3);
		return (int)((width - (int)(width * (index == 0 ? 0F : columnWidth(index-1, 2)))) - 8);
	}
	
	@Override
	public void tick() {
		super.tick();
	}
	
	@Override
	public int calculateSegmentWidth(int layer) {
		return (int)(width * columnWidth(layer, countFilledList()) - 4);
	}
	
	private int countFilledList() {
		int counted = 0;
		for(int i = 0;i<3;i++) {
			if(!all[i].isEmpty()) counted++;
		}
		return counted;
	}
	
	@Override
	public void onClose() {
		if(rootElement.needsSaving()) {
			Screen current = this;
			setScreen(new MultiChoiceScreen(T -> {
				if(T.isMain()) {
					save();
					setScreen(parent);
				}
				else if(T.isOther()) setScreen(parent);
				else if(T.isCancel()) setScreen(current);
			}, 
			Component.translatable("gui.carbonconfig.warn.changed"), 
			Component.translatable("gui.carbonconfig.warn.changed.desc"),
			Component.translatable("gui.carbonconfig.warn.changed.save"),
			Component.translatable("gui.carbonconfig.warn.changed.discard"),
			Component.translatable("gui.carbonconfig.warn.changed.back")));
			return;
		}
		setScreen(parent);
	}
	
	@Override
	public void renderBackground(PoseStack matrix, int mouseX, int mouseY, float partialTicks) {
		if(save != null) save.active = rootElement.needsSaving();
		int minY = (int)(height * 0.15F);
		if(!holder.shouldDisableInLevel() || minecraft.level == null) GuiUtils.renderBackground(0, width, 0, height, 0F, holder.getTexture());
		GuiUtils.renderListOverlay(0, width, minY, (int)(height * 0.8F), width, height, holder.getTexture());
	}
	
	@Override
	public void renderForeground(PoseStack matrix, int mouseX, int mouseY, float partialTicks) {
		drawText(matrix, display, 0, -centerY + 3, Align.CENTER, -1);
		GuiUtils.renderListShadow(0, width, (int)(height * 0.15F), (int)(height * 0.8F), width, height);
		int widthOne = calculateWidth(0);
		if(!rowOne.isScrollbarVisible() && !rowTwo.isEmpty()) {
			GuiComponent.fill(matrix, widthOne, (int)(height * 0.15F), widthOne+4, (int)(height * 0.8F), 0xFF000000);
		}
		int widthTwo = calculateWidth(1) + widthOne + 4;
		if(rowTwo.isVisible() && !rowTwo.isScrollbarVisible() && !rowThree.isEmpty()) {
			GuiComponent.fill(matrix, widthTwo, (int)(height * 0.15F), widthTwo+4, (int)(height * 0.8F), 0xFF000000);
		}
		if(rowThree.isVisible()) {
			int widthThree = (int)(width * totalWidth()) - 4;
			GuiComponent.fill(matrix, widthThree, (int)(height * 0.15F), widthThree+4, (int)(height * 0.8F), 0xFF000000);
		}
		GuiComponent.fill(matrix, 0, (int)(height * 0.8F), width, height, 0xFF1C1C1C);
		boolean found = false;
		for(int i = 2;i>=0;i--) {
			if(all[i].isVisible()) {
				if(all[i].getOwner() != null && all[i].getOwner().getHovered() != null) {
					drawTooltip(all[i].getOwner().getHovered(), matrix);
					found = true;
					break;
				}
			}
		}
		if(!found && tooltipFocused != null) {
			drawTooltip(tooltipFocused, matrix);
		}
		GuiComponent.fill(matrix, 0, (int)(height * 0.85F), width, (int)(height * 0.85F+2), 0xFF000000);
	}
	
	private void drawTooltip(BaseElement element, PoseStack matrix) {
		Component text = element.getName();
		if(text != null) {
			String raw = element.getNodeName();
			if(raw != null) text = text.copy().append("("+raw+")");
			int scale = (int)((height * 0.85F) - (height * 0.8F)) / font.lineHeight;
			float minY = (height * 0.8F);
			float diff = (height * 0.85F - minY) * 0.5F;
			matrix.pushPose();
			matrix.translate(2F, minY + diff - (font.lineHeight * scale * 0.5F), 0F);
			matrix.scale(scale, scale, 1F);
			GuiUtils.drawText(matrix, font, text, 0F, 0F, Align.START, -1);
			matrix.popPose();
		}
		text = element.getTooltip();
		if(text != null) {
			int freeHeight = ((int)(height - (height * 0.85F)));
			float scale = 1F;
			int needed = findHeight(text, width-4);
			if(freeHeight < needed) scale = 0.5F;
			else if(freeHeight >= needed*1.5F) {
				needed = findHeight(text, (int)(width / 1.5F) - 4);
				if(freeHeight >= needed*1.5F) scale = 1.5F;
			}
			float minY = (height * 0.85F) + 2F;
			matrix.pushPose();
			matrix.translate(2F, minY, 0F);
			matrix.scale(scale, scale, 1F);
			GuiUtils.drawSplitText(matrix, font, text, 0F, 0F, Align.START, -1, (int)(width / scale) - 4);
			matrix.popPose();
		}
	}
	
	private int findHeight(Component comp, int width) {
		return font.split(comp, width).size()*font.lineHeight;
	}
	
	@Override
	public void pushChild(BaseElement element, int index, int childIndex, boolean reverse) {
		if(pickedNode.top() != element) return;
		List<BaseElement> children = visibleChildren.top();
		if(childIndex < 0 || childIndex >= children.size()) return;
		pushNode(children.get(reverse ? (children.size()-1)-childIndex : childIndex), index+1, false);
	}
	
	@Override
	public void pushNode(BaseElement node, int index, boolean reload) {
		int toPop = 0;
		if(!reload && pickedNode.size() > 1 && (toPop = findActiveElement(node)) >= 0) {
			toPop = pickedNode.size() - toPop;
			for(int i = 0;i<toPop;i++) {
				visibleChildren.pop();
				pickedNode.pop();
				search.pop();
			}
		}
		else {
			int max = layerMode.getValue() ? 2 : 1;
			if(index != max && visibleChildren.size() > 1+index) {
				for(int i = 0,m=max-index;i<m && visibleChildren.size() > 1;i++) {
					visibleChildren.pop();
					pickedNode.pop();
					search.pop();
				}
			}
			addElement(node);
		}
		recalculateNode();
	}
	
	private void addElement(BaseElement node) {
		pickedNode.push(node);
		List<BaseElement> nodes = node.getChildNodes();
		for(int i = 0,m=nodes.size();i<m;i++) {
			BaseElement element = nodes.get(i);
			element.setContext(this);
			if(bulkEdit.getValue()) element.setBulkEdit(true);
			if(element instanceof IFolderNode) {
				((IFolderNode)element).setCallbacks(this);
			}
		}
		visibleChildren.push(nodes);
		search.push("");
	}
	
	private void applySearch(String value) {
		search.pop();
		search.push(value);
		for(int i = 2;i>=0;i--) {
			if(all[i].isEmpty()) continue;
			all[i].search(value);
			break;
		}
	}
	
	protected void onSwapped(int oldIndex, int newIndex) {
		BaseElement root = pickedNode.top();
		if(root instanceof ISortableNode) {
			((ISortableNode)root).onSwapped(oldIndex, newIndex);
		}
	}
	
	private void onBulkEdit(boolean value) {
		for(BaseElement element : visibleChildren.top()) {
			element.setBulkEdit(value);
		}
	}
	
	private void recalculateNode() {
		for(int i = 0;i<3;i++) {
			all[i].setDraggable(false).clear().search("");
		}
		int max = layerMode.getValue() ? 3 : 2;
		for(int i = 0,offset = Math.min(visibleChildren.size()-1, max-1);i<max && i < visibleChildren.size();i++) {
			all[i].replace(processElements(i, pickedNode.peek(offset), visibleChildren.peek(offset))).search(search.peek(offset));
			offset--;
		}
		for(int i = 2;i>=0;i--) {
			if(!all[i].isEmpty()) {
				if(pickedNode.top() instanceof ISortableNode) {
					all[i].setDraggable(true);
				}
				break;
			}
		}
		searchState.setSilentValue(search.top());
		//Lets pull a Skyrim. Because reloading the GUI from scratch simply works better than applying the new Positions
		//LIKE WHAT THE FUCK....
		//But this doesn't matter as Carbons new API is state-less within the components and the state is stored in the screen itself.
		//So Unless the screen object gets unloaded there is no difference.
		init();
	}
	
	private List<BaseElement> processElements(int index, BaseElement owner, List<BaseElement> data) {
		for(int i = 0,m=data.size();i<m;i++) {
			data.get(i).setLayer(index);
		}
		data.sort(owner instanceof ISortableNode ? SORTER : SPECIAL_SORTER);
		return data;
	}
	
	@SuppressWarnings("unchecked")
	private int findActiveElement(BaseElement base) {
		return ((List<BaseElement>)pickedNode).indexOf(base);
	}
	
	@Override
	public BackgroundHolder getHolder() {
		return holder;
	}
	
	@Override
	public boolean isElementActive(BaseElement base) {
		for(int i = 0,m=pickedNode.size();i<m;i++) {
			if(pickedNode.peek(i) == base) return true;
		}
		return false;
	}
	
	@Override
	public boolean isAtTop(int index) {
		return index == 2 || all[index+1].isEmpty();
	}
	
	@Override
	public void setTooltipFocused(BaseElement element) {
		tooltipFocused = element;
	}
	
	@Override
	public void onNodeChanged() {
		if(!autoSave.getValue()) return;
		save();
	}
	
	private void save() {
		if(rootElement.save(this::notifyChanges)) {
			configs.save(CarbonConfig.AUTO_BACKUP.get());
		}
	}
	
	private void notifyChanges(ReloadMode mode) {
		if(autoSave.getValue()) {
			if(ReloadMode.or(notifiedMode, mode) == notifiedMode) {
				return;
			}
			notifiedMode = mode;
		}
		Screen owner = this;
		if(mode == ReloadMode.GAME) {
			minecraft.setScreen(new MultiChoiceScreen(T -> minecraft.setScreen(owner), Component.translatable("gui.carbonconfig.restart.title"), Component.translatable("gui.carbonconfig.restart.message").withStyle(ChatFormatting.GRAY), Component.translatable("gui.carbonconfig.ok")));
		}
		else if(mode == ReloadMode.WORLD && minecraft.level != null) {
			minecraft.setScreen(new MultiChoiceScreen(T -> minecraft.setScreen(owner), Component.translatable("gui.carbonconfig.reload.title"), Component.translatable("gui.carbonconfig.reload.message").withStyle(ChatFormatting.GRAY), Component.translatable("gui.carbonconfig.ok")));
		}
	}
}
