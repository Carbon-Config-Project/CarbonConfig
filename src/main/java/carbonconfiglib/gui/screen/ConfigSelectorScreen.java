package carbonconfiglib.gui.screen;

import java.util.List;
import java.util.function.Consumer;

import carbonconfiglib.CarbonConfig;
import carbonconfiglib.api.ConfigType;
import carbonconfiglib.gui.api.BackgroundTexture.BackgroundHolder;
import carbonconfiglib.gui.api.IModConfig;
import carbonconfiglib.gui.api.IModConfigs;
import carbonconfiglib.gui.config.ConfigElement.GuiAlign;
import carbonconfiglib.gui.config.Element;
import carbonconfiglib.gui.config.IIgnoreSearch;
import carbonconfiglib.gui.config.ListScreen;
import carbonconfiglib.gui.screen.ConfigScreen.Navigator;
import carbonconfiglib.gui.widgets.CarbonButton;
import carbonconfiglib.gui.widgets.CarbonIconButton;
import carbonconfiglib.gui.widgets.GuiUtils;
import carbonconfiglib.gui.widgets.Icon;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

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
public class ConfigSelectorScreen extends ListScreen
{
	IModConfigs configs;
	Screen parent;
	ITextComponent modName;
	Label toAdd;
	
	public ConfigSelectorScreen(IModConfigs configs, Screen parent) {
		this(configs, configs.getBackground(), parent);
	}
	
	public ConfigSelectorScreen(IModConfigs configs, BackgroundHolder customTexture, Screen parent) {
		super(new TranslationTextComponent("gui.carbonconfig.select_config"), customTexture);
		this.configs = configs;
		this.parent = parent;
		modName = new StringTextComponent(configs.getModName());
	}
	
	@Override
	protected void init() {
		super.init();
		int x = width / 2;
		int y = height;
		addButton(new CarbonButton(x-80, y-27, 160, 20, new TranslationTextComponent("gui.carbonconfig.back"), T -> onClose()));
	}
	
	@Override
	public void render(int mouseX, int mouseY, float partialTicks) {
		super.render(mouseX, mouseY, partialTicks);
		String modName = this.modName.getFormattedText();
		font.drawString(modName, (width/2)-(font.getStringWidth(modName)/2), 8, -1);
	}
	
	@Override
	protected void collectElements(Consumer<Element> elements) {
		toAdd = new Label(new TranslationTextComponent("gui.carbonconfig.configs.local").applyTextStyles(TextFormatting.GOLD, TextFormatting.BOLD));
		addConfigs(ConfigType.CLIENT, false, elements);
		addConfigs(ConfigType.SHARED, false, elements);
		toAdd = null;
		if(minecraft.world != null) {
			if(!minecraft.isIntegratedServerRunning()) {
				if(!isInstalledOnServer()) {
					return;
				}
				if(isLanServer()) {
					return;
				}
				if(!minecraft.player.hasPermissionLevel(4)) {
					return;
				}
				toAdd = new Label(new TranslationTextComponent("gui.carbonconfig.configs.multiplayer").applyTextStyles(TextFormatting.GOLD, TextFormatting.BOLD));
				addConfigs(ConfigType.SHARED, true, elements);
			}
			else  {
				toAdd = new Label(new TranslationTextComponent("gui.carbonconfig.configs.world").applyTextStyles(TextFormatting.GOLD, TextFormatting.BOLD));
			}
		}
		else {
			toAdd = new Label(new TranslationTextComponent("gui.carbonconfig.configs.world").applyTextStyles(TextFormatting.GOLD, TextFormatting.BOLD));
		}
		addConfigs(ConfigType.SERVER, true, elements);
		toAdd = null;
	}
	
	private void addConfigs(ConfigType type, boolean multiplayer, Consumer<Element> elements) {
		configs.getConfigInstances(type).forEach(T -> {
			if(toAdd != null) {
				elements.accept(toAdd);
				toAdd = null;
			}
			elements.accept(new DirectConfig(T, modName, this, multiplayer));
		});
	}
	
	@Override
	public void onClose() {
		minecraft.displayGuiScreen(parent);
	}
	
	private boolean isInstalledOnServer() {
		return CarbonConfig.NETWORK.isInstalledOnServer();
	}
	
	private boolean isLanServer() {
		ServerData data = minecraft.getCurrentServerData();
		return data != null && data.isOnLAN();
	}
	
	public static class Label extends Element implements IIgnoreSearch {
		public Label(ITextComponent name) {
			super(name);
		}

		@Override
		public boolean shouldIgnoreSearch() {
			return true;
		}
		
		@Override
		public void render(int x, int top, int left, int width, int height, int mouseX, int mouseY, boolean selected, float partialTicks) {
			renderText(name, left, top+1, width, height, GuiAlign.CENTER, -1);
		}
	}
	
	public static class DirectConfig extends Element {
		List<IGuiEventListener> children = new ObjectArrayList<>();
		Screen parent;
		IModConfig handler;
		Button button;
		CarbonIconButton reset;
		boolean multi;
		boolean multiplayer;
		Navigator nav;
		ITextComponent type;
		ITextComponent fileName;
		ITextComponent baseName;
		
		public DirectConfig(IModConfig handler, ITextComponent baseName, Screen parent, boolean multiplayer) {
			super(new StringTextComponent(handler.getFileName()));
			nav = new Navigator(baseName);
			nav.setScreenForLayer(parent);
			this.handler = handler;
			this.baseName = baseName;
			this.parent = parent;
			this.multiplayer = multiplayer;
		}
		
		@Override
		public void init() {
			multi = shouldCreatePick();
			if(multi) {
				button = new CarbonButton(0, 0, 82, 20, new TranslationTextComponent("gui.carbonconfig.pick_file"), this::onPick);
				children.add(button);
			}
			else {
				button = new CarbonButton(0, 0, 60, 20, new TranslationTextComponent("gui.carbonconfig.modify"), this::onEdit);
				reset = new CarbonIconButton(0, 0, 20, 20, Icon.REVERT, new StringTextComponent(""), this::reset).setIconOnly();
				reset.active = !handler.isDefault() && !isInWorldConfig();
				children.add(button);
				children.add(reset);
			}
			type = new TranslationTextComponent("gui.carbonconfig.type."+handler.getConfigType().name().toLowerCase());
			fileName = new StringTextComponent(handler.getFileName()).applyTextStyle(TextFormatting.GRAY);
		}
		
		@Override
		public void render(int x, int top, int left, int width, int height, int mouseX, int mouseY, boolean selected, float partialTicks) {
			button.x = left+width-82;
			button.y = top + 2;
			button.render(mouseX, mouseY, partialTicks);
			if(reset != null) {
				reset.x = left+width-20;
				reset.y = top + 2;
				reset.render(mouseX, mouseY, partialTicks);
			}
			GuiUtils.drawScrollingString(font, type, left+5, top, 130, 10, GuiAlign.LEFT, -1, 0);
			GuiUtils.drawScrollingString(font, fileName, left+5, top+9, 130, 10, GuiAlign.LEFT, -1, 0);
			GuiUtils.drawTextureRegion(left-20, top, 22, 22, getIcon(), 16, 16);
		}
		
		@Override
		public List<? extends IGuiEventListener> children() {
			return children;
		}
		
		private boolean shouldCreatePick() {
			return handler.isDynamicConfig() && !isInWorldConfig();
		}
		
		private boolean isInWorldConfig() {
			return mc.world != null && (handler.getConfigType() == ConfigType.SERVER || (handler.getConfigType() == ConfigType.SHARED && multiplayer));
		}
		
		private Icon getIcon() {
			return (multi ? Icon.MULTITYPE_ICON : Icon.TYPE_ICON).get(handler.getConfigType());
		}
		
		private void onPick(Button button) {
			mc.displayGuiScreen(new SelectFileScreen(baseName, owner.getCustomTexture(), parent, handler));
		}
		
		private void reset(CarbonIconButton button) {
			handler.restoreDefault();
			handler.save();
			reset.active = !handler.isDefault();
		}
		
		private void onEdit(Button button) {
			if(isInWorldConfig() && !mc.isIntegratedServerRunning()) mc.displayGuiScreen(new RequestScreen(owner.getCustomTexture(), nav.add(type), parent, handler));
			else mc.displayGuiScreen(new ConfigScreen(nav.add(type), handler, parent, owner.getCustomTexture()));
		}
	}
}