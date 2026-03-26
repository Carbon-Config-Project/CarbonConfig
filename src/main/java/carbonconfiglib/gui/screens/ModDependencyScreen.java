package carbonconfiglib.gui.screens;

import java.awt.Desktop;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.lwjgl.opengl.GL11;

import com.google.gson.JsonObject;

import carbonconfiglib.gui.api.IModConfigs;
import carbonconfiglib.gui.api.background.BackgroundTexture;
import carbonconfiglib.gui.base.helpers.Align;
import carbonconfiglib.gui.base.helpers.GuiUtils;
import carbonconfiglib.gui.base.helpers.Texts;
import carbonconfiglib.gui.base.menu.MenuScreen;
import carbonconfiglib.gui.base.menu.SubMenuItem;
import carbonconfiglib.gui.base.screen.BaseCarbonScreen;
import carbonconfiglib.impl.internal.EventHandler;
import carbonconfiglib.utils.Helpers;
import cpw.mods.fml.client.GuiModList;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.ModContainer;
import cpw.mods.fml.common.versioning.ArtifactVersion;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.IChatComponent;
import net.minecraft.world.ChunkCoordIntPair;
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
	ChunkCoordIntPair pos;
	List<ModNode> nodes = new ObjectArrayList<>();
	JsonObject provider = new JsonObject();
	ModNode focused;
	
	public ModDependencyScreen() {}

	@Override
	public void initGui() {
		super.initGui();
		button(-40, -30, 80, 16, Align.CENTER, Align.END, Texts.translatable("gui.carbonconfig.back"), T -> onClose());
		button(-115, -48, 100, 16, Align.END, Align.END, Texts.translatable("gui.carbonconfig.dependency.config"), T -> openConfigs());
		button(-115, -30, 100, 16, Align.END, Align.END, Texts.translatable("gui.carbonconfig.dependency.mods"), T -> openMods());
	}
	
	private void openConfigs() {
		setScreen(GuiScreen.isShiftKeyDown() ? new GuiModList(this) : new ConfigListScreen(this, BackgroundTexture.DEFAULT.asHolder(), EventHandler.INSTANCE.getAllConfigs()));
	}
	
	private void openMods() {
		try { Desktop.getDesktop().open(Loader.instance().getConfigDir().toPath().getParent().resolve("mods").toFile()); }
		catch(Exception e) { e.printStackTrace(); }
	}
	
	@Override
	public void tick() {
		if(!GuiScreen.isShiftKeyDown() && !nodes.isEmpty()) return;
		nodes.clear();
		nodes.add(new ModNode("minecraft"));
		nodes.addAll(Loader.instance().getActiveModList().stream().map(ModContainer::getModId).map(ModNode::new).collect(Collectors.toList()));
		Set<String> installedMods = nodes.stream().map(ModNode::id).collect(Collectors.toSet());
		Set<String> requirements = Loader.instance().getActiveModList().stream().flatMap(T -> Stream.concat(T.getRequirements().stream(), T.getDependencies().stream())).map(ArtifactVersion::getLabel).filter(T -> !T.equalsIgnoreCase("*")).filter(T -> !installedMods.contains(T)).collect(Collectors.toSet());
		requirements.forEach(T -> nodes.add(new DependencyNode(T)));
		
		Map<String, ModNode> mapped = new Object2ObjectOpenHashMap<>();
		nodes.forEach(T -> mapped.put(T.modId, T));
		ModNode node = mapped.get("minecraft");
		nodes.remove(mapped.put("forge", node));
		nodes.remove(node);
		for(ModContainer info : Loader.instance().getActiveModList()) {
			ModNode owner = mapped.get(info.getModId());
			if(owner == null) continue;
			Set<ModNode> deps = new ObjectOpenHashSet<>();
			for(ArtifactVersion version : info.getDependencies()) {
				ModNode dependency = mapped.get(version.getLabel());
				if(dependency == null || dependency == owner) continue;
				deps.add(dependency);
				dependency.dependants.put(owner, false);
				owner.dependencies.put(dependency, false);
			}
			for(ArtifactVersion version : info.getRequirements()) {
				ModNode dependency = mapped.get(version.getLabel());
				if(dependency == null || dependency == owner) continue;
				deps.add(dependency);
				dependency.dependants.put(owner, true);
				owner.dependencies.put(dependency, true);
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
	public void renderBackground(int mouseX, int mouseY, float partialTicks) {
		if(pos != null && (pos.chunkXPos != mouseX || pos.chunkZPos != mouseY)) {
			int diffX = pos.chunkXPos - mouseX;
			int diffY = pos.chunkZPos - mouseY;
			x -= diffX / scale;
			y -= diffY / scale;
			pos = new ChunkCoordIntPair(mouseX, mouseY);
		}
		drawDefaultBackground();
		int bottomSpace = 75;
		int legendenY = 47;
		
		GuiUtils.fillDropArea(10, 10, width-20, height-20, -3750202, false);
		GuiUtils.fillDropArea(15, 25, width-30, height-bottomSpace-10, -7631989, true);
		drawUnalignedText(Texts.translatable("gui.carbonconfig.dependency.header"), centerX, 12, Align.CENTER, 4210752);
		drawRect(14, height-legendenY, 30, height-(legendenY-fontRendererObj.FONT_HEIGHT), dependencyColors[0] | 0xFF000000);
		drawRect(14, height-(legendenY-fontRendererObj.FONT_HEIGHT-1), 30, height-(legendenY-fontRendererObj.FONT_HEIGHT*2), dependencyColors[1] | 0xFF000000);
		drawRect(14, height-(legendenY-fontRendererObj.FONT_HEIGHT*2-1), 30, height-(legendenY-fontRendererObj.FONT_HEIGHT*3), dependencyColors[2] | 0xFF000000);
		drawRect(14, height-(legendenY-fontRendererObj.FONT_HEIGHT*3-1), 30, height-(legendenY-fontRendererObj.FONT_HEIGHT*4), dependencyColors[3] | 0xFF000000);
		
		GuiUtils.drawFrame(14, height-legendenY, 29, height-(legendenY-fontRendererObj.FONT_HEIGHT)-1, 0xFF404040, 1);
		GuiUtils.drawFrame(14, height-(legendenY-fontRendererObj.FONT_HEIGHT-1), 29, height-(legendenY-fontRendererObj.FONT_HEIGHT*2)-1, 0xFF404040, 1);
		GuiUtils.drawFrame(14, height-(legendenY-fontRendererObj.FONT_HEIGHT*2-1), 29, height-(legendenY-fontRendererObj.FONT_HEIGHT*3)-1, 0xFF404040, 1);
		GuiUtils.drawFrame(14, height-(legendenY-fontRendererObj.FONT_HEIGHT*3-1), 29, height-(legendenY-fontRendererObj.FONT_HEIGHT*4)-1, 0xFF404040, 1);
		drawUnalignedText(Texts.translatable("gui.carbonconfig.dependency.legend"), 14, height-legendenY-fontRendererObj.FONT_HEIGHT-3, Align.START, 4210752);
		
		drawUnalignedText(Texts.translatable("gui.carbonconfig.dependency.required_need"), 31, height-legendenY, Align.START, 4210752);
		drawUnalignedText(Texts.translatable("gui.carbonconfig.dependency.optional_need"), 31, height-(legendenY-fontRendererObj.FONT_HEIGHT-1), Align.START, 4210752);
		drawUnalignedText(Texts.translatable("gui.carbonconfig.dependency.required_have"), 31, height-(legendenY-fontRendererObj.FONT_HEIGHT*2-1), Align.START, 4210752);
		drawUnalignedText(Texts.translatable("gui.carbonconfig.dependency.optional_have"), 31, height-(legendenY-fontRendererObj.FONT_HEIGHT*3-1), Align.START, 4210752);

		GuiUtils.pushScissors(15, 24, width-30, height-bottomSpace-9);
		GuiUtils.renderBackground(15, width-15, 15, height-15, -x*0.2F*(scale*2), -y*0.2F*scale*2, BackgroundTexture.DEFAULT);
		GL11.glPushMatrix();
		GL11.glTranslatef(centerX, centerY, 0F);
		GL11.glScalef(scale, scale, 1F);
		int radius = 10;
		float textScale = Math.min(2.5F, 1F / scale);
		float quadScale = Helpers.clamp(textScale, 1F, 2F)*0.75F;
		for(ModNode node : nodes) {
			int x = (int)(node.x + this.x);
			int y = (int)(node.y + this.y);
			GL11.glPushMatrix();
			GL11.glTranslatef(x, y, 0F);
			GL11.glScalef(quadScale, quadScale, 1F);
			GuiUtils.drawCircle(0, 0, radius, node.modId.hashCode() | 0xFF000000, 0xFF000000, 128, 1F);
			GL11.glPopMatrix();
		}
		if(focused != null) {
			Tessellator tes = Tessellator.instance;
			tes.startDrawing(GL11.GL_TRIANGLES);
			drawNode(focused, false, dependencyColors[2], dependencyColors[3], tes);
			drawNode(focused, true, dependencyColors[0], dependencyColors[1], tes);
	        GL11.glDisable(GL11.GL_TEXTURE_2D);
			GL11.glEnable(GL11.GL_BLEND);
	        OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
			tes.draw();
	        GL11.glEnable(GL11.GL_TEXTURE_2D);
	        GL11.glDisable(GL11.GL_BLEND);
		}
		for(ModNode node : nodes) {
			int x = (int)(node.x + this.x);
			int y = (int)(node.y + this.y);
			GL11.glPushMatrix();
			GL11.glTranslatef(x, y, 0F);
			GL11.glTranslatef(0F, -radius*textScale-fontRendererObj.FONT_HEIGHT, 0F);
			GL11.glScalef(textScale, textScale, 1F);
			boolean notLoaded = node instanceof DependencyNode;
			
			IChatComponent name = Texts.literal(Optional.ofNullable(Loader.instance().getIndexedModList().get(node.id())).map(ModContainer::getName).orElse(Helpers.firstLetterUppercase(node.id()))); 
			drawUnalignedText(name, 0F, 0F, Align.CENTER, notLoaded ? 0xFFFF0000 : -1);
			GL11.glPopMatrix();
		}
		GL11.glPopMatrix();
		GuiUtils.popScissors();
	}
	
	private void drawNode(ModNode source, boolean dep, int requiredColor, int optionalColor, Tessellator builder) {
		float textScale = Math.min(2.5F, 1F / scale);
		float quadScale = Helpers.clamp(textScale, 1F, 2F)*0.75F;
		float radius = (dep ? 2.5F : -2.5F) * quadScale;
		for(Map.Entry<ModNode, Boolean> entry : (dep ? source.dependencies : source.dependants).entrySet()) {
			ModNode child = entry.getKey();
			GuiUtils.drawLine((float)source.x+this.x+radius, (float)source.y+this.y+radius, (float)child.x+this.x+radius, (float)child.y+this.y+radius, 2F, builder, entry.getValue() ? requiredColor : optionalColor);
			if(entry.getValue()) drawNode(child, dep, requiredColor, optionalColor, builder);
		}
	}
	
	@Override
	public void collectTooltips(int mouseX, int mouseY, float partialTicks, Consumer<IChatComponent> tooltips) {
		double radius = 10 * scale;
		float xOff = x * scale;
		float yOff = y * scale;
		for(ModNode node : nodes) {
			int x = (int)(node.x * scale) + centerX;
			int y = (int)(node.y * scale) + centerY;
			if(mouseX >= x-radius+xOff && mouseX <= x+radius+xOff && mouseY >= y-radius+yOff && mouseY <= y+radius+yOff) {
				tooltips.accept(Texts.literal(Optional.ofNullable(Loader.instance().getIndexedModList().get(node.id())).map(ModContainer::getName).orElse(Helpers.firstLetterUppercase(node.id()))));
				if(node instanceof DependencyNode) {
					tooltips.accept(Texts.translatable("gui.carbonconfig.dependency.missing"));
				}
			}
		}
	}
	
	@Override
	public boolean mouseClick(double mouseX, double mouseY, int button) {
		if(super.mouseClick(mouseX, mouseY, button)) return true;
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
				ModContainer info = Loader.instance().getIndexedModList().get(found.id());
				if(info == null && found.id().equals("minecraft")) {
					info = Loader.instance().getMinecraftModContainer();
				}
				if(info != null) {
					item.addLabel(info.getName());
					item.addLabel(Texts.translatable("gui.carbonconfig.dependency.version", info.getVersion().toString()));
					SubMenuItem desc = new SubMenuItem("gui.carbonconfig.dependency.description");
					desc.addLabel(info.getMetadata().description.trim());
					item.addSubMenu("desc", desc);
					
					try
					{
						URL url = new URL(info.getMetadata().url);
						item.addNode("gui.carbonconfig.dependency.open_page", () -> {
							try { Desktop.getDesktop().browse(url.toURI()); }
							catch(Exception e) { e.printStackTrace(); }
						});					
					}
					catch(Exception e) {}
					
					IModConfigs configs = EventHandler.INSTANCE.getConfigsForMod(info.getModId());
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
			pos = new ChunkCoordIntPair((int)mouseX, (int)mouseY);
			return true;
		}
		focused = null;
		return false;
	}
	
	@Override
	public boolean mouseRelease(double pMouseX, double pMouseY, int pButton) {
		if(pos != null) {
			pos = null;
			return true;
		}
		if(super.mouseRelease(pMouseX, pMouseY, pButton)) return true;
		return false;
	}
	
	@Override
	public boolean mouseScroll(double pMouseX, double pMouseY, double pDelta) {
		scale = Helpers.clamp(scale - (float)pDelta * 0.01F * (GuiScreen.isShiftKeyDown() ? 10 : 1F), 0.025F, 2F);
		return true;
	}
	
	static class DependencyNode extends ModNode {
		public DependencyNode(String modId) {
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
