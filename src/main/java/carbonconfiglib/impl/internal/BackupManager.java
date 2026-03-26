package carbonconfiglib.impl.internal;

import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.AbstractMap;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.lang3.mutable.MutableObject;

import carbonconfiglib.api.ConfigType;
import carbonconfiglib.gui.api.IModConfig;
import carbonconfiglib.gui.api.IRequestReceiver;
import carbonconfiglib.gui.api.background.BackgroundTexture;
import carbonconfiglib.gui.base.screen.BaseCarbonScreen;
import carbonconfiglib.gui.screens.BackupSelectionScreen;
import cpw.mods.fml.common.Loader;
import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraft.server.integrated.IntegratedServer;
import speiger.src.collections.objects.lists.ObjectArrayList;
import speiger.src.collections.objects.maps.impl.hash.Object2ObjectOpenHashMap;
import speiger.src.collections.objects.utils.ObjectLists;
import speiger.src.collections.objects.utils.maps.Object2ObjectMaps;

public class BackupManager {
	private static final char[] ILLEGAL_FILE_CHARACTERS = new char[]{'/', '\n', '\r', '\t', '\u0000', '\f', '`', '?', '*', '\\', '<', '>', '|', '"', ':'};
	private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy_MM_dd-HH_mm_ss");
	private static final Predicate<ZipEntry> FILTER = ((Predicate<ZipEntry>)ZipEntry::isDirectory).negate().and(BackupManager::isValidFile);
	
	public static void createBackup(IModConfig config) {
		createBackup(createPath(config), config);
	}
	
	public static void loadLastBackup(IModConfig config) {
		loadBackup(createPath(config), config);
	}
	
	public static List<BackupEntry> listBackups(IModConfig config) {
		Path path = createPath(config);
		if(Files.notExists(path)) return ObjectLists.empty();
		String[] result = splitExtension(config);
		if(Files.notExists(path.resolve(result[0]))) return ObjectLists.empty();
		try(ZipFile file = new ZipFile(path.resolve(result[0]).toFile())) {
			return ObjectArrayList.wrap(file.stream().filter(FILTER).map(BackupEntry::new).toArray(BackupEntry[]::new));
		}
		catch(Exception e) { e.printStackTrace(); }
		return ObjectLists.empty();
	}
	
	public static void deleteBackup(IModConfig config, BackupEntry entry) {
		Path path = createPath(config);
		if(Files.notExists(path)) return;
		String[] result = splitExtension(config);
		try(FileSystem system = FileSystems.newFileSystem(toURI(path.resolve(result[0])), Object2ObjectMaps.singleton("create", "true"))) {
			Files.deleteIfExists(system.getPath(entry.getPath()));
		}
		catch(Exception e) { e.printStackTrace(); }
	}
	
	public static void loadBackup(IModConfig config, BackupEntry entry) {
		Path path = createPath(config);
		if(Files.notExists(path)) return;
		String[] result = splitExtension(config);
		try(FileSystem system = FileSystems.newFileSystem(toURI(path.resolve(result[0])), Object2ObjectMaps.singleton("create", "true"))) {
			Path file = system.getPath(entry.getPath());
			if(Files.notExists(file)) return;
			config.loadBackup(Files.readAllBytes(file));
		}
		catch(Exception e) { e.printStackTrace(); }
	}
	
	private static void loadBackup(Path path, IModConfig config) {
		if(Files.notExists(path)) return;
		String[] result = splitExtension(config);
		try(ZipFile file = new ZipFile(path.resolve(result[0]).toFile())) {
			Optional<? extends ZipEntry> potential = file.stream().filter(FILTER).sorted(Comparator.comparing((ZipEntry T) -> LocalDateTime.parse(removeExtension(Paths.get(T.getName()).getFileName().toString()), FORMATTER)).reversed()).findFirst();
			if(!potential.isPresent()) return;
			config.loadBackup(IOUtils.toByteArray(file.getInputStream(potential.get())));
		}
		catch(Exception e) { e.printStackTrace(); }
	}
	
	private static URI toURI(Path path) {
		return URI.create("jar:" + path.toUri().toString());
	}
	
	private static void createBackup(Path path, IModConfig config) {
		if(Files.notExists(path)) {
			try { Files.createDirectories(path); }
			catch(Exception e) { e.printStackTrace(); }
		}
		String[] result = splitExtension(config);
		try(FileSystem system = FileSystems.newFileSystem(toURI(path.resolve(result[0])), Object2ObjectMaps.singleton("create", "true"))) {
			Files.write(system.getPath(FORMATTER.format(LocalDateTime.now())+"."+result[1]), config.createBackup());
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	private static Path createPath(IModConfig config) {
		Path path = getBasePath(config.getModId(), config.getConfigType(), !config.isLocalConfig());
		if(config.isLocalConfig()) {
			return config.getConfigType() == ConfigType.SERVER ? appendWorld(path) : path;
		}
		return appendServer(path);
	}
	
	private static String[] splitExtension(IModConfig config) {
		String name = config.getFileName();
		int index = name.lastIndexOf('.');
		String extension = index == -1 ? "txt" : name.substring(index+1);
		return new String[] {(index == -1 ? name : name.substring(0, index))+".zip", extension};
	}
	
	private static String removeExtension(String file) {
		int index = file.lastIndexOf('.');
		return index == -1 ? file : file.substring(0, index);
	}
	
	private static Path appendWorld(Path path) {
		IntegratedServer server = Minecraft.getMinecraft().getIntegratedServer();
		return path.resolve(server.getFile(".").toPath().getParent().getFileName()).resolve(Long.toHexString(server.worldServerForDimension(0).getSeed()));
	}
	
	private static Path appendServer(Path path) {
		return path.resolve(sanitizeName(Minecraft.getMinecraft().getIntegratedServer().getWorldName()));
	}
	
	private static String sanitizeName(String id) {
		for(char c0 : ILLEGAL_FILE_CHARACTERS) {
			id = id.replace(c0, '_');
		}
		return id.replaceAll("[./\"]", "_").replaceAll("\\W+", "_");
	}
	
	private static Path getBasePath(String modId, ConfigType type, boolean multiplayer) {
		return Loader.instance().getConfigDir().toPath().getParent()
				.resolve("configbackup")
				.resolve(multiplayer ? "multiplayer" : (type == ConfigType.SERVER ? "world" : "local"))
				.resolve(modId)
				.resolve(type(type));
	}
	
	private static String type(ConfigType type) {
		return type == ConfigType.CLIENT ? "client" : (type == ConfigType.SHARED ? "common" : "server");
	}
	
	private static boolean isValidFile(ZipEntry path) {
		try { return FORMATTER.parse(removeExtension(path.getName())) != null; }
		catch(Exception e){ return false; }
	}
	
	public static class BackupEntry implements Comparable<BackupEntry> {
		String filePath;
		String fileName;
		LocalDateTime creationTime;
		long size;
		long compressed;
		
		public BackupEntry(ZipEntry path) {
			Path file = Paths.get(path.getName());
			filePath = file.toString();
			fileName = file.getFileName().toString();
			creationTime = LocalDateTime.parse(removeExtension(fileName), FORMATTER);
			size = path.getSize();
			compressed = path.getCompressedSize();
		}
		
		@Override
		public int compareTo(BackupEntry o) {
			return creationTime.compareTo(o.creationTime);
		}
		
		public String getPath() {
			return filePath;
		}
		
		public String getFileName() {
			return fileName;
		}
		
		public long fileSize() {
			return size;
		}
		
		public long compressedSize() {
			return compressed;
		}
		
		public double getRatio() {
			double ratio = (1D - (double)compressed / (double)size);
			return ratio < 0D ? 1D + Math.abs(ratio) : ratio;
		}
		
		public LocalDateTime created() {
			return creationTime;
		}
		
		@Override
		public String toString() {
			double ratio = (1D - (double)compressed / (double)size);
			if(ratio < 0D) ratio = 1D + Math.abs(ratio);
			return "Backup Entry: [Name="+fileName+", Created="+creationTime+", Size="+size+", Compressed="+compressed+", Compression Ratio: "+((int)(getRatio() * 100D))+"%]";
		}
	}
	
	public static class BulkRequest implements IRequestReceiver {
		Mode mode;
		Map<UUID, Map.Entry<IModConfig, Predicate<PacketBuffer>>> toCheck = new Object2ObjectOpenHashMap<>();
		
		public BulkRequest(List<IModConfig> configs, Mode mode) {
			this.mode = mode;
			IRequestReceiver.Impl.register(this);
			for(int i = 0,m=configs.size();i<m;i++) {
				UUID id = UUID.randomUUID();
				MutableObject<Predicate<PacketBuffer>> result = new MutableObject<>();
				IModConfig net = configs.get(i).loadFromNetworking(id, result::setValue);
				add(id, net, result.getValue());
			}
		}
		
		private void add(UUID id, IModConfig config, Predicate<PacketBuffer> tester) {
			toCheck.put(id, new AbstractMap.SimpleEntry<>(config, tester));
		}

		@Override
		public void receiveConfigData(UUID requestId, PacketBuffer buf) {
			Map.Entry<IModConfig, Predicate<PacketBuffer>> entry = toCheck.remove(requestId);
			if(entry == null) return;
			if(entry.getValue().test(buf)) {
				switch(mode) {
					case CREATE:
						BackupManager.createBackup(entry.getKey());
						break;
					case LOAD:
						BackupManager.loadLastBackup(entry.getKey());
						break;
					case LIST:
						BaseCarbonScreen.pushExternalScreen(new BackupSelectionScreen(Minecraft.getMinecraft().currentScreen, BackgroundTexture.DEFAULT.asHolder(), entry.getKey(), BackupManager.listBackups(entry.getKey())));
						break;
				}
				if(toCheck.isEmpty()) {
//					HELP WANTED: If someone wants to help getting toasts in the mod :)
//					if(CarbonConfig.BACKUP_TOASTS.get()) {
//						if(mode == Mode.CREATE) Minecraft.getMinecraft().getToastGui().add(new SystemToast(SystemToast.Type.TUTORIAL_HINT, Texts.translatable("gui.carbonconfig.toast.create"), Texts.translatable("gui.carbonconfig.toast.create.desc")));
//						else if(mode == Mode.LOAD) Minecraft.getMinecraft().getToastGui().add(new SystemToast(SystemToast.Type.TUTORIAL_HINT, Texts.translatable("gui.carbonconfig.toast.load"), Texts.translatable("gui.carbonconfig.toast.load.desc")));
//					}
					IRequestReceiver.Impl.unregister(this);
				}
				return;
			}
			if(toCheck.isEmpty()) IRequestReceiver.Impl.unregister(this);
		}
		
		public boolean isStillWorking() {
			return toCheck.size() > 0;
		}
	}
	
	public static enum Mode {
		CREATE,
		LOAD,
		LIST;
	}
}
