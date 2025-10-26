package carbonconfiglib.gui.screens;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import com.mojang.math.Matrix4f;

import carbonconfiglib.CarbonConfig;
import carbonconfiglib.gui.base.helpers.GuiUtils;
import carbonconfiglib.gui.base.screen.BaseCarbonScreen;
import carbonconfiglib.gui.base.widgets.CarbonButton;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.fml.loading.moddiscovery.ModInfo;
import net.minecraftforge.forgespi.language.IModInfo.ModVersion;

public class ModListScreen extends BaseCarbonScreen {

	ModListState listState = ModListState.NORMAL;
	List<ModElement> mods = new ArrayList<>();
	List<ModButton> activeElements = new ArrayList<>();
	float[] offset = new float[] { -1.88f, 0.6f, 1.0f };
	final int padding = 4;
	Screen parentScreen;

	public ModListScreen(Screen parent) {
		this.parentScreen = parent;
		gatherMods();
		createButtons();
		changeMode(ModListState.NORMAL, Optional.empty());
		updateButtons();
	}

	@Override
	protected void init() {
		super.init();
		for (ModButton button : activeElements) {
			addRenderableWidget(button);
		}
	}

	public void gatherMods() {
		mods.clear();
		FMLLoader.getLoadingModList().getMods().forEach(mod -> {
			mods.add(new ModElement(mod));
		});
	}

	public void createButtons() {
		List<ModElement> modCopy = new ArrayList<>(mods);
		for (ModElement mod : modCopy) {
			System.out.println("Creating a Button for mod: " + mod.mod.getModId());
			activeElements.add(createButton(mod, 32));
		}
	}

	public ModButton createButton(ModElement mod, int size) {
		return new ModButton(mod, size, size, (button) -> {
			listArea(button.x, button.y + button.getHeight(), size, size, null);
		});
	}
	
	public void updateButtons() {
		for (ModButton button : activeElements) {
			button.setPosition((int) -(offset[0] * 100), (int) (offset[1] * 100));
		}
	}

	public void changeMode(ModListState state, Optional<ModElement> mod) {
		this.listState = state;
		switch (state) {
		case NORMAL:
			normalScatter();
			break;
		case DEPENDENCIES:
			break;
		case DEPENDANTS:
			break;
		}
	}

	public void normalScatter() {
		List<ModButton> copy = new ArrayList<>(activeElements);
		int half = copy.size() / 2, newX = half, newY = newX + (copy.size() % 2);
		for (int y = 0; y < newY; ++y) {
			for (int x = 0; x < newX; ++x) {
				int index = y * newX + x;
				if (index >= copy.size())
					return;
				ModButton button = copy.get(index);
				button.setNativePosition(x * (padding + button.getWidth()), y * (padding + button.getHeight()));
			}
		}
	}

	public void findDependencies(ModElement element) {
//		List<ModElement> dependencies = new ArrayList<>(); Speiger to Xaikii this line was the only warning/error in my code... HAD TO GET RID of it xD
		element.mod.getDependencies().forEach(dependant -> {

		});
	}

	@Override
	public boolean mouseScrolled(double pMouseX, double pMouseY, double pDelta) {
		offset[2] = (float) Mth.clamp(offset[2] + pDelta * 0.05, 0.5, 2.0);
		return super.mouseScrolled(pMouseX, pMouseY, pDelta);
	}

	@Override
	public boolean mouseDragged(double pMouseX, double pMouseY, int pButton, double pDragX, double pDragY) {
		offset[0] -= pDragX * 0.008f / offset[2];
		offset[1] += pDragY * 0.008f / offset[2];
		updateButtons();
		return super.mouseDragged(pMouseX, pMouseY, pButton, pDragX, pDragY);
	}

	@Override
	public void tick() {
		super.tick();
		ShaderInstance shader = CarbonConfig.modListBackground;
		if (shader != null) {
			shader.apply();
			Uniform offset = shader.getUniform("Offset");
			offset.set(this.offset);
			offset.upload();
			shader.clear();
		}
//		System.out.println(offset[0] + " : " + offset[1]);
	}
	
	public void moveScreenToNode(ModButton button) {
		offset[0] = button.x * 0.01f;
		offset[1] = button.y * 0.01f;
	}

	@Override
	public void renderBackground(PoseStack stack, int mouseX, int mouseY, float partialTicks) {
		fill(stack, 0, 0, this.width, this.height, 0xff585a5e);
		float x = width * 0.1f, y = height * 0.1f, maxX = width * 0.9f, maxY = height * 0.9f;
		Matrix4f matrix = stack.last().pose();
		Tesselator tessellator = Tesselator.getInstance();
		BufferBuilder bufferbuilder = tessellator.getBuilder();
		RenderSystem.setShader(CarbonConfig::getBackgroundShader);
		if (Minecraft.getInstance().level == null)
			RenderSystem.setShaderGameTime(System.currentTimeMillis(), partialTicks);

		bufferbuilder.begin(Mode.QUADS, CarbonConfig.BACKGROUND_SCREEN);
		bufferbuilder.vertex(matrix, x, maxY, 2.f).uv(0.f, 1.f).endVertex();
		bufferbuilder.vertex(matrix, maxX, maxY, 2.f).uv(1.f, 1.f).endVertex();
		bufferbuilder.vertex(matrix, maxX, y, 2.f).uv(1.f, 0.f).endVertex();
		bufferbuilder.vertex(matrix, x, y, 2.f).uv(0.f, 0.0f).endVertex();
		tessellator.end();
	}

	@Override
	public void renderWidgets(PoseStack matrix, int mouseX, int mouseY, float partialTicks) {
		GuiUtils.pushScissors((int) (width * 0.1f), (int) (height * 0.1f), (int) (width * 0.8f), (int) (height * 0.8f));
		super.renderWidgets(matrix, mouseX, mouseY, partialTicks);
		GuiUtils.popScissors();
	}

	@Override
	public void renderTooltips(PoseStack matrix, int mouseX, int mouseY, float partialTicks) {
		// TODO Auto-generated method stub
		super.renderTooltips(matrix, mouseX, mouseY, partialTicks);
	}

	public static class ModElement {
		ModInfo mod;

		public ModElement(ModInfo mod) {
			this.mod = mod;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj instanceof String name) {
				return this.mod.getModId().equals(name);
			} else if (obj instanceof ModVersion version) {
				return this.mod.getModId().equals(version.getModId());
			}
			return super.equals(obj);
		}

	}

	public static class ModButton extends CarbonButton {
		ModElement mod;
		int absoluteX = 0, absoluteY = 0;

		public ModButton(ModElement mod, int pWidth, int pHeight, OnPress listener) {
			super(0, 0, pWidth, pHeight, Component.empty(), listener);
			this.mod = mod;
		}

		@Override
		public void renderButton(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
			// TODO Auto-generated method stub
			super.renderButton(pPoseStack, pMouseX, pMouseY, pPartialTick);
//			System.out.println((x) + " : " + (y));
		}

		public void setNativePosition(int x, int y) {
			this.absoluteX = x;
			this.absoluteY = y;
		}

		public void setPosition(int x, int y) {
			this.x = x + absoluteX;
			this.y = y + absoluteY;
		}

		@Override
		public void provideTooltips(int mouseX, int mouseY, java.util.function.Consumer<Component> tooltips) {
			super.provideTooltips(mouseX, mouseY, tooltips);
			if(isHoveredOrFocused()) 
			tooltips.accept(Component.literal(mod.mod.getDisplayName()));
		};

	}

	public static enum ModListState {
		NORMAL, DEPENDENCIES, DEPENDANTS;
	}

}
