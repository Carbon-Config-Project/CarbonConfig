package carbonconfiglib.gui.config;

import java.awt.Desktop;
import java.net.URL;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

import carbonconfiglib.gui.api.BackgroundTexture.BackgroundHolder;
import carbonconfiglib.gui.screen.MultiChoiceScreen;
import carbonconfiglib.gui.widgets.CarbonEditBox;
import carbonconfiglib.gui.widgets.GuiUtils;
import carbonconfiglib.gui.widgets.Icon;
import carbonconfiglib.gui.widgets.screen.CarbonScreen;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.client.gui.GuiConfirmOpenLink;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import speiger.src.collections.objects.utils.ObjectLists;

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
public abstract class ListScreen extends CarbonScreen implements IListOwner
{
	private static final ITextComponent LOG_INFO = new TextComponentTranslation("gui.carbonconfig.logo.name").setStyle(new Style().setColor(TextFormatting.GOLD)).appendText("\n").appendSibling(new TextComponentTranslation("gui.carbonconfig.logo.page").setStyle(new Style().setColor(TextFormatting.GRAY)));
	protected ITextComponent title;
	protected ElementList visibleList;
	protected List<Element> allEntries = new ObjectArrayList<>();
	protected List<ITextComponent> tooltips = new ObjectArrayList<>();
	protected CarbonEditBox activeWidget;
	protected long currentTick = 0;
	protected long lastTick = -1;
	protected double lastScroll = -1;
	protected CarbonEditBox searchBox;
	BackgroundHolder customTexture;
	
	public ListScreen(ITextComponent name, BackgroundHolder customTexture) {
		this.title = name;
		this.customTexture = customTexture;
	}
	
	@Override
	public void initGui() {
		super.initGui();
		buttonList.clear();
		allEntries.clear();
		visibleList = new ElementList(width, height, getHeaderSpace(), height - getFooterSpace(), getElementHeight());
		visibleList.setCustomBackground(customTexture);
		visibleList.setListWidth(getListWidth());
		visibleList.setScrollPadding(getScrollPadding());
		collectElements(this::addEntry);
		visibleList.addElements(sortElements(allEntries));
		addWidget(visibleList);
		if(shouldHaveSearch()) {
			searchBox = new CarbonEditBox(fontRendererObj, width / 2 - 100, 25, 200, 20);
			searchBox.setSuggestion(I18n.format("gui.carbonconfig.search"));
			searchBox.setListener(T -> onSearchChange(searchBox, T.toLowerCase(Locale.ROOT)));
			addWidget(searchBox);
		}
		if(lastScroll >= 0D) visibleList.setScrollAmount(lastScroll);
	}
	
	protected List<Element> sortElements(List<Element> list) {
		return list;
	}
	
	@Override
	public void tick() {
		currentTick++;
		super.tick();
		visibleList.tick();
		if(searchBox != null) {
			searchBox.tick();
		}
	}
	
	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		super.drawScreen(mouseX, mouseY, partialTicks);
		GuiUtils.drawTextureRegion(5, 5, 40, 40, Icon.LOGO, 400, 400);
		if(mouseX >= 5 && mouseX <= 45 && mouseY >= 5 && mouseY <= 40) {
			addTooltips(LOG_INFO);
		}
		handleForground(mouseX, mouseY, partialTicks);
		if(!tooltips.isEmpty()) {
			List<String> text = new ObjectArrayList<>();
			for(ITextComponent entry : tooltips) {
				text.addAll(fontRendererObj.listFormattedStringToWidth(entry.getFormattedText(), Math.max(mouseX, width - mouseX) - 20));
			}
			drawHoveringText(text, mouseX, mouseY);
			tooltips.clear();
		}
	}
	
	public void handleForground(int mouseX, int mouseY, float partialTicks) {

	}
	
	@Override
	public void onGuiClosed() {
		lastScroll = visibleList.getScrollAmount();
		super.onGuiClosed();
	}
	
	@Override
	public boolean mouseClick(double mouseX, double mouseY, int button) {
		if(mouseX >= 5 && mouseX <= 45 && mouseY >= 5 && mouseY <= 45) {
			activeWidget = null;
			MultiChoiceScreen screen = new MultiChoiceScreen(T -> {
				if(T.isMain()) openURL("https://curseforge.com/minecraft/mc-mods/carbon-config");
				else if(T.isOther()) openURL("https://modrinth.com/mod/carbon-config");
				else mc.displayGuiScreen(this);
			}, new TextComponentTranslation("gui.carbonconfig.logo.link.title"), new TextComponentTranslation("gui.carbonconfig.logo.link.message").setStyle(new Style().setColor(TextFormatting.GRAY)), 
			   new TextComponentTranslation("gui.carbonconfig.logo.link.curseforge"), new TextComponentTranslation("gui.carbonconfig.logo.link.modrinth"), new TextComponentTranslation("gui.carbonconfig.reset_all.cancel"));
			mc.displayGuiScreen(screen);
			return true;
		}
		boolean result = super.mouseClick(mouseX, mouseY, button);
		if(currentTick - lastTick >= 5) activeWidget = null;
		return result;
	}
	
	private void openURL(String url) {
		mc.displayGuiScreen(new GuiConfirmOpenLink((T, U) -> {
            if (T)
				try { Desktop.getDesktop().browse(new URL(url).toURI()); }
				catch(Exception e) { e.printStackTrace(); }
            this.mc.displayGuiScreen(this);
         }, url, 0, true));
	}
	
	protected void addInternal(Element element) {
		element.owner = this;
		element.init();
	}
	
	protected void addEntry(Element element) {
		element.owner = this;
		allEntries.add(element);
		element.init();
	}
	
	protected abstract void collectElements(Consumer<Element> elements);
	
	protected void onSearchChange(CarbonEditBox box, String value) {
		onSearchChange(box, value, allEntries);
	}
		
	protected boolean onSearchChange(CarbonEditBox box, String value, List<Element> allEntries) {
		if(value.isEmpty()) {
			box.setSuggestion(I18n.format("gui.carbonconfig.search"));
			visibleList.updateList(allEntries);
			visibleList.setScrollAmount(visibleList.getScrollAmount());
			return true;
		}
		List<Element> validElements = new ObjectArrayList<>();
		String suggestion = null;
		for(int i = 0,m=allEntries.size();i<m;i++) {
			Element el = allEntries.get(i);
			if(IIgnoreSearch.shouldIgnoreSearch(el)) continue;
			String name = el.getName().toLowerCase(Locale.ROOT);
			if(name.contains(value)) 
			{
				validElements.add(el);
				if(name.startsWith(value) && (suggestion == null || suggestion.length() > name.length())) {
					suggestion = el.getName();
				}
			}
		}
		if(validElements.size() > 0 && suggestion == null) {
			List<Element> sorted = validElements.size() > 2 ? new ObjectArrayList<>(validElements) : validElements;
			if(validElements.size() > 2) sorted.sort(Comparator.comparing(Element::getName, Comparator.comparingInt(String::length).reversed()));
			for(int i = 0,m=sorted.size();i<m;i++) {
				String sub = sorted.get(i).getName();
				int offset = sub.toLowerCase(Locale.ROOT).indexOf(value);
				suggestion = sub.substring(offset);
				if(suggestion.length() > value.length()) break; 
			}
			if(suggestion.length() <= value.length()) suggestion = null;
		}
		if(validElements.isEmpty()) {
			box.setSuggestion("");
			visibleList.updateList(ObjectLists.empty());
			visibleList.setScrollAmount(visibleList.getScrollAmount());
			return false;
		}
		if(suggestion == null) box.setSuggestion("");
		else box.setSuggestion(suggestion.substring(value.length()));		
		visibleList.updateList(validElements);
		visibleList.setScrollAmount(visibleList.getScrollAmount());
		return false;
	}
	
	protected boolean shouldHaveSearch() {
		return true;
	}
	
	protected boolean shouldHaveTooltips() {
		return true;
	}
	
	protected int getListWidth() {
		return 220;
	}
	
	protected int getScrollPadding() {
		return 124;
	}
	
	protected int getHeaderSpace() {
		return 50;
	}
	
	protected int getFooterSpace() {
		return 36;
	}
	
	protected int getElementHeight() {
		return 24;
	}
	
	@Override
	public void addTooltips(ITextComponent tooltip) {
		if(shouldHaveTooltips()) tooltips.add(tooltip);
	}
	
	@Override
	public boolean isInsideList(double mouseX, double mouseY) {
		return visibleList.isMouseOver(mouseX, mouseY);
	}
	
	@Override
	public boolean isActiveWidget(CarbonEditBox widget) {
		return activeWidget == widget;
	}
	
	@Override
	public void setActiveWidget(CarbonEditBox widget) {
		activeWidget = widget;
		lastTick = currentTick;
	}
	
	@Override
	public void updateInformation() {
		visibleList.children().forEach(Element::updateValues);	
	}
	
	@Override
	public void removeEntry(Element element) {
	}
	
	@Override
	public BackgroundHolder getCustomTexture() {
		return customTexture;
	}
}