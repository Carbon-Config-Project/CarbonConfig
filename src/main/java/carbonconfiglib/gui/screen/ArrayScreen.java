package carbonconfiglib.gui.screen;

import java.util.function.Consumer;

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
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
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
	IArrayNode array;
	StructureType innerType;
	Runnable closeListener;
	
	public ArrayScreen(IArrayNode entry, GuiScreen prev, BackgroundHolder customTexture) {
		super(entry.getName(), customTexture);
		this.prev = prev;
		this.array = entry;
		this.innerType = entry.getInnerType();
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
		mc.displayGuiScreen(prev);
	}
	
	private void apply(GuiButton button) {
		array.apply();
		mc.displayGuiScreen(prev);
	}
	
	private void goBack(GuiButton button) {
		if(array.isChanged()) {
			mc.displayGuiScreen(new GuiMultiLineYesNo((T, V) -> {
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
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		super.drawScreen(mouseX, mouseY, partialTicks);
		String title = this.title.getFormattedText();
		fontRendererObj.drawString(title, (width/2)-(fontRendererObj.getStringWidth(title)/2), 30, -1);
	}
	
	public void createEntry(GuiButton button) {
		int size = array.size();
		array.createNode();
		if(array.getSuggestions().size() > 0) {
			ListSelectionScreen screen = new ListSelectionScreen(this, array.get(size), innerType == StructureType.COMPOUND ? NodeSupplier.ofCompound(array) : NodeSupplier.ofValue(), getCustomTexture());
			screen.withListener(() -> postCreate(size, true), () -> array.removeNode(size)).disableAbortWarning();
			mc.displayGuiScreen(screen);
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
				mc.displayGuiScreen(compoundScreen);
				lastScroll = Double.MAX_VALUE;
				break;
			case LIST:
				ArrayScreen arrayScreen = new ArrayScreen(node.asArray(), this, getCustomTexture());
				arrayScreen.setAbortListener(() -> array.removeNode(size));
				mc.displayGuiScreen(arrayScreen);
				lastScroll = Double.MAX_VALUE;
				break;
			case SIMPLE:
				ConfigElement element = node.asValue().getDataType().create(array, node.asValue());
				if(element != null) {
					addEntry(element);
					visibleList.addElement(element);
					visibleList.setScrollAmount(visibleList.getMaxScroll());
				}				
				break;
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