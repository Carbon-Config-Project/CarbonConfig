package carbonconfiglib.gui.screens;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import com.mojang.math.Matrix4f;

import carbonconfiglib.CarbonConfig;
import carbonconfiglib.gui.base.screen.BaseCarbonScreen;
import carbonconfiglib.gui.base.widgets.CarbonButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.fml.loading.moddiscovery.ModInfo;
import net.minecraftforge.forgespi.language.IModInfo.ModVersion;

public class ModListScreen extends BaseCarbonScreen {

	List<ModElement> mods = new ArrayList<>();
	List<ModButton> activeElements = new ArrayList<>();
	float xOffset, yOffset, zoom;
	Screen parentScreen;

	public ModListScreen(Screen parent) {
		this.parentScreen = parent;
	}

	public void gatherMods() {
		FMLLoader.getLoadingModList().getMods().forEach(mod -> {
			mods.add(new ModElement(mod));
		});
	}

	public void createButtons() {
		List<ModElement> modCopy = new ArrayList<>(mods);
		for (ModElement mod : modCopy) {
			activeElements.add(createButton(mod, 64));
		}
	}

	public ModButton createButton(ModElement mod, int size) {
		return new ModButton(mod, size, size, (button) -> {
			listArea(button.x, button.y + button.getHeight(), size, size, null);
		});
	}

	public void findDependencies(ModElement element) {
		List<ModElement> dependencies = new ArrayList<>();
		element.mod.getDependencies().forEach(dependant -> {

		});
	}

	@Override
	public void renderBackground(PoseStack stack, int mouseX, int mouseY, float partialTicks) {
		fill(stack, 0, 0, this.width, this.height, 0xff585a5e);

		float x = width * 0.1f, y = height * 0.1f, maxX = width * 0.9f, maxY = height * 0.9f;

		Matrix4f matrix = stack.last().pose();
		Tesselator tessellator = Tesselator.getInstance();
		BufferBuilder bufferbuilder = tessellator.getBuilder();
		RenderSystem.setShader(CarbonConfig::getBackgroundShader);
		bufferbuilder.begin(Mode.QUADS, CarbonConfig.BACKGROUND_SCREEN);
		bufferbuilder.vertex(matrix, x, maxY, 2.f).endVertex();
		bufferbuilder.vertex(matrix, maxX, maxY, 2.f).endVertex();
		bufferbuilder.vertex(matrix, maxX, y, 2.f).endVertex();
		bufferbuilder.vertex(matrix, x, y, 2.f).endVertex();
		tessellator.end();
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

		int x, y;
		ModElement mod;

		public ModButton(ModElement mod, int pWidth, int pHeight, OnPress listener) {
			super(0, 0, pWidth, pHeight, Component.empty(), listener);
			this.mod = mod;
		}

		public void setPosition(int x, int y) {
			this.x = x;
			this.y = y;
		}

	}

	public static enum ModListState {
		NORMAL, DEPENDENCIES, DEPENDANTS;
	}

}
