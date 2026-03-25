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
import carbonconfiglib.gui.base.helpers.Texts;
import carbonconfiglib.gui.base.screen.BaseCarbonScreen;
import carbonconfiglib.gui.base.widgets.CarbonButton;
import carbonconfiglib.gui.base.widgets.CarbonList.ListEntry;
import carbonconfiglib.gui.base.widgets.CarbonList.ListState;
import carbonconfiglib.impl.internal.BackupManager;
import carbonconfiglib.impl.internal.BackupManager.BackupEntry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.toasts.SystemToast;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;

public class BackupSelectionScreen extends BaseCarbonScreen
{
	private static final String[] DATA_TYPES = new String[]{"B", "KB", "MB", "GB", "TB", "PB"};
	private static final DecimalFormat FORMAT = new DecimalFormat("###,###.###", DecimalFormatSymbols.getInstance(Locale.ROOT));
	public static final DecimalFormat DATE_FORMAT = new DecimalFormat("#.##", DecimalFormatSymbols.getInstance(Locale.ROOT));
	ListState<BackupListEntry> listState = new ListState<>();
	BackgroundHolder holder;
	ITextComponent header;
	GuiScreen parent;

	public BackupSelectionScreen(GuiScreen parent, BackgroundHolder holder, IModConfig config, List<BackupEntry> entries) {
		this.parent = parent;
		this.holder = holder;
		
		header = Texts.literal(Optional.ofNullable(Loader.instance().getIndexedModList().get(config.getModId())).map(ModContainer::getName).orElse(config.getModId())).appendText(" -> ").appendText(config.getConfigName());
		for(BackupEntry target : entries) {
			listState.add(new BackupListEntry(config, target, this));
		}
		listState.sort(Comparator.comparing(BackupListEntry::getEntry, Comparator.reverseOrder()));
	}

	@Override
	public void initGui() {
		super.initGui();
		int minX = (int)(width * 0.15F);
		int maxX = (int)(width * 0.8F) - minX;
		int minY = (int)(height * 0.15F);
		int maxY = (int)(height * 0.8F) - minY;
		modlogo(2, 2, minY - 4, minY - 4);
		listArea(minX, minY, maxX, maxY, listState);
		button(-80, -35, 160, 20, Align.CENTER, Align.END, Texts.translatable("gui.carbonconfig.back"), T -> onClose());
	}
	
	@Override
	public void renderBackground(int mouseX, int mouseY, float partialTicks) {
		if(!holder.shouldDisableInLevel() || mc.world == null) GuiUtils.renderBackground(0, width, 0, height, 0F, holder.getTexture());
		GuiUtils.renderListOverlay(0, width, (int)(height * 0.15F), (int)(height * 0.8F), width, height, holder.getTexture());
	}
	
	@Override
	public void renderForeground(int mouseX, int mouseY, float partialTicks) {
		GuiUtils.renderListShadow(0, width, (int)(height * 0.15F), (int)(height * 0.8F), width, height);
		GuiUtils.drawScrollingShadowText(fontRenderer, header, 0, 0, width, (int)(height * 0.15F)-20, Align.CENTER, -1, 0);
		GuiUtils.drawScrollingShadowText(fontRenderer, Texts.translatable("gui.carbonconfig.backup.header"), 0, 0, width, (int)(height * 0.15F)+20, Align.CENTER, -1, 0);
	}
	
	@Override
	public void onClose() {
		pushScreen(parent);
	}
	
	public static class BackupListEntry extends ListEntry<BackupListEntry> {
		BackupSelectionScreen owner;
		IModConfig config;
		BackupEntry entry;
		CarbonButton load = addChild(new CarbonButton(0, 0, 18, 18, Texts.empty(), T -> load()).withIcon(Optional.of(Icon.EXPORT)).withTooltip(Texts.translatable("gui.carbonconfig.pick")));
		CarbonButton delete = addChild(new CarbonButton(0, 0, 18, 18, Texts.empty(), T -> delete()).withIcon(Optional.of(Icon.DELETE)).withTooltip(Texts.translatable("gui.carbonconfig.delete")));
		
		public BackupListEntry(IModConfig config, BackupEntry entry, BackupSelectionScreen owner) {
			this.config = config;
			this.entry = entry;
			this.owner = owner;
		}
		
		public BackupEntry getEntry() {
			return entry;
		}
		
		@Override
		public int getItemHeight() {
			return 34;
		}
		
		private void delete() {
			BackupManager.deleteBackup(config, entry);
			owner.listState.remove(this);
			owner.listState.updateSearch();
		}
		
		private void load() {
			BackupManager.loadBackup(config, entry);
			if(CarbonConfig.BACKUP_TOASTS.get()) Minecraft.getMinecraft().getToastGui().add(new SystemToast(SystemToast.Type.TUTORIAL_HINT, Texts.translatable("gui.carbonconfig.toast.load"), Texts.translatable("gui.carbonconfig.toast.load.desc")));
			owner.onClose();
		}
		
		@Override
		protected boolean containsSearch(String searchString) {
			return false;
		}
		
		@Override
		public void render(int x, int top, int left, int width, int height, int mouseX, int mouseY, boolean selected, float partialTicks) {
			GuiUtils.drawTextureRegion(left, Align.CENTER.alignStart(top, height, 22), 22, 22, Icon.TYPE_ICON.get(config.getConfigType()), 16, 16);
			GuiUtils.drawText(font, Texts.translatable("gui.carbonconfig.backup.entry", entry.getFileName()), left+25, top+2, Align.START, -1);
			
			GuiUtils.drawText(font, Texts.translatable("gui.carbonconfig.backup.created", createDuration(Duration.between(entry.created(), LocalDateTime.now()))).setStyle(Texts.applyStyle(TextFormatting.GRAY)), left+25, top+font.FONT_HEIGHT+2, Align.START, -1);
			GuiUtils.drawText(font, Texts.translatable("gui.carbonconfig.backup.size", findBestMemory(entry.fileSize(), 1000), findBestMemory(entry.compressedSize(), 1000), ((int)(entry.getRatio()*100))+"%").setStyle(Texts.applyStyle(TextFormatting.DARK_AQUA)), left+25, top+font.FONT_HEIGHT*2+2, Align.START, -1);
			
			int right = left + width;
			load.x = right - 39;
			load.y = (int)Align.CENTER.alignStart(top, height, 18);
			load.render(mouseX, mouseY, partialTicks);
			delete.x = right - 20;
			delete.y = (int)Align.CENTER.alignStart(top, height, 18);
			delete.render(mouseX, mouseY, partialTicks);
		}
		
		private ITextComponent createDuration(Duration duration) {
			if(duration.getSeconds() < 2) return Texts.translatable("gui.carbonconfig.ago.seconds", duration.getSeconds());
			if(duration.toMinutes() < 100) return Texts.translatable("gui.carbonconfig.ago.minutes", duration.toMinutes());
			if(duration.toHours() < 100) return Texts.translatable("gui.carbonconfig.ago.hours", duration.toHours());
			if(duration.toDays() < 40) return Texts.translatable("gui.carbonconfig.ago.days", DATE_FORMAT.format(duration.toHours() / 24D));
			if(duration.toDays() < 365) return Texts.translatable("gui.carbonconfig.ago.months", DATE_FORMAT.format(duration.toDays() / 30D));
			return Texts.translatable("gui.carbonconfig.ago.years", DATE_FORMAT.format(duration.toDays() / 365D));
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
