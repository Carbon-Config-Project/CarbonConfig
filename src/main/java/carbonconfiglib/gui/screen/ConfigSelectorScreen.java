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
import carbonconfiglib.gui.widgets.screen.IInteractable;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;
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
public class ConfigSelectorScreen extends ListScreen
{
	IModConfigs configs;
	GuiScreen parent;
	IChatComponent modName;
	Label toAdd;
	
	public ConfigSelectorScreen(IModConfigs configs, GuiScreen parent) {
		this(configs, configs.getBackground(), parent);
	}
	
	public ConfigSelectorScreen(IModConfigs configs, BackgroundHolder customTexture, GuiScreen parent) {
		super(new ChatComponentTranslation("gui.carbonconfig.select_config"), customTexture);
		this.configs = configs;
		this.parent = parent;
		modName = new ChatComponentText(configs.getModName());
	}
	
	@Override
	public void initGui() {
		super.initGui();
		int x = width / 2;
		int y = height;
		addWidget(new CarbonButton(x-80, y-27, 160, 20, I18n.format("gui.carbonconfig.back"), T -> onClose()));
	}
	
	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		super.drawScreen(mouseX, mouseY, partialTicks);
		String modName = this.modName.getFormattedText();
		fontRendererObj.drawString(modName, (width/2)-(fontRendererObj.getStringWidth(modName)/2), 8, -1);
	}
	
	@Override
	protected void collectElements(Consumer<Element> elements) {
		toAdd = new Label(new ChatComponentTranslation("gui.carbonconfig.configs.local").setChatStyle(new ChatStyle().setColor(EnumChatFormatting.GOLD).setBold(true)));
		addConfigs(ConfigType.CLIENT, false, elements);
		addConfigs(ConfigType.SHARED, false, elements);
		toAdd = null;
		if(mc.theWorld != null) {
			if(!mc.isIntegratedServerRunning()) {
				if(!isInstalledOnServer()) {
					return;
				}
				if(isLanServer()) {
					return;
				}
				if(!CarbonConfig.NETWORK.hasPermissions()) {
					return;
				}
				toAdd = new Label(new ChatComponentTranslation("gui.carbonconfig.configs.multiplayer").setChatStyle(new ChatStyle().setColor(EnumChatFormatting.GOLD).setBold(true)));
				addConfigs(ConfigType.SHARED, true, elements);
			}
			else  {
				toAdd = new Label(new ChatComponentTranslation("gui.carbonconfig.configs.world").setChatStyle(new ChatStyle().setColor(EnumChatFormatting.GOLD).setBold(true)));
			}
		}
		else {
			toAdd = new Label(new ChatComponentTranslation("gui.carbonconfig.configs.world").setChatStyle(new ChatStyle().setColor(EnumChatFormatting.GOLD).setBold(true)));
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
		mc.displayGuiScreen(parent);
	}
	
	private boolean isInstalledOnServer() {
		return CarbonConfig.NETWORK.isInstalledOnServer();
	}
	
	private boolean isLanServer() {
		ServerData data = mc.getCurrentServerData();
		return data != null && data.func_181041_d();
	}
	
	public static class Label extends Element implements IIgnoreSearch {
		public Label(IChatComponent name) {
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
		List<IInteractable> children = new ObjectArrayList<>();
		GuiScreen parent;
		IModConfig handler;
		CarbonButton button;
		CarbonIconButton reset;
		boolean multi;
		boolean multiplayer;
		Navigator nav;
		IChatComponent type;
		IChatComponent fileName;
		IChatComponent baseName;
		
		public DirectConfig(IModConfig handler, IChatComponent baseName, GuiScreen parent, boolean multiplayer) {
			super(new ChatComponentText(handler.getFileName()));
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
				button = new CarbonButton(0, 0, 82, 20, I18n.format("gui.carbonconfig.pick_file"), this::onPick);
				children.add(button);
			}
			else {
				button = new CarbonButton(0, 0, 60, 20, I18n.format("gui.carbonconfig.modify"), this::onEdit);
				reset = new CarbonIconButton(0, 0, 20, 20, Icon.REVERT, "", this::reset).setIconOnly();
				reset.enabled = !handler.isDefault() && !isInWorldConfig();
				children.add(button);
				children.add(reset);
			}
			type = new ChatComponentTranslation("gui.carbonconfig.type."+handler.getConfigType().name().toLowerCase());
			fileName = new ChatComponentText(handler.getFileName()).setChatStyle(new ChatStyle().setColor(EnumChatFormatting.GRAY));
		}
		
		@Override
		public void render(int x, int top, int left, int width, int height, int mouseX, int mouseY, boolean selected, float partialTicks) {
			button.xPosition = left+width-82;
			button.yPosition = top + 2;
			button.render(mc, mouseX, mouseY, partialTicks);
			if(reset != null) {
				reset.xPosition = left+width-20;
				reset.yPosition = top + 2;
				reset.render(mc, mouseX, mouseY, partialTicks);
			}
			GuiUtils.drawScrollingString(font, type.getFormattedText(), left+5, top, 130, 10, GuiAlign.LEFT, -1, 0);
			GuiUtils.drawScrollingString(font, fileName.getFormattedText(), left+5, top+9, 130, 10, GuiAlign.LEFT, -1, 0);
			GuiUtils.drawTextureRegion(left-20, top, 22, 22, getIcon(), 16, 16);
		}
		
		@Override
		public List<? extends IInteractable> children() {
			return children;
		}
		
		private boolean shouldCreatePick() {
			return handler.isDynamicConfig() && !isInWorldConfig();
		}
		
		private boolean isInWorldConfig() {
			return mc.theWorld != null && (handler.getConfigType() == ConfigType.SERVER || (handler.getConfigType() == ConfigType.SHARED && multiplayer));
		}
		
		private Icon getIcon() {
			return (multi ? Icon.MULTITYPE_ICON : Icon.TYPE_ICON).get(handler.getConfigType());
		}
		
		private void onPick(GuiButton button) {
			mc.displayGuiScreen(new SelectFileScreen(baseName, owner.getCustomTexture(), parent, handler));
		}
		
		private void reset(CarbonIconButton button) {
			handler.restoreDefault();
			handler.save();
			reset.enabled = !handler.isDefault();
		}
		
		private void onEdit(GuiButton button) {
			if(isInWorldConfig() && !mc.isIntegratedServerRunning()) mc.displayGuiScreen(new RequestScreen(owner.getCustomTexture(), nav.add(type), parent, handler));
			else mc.displayGuiScreen(new ConfigScreen(nav.add(type), handler, parent, owner.getCustomTexture()));
		}
	}
}