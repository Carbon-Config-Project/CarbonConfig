package carbonconfiglib.gui.screen;

import java.util.List;
import java.util.function.Consumer;

import com.mojang.blaze3d.vertex.PoseStack;

import carbonconfiglib.gui.api.BackgroundTexture;
import carbonconfiglib.gui.api.DataType;
import carbonconfiglib.gui.api.IArrayNode;
import carbonconfiglib.gui.api.IConfigNode;
import carbonconfiglib.gui.config.CompoundElement;
import carbonconfiglib.gui.config.ConfigElement;
import carbonconfiglib.gui.config.Element;
import carbonconfiglib.gui.config.ListScreen;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Button;
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
public class ArrayScreen extends ListScreen
{
	Screen prev;
	IConfigNode entry;
	IArrayNode array;
	List<DataType> type;
	
	public ArrayScreen(IConfigNode entry, Screen prev, BackgroundTexture customTexture) {
		super(entry.getName(), customTexture);
		this.prev = prev;
		this.entry = entry;
		this.array = entry.asArray();
		this.type = entry.getDataType();
		array.createTemp();
	}
	
	@Override
	protected void init() {
		super.init();
		int x = width / 2;
		int y = height;
		addRenderableWidget(new ExtendedButton(x-92, y-27, 80, 20, Component.translatable("gui.carbonconfig.apply"), this::apply));
		addRenderableWidget(new ExtendedButton(x-10, y-27, 20, 20, Component.literal("+"), this::createEntry));
		addRenderableWidget(new ExtendedButton(x+12, y-27, 80, 20, Component.translatable("gui.carbonconfig.back"), this::goBack));
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
		minecraft.setScreen(prev);
	}
	
	private void apply(Button button) {
		array.apply();
		minecraft.setScreen(prev);
	}
	
	private void goBack(Button button) {
		if(array.isChanged()) {
			minecraft.setScreen(new ConfirmScreen(T -> {
				if(T) array.setPrevious();
				minecraft.setScreen(T ? prev : this);				
			}, Component.translatable("gui.carbonconfig.warn.changed"), Component.translatable("gui.carbonconfig.warn.changed.desc").withStyle(ChatFormatting.GRAY)));
			return;
		}
		array.setPrevious();
		minecraft.setScreen(prev);
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
	public void render(PoseStack stack, int mouseX, int mouseY, float partialTicks) {
		super.render(stack, mouseX, mouseY, partialTicks);
		font.draw(stack, title, (width/2)-(font.width(title)/2), 30, -1);
	}
	
	public void createEntry(Button button) {
		int size = array.size();
		array.createNode();
		if(entry.getValidValues().size() > 0) {
			ListSelectionScreen screen = entry.getDataType().size() > 1 ? ListSelectionScreen.ofCompound(prev, entry, array.asCompound(size), getCustomTexture()) : ListSelectionScreen.ofValue(prev, entry, array.asValue(size), getCustomTexture());
			screen.withListener(() -> postCreate(size, true), () -> array.removeNode(size)).disableAbortWarning();
			minecraft.setScreen(screen);
			return;
		}
		postCreate(size, false);
	}
	
	private void postCreate(int size, boolean reopen) {
		if(type.size() > 1) {
			CompoundScreen screen = new CompoundScreen(entry, array.asCompound(size), this, getCustomTexture());
			screen.setAbortListener(() -> array.removeNode(size));
			minecraft.setScreen(screen);
			lastScroll = Double.MAX_VALUE;
			return;
		}
		ConfigElement element = type.get(0).create(entry, array, size);
		if(element != null) {
			addEntry(element);
			visibleList.addElement(element);
			visibleList.setScrollAmount(visibleList.getMaxScroll(), true);
		}
		if(reopen) minecraft.setScreen(this);
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