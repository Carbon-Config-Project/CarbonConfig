package carbonconfiglib.gui.screens;


import java.util.List;
import java.util.Optional;

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
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.network.chat.Component;
import speiger.src.collections.objects.lists.ObjectArrayList;
import speiger.src.collections.objects.utils.ObjectLists;

public class ConfigListScreen extends BaseCarbonScreen 
{
	Screen parent;
	BackgroundHolder holder;
	ListState<Element> listState = new ListState<Element>().setItemHeight(26);
	TextState searchState = new TextState().setSuggestion("Search...").setCallback(listState::search);
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
		this.header = configs.size() > 1 ? Component.literal("Mod List") : Component.literal(configs.get(0).getModName());
	}
	
	@Override
	protected void init() {
		super.init();
		int searchWidth = (int)(width * 0.3F);
		int minX = (int)(width * 0.15F);
		int maxX = (int)(width * 0.8F) - minX;
		int minY = (int)(height * 0.15F);
		int maxY = (int)(height * 0.8F) - minY;
		listArea(minX, minY, maxX, maxY, listState);
		text(-(searchWidth >> 1), minY - 20, searchWidth, 16, Align.CENTER, Align.START, searchState);
		button(-80, -35, 160, 20, Align.CENTER, Align.END, Component.translatable("gui.carbonconfig.back"), T -> onClose());
	}
	
	@Override
	public void renderBackground(PoseStack matrix, int mouseX, int mouseY, float partialTicks) {
		GuiUtils.renderBackground(0, width, 0, height, 0F, holder.getTexture());
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
		config.getConfigInstances(ConfigType.SERVER).forEach(T -> serverConfigs.add(new ConfigEntry(T, holder, name, true)));
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
		public boolean containsSearch(String searchString) { return false; }
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
			return 16;
		}

		@Override
		public boolean containsSearch(String searchString) {
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
		
		public ConfigEntry(IModConfig config, BackgroundHolder holder, Component modName, boolean multiplayer) {
			this.config = config;
			this.holder = holder;
			this.modName = modName;
			this.multiplayer = multiplayer;
			this.type = Component.translatable("gui.carbonconfig.type."+config.getConfigType().name().toLowerCase());
			this.fileName = Component.literal(config.getConfigName()).withStyle(ChatFormatting.GRAY);
			this.open = addChild(new CarbonButton(0, 0, 50, 20, Component.translatable("gui.carbonconfig."+(shouldCreatePick() ? "pick_file" : "modify")), T -> open()));
			if(!shouldCreatePick()) this.reset = addChild(new CarbonButton(0, 0, 20, 20, Component.empty(), T -> resetConfig()).withIcon(Optional.of(Icon.REVERT)).withTooltip(Component.translatable("gui.carbonconfig.default")));
		}
		
		@Override
		public boolean containsSearch(String searchString) {
			return config.getFileName().contains(searchString);
		}

		@Override
		public void render(PoseStack poseStack, int x, int top, int left, int width, int height, int mouseX, int mouseY, boolean selected, float partialTicks) {
			GuiUtils.drawTextureRegion(poseStack, left, top, 22, 22, getIcon(), 16, 16);
			GuiUtils.drawText(poseStack, font, type, left+25, top+3, Align.START, -1);
			GuiUtils.drawText(poseStack, font, fileName, left+25, top+12, Align.START, -1);
			int right = left + width;
			open.x = right - 71;
			open.y = (int)Align.CENTER.alignStart(top, height, open.getHeight());
			fixFocus(open);
			open.render(poseStack, mouseX, mouseY, partialTicks);
			if(reset != null) {
				reset.x = right - 20;
				reset.y = (int)Align.CENTER.alignStart(top, height, reset.getHeight());
				reset.active = !config.isDefault();
				fixFocus(reset);
				reset.render(poseStack, mouseX, mouseY, partialTicks);
			}
		}
		
		private void fixFocus(AbstractWidget widget) {
			if(widget != null && widget.isFocused()) widget.changeFocus(false);
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
			config.restoreDefault();
			config.save();
		}
	}
}
