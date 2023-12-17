package carbonconfiglib.gui.config;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

import com.mojang.blaze3d.vertex.PoseStack;

import carbonconfiglib.gui.api.BackgroundTexture.BackgroundHolder;
import carbonconfiglib.gui.screen.MultiChoiceScreen;
import carbonconfiglib.gui.widgets.CarbonEditBox;
import carbonconfiglib.gui.widgets.GuiUtils;
import carbonconfiglib.gui.widgets.Icon;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectLists;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

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
public abstract class ListScreen extends Screen implements IListOwner
{
	private static final Component LOG_INFO = Component.translatable("gui.carbonconfig.logo.name").withStyle(ChatFormatting.GOLD).append("\n").append(Component.translatable("gui.carbonconfig.logo.page").withStyle(ChatFormatting.GRAY));
	protected ElementList visibleList;
	protected List<Element> allEntries = new ObjectArrayList<>();
	protected List<Component> tooltips = new ObjectArrayList<>();
	protected AbstractWidget activeWidget;
	protected long currentTick = 0;
	protected long lastTick = -1;
	protected double lastScroll = -1;
	protected CarbonEditBox searchBox;
	BackgroundHolder customTexture;
	
	public ListScreen(Component name, BackgroundHolder customTexture) {
		super(name);
		this.customTexture = customTexture;
	}
	
	@Override
	protected void init() {
		super.init();
		clearWidgets();
		allEntries.clear();
		visibleList = new ElementList(width, height, getHeaderSpace(), height - getFooterSpace(), getElementHeight());
		visibleList.setCustomBackground(customTexture);
		visibleList.setListWidth(getListWidth());
		visibleList.setScrollPadding(getScrollPadding());
		collectElements(this::addEntry);
		visibleList.addElements(sortElements(allEntries));
		addRenderableWidget(visibleList);
		if(shouldHaveSearch()) {
			searchBox = new CarbonEditBox(font, width / 2 - 100, 25, 200, 20);
			searchBox.setSuggestion(I18n.get("gui.carbonconfig.search"));
			searchBox.setResponder(T -> onSearchChange(searchBox, T.toLowerCase(Locale.ROOT)));
			addRenderableWidget(searchBox);
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
	}
	
	@Override
	public void render(GuiGraphics stack, int mouseX, int mouseY, float partialTicks) {
		renderBackground(stack);
		super.render(stack, mouseX, mouseY, partialTicks);
		GuiUtils.drawTextureRegion(stack, 5, 5, 40, 40, Icon.LOGO, 400, 400);
		if(mouseX >= 5 && mouseX <= 45 && mouseY >= 5 && mouseY <= 40) {
			addTooltips(LOG_INFO);
		}
		handleForground(stack, mouseX, mouseY, partialTicks);
		if(!tooltips.isEmpty()) {
			List<FormattedCharSequence> text = new ObjectArrayList<>();
			for(Component entry : tooltips) {
				text.addAll(font.split(entry, Math.max(mouseX, width - mouseX) - 20));
			}
			stack.renderTooltip(font, text, mouseX, mouseY);
			tooltips.clear();
		}

	}
	
	public void handleForground(GuiGraphics stack, int mouseX, int mouseY, float partialTicks) {
	}
	
	@Override
	public void removed() {
		lastScroll = visibleList.getScrollAmount();
		super.removed();
	}
	
	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if(mouseX >= 5 && mouseX <= 45 && mouseY >= 5 && mouseY <= 45) {
			activeWidget = null;
			MultiChoiceScreen screen = new MultiChoiceScreen(T -> {
				if(T.isMain()) openURL("https://curseforge.com/minecraft/mc-mods/carbon-config");
				else if(T.isOther()) openURL("https://modrinth.com/mod/carbon-config");
				else minecraft.setScreen(this);
			}, Component.translatable("gui.carbonconfig.logo.link.title"), Component.translatable("gui.carbonconfig.logo.link.message").withStyle(ChatFormatting.GRAY), 
			   Component.translatable("gui.carbonconfig.logo.link.curseforge"), Component.translatable("gui.carbonconfig.logo.link.modrinth"), Component.translatable("gui.carbonconfig.reset_all.cancel"));
			minecraft.setScreen(screen);
			return true;
		}
		boolean result = super.mouseClicked(mouseX, mouseY, button);
		if(currentTick - lastTick >= 5) activeWidget = null;
		return result;
	}
	
	private void openURL(String url) {
		minecraft.setScreen(new ConfirmLinkScreen(T -> {
            if (T) Util.getPlatform().openUri(url);
            this.minecraft.setScreen(this);
         }, url, true));
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
	
	protected void onSearchChange(EditBox box, String value) {
		onSearchChange(box, value, allEntries);
	}
		
	protected boolean onSearchChange(EditBox box, String value, List<Element> allEntries) {
		if(value.isEmpty()) {
			box.setSuggestion(I18n.get("gui.carbonconfig.search"));
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
			visibleList.updateList(ObjectLists.emptyList());
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
	public void addTooltips(Component tooltip) {
		if(shouldHaveTooltips()) tooltips.add(tooltip);
	}
	
	@Override
	public boolean isInsideList(double mouseX, double mouseY) {
		return visibleList.isMouseOver(mouseX, mouseY);
	}
	
	@Override
	public boolean isActiveWidget(AbstractWidget widget) {
		return activeWidget == widget;
	}
	
	@Override
	public void setActiveWidget(AbstractWidget widget) {
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