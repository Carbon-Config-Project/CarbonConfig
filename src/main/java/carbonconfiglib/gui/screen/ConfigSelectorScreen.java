package carbonconfiglib.gui.screen;

import java.util.List;
import java.util.function.Consumer;

import com.mojang.blaze3d.vertex.PoseStack;

import carbonconfiglib.CarbonConfig;
import carbonconfiglib.api.ConfigType;
import carbonconfiglib.gui.api.BackgroundTexture;
import carbonconfiglib.gui.api.IModConfig;
import carbonconfiglib.gui.api.IModConfigs;
import carbonconfiglib.gui.config.ConfigElement.GuiAlign;
import carbonconfiglib.gui.config.Element;
import carbonconfiglib.gui.config.IIgnoreSearch;
import carbonconfiglib.gui.config.ListScreen;
import carbonconfiglib.gui.widgets.CarbonIconButton;
import carbonconfiglib.gui.widgets.GuiUtils;
import carbonconfiglib.gui.widgets.Icon;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.locale.Language;
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
public class ConfigSelectorScreen extends ListScreen
{
	IModConfigs configs;
	Screen parent;
	Component modName;
	Label toAdd;
	
	public ConfigSelectorScreen(IModConfigs configs, Screen parent) {
		this(configs, configs.getBackground(), parent);
	}
	
	public ConfigSelectorScreen(IModConfigs configs, BackgroundTexture customTexture, Screen parent) {
		super(Component.translatable("gui.carbonconfig.select_config"), customTexture);
		this.configs = configs;
		this.parent = parent;
		modName = Component.literal(configs.getModName());
	}
	
	@Override
	protected void init() {
		super.init();
		int x = width / 2;
		int y = height;
		addRenderableWidget(new ExtendedButton(x-80, y-27, 160, 20, Component.translatable("gui.carbonconfig.back"), T -> onClose()));
	}
	
	@Override
	public void render(PoseStack stack, int mouseX, int mouseY, float partialTicks) {
		super.render(stack, mouseX, mouseY, partialTicks);
		font.draw(stack, modName, (width/2)-(font.width(modName)/2), 8, -1);
	}
	
	@Override
	protected void collectElements(Consumer<Element> elements) {
		toAdd = new Label(Component.translatable("gui.carbonconfig.configs.local").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
		addConfigs(ConfigType.CLIENT, false, elements);
		addConfigs(ConfigType.SHARED, false, elements);
		toAdd = null;
		if(minecraft.level != null) {
			if(!minecraft.hasSingleplayerServer()) {
				if(!isInstalledOnServer()) {
					return;
				}
				if(isLanServer()) {
					return;
				}
				if(!minecraft.player.hasPermissions(4)) {
					return;
				}
				toAdd = new Label(Component.translatable("gui.carbonconfig.configs.multiplayer").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
				addConfigs(ConfigType.SHARED, true, elements);
			}
			else  {
				toAdd = new Label(Component.translatable("gui.carbonconfig.configs.world").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
			}
		}
		else {
			toAdd = new Label(Component.translatable("gui.carbonconfig.configs.world").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
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
		minecraft.setScreen(parent);
	}
	
	private boolean isInstalledOnServer() {
		return CarbonConfig.NETWORK.isInstalledOnServer(minecraft.player);
	}
	
	private boolean isLanServer() {
		ServerData data = minecraft.getCurrentServer();
		return data != null && data.isLan();
	}
	
	public static class Label extends Element implements IIgnoreSearch {
		public Label(Component name) {
			super(name);
		}

		@Override
		public boolean shouldIgnoreSearch() {
			return true;
		}
		
		@Override
		public void render(PoseStack poseStack, int x, int top, int left, int width, int height, int mouseX, int mouseY, boolean selected, float partialTicks) {
			renderText(poseStack, name, GuiAlign.CENTER.align(left, width, font.width(name)), GuiAlign.CENTER.align(top, height, font.lineHeight), -1);
		}
	}
	
	public static class DirectConfig extends Element {
		List<GuiEventListener> children = new ObjectArrayList<>();
		Screen parent;
		IModConfig handler;
		Button button;
		CarbonIconButton reset;
		boolean multi;
		boolean multiplayer;
		Component type;
		Component fileName;
		Component baseName;
		
		public DirectConfig(IModConfig handler, Component baseName, Screen parent, boolean multiplayer) {
			super(Component.literal(handler.getFileName()));
			this.handler = handler;
			this.baseName = baseName;
			this.parent = parent;
			this.multiplayer = multiplayer;
		}
		
		@Override
		public void init() {
			multi = shouldCreatePick();
			if(multi) {
				button = new ExtendedButton(0, 0, 82, 20, Component.translatable("gui.carbonconfig.pick_file"), this::onPick);
				children.add(button);
			}
			else {
				button = new ExtendedButton(0, 0, 60, 20, Component.translatable("gui.carbonconfig.modify"), this::onEdit);
				reset = new CarbonIconButton(0, 0, 20, 20, Icon.REVERT, Component.empty(), this::reset).setIconOnly();
				reset.active = !handler.isDefault() && !isInWorldConfig();
				children.add(button);
				children.add(reset);
			}
			type = Component.translatable("gui.carbonconfig.type."+handler.getConfigType().name().toLowerCase());
			fileName = Component.literal(handler.getFileName()).withStyle(ChatFormatting.GRAY);
		}
		
		@Override
		public void render(PoseStack poseStack, int x, int top, int left, int width, int height, int mouseX, int mouseY, boolean selected, float partialTicks) {
			button.x = left+width-82;
			button.y = top + 2;
			button.render(poseStack, mouseX, mouseY, partialTicks);
			if(reset != null) {
				reset.x = left+width-20;
				reset.y = top + 2;
				reset.render(poseStack, mouseX, mouseY, partialTicks);
			}
			
			font.draw(poseStack, Language.getInstance().getVisualOrder(GuiUtils.ellipsize(type, 130, font)), left+5, top+2, -1);
			font.draw(poseStack, Language.getInstance().getVisualOrder(GuiUtils.ellipsize(fileName, 130, font)), left+5, top+11, -1);
			GuiUtils.drawTextureRegion(poseStack, left-20, top, 22, 22, getIcon(), 16, 16);
		}
		
		@Override
		public List<? extends GuiEventListener> children() {
			return children;
		}
		
		private boolean shouldCreatePick() {
			return handler.isDynamicConfig() && !isInWorldConfig();
		}
		
		private boolean isInWorldConfig() {
			return mc.level != null && (handler.getConfigType() == ConfigType.SERVER || (handler.getConfigType() == ConfigType.SHARED && multiplayer));
		}
		
		private Icon getIcon() {
			return (multi ? Icon.MULTITYPE_ICON : Icon.TYPE_ICON).get(handler.getConfigType());
		}
		
		private void onPick(Button button) {
			mc.setScreen(new SelectFileScreen(baseName, owner.getCustomTexture(), parent, handler));
		}
		
		private void reset(CarbonIconButton button) {
			handler.restoreDefault();
			handler.save();
			reset.active = !handler.isDefault();
		}
		
		private void onEdit(Button button) {
			if(isInWorldConfig() && !mc.hasSingleplayerServer()) mc.setScreen(new RequestScreen(owner.getCustomTexture(), parent, handler));
			else mc.setScreen(new ConfigScreen(baseName.copy().append(Component.literal(" > ").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD)).append(type), handler, parent, owner.getCustomTexture()));
		}
	}
}