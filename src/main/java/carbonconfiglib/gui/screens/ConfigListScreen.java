package carbonconfiglib.gui.screens;


import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.OptionalInt;

import com.mojang.blaze3d.vertex.PoseStack;

import carbonconfiglib.CarbonConfig;
import carbonconfiglib.api.ConfigType;
import carbonconfiglib.gui.api.IModConfig;
import carbonconfiglib.gui.api.IModConfigs;
import carbonconfiglib.gui.api.background.BackgroundTexture.BackgroundHolder;
import carbonconfiglib.gui.base.helpers.Align;
import carbonconfiglib.gui.base.helpers.GuiUtils;
import carbonconfiglib.gui.base.helpers.Icon;
import carbonconfiglib.gui.base.screen.BaseCarbonScreen;
import carbonconfiglib.gui.base.widgets.CarbonButton;
import carbonconfiglib.gui.base.widgets.CarbonEditBox.TextState;
import carbonconfiglib.gui.base.widgets.CarbonList.ListEntry;
import carbonconfiglib.gui.base.widgets.CarbonList.ListState;
import carbonconfiglib.impl.internal.BackupManager;
import carbonconfiglib.impl.internal.BackupManager.BulkRequest;
import carbonconfiglib.impl.internal.BackupManager.Mode;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.components.toasts.SystemToast.SystemToastIds;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import speiger.src.collections.objects.lists.ObjectArrayList;
import speiger.src.collections.objects.utils.ObjectLists;

/**
 * Copyright 2026 Speiger, Meduris
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
public class ConfigListScreen extends BaseCarbonScreen 
{
	Screen parent;
	BackgroundHolder holder;
	ListState<Element> listState = new ListState<Element>().setItemHeight(26);
	TextState searchState = new TextState().setSuggestion(I18n.get("gui.carbonconfig.search")).setCallback(listState::search);
	Component header;
	
	public ConfigListScreen(Screen parent, IModConfigs configs) {
		this(parent, configs.getBackground(), configs);
	}
	
	public ConfigListScreen(Screen parent, BackgroundHolder holder, IModConfigs configs) {
		this(parent, holder, ObjectLists.singleton(configs));
	}
	
	public ConfigListScreen(Screen parent, BackgroundHolder holder, List<IModConfigs> configs) {
		this.parent = parent;
		this.holder = holder;
		this.listState.add(generateModList(configs));
		this.header = configs.size() > 1 ? Component.translatable("gui.carbonconfig.modlist") : Component.literal(configs.get(0).getModName());
	}
	
	@Override
	protected void init() {
		super.init();
		int searchWidth = (int)(width * 0.3F);
		int minX = (int)(width * 0.5F) - 180;
		int maxX = 360;
		int minY = (int)(height * 0.15F);
		int maxY = (int)(height * 0.8F) - minY;
		modlogo(2, 2, minY - 4, minY - 4);
		listArea(minX, minY, maxX, maxY, listState);
		text(-(searchWidth >> 1), minY - 20, searchWidth, 16, Align.CENTER, Align.START, searchState);
		button(-80, -35, 160, 20, Align.CENTER, Align.END, Component.translatable("gui.carbonconfig.back"), T -> onClose());
		iconButton((searchWidth >> 1)+2, minY-21, 18, 18, Align.CENTER, Align.START, Icon.IMPORT, T -> bulkBackup(Mode.CREATE)).withTooltip(Component.translatable("gui.carbonconfig.backup.bulk.create"));
		iconButton((searchWidth >> 1)+22, minY-21, 18, 18, Align.CENTER, Align.START, Icon.EXPORT, T -> bulkBackup(Mode.LOAD)).withTooltip(Component.translatable("gui.carbonconfig.backup.bulk.load"));
		listState.forEach(T -> {
			if(T instanceof ConfigEntry) ((ConfigEntry)T).updateBackup();
		});
	}
	
	@Override
	public void renderBackground(PoseStack matrix, int mouseX, int mouseY, float partialTicks) {
		if(!holder.shouldDisableInLevel() || minecraft.level == null) GuiUtils.renderBackground(0, width, 0, height, 0F, holder.getTexture());
		GuiUtils.renderListOverlay(0, width, (int)(height * 0.15F), (int)(height * 0.8F), width, height, holder.getTexture());
	}
	
	@Override
	public void renderForeground(PoseStack matrix, int mouseX, int mouseY, float partialTicks) {
		GuiUtils.renderListShadow(0, width, (int)(height * 0.15F), (int)(height * 0.8F), width, height);
		GuiUtils.drawScrollingShadowText(matrix, font, header, 0, 0, width, (int)(height * 0.15F)-20, Align.CENTER, -1, 0);
	}
	
	@Override
	public void onClose() {
		setScreen(parent);
	}
	
	protected void bulkBackup(Mode mode) {
		List<IModConfig> configs = new ObjectArrayList<>();
		listState.forEach(T -> {
			if(T instanceof ConfigEntry) {
				((ConfigEntry)T).handleBulk(configs, mode);
			}
		});
		if(configs.isEmpty()) return;
		BulkRequest request = new BulkRequest(configs, mode);
		listState.forEach(T -> {
			if(T instanceof ConfigEntry) {
				((ConfigEntry)T).pendingRequest = request;
			}
		});
	}
	
	protected List<Element> generateModList(List<IModConfigs> configs) {
		List<Element> element = new ObjectArrayList<>();
		for(IModConfigs config : configs) {
			List<Element> modConfig = createElements(config);
			if(configs.size() > 1) {
				element.add(new Label(Component.literal(config.getModName()).withStyle(ChatFormatting.BOLD, ChatFormatting.RED)).withChildren(modConfig));
			}
			element.addAll(modConfig);
		}
		return element;
	}
	
	protected List<Element> createElements(IModConfigs config) {
		List<Element> result = new ObjectArrayList<>();
		List<Element> local = new ObjectArrayList<>();
		Component name = Component.literal(config.getModName());
		config.getConfigInstances(ConfigType.CLIENT).forEach(T -> local.add(new ConfigEntry(T, holder, name, false)));
		config.getConfigInstances(ConfigType.SHARED).forEach(T -> local.add(new ConfigEntry(T, holder, name, false)));
		if(local.size() > 0) {
			result.add(new Label(Component.translatable("gui.carbonconfig.configs.local").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD)).withChildren(local));
			result.addAll(local);
		}
		
		List<Element> serverConfigs = new ObjectArrayList<>();
		Minecraft mc = Minecraft.getInstance();
		if(mc.level != null && isMultiplayer()) {
			config.getConfigInstances(ConfigType.SHARED).forEach(T -> serverConfigs.add(new ConfigEntry(T, holder, name, true)));
		}
		if(mc.level == null || mc.hasSingleplayerServer() || isMultiplayer()) {
			config.getConfigInstances(ConfigType.SERVER).forEach(T -> serverConfigs.add(new ConfigEntry(T, holder, name, true)));
		}
		if(serverConfigs.size() > 0) {
			result.add(new Label(Component.translatable("gui.carbonconfig.configs."+(mc.level == null || !isMultiplayer() ? "world" : "multiplayer")).withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD)).withChildren(serverConfigs));
			result.addAll(serverConfigs);
		}
		return result;
	}
	
	private static boolean isMultiplayer() {
		Minecraft mc = Minecraft.getInstance();
		return !mc.hasSingleplayerServer() && CarbonConfig.NETWORK.isInstalledOnServer() && !isLanServer() && mc.player.hasPermissions(4);
	}
	
	private static boolean isLanServer() {
		ServerData data = Minecraft.getInstance().getCurrentServer();
		return data != null && data.isLan();
	}
	
	public static abstract class Element extends ListEntry<Element> {
		@Override
		protected boolean containsSearch(String searchString) { return false; }
	}
	
	public static class Label extends Element {
		Component label;
		List<Element> children;
		
		public Label(Component label) {
			this.label = label;
		}
		
		public Label withChildren(List<Element> children) {
			this.children = children;
			return this;
		}
		
		@Override
		public int getItemHeight() {
			return 20;
		}

		@Override
		protected boolean containsSearch(String searchString) {
			if(children != null) {
				for(int i = 0,m=children.size();i<m;i++) {
					if(children.get(i).containsSearch(searchString)) return true;
				}
			}
			return false;
		}
		
		@Override
		public void render(PoseStack poseStack, int x, int top, int left, int width, int height, int mouseX, int mouseY, boolean selected, float partialTicks) {			
			GuiUtils.drawScrollingText(poseStack, font, label, left, top, width, height, Align.CENTER, -1, 0);
		}
	}
		
	public static class ConfigEntry extends Element {
		protected BackgroundHolder holder;
		protected IModConfig config;
		protected Component type;
		protected Component fileName;
		protected Component modName;
		protected boolean multiplayer;
		protected CarbonButton open;
		protected CarbonButton reset;
		protected CarbonButton backup;
		protected CarbonButton restore;
		protected CarbonButton listBackups;
		protected BulkRequest pendingRequest;
		protected OptionalInt hasBackups = OptionalInt.empty();
		
		public ConfigEntry(IModConfig config, BackgroundHolder holder, Component modName, boolean multiplayer) {
			this.config = config;
			this.holder = holder;
			this.modName = modName;
			this.multiplayer = multiplayer;
			this.type = Component.translatable("gui.carbonconfig.type."+config.getConfigType().name().toLowerCase());
			this.fileName = Component.literal(config.getFileName()).withStyle(ChatFormatting.GRAY);
			boolean isLarge = shouldCreatePick();
			this.open = addChild(new CarbonButton(0, 0, isLarge ? 50 : 40, 20, Component.translatable("gui.carbonconfig."+(shouldCreatePick() ? "pick_file" : "modify")), T -> open()));
			if(!isLarge) {
				this.reset = addChild(new CarbonButton(0, 0, 20, 20, Component.empty(), T -> resetConfig()).withIcon(Optional.of(Icon.REVERT)).setPadding(3).withTooltip(Component.translatable("gui.carbonconfig.reset")));
				this.backup = addChild(new CarbonButton(0, 0, 20, 20, Component.empty(), T -> createBackup()).withIcon(Optional.of(Icon.IMPORT)).setPadding(3).withTooltip(Component.translatable("gui.carbonconfig.backup.create")));
				this.restore = addChild(new CarbonButton(0, 0, 20, 20, Component.empty(), T -> restoreBackup()).withIcon(Optional.of(Icon.EXPORT)).setPadding(3).withTooltip(Component.translatable("gui.carbonconfig.backup.load_last")));
				this.listBackups = addChild(new CarbonButton(0, 0, 20, 20, Component.empty(), T -> selectBackups()).withIcon(Optional.of(Icon.LIST)).setPadding(3).withTooltip(Component.translatable("gui.carbonconfig.backup.select")));
			}
			updateBackup();
		}
		
		private void updateBackup() {
			if(shouldCreatePick()) return;
			this.hasBackups = OptionalInt.of(BackupManager.listBackups(config).size());
		}
		
		@Override
		public int getItemHeight() {
			return super.getItemHeight();
		}
		
		@Override
		public boolean containsSearch(String searchString) {
			return config.getFileName().toLowerCase(Locale.ROOT).contains(searchString.toLowerCase(Locale.ROOT)) || config.getModId().toLowerCase(Locale.ROOT).contains(searchString.toLowerCase(Locale.ROOT));
		}
		
		private boolean isNotRequesting() {
			return pendingRequest == null;
		}
		
		private boolean hasBackups() {
			return !hasBackups.isEmpty() && hasBackups.getAsInt() > 0;
		}
		
		@Override
		public void render(PoseStack poseStack, int x, int top, int left, int width, int height, int mouseX, int mouseY, boolean selected, float partialTicks) {
			if(pendingRequest != null && !pendingRequest.isStillWorking()) {
				pendingRequest = null;
				updateBackup();
			}
			GuiUtils.drawTextureRegion(poseStack, left, top, 22, 22, getIcon(), 16, 16);
			GuiUtils.drawText(poseStack, font, type, left+25, top+3, Align.START, -1);
			GuiUtils.drawText(poseStack, font, fileName, left+25, top+12, Align.START, -1);
			int right = left + width;
			open.setX(right - (70 + (reset != null ? 60 : 0)));
			open.setY((int)Align.CENTER.alignStart(top, height, open.getHeight()));
			open.render(poseStack, mouseX, mouseY, partialTicks);
			if(reset != null) {
				reset.setX(right - 89);
				reset.setY((int)Align.CENTER.alignStart(top, height, reset.getHeight()));
				reset.active = !config.isDefault();
				reset.render(poseStack, mouseX, mouseY, partialTicks);
				
				backup.setX(right - 68);
				backup.setY((int)Align.CENTER.alignStart(top, height, backup.getHeight()));
				backup.active = isNotRequesting();
				backup.render(poseStack, mouseX, mouseY, partialTicks);
				
				restore.setX(right - 47);
				restore.setY((int)Align.CENTER.alignStart(top, height, restore.getHeight()));
				restore.active = isNotRequesting() && hasBackups();
				restore.render(poseStack, mouseX, mouseY, partialTicks);
				
				listBackups.setX(right - 26);
				listBackups.setY((int)Align.CENTER.alignStart(top, height, listBackups.getHeight()));
				listBackups.active = isNotRequesting() && hasBackups();
				listBackups.render(poseStack, mouseX, mouseY, partialTicks);
			}
		}
		
		private boolean shouldCreatePick() {
			return config.isDynamicConfig() && !isInWorldConfig();
		}
		
		private boolean isInWorldConfig() {
			return Minecraft.getInstance().level != null && (config.getConfigType() == ConfigType.SERVER || (config.getConfigType() == ConfigType.SHARED && multiplayer));
		}
		
		public Icon getIcon() {
			return (shouldCreatePick() ? Icon.MULTITYPE_ICON : Icon.TYPE_ICON).get(config.getConfigType());
		}
		
		public void handleBulk(List<IModConfig> download, Mode mode) {
			Minecraft mc = Minecraft.getInstance();
			if(shouldCreatePick()) return;
			if(isInWorldConfig() && !mc.hasSingleplayerServer()) {
				download.add(config);
				return;
			}
			if(mode == Mode.LIST) throw new IllegalStateException("List Mode is unsupported");
			if(mode == Mode.CREATE) BackupManager.createBackup(config);
			if(mode == Mode.LOAD) BackupManager.loadLastBackup(config);
		}
		
		private void createBackup() {
			Minecraft mc = Minecraft.getInstance();
			if(isInWorldConfig() && !mc.hasSingleplayerServer()) {
				pendingRequest = new BulkRequest(ObjectLists.singleton(config), Mode.CREATE);
				return;
			}
			BackupManager.createBackup(config);
			updateBackup();
			if(CarbonConfig.BACKUP_TOASTS.get()) mc.getToasts().addToast(new SystemToast(SystemToastIds.TUTORIAL_HINT, Component.translatable("gui.carbonconfig.toast.create"), Component.translatable("gui.carbonconfig.toast.create.desc")));
		}
		
		private void restoreBackup() {
			Minecraft mc = Minecraft.getInstance();
			if(isInWorldConfig() && !mc.hasSingleplayerServer()) {
				pendingRequest = new BulkRequest(ObjectLists.singleton(config), Mode.LOAD);
				return;
			}
			BackupManager.loadLastBackup(config);
			if(CarbonConfig.BACKUP_TOASTS.get()) mc.getToasts().addToast(new SystemToast(SystemToastIds.TUTORIAL_HINT, Component.translatable("gui.carbonconfig.toast.load"), Component.translatable("gui.carbonconfig.toast.load.desc")));
		}
		
		private void selectBackups() {
			setExternalScreen(new BackupSelectionScreen(Minecraft.getInstance().screen, holder, config, BackupManager.listBackups(config)));
		}
		
		public void open() {
			Minecraft mc = Minecraft.getInstance();
			if(shouldCreatePick()) {
				mc.setScreen(new ConfigSelectScreen(mc.screen, holder, config));
			}
			else if(isInWorldConfig() && !mc.hasSingleplayerServer()) {
				mc.setScreen(new ConfigRequestScreen(holder, mc.screen, config));
			}
			else {
				mc.setScreen(new ConfigScreen(config, holder, mc.screen));				
			}
		}
		
		public void resetConfig() {
			if(Screen.hasShiftDown()) {
				config.restoreDefault();
				config.save(false);
				return;
			}
			Screen parent = Minecraft.getInstance().screen;
			setExternalScreen(new ConfirmScreen(T -> {
				if(T) {
					config.restoreDefault();
					config.save(false);
				}
				setExternalScreen(parent);
			}, 
			Component.translatable("gui.carbonconfig.reset_all.title"), 
			Component.translatable("gui.carbonconfig.reset_all.message"), 
			Component.translatable("gui.carbonconfig.reset_all.default"),
			Component.translatable("gui.carbonconfig.reset_all.cancel")));
		}
	}
}
