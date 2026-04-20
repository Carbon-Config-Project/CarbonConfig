package carbonconfiglib.gui.screens;

import java.net.URI;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.joml.Matrix3x2fStack;

import com.google.gson.JsonObject;

import carbonconfiglib.gui.api.IModConfigs;
import carbonconfiglib.gui.api.background.BackgroundTexture;
import carbonconfiglib.gui.base.helpers.Align;
import carbonconfiglib.gui.base.helpers.GuiUtils;
import carbonconfiglib.gui.base.menu.MenuScreen;
import carbonconfiglib.gui.base.menu.SubMenuItem;
import carbonconfiglib.gui.base.screen.BaseCarbonScreen;
import carbonconfiglib.impl.internal.EventHandler;
import carbonconfiglib.utils.Helpers;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.fml.loading.moddiscovery.ModFileInfo;
import net.neoforged.neoforge.client.gui.ModListScreen;
import net.neoforged.neoforgespi.language.IModInfo;
import net.neoforged.neoforgespi.language.IModInfo.DependencyType;
import net.neoforged.neoforgespi.language.IModInfo.ModVersion;
import speiger.src.collections.objects.lists.ObjectArrayList;
import speiger.src.collections.objects.maps.impl.hash.Object2ObjectOpenHashMap;
import speiger.src.collections.objects.sets.ObjectOpenHashSet;

public class ModDependencyScreen extends BaseCarbonScreen
{
	int[] dependencyColors = new int[] {
			0xFFFF0000, //Required Dependency
			0xFFFFFF00, //Optional Dependency
			0xFF0000FF, //Required Dependent (Mod requires ME)
			0xFF00FFFF //Optional Dependent
	};
	float scale = 0.32F;
	float x;
	float y;
	ChunkPos pos;
	List<ModNode> nodes = new ObjectArrayList<>();
	JsonObject provider = new JsonObject();
	ModNode focused;
	
	public ModDependencyScreen() {}

	@Override
	protected void init() {
		super.init();
		button(-40, -30, 80, 16, Align.CENTER, Align.END, Component.translatable("gui.carbonconfig.back"), _ -> onClose());
		button(-115, -48, 100, 16, Align.END, Align.END, Component.translatable("gui.carbonconfig.dependency.config"), _ -> openConfigs());
		button(-115, -30, 100, 16, Align.END, Align.END, Component.translatable("gui.carbonconfig.dependency.mods"), _ -> openMods());
	}
	
	private void openConfigs() {
		setScreen(minecraft.hasShiftDown() ? new ModListScreen(this) : new ConfigListScreen(this, BackgroundTexture.DEFAULT.asHolder(), EventHandler.INSTANCE.getAllConfigs()));
	}
	
	private void openMods() {
		Util.getPlatform().openUri(FMLPaths.MODSDIR.get().toUri());
	}
	
	@Override
	public void tick() {
		if(!minecraft.hasShiftDown() && !nodes.isEmpty()) return;
		nodes.clear();
		nodes.addAll(ModList.get().getMods().stream().map(IModInfo::getModId).map(ModNode::new).toList());
		Set<String> installedMods = nodes.stream().map(ModNode::id).collect(Collectors.toSet());
		Map<String, List<ModVersion>> dependentNodes = ModList.get().getMods().stream().flatMap(T -> T.getDependencies().stream()).filter(T -> !installedMods.contains(T.getModId())).filter(T -> T.getType() != DependencyType.INCOMPATIBLE && T.getType() != DependencyType.DISCOURAGED).collect(Collectors.groupingBy(ModVersion::getModId));
		dependentNodes.forEach((K, V) -> nodes.add(new DependencyNode(K, V)));
		Map<String, ModNode> mapped = new Object2ObjectOpenHashMap<>();
		nodes.forEach(T -> mapped.put(T.modId, T));
		ModNode node = mapped.get("minecraft");
		nodes.remove(mapped.put("neoforge", node));
		nodes.remove(node);
		for(IModInfo info : ModList.get().getMods()) {
			ModNode owner = mapped.get(info.getModId());
			if(owner == null) continue;
			Set<ModNode> deps = new ObjectOpenHashSet<>();
			for(ModVersion version : info.getDependencies()) {
				ModNode dependency = mapped.get(version.getModId());
				if(dependency == null || dependency == owner) continue;
				deps.add(dependency);
				boolean required = version.getType() == DependencyType.REQUIRED;
				dependency.dependants.put(owner, required);
				owner.dependencies.put(dependency, required);
			}
			if(owner != node && !deps.contains(node)) {
				owner.dependencies.put(node, true);
				node.dependants.put(owner, true);
			}
		}
		layout(nodes, 0, 0, 200F, 250, 15);
		node.x = 0;
		node.y = 0;
		nodes.add(0, node);
	}
	
	@Override
	public void drawBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
		if(pos != null && (pos.x() != mouseX || pos.z() != mouseY)) {
			int diffX = pos.x() - mouseX;
			int diffY = pos.z() - mouseY;
			x -= diffX / scale;
			y -= diffY / scale;
			pos = new ChunkPos(mouseX, mouseY);
		}
		extractMenuBackground(graphics);
		int bottomSpace = 75;
		int legendenY = 47;
		
		GuiUtils.fillDropArea(graphics, 10, 10, width-20, height-20, -3750202, false);
		GuiUtils.fillDropArea(graphics, 15, 25, width-30, height-bottomSpace-10, -7631989, true);
		drawUnalignedText(graphics, Component.translatable("gui.carbonconfig.dependency.header"), centerX, 12, Align.CENTER, 0xFF404040);
		graphics.fill(14, height-legendenY, 30, height-(legendenY-font.lineHeight), dependencyColors[0] | 0xFF000000);
		graphics.fill(14, height-(legendenY-font.lineHeight-1), 30, height-(legendenY-font.lineHeight*2), dependencyColors[1] | 0xFF000000);
		graphics.fill(14, height-(legendenY-font.lineHeight*2-1), 30, height-(legendenY-font.lineHeight*3), dependencyColors[2] | 0xFF000000);
		graphics.fill(14, height-(legendenY-font.lineHeight*3-1), 30, height-(legendenY-font.lineHeight*4), dependencyColors[3] | 0xFF000000);
		
		GuiUtils.drawFrame(graphics, 14, height-legendenY, 29, height-(legendenY-font.lineHeight)-1, 0xFF404040, 1);
		GuiUtils.drawFrame(graphics, 14, height-(legendenY-font.lineHeight-1), 29, height-(legendenY-font.lineHeight*2)-1, 0xFF404040, 1);
		GuiUtils.drawFrame(graphics, 14, height-(legendenY-font.lineHeight*2-1), 29, height-(legendenY-font.lineHeight*3)-1, 0xFF404040, 1);
		GuiUtils.drawFrame(graphics, 14, height-(legendenY-font.lineHeight*3-1), 29, height-(legendenY-font.lineHeight*4)-1, 0xFF404040, 1);
		drawUnalignedText(graphics, Component.translatable("gui.carbonconfig.dependency.legend"), 14, height-legendenY-font.lineHeight-3, Align.START, 0xFF404040);
		
		drawUnalignedText(graphics, Component.translatable("gui.carbonconfig.dependency.required_need"), 31, height-legendenY, Align.START, 0xFF404040);
		drawUnalignedText(graphics, Component.translatable("gui.carbonconfig.dependency.optional_need"), 31, height-(legendenY-font.lineHeight-1), Align.START, 0xFF404040);
		drawUnalignedText(graphics, Component.translatable("gui.carbonconfig.dependency.required_have"), 31, height-(legendenY-font.lineHeight*2-1), Align.START, 0xFF404040);
		drawUnalignedText(graphics, Component.translatable("gui.carbonconfig.dependency.optional_have"), 31, height-(legendenY-font.lineHeight*3-1), Align.START, 0xFF404040);
		graphics.enableScissor(15, 24, width-15, height-bottomSpace+15);
		GuiUtils.renderBackground(graphics, 15, width-15, 15, height-15, -x*0.2F*(scale*2), -y*0.2F*scale*2, BackgroundTexture.DEFAULT);
		Matrix3x2fStack stack = graphics.pose();
		stack.pushMatrix();
		stack.translate(centerX, centerY);
		stack.scale(scale, scale);
		int radius = 10;
		float textScale = Math.min(2.5F, 1F / scale);
		float quadScale = Mth.clamp(textScale, 1F, 2F)*0.75F;
		for(ModNode node : nodes) {
			int x = (int)(node.x + this.x);
			int y = (int)(node.y + this.y);
			stack.pushMatrix();
			stack.translate(x, y);
			stack.scale(quadScale, quadScale);
			GuiUtils.drawCircle(graphics, 0, 0, radius, node.modId.hashCode() | 0xFF000000, 0xFF000000, 128, 1F);
			stack.popMatrix();
		}
		if(focused != null) {
			drawNode(graphics, focused, false, dependencyColors[2], dependencyColors[3]);
			drawNode(graphics, focused, true, dependencyColors[0], dependencyColors[1]);
		}
		for(ModNode node : nodes) {
			int x = (int)(node.x + this.x);
			int y = (int)(node.y + this.y);
			stack.pushMatrix();
			stack.translate(x, y);
			stack.translate(0F, -radius*textScale-font.lineHeight);
			stack.scale(textScale, textScale);
			boolean notLoaded = node instanceof DependencyNode;
			Component name = Component.literal(ModList.get().getModContainerById(node.id()).map(ModContainer::getModInfo).map(IModInfo::getDisplayName).orElse(Helpers.firstLetterUppercase(node.id()))); 
			drawUnalignedText(graphics, name, 0F, 0F, Align.CENTER, notLoaded ? 0xFFFF0000 : -1);
			stack.popMatrix();
		}
		stack.popMatrix();
		graphics.disableScissor();
	}
	
	private void drawNode(GuiGraphicsExtractor graphics, ModNode source, boolean dep, int requiredColor, int optionalColor) {
		float textScale = Math.min(2.5F, 1F / scale);
		float quadScale = Mth.clamp(textScale, 1F, 2F)*0.75F;
		float radius = (dep ? 2.5F : -2.5F) * quadScale;
		for(Map.Entry<ModNode, Boolean> entry : (dep ? source.dependencies : source.dependants).entrySet()) {
			ModNode child = entry.getKey();
			GuiUtils.drawLine(graphics, (float)source.x+this.x+radius, (float)source.y+this.y+radius, (float)child.x+this.x+radius, (float)child.y+this.y+radius, 2F, entry.getValue() ? requiredColor : optionalColor);
			if(entry.getValue()) drawNode(graphics, child, dep, requiredColor, optionalColor);
		}
	}
	
	@Override
	public void collectTooltips(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks, Consumer<Component> tooltips) {
		double radius = 10 * scale;
		float xOff = x * scale;
		float yOff = y * scale;
		for(ModNode node : nodes) {
			int x = (int)(node.x * scale) + centerX;
			int y = (int)(node.y * scale) + centerY;
			if(mouseX >= x-radius+xOff && mouseX <= x+radius+xOff && mouseY >= y-radius+yOff && mouseY <= y+radius+yOff) {
				tooltips.accept(Component.literal(ModList.get().getModContainerById(node.id()).map(ModContainer::getModInfo).map(IModInfo::getDisplayName).orElse(Helpers.firstLetterUppercase(node.id()))));
				if(node instanceof DependencyNode) {
					tooltips.accept(Component.translatable("gui.carbonconfig.dependency.missing"));
				}
			}
		}
	}
	
	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
		if(super.mouseClicked(event, doubleClick)) return true;
		double radius = 10 * scale;
		float xOff = x * scale;
		float yOff = y * scale;
		ModNode found = null;
		for(ModNode node : nodes) {
			int x = (int)(node.x * scale) + centerX;
			int y = (int)(node.y * scale) + centerY;
			if(event.x() >= x-radius+xOff && event.x() <= x+radius+xOff && event.y() >= y-radius+yOff && event.y() <= y+radius+yOff) {
				found = node;
			}
		}
		if(found != null) {
			if(event.input() == 0) {
				focused = found;
				return true;
			}
			else if(event.input() == 1) {
				if(found instanceof DependencyNode) {
					DependencyNode dep = (DependencyNode)found;
					SubMenuItem item = new SubMenuItem("Root");
					item.addLabel(Helpers.firstLetterUppercase(found.id()));
					item.addLabel(Component.translatable("gui.carbonconfig.dependency.missing"));
					if(dep.getURL() == null) item.addLabel(Component.translatable("gui.carbonconfig.dependency.not_provided"));
					else item.addNode("gui.carbonconfig.dependency.open_page", () -> {
						try {
							Util.getPlatform().openUri(dep.getURL().toURI());
						}
						catch(Exception e) { e.printStackTrace(); }
					});
					pushScreen(new MenuScreen(item, (int)event.x()+5, (int)event.y()-5));
					return true;
				}
				SubMenuItem item = new SubMenuItem("Root");
				IModInfo info = ModList.get().getModContainerById(found.id()).map(ModContainer::getModInfo).orElse(null);
				if(info != null) {
					item.addLabel(info.getDisplayName());
					item.addLabel(Component.translatable("gui.carbonconfig.dependency.version", info.getVersion().toString()));
					SubMenuItem desc = new SubMenuItem("gui.carbonconfig.dependency.description");
					desc.addLabel(info.getDescription().trim());
					item.addSubMenu("desc", desc);
					SubMenuItem license = new SubMenuItem("gui.carbonconfig.dependency.license");
					license.addLabel(Optional.ofNullable(info.getOwningFile().getLicense()).orElse("All Rights reserved"));
					item.addSubMenu("license", license);
					
					info.getModURL().ifPresentOrElse(T -> {
						try {
							URI uri = T.toURI();
							item.addNode("gui.carbonconfig.dependency.open_page", () -> Util.getPlatform().openUri(uri));					
						}
						catch(Exception e) { e.printStackTrace(); }
					}, () -> {
						item.addLabel(Component.translatable("gui.carbonconfig.dependency.not_provided"));
					});
					Optional.ofNullable(((ModFileInfo)info.getOwningFile()).getIssueURL()).ifPresent(T -> {
						try {
							URI uri = T.toURI();
							item.addNode("gui.carbonconfig.dependency.open_issue", () -> Util.getPlatform().openUri(uri));					
						}
						catch(Exception e) { e.printStackTrace(); }
					});
					IModConfigs configs = EventHandler.INSTANCE.getConfigsForMod(info.getModId());
					if(configs != null) {
						item.addNode("gui.carbonconfig.dependency.config", () -> {
							MenuScreen.popAllMenus();
							setScreen(new ConfigListScreen(this, configs));
						});
					}
					
					pushScreen(new MenuScreen(item, (int)event.x()+5, (int)event.y()-5));
				}
				return true;
			}
		}
		if(event.input() == 0) {
			pos = new ChunkPos((int)event.x(), (int)event.y());
			return true;
		}
		focused = null;
		return false;
	}
	
	
	@Override
	public boolean mouseReleased(MouseButtonEvent event) {
		if(pos != null) {
			pos = null;
			return true;
		}
		return super.mouseReleased(event);
	}
	
	@Override
	public boolean mouseScrolled(double pMouseX, double pMouseY, double scrollX, double scrollY) {
		scale = Mth.clamp(scale - (float)scrollY * 0.01F * (minecraft.hasShiftDown() ? 10 : 1F), 0.025F, 2F);
		return true;
	}
	
	static class DependencyNode extends ModNode {
		Optional<URL> link;
		public DependencyNode(String modId, List<ModVersion> version) {
			super(modId);
			link = version.stream().filter(T -> T.getReferralURL().isPresent()).map(ModVersion::getReferralURL).findFirst().flatMap(Function.identity());
		}
		
		public URL getURL() {
			return link.orElse(null);
		}
	}
	
	static class ModNode {
		private final String modId;
		double x;
		double y;
		int layer = -1;
		Map<ModNode, Boolean> dependencies = new LinkedHashMap<>();
		Map<ModNode, Boolean> dependants = new LinkedHashMap<>();
		
		public ModNode(String modId) {
			this.modId = modId;
		}
		
		public void setPos(double x, double y) {
			this.x = x;
			this.y = y;
		}
		
		public String id() {
			return modId;
		}
		
		@Override
		public int hashCode() {
			return modId.hashCode();
		}
		
		@Override
		public boolean equals(Object obj) {
			return obj instanceof ModNode && ((ModNode)obj).modId.equals(modId);
		}
	}
	
	public static void layout(List<ModNode> nodes, double centerX, double centerY, double ringSpacing, double advance, int baseRingCapacity) {   
		int ringIndex = 1;
		for(int i = 0,m=nodes.size();i<m;) {
			int countThisRing = Math.min(baseRingCapacity * ringIndex, m - i);

			double radius = ringSpacing + (advance * (ringIndex-1));
			double angleStep = (2 * Math.PI) / countThisRing;

			for (int j = 0; j < countThisRing; j++) {
				double angle = j * angleStep;
				nodes.get(i++).setPos(centerX + Math.cos(angle) * radius, centerY + Math.sin(angle) * radius);
			}
			ringIndex++;
		}
	}
}
