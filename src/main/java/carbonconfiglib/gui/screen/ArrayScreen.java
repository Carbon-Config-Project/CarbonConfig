package carbonconfiglib.gui.screen;

import java.util.function.Consumer;

import com.mojang.blaze3d.vertex.PoseStack;

import carbonconfiglib.gui.api.BackgroundTexture.BackgroundHolder;
import carbonconfiglib.gui.api.IArrayNode;
import carbonconfiglib.gui.api.INode;
import carbonconfiglib.gui.api.IValueNode;
import carbonconfiglib.gui.config.ArrayElement;
import carbonconfiglib.gui.config.CompoundElement;
import carbonconfiglib.gui.config.ConfigElement;
import carbonconfiglib.gui.config.Element;
import carbonconfiglib.gui.config.ListScreen;
import carbonconfiglib.gui.screen.ListSelectionScreen.NodeSupplier;
import carbonconfiglib.gui.widgets.CarbonButton;
import carbonconfiglib.utils.structure.IStructuredData.StructureType;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

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
	IArrayNode array;
	StructureType innerType;
	Runnable closeListener;
	
	public ArrayScreen(IArrayNode entry, Screen prev, BackgroundHolder customTexture) {
		super(entry.getName(), customTexture);
		this.prev = prev;
		this.array = entry;
		this.innerType = entry.getInnerType();
		array.createTemp();
	}
	
	@Override
	protected void init() {
		super.init();
		int x = width / 2;
		int y = height;
		addRenderableWidget(new CarbonButton(x-92, y-27, 80, 20, Component.translatable("gui.carbonconfig.apply"), this::apply));
		addRenderableWidget(new CarbonButton(x-10, y-27, 20, 20, Component.literal("+"), this::createEntry));
		addRenderableWidget(new CarbonButton(x+12, y-27, 80, 20, Component.translatable("gui.carbonconfig.back"), this::goBack));
	}
	
	@Override
	protected boolean shouldHaveTooltips() {
		return true;
	}
	
	@Override
	protected boolean shouldHaveSearch() {
		return false;
	}
	
	public void setAbortListener(Runnable run) {
		this.closeListener = run;
	}
	
	private void notifyClose() {
		array.setPrevious();
		if(closeListener == null) return;
		closeListener.run();
	}
	
	@Override
	public void onClose() {
		notifyClose();
		minecraft.setScreen(prev);
	}
	
	private void apply(Button button) {
		array.apply();
		minecraft.setScreen(prev);
	}
	
	private void goBack(Button button) {
		if(array.isChanged()) {
			minecraft.setScreen(new ConfirmScreen(T -> {
				if(T) notifyClose();
				minecraft.setScreen(T ? prev : this);				
			}, Component.translatable("gui.carbonconfig.warn.changed"), Component.translatable("gui.carbonconfig.warn.changed.desc").withStyle(ChatFormatting.GRAY)));
			return;
		}
		notifyClose();
		minecraft.setScreen(prev);
	}
	
	@Override
	protected void collectElements(Consumer<Element> elements) {
		switch(innerType) {
			case COMPOUND:
				for(int i = 0,m=array.size();i<m;i++) {
					elements.accept(new CompoundElement(array, array.get(i).asCompound()));
				}
				break;
			case LIST:
				for(int i = 0,m=array.size();i<m;i++) {
					elements.accept(new ArrayElement(array, array.get(i).asArray()));
				}
				break;
			case SIMPLE:
				for(int i = 0,m=array.size();i<m;i++) {
					IValueNode node = array.get(i).asValue();
					ConfigElement element = node.getDataType().create(array, node);
					if(element != null) elements.accept(element);
				}
				break;
		}
	}
	
	@Override
	public void render(PoseStack stack, int mouseX, int mouseY, float partialTicks) {
		super.render(stack, mouseX, mouseY, partialTicks);
		font.draw(stack, title, (width/2)-(font.width(title)/2), 30, -1);
	}
	
	public void createEntry(Button button) {
		int size = array.size();
		array.createNode(null);
		if(array.getSuggestions().size() > 0) {
			ListSelectionScreen screen = new ListSelectionScreen(this, array.get(size), innerType == StructureType.COMPOUND ? NodeSupplier.ofCompound(array) : NodeSupplier.ofValue(), getCustomTexture());
			screen.withListener(() -> postCreate(size, true), () -> array.removeNode(size)).disableAbortWarning();
			minecraft.setScreen(screen);
			return;
		}
		postCreate(size, false);
	}
	
	private void postCreate(int size, boolean reopen) {
		INode node = array.get(size);
		switch(node.getNodeType()) {
			case COMPOUND:
				CompoundScreen compoundScreen = new CompoundScreen(node.asCompound(), this, getCustomTexture());
				compoundScreen.setAbortListener(() -> array.removeNode(size));
				minecraft.setScreen(compoundScreen);
				lastScroll = Double.MAX_VALUE;
				break;
			case LIST:
				ArrayScreen arrayScreen = new ArrayScreen(node.asArray(), this, getCustomTexture());
				arrayScreen.setAbortListener(() -> array.removeNode(size));
				minecraft.setScreen(arrayScreen);
				lastScroll = Double.MAX_VALUE;
				break;
			case SIMPLE:
				ConfigElement element = node.asValue().getDataType().create(array, node.asValue());
				if(element != null) {
					addEntry(element);
					visibleList.addElement(element);
					visibleList.setScrollAmount(visibleList.getMaxScroll(), true);
				}				
				break;
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