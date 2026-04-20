package carbonconfiglib.gui.screens;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import carbonconfiglib.CarbonConfig;
import carbonconfiglib.gui.api.IModConfig;
import carbonconfiglib.gui.api.background.BackgroundTexture.BackgroundHolder;
import carbonconfiglib.gui.base.helpers.Align;
import carbonconfiglib.gui.base.helpers.GuiUtils;
import carbonconfiglib.gui.base.helpers.Icon;
import carbonconfiglib.gui.base.screen.BaseCarbonScreen;
import carbonconfiglib.gui.base.widgets.CarbonButton;
import carbonconfiglib.gui.base.widgets.CarbonList.ListEntry;
import carbonconfiglib.gui.base.widgets.CarbonList.ListState;
import carbonconfiglib.impl.internal.BackupManager;
import carbonconfiglib.impl.internal.BackupManager.BackupEntry;
import carbonconfiglib.impl.internal.BackupManager.BulkRequest;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.neoforgespi.language.IModInfo;

public class BackupSelectionScreen extends BaseCarbonScreen
{
	private static final String[] DATA_TYPES = new String[]{"B", "KB", "MB", "GB", "TB", "PB"};
	private static final DecimalFormat FORMAT = new DecimalFormat("###,###.###", DecimalFormatSymbols.getInstance(Locale.ROOT));
	public static final DecimalFormat DATE_FORMAT = new DecimalFormat("#.##", DecimalFormatSymbols.getInstance(Locale.ROOT));
	ListState<BackupListEntry> listState = new ListState<>();
	BackgroundHolder holder;
	Component header;
	Screen parent;

	public BackupSelectionScreen(Screen parent, BackgroundHolder holder, IModConfig config, List<BackupEntry> entries) {
		this.parent = parent;
		this.holder = holder;
		header = Component.literal(ModList.get().getModContainerById(config.getModId()).map(ModContainer::getModInfo).map(IModInfo::getDisplayName).orElse(config.getModId())).append(" -> ").append(config.getConfigName());
		for(BackupEntry target : entries) {
			listState.add(new BackupListEntry(config, target, this));
		}
		listState.sort(Comparator.comparing(BackupListEntry::getEntry, Comparator.reverseOrder()));
	}

	@Override
	protected void init() {
		super.init();
		int minX = (int)(width * 0.15F);
		int maxX = (int)(width * 0.8F) - minX;
		int minY = (int)(height * 0.15F);
		int maxY = (int)(height * 0.8F) - minY;
		modlogo(2, 2, minY - 4, minY - 4);
		listArea(minX, minY, maxX, maxY, listState);
		button(-80, -35, 160, 20, Align.CENTER, Align.END, Component.translatable("gui.carbonconfig.back"), _ -> onClose());
	}
	
	@Override
	public void drawBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
		if(!holder.shouldDisableInLevel() || minecraft.level == null) GuiUtils.renderBackground(graphics, 0, width, 0, height, 0F, holder.getTexture());
		GuiUtils.renderListOverlay(graphics, 0, width, (int)(height * 0.15F), (int)(height * 0.8F), width, height, holder.getTexture());
	}
	
	@Override
	public void drawForeground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
		GuiUtils.renderListShadow(graphics, 0, width, (int)(height * 0.15F), (int)(height * 0.8F), width, height);
		GuiUtils.drawScrollingShadowText(graphics, font, header, 0, 0, width, (int)(height * 0.15F)-20, Align.CENTER, -1, 0);
		GuiUtils.drawScrollingShadowText(graphics, font, Component.translatable("gui.carbonconfig.backup.header"), 0, 0, width, (int)(height * 0.15F)+20, Align.CENTER, -1, 0);
	}
	
	@Override
	public void onClose() {
		setScreen(parent);
	}
	
	public static class BackupListEntry extends ListEntry<BackupListEntry> {
		BackupSelectionScreen owner;
		IModConfig config;
		BackupEntry entry;
		CarbonButton load = addChild(new CarbonButton(0, 0, 18, 18, Component.empty(), _ -> load()).withIcon(Optional.of(Icon.EXPORT)).withTooltip(Component.translatable("gui.carbonconfig.pick")));
		CarbonButton delete = addChild(new CarbonButton(0, 0, 18, 18, Component.empty(), _ -> delete()).withIcon(Optional.of(Icon.DELETE)).withTooltip(Component.translatable("gui.carbonconfig.delete")));
		
		public BackupListEntry(IModConfig config, BackupEntry entry, BackupSelectionScreen owner) {
			this.config = config;
			this.entry = entry;
			this.owner = owner;
		}
		
		public BackupEntry getEntry() {
			return entry;
		}
		
		@Override
		public int getHeight() {
			return 34;
		}
		
		private void delete() {
			BackupManager.deleteBackup(config, entry);
			owner.listState.remove(this);
			owner.listState.updateSearch();
		}
		
		private void load() {
			BackupManager.loadBackup(config, entry);
			if(CarbonConfig.BACKUP_TOASTS.get()) Minecraft.getInstance().getToastManager().addToast(new SystemToast(BulkRequest.BACKUP_HINT, Component.translatable("gui.carbonconfig.toast.load"), Component.translatable("gui.carbonconfig.toast.load.desc")));
			owner.onClose();
		}
		
		@Override
		protected boolean containsSearch(String searchString) {
			return false;
		}
		
		@Override
		public void extractContent(GuiGraphicsExtractor graphics, int mouseX, int mouseY, boolean selected, float partialTicks) {
			GuiUtils.drawTextureRegion(graphics, getContentX(), Align.CENTER.alignStart(getContentY(), getContentHeight(), 22), 22, 22, Icon.TYPE_ICON.get(config.getConfigType()), 16, 16);
			graphics.text(font, Component.translatable("gui.carbonconfig.backup.entry", entry.getFileName()), getContentX()+25, getContentY()+2, -1);
			
			graphics.text(font, Component.translatable("gui.carbonconfig.backup.created", createDuration(Duration.between(entry.created(), LocalDateTime.now()))).withStyle(ChatFormatting.GRAY), getContentX()+25, getContentY()+font.lineHeight+2, -1);
			graphics.text(font, Component.translatable("gui.carbonconfig.backup.size", findBestMemory(entry.fileSize(), 1000), findBestMemory(entry.compressedSize(), 1000), ((int)(entry.getRatio()*100))+"%").withStyle(ChatFormatting.DARK_AQUA), getContentX()+25, getContentY()+font.lineHeight*2+2, -1);
			
			int right = getContentRight();
			load.setX(right - 39);
			load.setY((int)Align.CENTER.alignStart(getContentY(), getContentHeight(), 18));
			load.extractRenderState(graphics, mouseX, mouseY, partialTicks);
			delete.setX(right - 20);
			delete.setY((int)Align.CENTER.alignStart(getContentY(), getContentHeight(), 18));
			delete.extractRenderState(graphics, mouseX, mouseY, partialTicks);
		}
		
		private Component createDuration(Duration duration) {
			if(duration.toSeconds() < 100) return Component.translatable("gui.carbonconfig.ago.seconds", duration.toSeconds());
			if(duration.toMinutes() < 100) return Component.translatable("gui.carbonconfig.ago.minutes", duration.toMinutes());
			if(duration.toHours() < 100) return Component.translatable("gui.carbonconfig.ago.hours", duration.toHours());
			if(duration.toDays() < 40) return Component.translatable("gui.carbonconfig.ago.days", DATE_FORMAT.format(duration.toHours() / 24D));
			if(duration.toDays() < 365) return Component.translatable("gui.carbonconfig.ago.months", DATE_FORMAT.format(duration.toDays() / 30D));
			return Component.translatable("gui.carbonconfig.ago.years", DATE_FORMAT.format(duration.toDays() / 365D));
		}
		
		static String findBestMemory(long input, int threshold) {
			long actual = Math.abs(input);
			int layersFound = 0;
			for(int i = 0;i<DATA_TYPES.length;i++) {
				if(actual < threshold) {
					break;
				}
				actual >>= 10;
				layersFound++;
			}
			FORMAT.setMaximumFractionDigits(Math.max(0, actual >= 100 ? 0 : (actual >= 10 ? 1 : 2)));
			return FORMAT.format(input * (1D / (1L << (10*layersFound))))+DATA_TYPES[Math.min(DATA_TYPES.length-1, layersFound)];
		}
	}
}
