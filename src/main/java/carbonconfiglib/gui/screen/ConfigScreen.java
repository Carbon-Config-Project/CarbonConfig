package carbonconfiglib.gui.screen;

import java.util.Comparator;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.Predicate;

import carbonconfiglib.gui.api.BackgroundTexture;
import carbonconfiglib.gui.api.BackgroundTexture.BackgroundHolder;
import carbonconfiglib.gui.api.IConfigNode;
import carbonconfiglib.gui.api.IModConfig;
import carbonconfiglib.gui.config.ArrayElement;
import carbonconfiglib.gui.config.CompoundElement;
import carbonconfiglib.gui.config.ConfigElement.GuiAlign;
import carbonconfiglib.gui.config.Element;
import carbonconfiglib.gui.config.FolderElement;
import carbonconfiglib.gui.config.ListScreen;
import carbonconfiglib.gui.config.SelectionElement;
import carbonconfiglib.gui.widgets.CarbonButton;
import carbonconfiglib.gui.widgets.CarbonEditBox;
import carbonconfiglib.gui.widgets.CarbonIconCheckbox;
import carbonconfiglib.gui.widgets.GuiUtils;
import carbonconfiglib.gui.widgets.Icon;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiYesNo;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import speiger.src.collections.objects.lists.ObjectArrayList;

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
	
	GuiScreen parent;
	IModConfig config;
	IConfigNode node;
	CarbonIconCheckbox deepSearch;
	CarbonIconCheckbox onlyChanged;
	CarbonIconCheckbox onlyNonDefault;
	boolean wasChanged = false;
	Navigator nav;
	List<Element> cache = null;
	
	public ConfigScreen(Navigator nav, IModConfig config, GuiScreen parent) {
		this(nav, config, parent, BackgroundTexture.DEFAULT.asHolder());
	}
	
	public ConfigScreen(Navigator nav, IModConfig config, GuiScreen parent, BackgroundHolder customTexture) {
		super(new TextComponentString(""), customTexture);
		this.nav = nav;
		this.config = config;
		this.node = config.getRootNode();
		this.parent = parent;
		this.nav.setScreenForLayer(this);
	}
	
	public ConfigScreen(Navigator nav, IConfigNode node, GuiScreen parent, BackgroundHolder customTexture) {
		super(new TextComponentString(""), customTexture);
		this.nav = nav;
		this.node = node;
		this.parent = parent;
		this.nav.setScreenForLayer(this);
	}
	
	@Override
	public void initGui() {
		super.initGui();
		int x = width / 2 - 100;
		int y = height;
		if(node.isRoot()) {
			addWidget(new CarbonButton(x-51, y-27, 100, 20, I18n.format("gui.carbonconfig.save"), this::save));
			addWidget(new CarbonButton(x+51, y-27, 100, 20, I18n.format("gui.carbonconfig.reset"), this::reset));
			addWidget(new CarbonButton(x+153, y-27, 100, 20, I18n.format("gui.carbonconfig.back"), this::goBack));
		}
		else {
			addWidget(new CarbonButton(x+101, y-27, 100, 20, I18n.format("gui.carbonconfig.back"), this::goBack));
			addWidget(new CarbonButton(x-1, y-27, 100, 20, I18n.format("gui.carbonconfig.home"), this::goToRoot));
		}
		if(shouldHaveSearch()) {
			deepSearch = addWidget(new CarbonIconCheckbox(x+205, 25, 20, 20, Icon.SEARCH_SELECTED, Icon.SEARCH, false).withListener(this::onDeepSearch).setTooltip(this, "gui.carbonconfig.deepsearch"));
			onlyChanged = addWidget(new CarbonIconCheckbox(x+227, 25, 20, 20, Icon.SET_DEFAULT, Icon.REVERT, false).withListener(this::onChangedButton).setTooltip(this, "gui.carbonconfig.changed_only"));
			onlyNonDefault = addWidget(new CarbonIconCheckbox(x+249, 25, 20, 20, Icon.NOT_DEFAULT_SELECTED, Icon.NOT_DEFAULT, false).withListener(this::onDefaultButton).setTooltip(this, "gui.carbonconfig.default_only"));
		}
		String walkNode = nav.getWalkNode();
		if(walkNode != null) {
			FolderElement element = getElement(walkNode);
			if(element != null) {
				element.onPress(null);
			}
			nav.consumeWalker();
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
			onSearchChange(searchBox, searchBox.getText().toLowerCase(Locale.ROOT));
			wasChanged = onlyChanged.selected() || onlyNonDefault.selected();
		}
	}
	
	@Override
	public void handleForground(int mouseX, int mouseY, float partialTicks) {
		GuiUtils.drawScrollingString(fontRendererObj, nav.getHeader(), 50F, 6, width-100, 10, GuiAlign.CENTER, -1, 0);
	}
	
	@Override
	public boolean mouseClick(double mouseX, double mouseY, int button) {
		if(mouseX >= 50F && mouseX <= width-100 && mouseY >= 6 && mouseY <= 16) {
			float scroll = GuiUtils.calculateScrollOffset(width-100, fontRendererObj, GuiAlign.CENTER, nav.getHeader(), 0);
			GuiScreen screen = nav.getScreen(fontRendererObj, (int)(mouseX - GuiAlign.CENTER.align(50, width-100, fontRendererObj.getStringWidth(nav.getHeader())) - scroll));
			if(screen instanceof ConfigScreen) {
				mc.displayGuiScreen(screen);
				return true;
			}
			else if(screen != null) { 
				leave();
				return true;
			}
		}
		return super.mouseClick(mouseX, mouseY, button);
	}
	
	@Override
	protected List<Element> sortElements(List<Element> list) {
		list.sort(SORTER);
		return list;
	}
	
	private void goToRoot(GuiButton button) {
		GuiScreen prev = this;
		GuiScreen parent = this;
		while(parent instanceof ConfigScreen) {
			prev = parent;
			parent = ((ConfigScreen)parent).parent;
		}
		if(prev != this) {
			mc.displayGuiScreen(prev);
		}
	}
	
	private void leave() {
		ConfigScreen prev = this;
		GuiScreen parent = this;
		while(parent instanceof ConfigScreen) {
			prev = (ConfigScreen)parent;
			parent = ((ConfigScreen)parent).parent;
		}
		if(prev != this) {
			GuiScreen toOpen = prev.parent;
			if(node.isRoot() && prev.isChanged()) {
				mc.displayGuiScreen(new GuiYesNo((T, K) -> {
					mc.displayGuiScreen(T ? toOpen : this);	
				}, new TextComponentTranslation("gui.carbonconfig.warn.changed").getFormattedText(), new TextComponentTranslation("gui.carbonconfig.warn.changed.desc").setStyle(new Style().setColor(TextFormatting.GRAY)).getFormattedText(), 0));
				return;
			}
			mc.displayGuiScreen(toOpen);
		}
	}
	
	private void reset(GuiButton button) {
		mc.displayGuiScreen(new MultiChoiceScreen(T -> {
			if(T.isMain()) processAction(IConfigNode::setDefault);
			else if(T.isOther()) processAction(IConfigNode::setPrevious);
			mc.displayGuiScreen(this);
		}, new TextComponentTranslation("gui.carbonconfig.reset_all.title"), new TextComponentTranslation("gui.carbonconfig.reset_all.message").setStyle(new Style().setColor(TextFormatting.GRAY)), 
			new TextComponentTranslation("gui.carbonconfig.reset_all.default"), new TextComponentTranslation("gui.carbonconfig.reset_all.reset"), new TextComponentTranslation("gui.carbonconfig.reset_all.cancel")));
	}
	
	private void save(GuiButton button) {
		List<IConfigNode> value = processedChanged(IConfigNode::save);
		config.save();
		if(findFirst(IConfigNode::requiresRestart, value)) {
			MultiChoiceScreen choice = new MultiChoiceScreen(T -> {
				mc.displayGuiScreen(parent);
			}, new TextComponentTranslation("gui.carbonconfig.restart.title"), new TextComponentTranslation("gui.carbonconfig.restart.message").setStyle(new Style().setColor(TextFormatting.GRAY)), new TextComponentTranslation("gui.carbonconfig.ok"));
			mc.displayGuiScreen(choice);
			return;
		}
		else if(mc.theWorld != null && findFirst(IConfigNode::requiresReload, value)) {
			MultiChoiceScreen choice = new MultiChoiceScreen(T -> {
				mc.displayGuiScreen(parent);
			}, new TextComponentTranslation("gui.carbonconfig.reload.title"), new TextComponentTranslation("gui.carbonconfig.reload.message").setStyle(new Style().setColor(TextFormatting.GRAY)), new TextComponentTranslation("gui.carbonconfig.ok"));
			mc.displayGuiScreen(choice);
			return;
		}
		mc.displayGuiScreen(parent);
	}
	
	private <T> boolean findFirst(Predicate<T> filter, List<T> elements) {
		for(int i = 0,m=elements.size();i<m;i++) {
			if(filter.test(elements.get(i))) return true;
		}
		return false;
	}
	
	private List<IConfigNode> processedChanged(Consumer<IConfigNode> action) {
		List<IConfigNode> output = new ObjectArrayList<>();
		Deque<IConfigNode> nodes = new LinkedList<>();
		nodes.push(node);
		while(!nodes.isEmpty()) {
			IConfigNode node = nodes.pop();
			if(!node.isLeaf()) {
				node.getChildren().forEach(nodes::push);
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
		Deque<IConfigNode> nodes = new LinkedList<>();
		nodes.push(node);
		while(!nodes.isEmpty()) {
			IConfigNode node = nodes.pop();
			if(!node.isLeaf()) {
				node.getChildren().forEach(nodes::push);
				continue;
			}
			action.accept(node);
		}
	}
	
	@Override
	protected void onSearchChange(CarbonEditBox box, String value) {
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
	
	private void goBack(GuiButton button) {
		if(node.isRoot() && isChanged()) {
			mc.displayGuiScreen(new GuiYesNo((T, K) -> {
				mc.displayGuiScreen(T ? parent : this);	
			}, new TextComponentTranslation("gui.carbonconfig.warn.changed").getFormattedText(), new TextComponentTranslation("gui.carbonconfig.warn.changed.desc").setStyle(new Style().setColor(TextFormatting.GRAY)).getFormattedText(), 0));
			return;
		}
		mc.displayGuiScreen(parent);
	}
	
	private boolean isChanged() {
		Deque<IConfigNode> nodes = new LinkedList<>();
		nodes.push(node);
		while(!nodes.isEmpty()) {
			IConfigNode node = nodes.pop();
			if(!node.isLeaf()) {
				node.getChildren().forEach(nodes::push);
				continue;
			}
			if(node.isChanged()) return true;
		}
		return false;
	}
	
	@Override
	protected int getListWidth() {
		return 340;
	}
	
	@Override
	protected int getScrollPadding() {
		return 175;
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
				if(child.isForcingSuggestions()) {
					elements.accept(new SelectionElement(child));
					continue;
				}
				Element element = child.getDataType().get(0).create(child);
				if(element != null) elements.accept(element);
			}
			else elements.accept(new FolderElement(child, nav));
		}
	}
	
	private static List<Element> getAllElements(IConfigNode initGui) {
		Deque<IConfigNode> nodes = new LinkedList<>();
		nodes.push(initGui);
		List<Element> results = new ObjectArrayList<>();
		while(!nodes.isEmpty()) {
			IConfigNode node = nodes.pop();
			if(!node.isLeaf()) {
				node.getChildren().forEach(nodes::push);
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
	
	public FolderElement getElement(String name) {
		for(Element element : allEntries) {
			if(element instanceof FolderElement) {
				FolderElement folder = (FolderElement)element;
				if(folder.getNode() != null && name.equalsIgnoreCase(folder.getNode().getNodeName())) {
					return folder;
				}
			}
		}
		return null;
	}
	
	public static class Navigator {
		private static final ITextComponent SPLITTER = new TextComponentString(" > ").setStyle(new Style().setColor(TextFormatting.GOLD).setBold(true));
		List<String> layer = new ObjectArrayList<>();
		List<GuiScreen> screenByIndex = new ObjectArrayList<>();
		List<String> walker = null;
		String buildCache = null;
		
		private Navigator() {}
		
		public Navigator(ITextComponent base) {
			layer.add(base.getFormattedText());
		}
		
		public static Navigator create(IModConfig config) {
			ModContainer container = Loader.instance().getIndexedModList().get(config.getModId());
			Navigator nav = new Navigator(new TextComponentString(container == null ? "Unknown" : container.getName()));
			nav.setScreenForLayer(null);
			return nav.add(new TextComponentTranslation("gui.carbonconfig.type."+config.getConfigType().name().toLowerCase()));
		}
		
		public Navigator add(ITextComponent name) {
			return add(name, null);
		}
		
		public Navigator add(ITextComponent name, String walkerEntry) {
			Navigator nav = new Navigator();
			nav.layer.addAll(layer);
			nav.screenByIndex.addAll(screenByIndex);
			nav.layer.add(name.getFormattedText());
			if(walker != null && walker.size() > 1 && walkerEntry != null && walker.indexOf(walkerEntry.toLowerCase(Locale.ROOT)) == 0) {
				nav.walker = new ObjectArrayList<>();
				for(int i = 1;i<walker.size();i++) {
					nav.walker.add(walker.get(i));
				}
			}
			return nav;
		}
		
		public Navigator withWalker(String...traversePath) {
			walker = new ObjectArrayList<>();
			for(String path : traversePath) {
				walker.add(path.toLowerCase(Locale.ROOT));
			}
			return this;
		}
		
		public void setScreenForLayer(GuiScreen owner) {
			if(layer.size() > screenByIndex.size()) screenByIndex.add(owner);
			else screenByIndex.set(layer.size()-1, owner);
		}
		
		public GuiScreen getScreen(FontRenderer font, int x) {
			int splitterWidth = font.getStringWidth(SPLITTER.getFormattedText());
			for(int i = 0,m=layer.size();i<m;i++) {
				int width = font.getStringWidth(layer.get(i));
				if(x >= 0 && x <= width) return screenByIndex.get(i);
				x-=width;
				x-=splitterWidth;
			}
			return null;
		}
		
		protected void consumeWalker() {
			walker = null;
		}
		
		protected String getWalkNode() {
			return walker == null ? null : walker.get(0);
		}
		
		public String getHeader() {
			if(buildCache == null) {
				StringBuilder builder = new StringBuilder();
				String splitter = SPLITTER.getFormattedText();
				for(int i = 0,m=layer.size();i<m;i++) {
					builder.append(layer.get(i));
					if(i == m-1) continue;
					builder.append(splitter);
				}
				buildCache = builder.toString();
			}
			return buildCache;
		}
	}
}