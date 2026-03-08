package carbonconfiglib.impl.internal;

import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.AbstractMap;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;

import org.apache.commons.lang3.mutable.MutableObject;

import carbonconfiglib.api.ConfigType;
import carbonconfiglib.gui.api.IModConfig;
import carbonconfiglib.gui.api.IRequestReceiver;
import carbonconfiglib.gui.api.background.BackgroundTexture;
import carbonconfiglib.gui.base.screen.BaseCarbonScreen;
import carbonconfiglib.gui.screens.BackupSelectionScreen;
import net.minecraft.SharedConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraftforge.fml.loading.FMLPaths;
import speiger.src.collections.objects.lists.ObjectArrayList;
import speiger.src.collections.objects.maps.impl.hash.Object2ObjectOpenHashMap;
import speiger.src.collections.objects.utils.ObjectLists;
import speiger.src.collections.objects.utils.maps.Object2ObjectMaps;

public class BackupManager {
	private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy_MM_dd-HH_mm_ss");
	private static final Predicate<Path> FILTER = ((Predicate<Path>)Files::isDirectory).negate().and(BackupManager::isValidFile);
	
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
		try(FileSystem system = FileSystems.newFileSystem(path.resolve(result[0]), Object2ObjectMaps.singleton("create", "true"))) {
			return ObjectArrayList.wrap(Files.walk(system.getPath(".")).filter(FILTER).map(BackupEntry::new).toArray(BackupEntry[]::new));
		}
		catch(Exception e) { e.printStackTrace(); }
		return ObjectLists.empty();
	}
	
	public static void deleteBackup(IModConfig config, BackupEntry entry) {
		Path path = createPath(config);
		if(Files.notExists(path)) return;
		String[] result = splitExtension(config);
		try(FileSystem system = FileSystems.newFileSystem(path.resolve(result[0]), Object2ObjectMaps.singleton("create", "true"))) {
			Files.deleteIfExists(system.getPath(entry.getPath()));
		}
		catch(Exception e) { e.printStackTrace(); }
	}
	
	public static void loadBackup(IModConfig config, BackupEntry entry) {
		Path path = createPath(config);
		if(Files.notExists(path)) return;
		String[] result = splitExtension(config);
		try(FileSystem system = FileSystems.newFileSystem(path.resolve(result[0]), Object2ObjectMaps.singleton("create", "true"))) {
			Path file = system.getPath(entry.getPath());
			if(Files.notExists(file)) return;
			config.loadBackup(Files.readAllBytes(file));
		}
		catch(Exception e) { e.printStackTrace(); }
	}
	
	private static void loadBackup(Path path, IModConfig config) {
		if(Files.notExists(path)) return;
		String[] result = splitExtension(config);
		try(FileSystem system = FileSystems.newFileSystem(path.resolve(result[0]), Object2ObjectMaps.singleton("create", "true"))) {
			Optional<Path> potential = Files.walk(system.getPath(".")).filter(FILTER).sorted(Comparator.comparing((Path T) -> LocalDateTime.parse(removeExtension(T.getFileName().toString()), FORMATTER)).reversed()).findFirst();
			if(potential.isEmpty()) return;
			config.loadBackup(Files.readAllBytes(path));
		}
		catch(Exception e) { e.printStackTrace(); }
	}
	
	private static void createBackup(Path path, IModConfig config) {
		if(Files.notExists(path)) {
			try { Files.createDirectories(path); }
			catch(Exception e) { e.printStackTrace(); }
		}
		String[] result = splitExtension(config);
		try(FileSystem system = FileSystems.newFileSystem(path.resolve(result[0]), Object2ObjectMaps.singleton("create", "true"))) {
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
		IntegratedServer server = Minecraft.getInstance().getSingleplayerServer();
		return path.resolve(server.getWorldPath(LevelResource.ROOT).getParent().getFileName()).resolve(Long.toHexString(server.overworld().getSeed()));
	}
	
	private static Path appendServer(Path path) {
		return path.resolve(sanitizeName(Minecraft.getInstance().getCurrentServer().name));
	}
	
	private static String sanitizeName(String id) {
		for(char c0 : SharedConstants.ILLEGAL_FILE_CHARACTERS) {
			id = id.replace(c0, '_');
		}
		return id.replaceAll("[./\"]", "_").replaceAll("\\W+", "_");
	}
	
	private static Path getBasePath(String modId, ConfigType type, boolean multiplayer) {
		return FMLPaths.GAMEDIR.get()
				.resolve("configbackup")
				.resolve(multiplayer ? "multiplayer" : (type == ConfigType.SERVER ? "world" : "local"))
				.resolve(modId)
				.resolve(type(type));
	}
	
	private static String type(ConfigType type) {
		return type == ConfigType.CLIENT ? "client" : (type == ConfigType.SHARED ? "common" : "server");
	}
	
	private static boolean isValidFile(Path path) {
		try { return FORMATTER.parse(removeExtension(path.getFileName().toString())) != null; }
		catch(Exception e){ return false; }
	}
	
	public static class BackupEntry implements Comparable<BackupEntry> {
		String filePath;
		String fileName;
		LocalDateTime creationTime;
		long size;
		long compressed;
		
		public BackupEntry(Path path) {
			filePath = path.toString();
			fileName = path.getFileName().toString();
			creationTime = LocalDateTime.parse(removeExtension(path.getFileName().toString()), FORMATTER);
			try {
				size = Files.size(path);
				compressed = ((Long)Files.readAttributes(path, "zip:*").get("compressedSize"));
			}
			catch(Exception e) {
				e.printStackTrace();
			}
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
		Map<UUID, Map.Entry<IModConfig, Predicate<FriendlyByteBuf>>> toCheck = new Object2ObjectOpenHashMap<>();
		
		public BulkRequest(List<IModConfig> configs, Mode mode) {
			this.mode = mode;
			IRequestReceiver.Impl.register(this);
			for(int i = 0,m=configs.size();i<m;i++) {
				UUID id = UUID.randomUUID();
				MutableObject<Predicate<FriendlyByteBuf>> result = new MutableObject<>();
				IModConfig net = configs.get(i).loadFromNetworking(id, result::setValue);
				add(id, net, result.getValue());
			}
		}
		
		private void add(UUID id, IModConfig config, Predicate<FriendlyByteBuf> tester) {
			toCheck.put(id, new AbstractMap.SimpleEntry<>(config, tester));
		}

		@Override
		public void receiveConfigData(UUID requestId, FriendlyByteBuf buf) {
			Map.Entry<IModConfig, Predicate<FriendlyByteBuf>> entry = toCheck.remove(requestId);
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
						BaseCarbonScreen.pushExternalScreen(new BackupSelectionScreen(Minecraft.getInstance().screen, BackgroundTexture.DEFAULT.asHolder(), entry.getKey(), BackupManager.listBackups(entry.getKey())));
						break;
				}
				if(toCheck.isEmpty()) IRequestReceiver.Impl.unregister(this);
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
