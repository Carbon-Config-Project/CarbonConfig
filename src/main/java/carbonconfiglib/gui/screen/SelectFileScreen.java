package carbonconfiglib.gui.screen;

import java.nio.file.Path;
import java.util.List;
import java.util.function.Consumer;

import carbonconfiglib.gui.api.BackgroundTexture.BackgroundHolder;
import carbonconfiglib.gui.api.IModConfig;
import carbonconfiglib.gui.api.IModConfig.IConfigTarget;
import carbonconfiglib.gui.api.IModConfig.WorldConfigTarget;
import carbonconfiglib.gui.config.ConfigElement.GuiAlign;
import carbonconfiglib.gui.config.Element;
import carbonconfiglib.gui.config.ListScreen;
import carbonconfiglib.gui.screen.ConfigScreen.Navigator;
import carbonconfiglib.gui.widgets.CarbonButton;
import carbonconfiglib.gui.widgets.GuiUtils;
import carbonconfiglib.gui.widgets.screen.IInteractable;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;
import net.minecraft.world.storage.SaveFormatComparator;
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
public class SelectFileScreen extends ListScreen
{
	private static final IChatComponent TEXT = new ChatComponentTranslation("gui.carbonconfig.select_world");
	IModConfig config;
	GuiScreen parent;
	
	public SelectFileScreen(IChatComponent name, BackgroundHolder customTexture, GuiScreen parent, IModConfig config) {
		super(name, customTexture);
		this.config = config;
		this.parent = parent;
	}
	
	@Override
	protected void collectElements(Consumer<Element> elements) {
		for(IConfigTarget target : config.getPotentialFiles()) {
			elements.accept(new WorldElement(target, config, parent, title));
		}
	}
	
	@Override
	public void initGui() {
		super.initGui();
		int x = width / 2;
		int y = height;
		addWidget(new CarbonButton(x-80, y-27, 160, 20, I18n.format("gui.carbonconfig.back"), T -> onClose()));
	}
	
	@Override
	protected int getElementHeight() {
		return 28;
	}
	
	@Override
	public void onClose() {
		mc.displayGuiScreen(parent);
	}
	
	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		super.drawScreen(mouseX, mouseY, partialTicks);
		String text = TEXT.getFormattedText();
		fontRendererObj.drawString(text, (width/2)-(fontRendererObj.getStringWidth(text)/2), 8, -1);
	}
		
	private static class WorldElement extends Element {
		IModConfig config;
		IConfigTarget target;
		GuiScreen parent;
		CarbonButton button;
		IChatComponent title;
		IChatComponent path;
		Navigator nav;
		
		public WorldElement(IConfigTarget target, IModConfig config, GuiScreen parent, IChatComponent prevName) {
			super(new ChatComponentText(target.getName()));
			nav = new Navigator(prevName);
			nav.setScreenForLayer(parent);
			this.target = target;
			this.config = config;
			this.parent = parent;
		}
		
		@Override
		public void init() {
			button = new CarbonButton(0, 0, 62, 20, I18n.format("gui.carbonconfig.pick"), this::onPick);
			if(target instanceof WorldConfigTarget) {
				WorldConfigTarget world = (WorldConfigTarget)target;
				SaveFormatComparator sum = world.getSummary();
				title = new ChatComponentText(sum.getDisplayName());
				path = new ChatComponentText(sum.getFileName()).setChatStyle(new ChatStyle().setColor(EnumChatFormatting.GRAY));
			}
			else 
			{
				title = new ChatComponentText(target.getName());
				Path folder = target.getFolder();
				int index = folder.getNameCount();
				path = new ChatComponentText(folder.subpath(index-3, index).toString()).setChatStyle(new ChatStyle().setColor(EnumChatFormatting.GRAY));
			}
		}
		
		@Override
		public void render(int x, int top, int left, int width, int height, int mouseX, int mouseY, boolean selected, float partialTicks) {
			button.xPosition = left+width-62;
			button.yPosition = top + 2;
			button.render(mc, mouseX, mouseY, partialTicks);
			GuiUtils.drawScrollingString(font, title.getFormattedText(), left+5, top+2, 150, 10, GuiAlign.LEFT, -1, 0);
			GuiUtils.drawScrollingString(font, path.getFormattedText(), left+5, top+12, 150, 10, GuiAlign.LEFT, -1, 0);
		}
				
		@Override
		public List<? extends IInteractable> children() {
			return ObjectLists.singleton(button);
		}
		
		private void onPick(GuiButton button) {
			IModConfig config = this.config.loadFromFile(target.getConfigFile());
			if(config == null) {
				mc.displayGuiScreen(parent);
				return;
			}
			mc.displayGuiScreen(new ConfigScreen(nav.add(path.createCopy().setChatStyle(new ChatStyle().setColor(EnumChatFormatting.WHITE))), config, parent, owner.getCustomTexture()));
		}
	}
}
