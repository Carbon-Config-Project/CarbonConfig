package carbonconfiglib.gui.screen;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.Predicate;

import com.mojang.blaze3d.vertex.PoseStack;

import carbonconfiglib.gui.api.BackgroundTexture;
import carbonconfiglib.gui.api.IConfigNode;
import carbonconfiglib.gui.api.IModConfig;
import carbonconfiglib.gui.config.ArrayElement;
import carbonconfiglib.gui.config.CompoundElement;
import carbonconfiglib.gui.config.Element;
import carbonconfiglib.gui.config.FolderElement;
import carbonconfiglib.gui.config.ListScreen;
import carbonconfiglib.gui.config.SelectionElement;
import carbonconfiglib.gui.widgets.CarbonIconCheckbox;
import carbonconfiglib.gui.widgets.Icon;
import it.unimi.dsi.fastutil.PriorityQueue;
import it.unimi.dsi.fastutil.objects.ObjectArrayFIFOQueue;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.client.gui.widget.ExtendedButton;

/**
 * Copyright 2023 Speiger, Meduris
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
public class ConfigScreen extends ListScreen
{
	private static final Comparator<Element> SORTER = (K, V) -> {
		int sort = (V instanceof FolderElement ? 1 : 0) - (K instanceof FolderElement ? 1 : 0);
		return sort != 0 ? sort : String.CASE_INSENSITIVE_ORDER.compare(K.getName(), V.getName());
	};
	
	Screen parent;
	IModConfig config;
	IConfigNode node;
	CarbonIconCheckbox deepSearch;
	CarbonIconCheckbox onlyChanged;
	CarbonIconCheckbox onlyNonDefault;
	boolean wasChanged = false;
	List<Element> cache = null;
	
	public ConfigScreen(Component name, IModConfig config, Screen parent) {
		this(name, config, parent, BackgroundTexture.DEFAULT);
	}
	
	public ConfigScreen(Component name, IModConfig config, Screen parent, BackgroundTexture customTexture) {
		super(name, customTexture);
		this.config = config;
		this.node = config.getRootNode();
		this.parent = parent;
	}
	
	public ConfigScreen(Component name, IConfigNode node, Screen parent, BackgroundTexture customTexture) {
		super(name, customTexture);
		this.node = node;
		this.parent = parent;
	}
	
	@Override
	protected void init() {
		super.init();
		int x = width / 2 - 100;
		int y = height;
		if(node.isRoot()) {
			addRenderableWidget(new ExtendedButton(x-51, y-27, 100, 20, Component.translatable("gui.carbonconfig.save"), this::save));
			addRenderableWidget(new ExtendedButton(x+51, y-27, 100, 20, Component.translatable("gui.carbonconfig.reset"), this::reset));
			addRenderableWidget(new ExtendedButton(x+153, y-27, 100, 20, Component.translatable("gui.carbonconfig.back"), this::goBack));
		}
		else {
			addRenderableWidget(new ExtendedButton(x+101, y-27, 100, 20, Component.translatable("gui.carbonconfig.back"), this::goBack));
			addRenderableWidget(new ExtendedButton(x-1, y-27, 100, 20, Component.translatable("gui.carbonconfig.home"), this::goToRoot));
		}
		if(shouldHaveSearch()) {
			deepSearch = addRenderableWidget(new CarbonIconCheckbox(x+205, 25, 20, 20, Icon.SEARCH_SELECTED, Icon.SEARCH, false).withListener(this::onDeepSearch).setTooltip(this, "gui.carbonconfig.deepsearch"));
			onlyChanged = addRenderableWidget(new CarbonIconCheckbox(x+227, 25, 20, 20, Icon.SET_DEFAULT, Icon.REVERT, false).withListener(this::onChangedButton).setTooltip(this, "gui.carbonconfig.changed_only"));
			onlyNonDefault = addRenderableWidget(new CarbonIconCheckbox(x+249, 25, 20, 20, Icon.NOT_DEFAULT_SELECTED, Icon.NOT_DEFAULT, false).withListener(this::onDefaultButton).setTooltip(this, "gui.carbonconfig.default_only"));
		}
	}
	
	private void onDeepSearch() {
		if(onlyChanged.selected() || onlyNonDefault.selected()) deepSearch.setSelected(false);
		else {
			wasChanged = true;
		}
	}
	
	private void onChangedButton() {
		deepSearch.setSelected(false);
		onlyNonDefault.setSelected(false);
	}
	
	private void onDefaultButton() {
		deepSearch.setSelected(false);
		onlyChanged.setSelected(false);
	}
	
	@Override
	public void tick() {
		super.tick();
		if(shouldHaveSearch() && (onlyChanged.selected() || onlyNonDefault.selected() || wasChanged)) {
			onSearchChange(searchBox, searchBox.getValue().toLowerCase(Locale.ROOT));
			wasChanged = onlyChanged.selected() || onlyNonDefault.selected();
		}
	}
	
	@Override
	public void render(PoseStack stack, int mouseX, int mouseY, float partialTicks) {
		super.render(stack, mouseX, mouseY, partialTicks);
		font.draw(stack, title, (width/2)-(font.width(title)/2), 8, -1);
	}
	
	@Override
	protected List<Element> sortElements(List<Element> list) {
		list.sort(SORTER);
		return list;
	}
	
	private void goToRoot(Button button) {
		Screen prev = this;
		Screen parent = this;
		while(parent instanceof ConfigScreen) {
			prev = parent;
			parent = ((ConfigScreen)parent).parent;
		}
		if(prev != this) {
			minecraft.setScreen(prev);
		}
	}
	
	private void reset(Button button) {
		minecraft.setScreen(new MultiChoiceScreen(T -> {
			if(T.isMain()) processAction(IConfigNode::setDefault);
			else if(T.isOther()) processAction(IConfigNode::setPrevious);
			minecraft.setScreen(this);
		}, Component.translatable("gui.carbonconfig.reset_all.title"), Component.translatable("gui.carbonconfig.reset_all.message").withStyle(ChatFormatting.GRAY), 
			Component.translatable("gui.carbonconfig.reset_all.default"), Component.translatable("gui.carbonconfig.reset_all.reset"), Component.translatable("gui.carbonconfig.reset_all.cancel")));
	}
	
	private void save(Button button) {
		List<IConfigNode> value = processedChanged(IConfigNode::save);
		config.save();
		if(findFirst(IConfigNode::requiresRestart, value)) {
			MultiChoiceScreen choice = new MultiChoiceScreen(T -> {
				minecraft.setScreen(parent);
			}, Component.translatable("gui.carbonconfig.restart.title"), Component.translatable("gui.carbonconfig.restart.message").withStyle(ChatFormatting.GRAY), Component.translatable("gui.carbonconfig.ok"));
			minecraft.setScreen(choice);
			return;
		}
		else if(minecraft.level != null && findFirst(IConfigNode::requiresReload, value)) {
			MultiChoiceScreen choice = new MultiChoiceScreen(T -> {
				minecraft.setScreen(parent);
			}, Component.translatable("gui.carbonconfig.reload.title"), Component.translatable("gui.carbonconfig.reload.message").withStyle(ChatFormatting.GRAY), Component.translatable("gui.carbonconfig.ok"));
			minecraft.setScreen(choice);
			return;
		}
		minecraft.setScreen(parent);
	}
	
	private <T> boolean findFirst(Predicate<T> filter, List<T> elements) {
		for(int i = 0,m=elements.size();i<m;i++) {
			if(filter.test(elements.get(i))) return true;
		}
		return false;
	}
	
	private List<IConfigNode> processedChanged(Consumer<IConfigNode> action) {
		List<IConfigNode> output = new ObjectArrayList<>();
		PriorityQueue<IConfigNode> nodes = new ObjectArrayFIFOQueue<>();
		nodes.enqueue(node);
		while(!nodes.isEmpty()) {
			IConfigNode node = nodes.dequeue();
			if(!node.isLeaf()) {
				node.getChildren().forEach(nodes::enqueue);
				continue;
			}
			if(node.isChanged()) {
				action.accept(node);
				output.add(node);
			}
		}
		return output;
	}
	
	private void processAction(Consumer<IConfigNode> action) {
		PriorityQueue<IConfigNode> nodes = new ObjectArrayFIFOQueue<>();
		nodes.enqueue(node);
		while(!nodes.isEmpty()) {
			IConfigNode node = nodes.dequeue();
			if(!node.isLeaf()) {
				node.getChildren().forEach(nodes::enqueue);
				continue;
			}
			action.accept(node);
		}
	}
	
	@Override
	protected void onSearchChange(EditBox box, String value) {
		if((!deepSearch.selected() || value.isEmpty()) && !onlyChanged.selected() && !onlyNonDefault.selected()) {
			super.onSearchChange(box, value);
			return;
		}
		if(cache == null) {
			cache = sortElements(ConfigScreen.getAllElements(node));
			cache.forEach(this::addInternal);
		}
		if(onlyNonDefault.selected()) {
			List<Element> subCache = new ObjectArrayList<>();
			for(Element element : cache) {
				if(!element.isDefault()) subCache.add(element);
			}
			super.onSearchChange(box, value, subCache);
			return;
		}
		if(onlyChanged.selected()) {
			List<Element> subCache = new ObjectArrayList<>();
			for(Element element : cache) {
				if(element.isChanged()) subCache.add(element);
			}
			super.onSearchChange(box, value, subCache);
			return;
		}
		super.onSearchChange(box, value, cache);
	}
	
	private void goBack(Button button) {
		if(node.isRoot() && isChanged()) {
			minecraft.setScreen(new ConfirmScreen(T -> {
				minecraft.setScreen(T ? parent : this);	
			}, Component.translatable("gui.carbonconfig.warn.changed"), Component.translatable("gui.carbonconfig.warn.changed.desc").withStyle(ChatFormatting.GRAY)));
			return;
		}
		minecraft.setScreen(parent);
	}
	
	private boolean isChanged() {
		PriorityQueue<IConfigNode> nodes = new ObjectArrayFIFOQueue<>();
		nodes.enqueue(node);
		while(!nodes.isEmpty()) {
			IConfigNode node = nodes.dequeue();
			if(!node.isLeaf()) {
				node.getChildren().forEach(nodes::enqueue);
				continue;
			}
			if(node.isChanged()) return true;
		}
		return false;
	}
	
	@Override
	protected int getListWidth() {
		return 300;
	}
	
	@Override
	protected int getScrollPadding() {
		return 155;
	}
		
	@Override
	protected void collectElements(Consumer<Element> elements) {
		for(IConfigNode child : node.getChildren()) {
			if(child.isLeaf()) {
				if(child.isArray()) {
					elements.accept(new ArrayElement(child));
					continue;
				}
				if(child.getDataType().size() > 1) {
					elements.accept(new CompoundElement(child));
					continue;
				}
				if(child.getValidValues().size() > 0) {
					elements.accept(new SelectionElement(child));
					continue;
				}
				Element element = child.getDataType().get(0).create(child);
				if(element != null) elements.accept(element);
			}
			else elements.accept(new FolderElement(child, title));
		}
	}
	
	private static List<Element> getAllElements(IConfigNode init) {
		PriorityQueue<IConfigNode> nodes = new ObjectArrayFIFOQueue<>();
		nodes.enqueue(init);
		List<Element> results = new ObjectArrayList<>();
		while(!nodes.isEmpty()) {
			IConfigNode node = nodes.dequeue();
			if(!node.isLeaf()) {
				node.getChildren().forEach(nodes::enqueue);
				continue;
			}
			if(node.isArray()) {
				results.add(new ArrayElement(node));
				continue;
			}
			if(node.getDataType().size() > 1) {
				results.add(new CompoundElement(node));
				continue;
			}
			if(node.getValidValues().size() > 0) {
				results.add(new SelectionElement(node));
				continue;
			}
			Element element = node.getDataType().get(0).create(node);
			if(element != null) results.add(element);
		}
		
		return results;
	}
}