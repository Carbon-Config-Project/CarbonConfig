package carbonconfiglib.gui.screens;

import java.net.URI;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.google.gson.JsonObject;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.GlStateManager.DestFactor;
import com.mojang.blaze3d.platform.GlStateManager.SourceFactor;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;

import carbonconfiglib.CarbonConfig;
import carbonconfiglib.gui.api.IModConfigs;
import carbonconfiglib.gui.api.Texts;
import carbonconfiglib.gui.api.background.BackgroundTexture;
import carbonconfiglib.gui.base.helpers.Align;
import carbonconfiglib.gui.base.helpers.GuiUtils;
import carbonconfiglib.gui.base.menu.MenuScreen;
import carbonconfiglib.gui.base.menu.SubMenuItem;
import carbonconfiglib.gui.base.screen.BaseCarbonScreen;
import carbonconfiglib.impl.internal.EventHandler;
import carbonconfiglib.utils.Helpers;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.ModDependency;
import net.fabricmc.loader.api.metadata.ModDependency.Kind;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.minecraft.Util;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
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
		button(-40, -30, 80, 16, Align.CENTER, Align.END, Texts.translatable("gui.carbonconfig.back"), T -> onClose());
		button(-115, -48, 100, 16, Align.END, Align.END, Texts.translatable("gui.carbonconfig.dependency.config"), T -> openConfigs());
		button(-115, -30, 100, 16, Align.END, Align.END, Texts.translatable("gui.carbonconfig.dependency.mods"), T -> openMods());
	}
	
	private void openConfigs() {
		if(Screen.hasShiftDown() && CarbonConfig.createModMenuScreen(this, this::setScreen)) return;
		setScreen(new ConfigListScreen(this, BackgroundTexture.DEFAULT.asHolder(), EventHandler.INSTANCE.getAllConfigs()));
	}
	
	private void openMods() {
		Util.getPlatform().openUri(FabricLoader.getInstance().getGameDir().resolve("mods").toUri());
	}
	
	@Override
	public void tick() {
		if(!hasShiftDown() && !nodes.isEmpty()) return;
		nodes.clear();
		nodes.addAll(FabricLoader.getInstance().getAllMods().stream().map(ModContainer::getMetadata).map(ModMetadata::getId).map(ModNode::new).toList());
		Set<String> installedMods = nodes.stream().map(ModNode::id).collect(Collectors.toSet());
		
		
		Map<String, List<ModDependency>> dependentNodes = FabricLoader.getInstance()
				.getAllMods()
				.stream()
				.map(ModContainer::getMetadata)
				.flatMap(T -> T.getDependencies().stream())
				.filter(T -> T.getKind() != Kind.BREAKS && T.getKind() != Kind.CONFLICTS)
				.filter(T -> !installedMods.contains(T.getModId()))
				.collect(Collectors.groupingBy(ModDependency::getModId));
		dependentNodes.forEach((K, V) -> nodes.add(new DependencyNode(K, V)));
		
		Map<String, ModNode> mapped = new Object2ObjectOpenHashMap<>();
		nodes.forEach(T -> mapped.put(T.modId, T));
		nodes.remove(mapped.remove("java"));
		ModNode node = mapped.get("minecraft");
		for(ModContainer info : FabricLoader.getInstance().getAllMods()) {
			ModNode owner = mapped.get(info.getMetadata().getId());
			if(owner == null) continue;
			Set<ModNode> deps = new ObjectOpenHashSet<>();
			for(ModDependency version : info.getMetadata().getDependencies()) {
				if(version.getKind() == Kind.BREAKS || version.getKind() == Kind.CONFLICTS) continue;
				ModNode dependency = mapped.get(version.getModId());
				if(dependency == null || dependency == owner) continue;
				deps.add(dependency);
				boolean required = !version.getKind().isSoft();
				dependency.dependants.put(owner, required);
				owner.dependencies.put(dependency, required);
			}
			if(owner != node && !deps.contains(node)) {
				owner.dependencies.put(node, true);
				node.dependants.put(owner, true);
			}
		}
		node.dependencies.clear();
		nodes.sort(Comparator.comparing(ModNode::id));
		layout(nodes, 0, 0, 200F, 250, 15);
		node.x = 0;
		node.y = 0;
		nodes.add(0, node);
	}
	
	@Override
	public void renderBackground(PoseStack matrix, int mouseX, int mouseY, float partialTicks) {
		if(pos != null && (pos.x != mouseX || pos.z != mouseY)) {
			int diffX = pos.x - mouseX;
			int diffY = pos.z - mouseY;
			x -= diffX / scale;
			y -= diffY / scale;
			pos = new ChunkPos(mouseX, mouseY);
		}
		renderDirtBackground(0);
		int bottomSpace = 75;
		int legendenY = 47;
		
		GuiUtils.fillDropArea(matrix, 10, 10, width-20, height-20, -3750202, false);
		GuiUtils.fillDropArea(matrix, 15, 25, width-30, height-bottomSpace-10, -7631989, true);
		drawUnalignedText(matrix, Texts.translatable("gui.carbonconfig.dependency.header"), centerX, 12, Align.CENTER, 4210752);
		fill(matrix, 14, height-legendenY, 30, height-(legendenY-font.lineHeight), dependencyColors[0] | 0xFF000000);
		fill(matrix, 14, height-(legendenY-font.lineHeight-1), 30, height-(legendenY-font.lineHeight*2), dependencyColors[1] | 0xFF000000);
		fill(matrix, 14, height-(legendenY-font.lineHeight*2-1), 30, height-(legendenY-font.lineHeight*3), dependencyColors[2] | 0xFF000000);
		fill(matrix, 14, height-(legendenY-font.lineHeight*3-1), 30, height-(legendenY-font.lineHeight*4), dependencyColors[3] | 0xFF000000);
		
		GuiUtils.drawFrame(matrix, 14, height-legendenY, 29, height-(legendenY-font.lineHeight)-1, 0xFF404040, 1);
		GuiUtils.drawFrame(matrix, 14, height-(legendenY-font.lineHeight-1), 29, height-(legendenY-font.lineHeight*2)-1, 0xFF404040, 1);
		GuiUtils.drawFrame(matrix, 14, height-(legendenY-font.lineHeight*2-1), 29, height-(legendenY-font.lineHeight*3)-1, 0xFF404040, 1);
		GuiUtils.drawFrame(matrix, 14, height-(legendenY-font.lineHeight*3-1), 29, height-(legendenY-font.lineHeight*4)-1, 0xFF404040, 1);
		drawUnalignedText(matrix, Texts.translatable("gui.carbonconfig.dependency.legend"), 14, height-legendenY-font.lineHeight-3, Align.START, 4210752);
		
		drawUnalignedText(matrix, Texts.translatable("gui.carbonconfig.dependency.required_need"), 31, height-legendenY, Align.START, 4210752);
		drawUnalignedText(matrix, Texts.translatable("gui.carbonconfig.dependency.optional_need"), 31, height-(legendenY-font.lineHeight-1), Align.START, 4210752);
		drawUnalignedText(matrix, Texts.translatable("gui.carbonconfig.dependency.required_have"), 31, height-(legendenY-font.lineHeight*2-1), Align.START, 4210752);
		drawUnalignedText(matrix, Texts.translatable("gui.carbonconfig.dependency.optional_have"), 31, height-(legendenY-font.lineHeight*3-1), Align.START, 4210752);

		GuiUtils.pushScissors(15, 24, width-30, height-bottomSpace-9);
		GuiUtils.renderBackground(15, width-15, 15, height-15, -x*0.2F*(scale*2), -y*0.2F*scale*2, BackgroundTexture.DEFAULT);
		matrix.pushPose();
		matrix.translate(centerX, centerY, 0F);
		matrix.scale(scale, scale, 1F);
		int radius = 10;
		float textScale = Math.min(2.5F, 1F / scale);
		float quadScale = Mth.clamp(textScale, 1F, 2F)*0.75F;
		for(ModNode node : nodes) {
			int x = (int)(node.x + this.x);
			int y = (int)(node.y + this.y);
			matrix.pushPose();
			matrix.translate(x, y, 0F);
			matrix.scale(quadScale, quadScale, 1F);
			GuiUtils.drawCircle(matrix, 0, 0, radius, node.modId.hashCode() | 0xFF000000, 0xFF000000, 128, 1F);
			matrix.popPose();
		}
		if(focused != null) {
			Tesselator tes = Tesselator.getInstance();
			BufferBuilder builder = tes.getBuilder();
			builder.begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR);
			drawNode(matrix, focused, false, dependencyColors[2], dependencyColors[3], builder, 0);
			drawNode(matrix, focused, true, dependencyColors[0], dependencyColors[1], builder, 0);
			GlStateManager._disableTexture();
			GlStateManager._enableBlend();
			RenderSystem.blendFuncSeparate(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA, SourceFactor.ONE, DestFactor.ZERO);
			tes.end();
			GlStateManager._enableTexture();
			GlStateManager._disableBlend();
		}
		for(ModNode node : nodes) {
			int x = (int)(node.x + this.x);
			int y = (int)(node.y + this.y);
			matrix.pushPose();
			matrix.translate(x, y, 0F);
			matrix.translate(0F, -radius*textScale-font.lineHeight, 0F);
			matrix.scale(textScale, textScale, 1F);
			boolean notLoaded = node instanceof DependencyNode;
			Component name = Texts.literal(FabricLoader.getInstance().getModContainer(node.id()).map(ModContainer::getMetadata).map(ModMetadata::getName).orElse(Helpers.firstLetterUppercase(node.id()))); 
			drawUnalignedText(matrix, name, 0F, 0F, Align.CENTER, notLoaded ? 0xFFFF0000 : -1);
			matrix.popPose();
		}
		matrix.popPose();
		GuiUtils.popScissors();
	}
	
	private void drawNode(PoseStack stack, ModNode source, boolean dep, int requiredColor, int optionalColor, VertexConsumer builder, int layer) {
		if(layer > 256) return;
		float textScale = Math.min(2.5F, 1F / scale);
		float quadScale = Mth.clamp(textScale, 1F, 2F)*0.75F;
		float radius = (dep ? 2.5F : -2.5F) * quadScale;
		for(Map.Entry<ModNode, Boolean> entry : (dep ? source.dependencies : source.dependants).entrySet()) {
			ModNode child = entry.getKey();
			GuiUtils.drawLine(stack, (float)source.x+this.x+radius, (float)source.y+this.y+radius, (float)child.x+this.x+radius, (float)child.y+this.y+radius, 2F, builder, entry.getValue() ? requiredColor : optionalColor);
			if(entry.getValue()) drawNode(stack, child, dep, requiredColor, optionalColor, builder, layer+1);
		}
	}
	
	@Override
	public void collectTooltips(PoseStack matrix, int mouseX, int mouseY, float partialTicks, Consumer<Component> tooltips) {
		double radius = 10 * scale;
		float xOff = x * scale;
		float yOff = y * scale;
		for(ModNode node : nodes) {
			int x = (int)(node.x * scale) + centerX;
			int y = (int)(node.y * scale) + centerY;
			if(mouseX >= x-radius+xOff && mouseX <= x+radius+xOff && mouseY >= y-radius+yOff && mouseY <= y+radius+yOff) {
				tooltips.accept(Texts.literal(FabricLoader.getInstance().getModContainer(node.id()).map(ModContainer::getMetadata).map(ModMetadata::getName).orElse(Helpers.firstLetterUppercase(node.id()))));
				if(node instanceof DependencyNode) {
					tooltips.accept(Texts.translatable("gui.carbonconfig.dependency.missing"));
				}
			}
		}
	}
	
	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if(super.mouseClicked(mouseX, mouseY, button)) return true;
		double radius = 10 * scale;
		float xOff = x * scale;
		float yOff = y * scale;
		ModNode found = null;
		for(ModNode node : nodes) {
			int x = (int)(node.x * scale) + centerX;
			int y = (int)(node.y * scale) + centerY;
			if(mouseX >= x-radius+xOff && mouseX <= x+radius+xOff && mouseY >= y-radius+yOff && mouseY <= y+radius+yOff) {
				found = node;
			}
		}
		if(found != null) {
			if(button == 0) {
				focused = found;
				return true;
			}
			else if(button == 1) {
				if(found instanceof DependencyNode) {
					SubMenuItem item = new SubMenuItem("Root");
					item.addLabel(Helpers.firstLetterUppercase(found.id()));
					item.addLabel(Texts.translatable("gui.carbonconfig.dependency.missing"));
					pushScreen(new MenuScreen(item, (int)mouseX+5, (int)mouseY-5));
					return true;
				}
				SubMenuItem item = new SubMenuItem("Root");
				ModMetadata info = FabricLoader.getInstance().getModContainer(found.id()).map(ModContainer::getMetadata).orElse(null);
				if(info != null) {
					item.addLabel(info.getName());
					item.addLabel(Texts.translatable("gui.carbonconfig.dependency.version", info.getVersion().toString()));
					String descText = info.getDescription().trim();
					if(!descText.isEmpty()) {
						SubMenuItem desc = new SubMenuItem("gui.carbonconfig.dependency.description");
						desc.addLabel(info.getDescription().trim());
						item.addSubMenu("desc", desc);
					}
					SubMenuItem license = new SubMenuItem("gui.carbonconfig.dependency.license");
					license.addLabel(Optional.ofNullable(String.join("\n", info.getLicense())).filter(((Predicate<String>)String::isBlank).negate()).orElse("All Rights reserved"));
					item.addSubMenu("license", license);
					info.getContact().get("issues").or(() -> info.getContact().get("sources")).ifPresent(T -> {
						try {
							URI uri = URI.create(T);
							item.addNode("gui.carbonconfig.dependency.open_issue", () -> Util.getPlatform().openUri(uri));					
						}
						catch(Exception e) { e.printStackTrace(); }						
					});
					IModConfigs configs = EventHandler.INSTANCE.getConfigsForMod(info.getId());
					if(configs != null) {
						item.addNode("gui.carbonconfig.dependency.config", () -> {
							MenuScreen.popAllMenus();
							setScreen(new ConfigListScreen(this, configs));
						});
					}
					pushScreen(new MenuScreen(item, (int)mouseX+5, (int)mouseY-5));
				}
				return true;
			}
		}
		if(button == 0) {
			pos = new ChunkPos((int)mouseX, (int)mouseY);
			return true;
		}
		focused = null;
		return false;
	}
	
	@Override
	public boolean mouseReleased(double pMouseX, double pMouseY, int pButton) {
		if(pos != null) {
			pos = null;
			return true;
		}
		if(super.mouseReleased(pMouseX, pMouseY, pButton)) return true;
		return false;
	}
	
	@Override
	public boolean mouseScrolled(double pMouseX, double pMouseY, double pDelta) {
		scale = Mth.clamp(scale - (float)pDelta * 0.01F * (Screen.hasShiftDown() ? 10 : 1F), 0.025F, 2F);
		return true;
	}
	
	static class DependencyNode extends ModNode {
		public DependencyNode(String modId, List<ModDependency> version) {
			super(modId);
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
