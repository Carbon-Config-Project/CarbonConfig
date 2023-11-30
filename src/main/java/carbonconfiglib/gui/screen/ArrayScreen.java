package carbonconfiglib.gui.screen;

import java.util.List;
import java.util.function.Consumer;

import carbonconfiglib.gui.api.BackgroundTexture.BackgroundHolder;
import carbonconfiglib.gui.api.DataType;
import carbonconfiglib.gui.api.IArrayNode;
import carbonconfiglib.gui.api.IConfigNode;
import carbonconfiglib.gui.config.CompoundElement;
import carbonconfiglib.gui.config.ConfigElement;
import carbonconfiglib.gui.config.Element;
import carbonconfiglib.gui.config.ListScreen;
import carbonconfiglib.gui.widgets.CarbonButton;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiYesNo;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;

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
public class ArrayScreen extends ListScreen
{
	GuiScreen prev;
	IConfigNode entry;
	IArrayNode array;
	List<DataType> type;
	
	public ArrayScreen(IConfigNode entry, GuiScreen prev, BackgroundHolder customTexture) {
		super(entry.getName(), customTexture);
		this.prev = prev;
		this.entry = entry;
		this.array = entry.asArray();
		this.type = entry.getDataType();
		array.createTemp();
	}
	
	@Override
	public void initGui() {
		super.initGui();
		int x = width / 2;
		int y = height;
		addWidget(new CarbonButton(x-92, y-27, 80, 20, I18n.format("gui.carbonconfig.apply"), this::apply));
		addWidget(new CarbonButton(x-10, y-27, 20, 20, "+", this::createEntry));
		addWidget(new CarbonButton(x+12, y-27, 80, 20, I18n.format("gui.carbonconfig.back"), this::goBack));
	}
	
	@Override
	protected boolean shouldHaveTooltips() {
		return true;
	}
	
	@Override
	protected boolean shouldHaveSearch() {
		return false;
	}
	
	@Override
	public void onClose() {
		array.setPrevious();
		mc.displayGuiScreen(prev);
	}
	
	private void apply(GuiButton button) {
		array.apply();
		mc.displayGuiScreen(prev);
	}
	
	private void goBack(GuiButton button) {
		if(array.isChanged()) {
			mc.displayGuiScreen(new GuiYesNo((T, V) -> {
				if(T) array.setPrevious();
				mc.displayGuiScreen(T ? prev : this);				
			}, new ChatComponentTranslation("gui.carbonconfig.warn.changed").getFormattedText(), new ChatComponentTranslation("gui.carbonconfig.warn.changed.desc").setChatStyle(new ChatStyle().setColor(EnumChatFormatting.GRAY)).getFormattedText(), 0));
			return;
		}
		array.setPrevious();
		mc.displayGuiScreen(prev);
	}
	
	@Override
	protected void collectElements(Consumer<Element> elements) {
		if(type.size() > 1) {
			for(int i = 0,m=array.size();i<m;i++) {
				elements.accept(new CompoundElement(entry, array, array.asCompound(i)));
			}
			return;
		}
		for(int i = 0,m=array.size();i<m;i++) {
			ConfigElement element = type.get(0).create(entry, array, i);
			if(element != null) elements.accept(element);
		}
	}
	
	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		super.drawScreen(mouseX, mouseY, partialTicks);
		String title = this.title.getFormattedText();
		fontRendererObj.drawString(title, (width/2)-(fontRendererObj.getStringWidth(title)/2), 30, -1);
	}
	
	public void createEntry(GuiButton button) {
		int size = array.size();
		array.createNode();
		if(entry.getValidValues().size() > 0) {
			ListSelectionScreen screen = entry.getDataType().size() > 1 ? ListSelectionScreen.ofCompound(prev, entry, array.asCompound(size), getCustomTexture()) : ListSelectionScreen.ofValue(prev, entry, array.asValue(size), getCustomTexture());
			screen.withListener(() -> postCreate(size, true), () -> array.removeNode(size)).disableAbortWarning();
			mc.displayGuiScreen(screen);
			return;
		}
		postCreate(size, false);
	}
	
	private void postCreate(int size, boolean reopen) {
		if(type.size() > 1) {
			CompoundScreen screen = new CompoundScreen(entry, array.asCompound(size), this, getCustomTexture());
			screen.setAbortListener(() -> array.removeNode(size));
			mc.displayGuiScreen(screen);
			lastScroll = Double.MAX_VALUE;
			return;
		}
		ConfigElement element = type.get(0).create(entry, array, size);
		if(element != null) {
			addEntry(element);
			visibleList.addElement(element);
			visibleList.setScrollAmount(visibleList.getMaxScroll());
		}
		if(reopen) mc.displayGuiScreen(this);
	}
	
	@Override
	public void removeEntry(Element element) {
		int index = allEntries.indexOf(element);
		if(index == -1) return;
		visibleList.removeElement(element);
		allEntries.remove(index);
		array.removeNode(index);
		visibleList.setScrollAmount(visibleList.getScrollAmount());
	}
}