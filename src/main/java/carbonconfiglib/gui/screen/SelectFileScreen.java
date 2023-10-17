package carbonconfiglib.gui.screen;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Consumer;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import carbonconfiglib.gui.api.BackgroundTexture.BackgroundHolder;
import carbonconfiglib.gui.api.IModConfig;
import carbonconfiglib.gui.api.IModConfig.IConfigTarget;
import carbonconfiglib.gui.api.IModConfig.WorldConfigTarget;
import carbonconfiglib.gui.config.ConfigElement.GuiAlign;
import carbonconfiglib.gui.config.Element;
import carbonconfiglib.gui.config.ListScreen;
import carbonconfiglib.gui.screen.ConfigScreen.Navigator;
import carbonconfiglib.gui.widgets.CarbonButton;
import carbonconfiglib.gui.widgets.GuiUtils;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.objects.ObjectLists;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.level.storage.LevelSummary;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Consumer;

/**
 * Copyright 2023 Speiger, Meduris
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
public class SelectFileScreen extends ListScreen
{
	private static final Component TEXT = new TranslatableComponent("gui.carbonconfig.select_world");
	IModConfig config;
	Screen parent;
	
	public SelectFileScreen(Component name, BackgroundHolder customTexture, Screen parent, IModConfig config) {
		super(name, customTexture);
		this.config = config;
		this.parent = parent;
	}
	
	@Override
	protected void collectElements(Consumer<Element> elements) {
		for(IConfigTarget target : config.getPotentialFiles()) {
			elements.accept(new WorldElement(target, config, parent, title));
		}
	}
	
	@Override
	protected void init() {
		super.init();
		int x = width / 2;
		int y = height;
		addRenderableWidget(new CarbonButton(x-80, y-27, 160, 20, new TranslatableComponent("gui.carbonconfig.back"), T -> onClose()));
	}
	
	@Override
	protected int getElementHeight() {
		return 28;
	}
	
	@Override
	public void onClose() {
		minecraft.setScreen(parent);
	}
	
	@Override
	public void render(PoseStack stack, int mouseX, int mouseY, float partialTicks) {
		super.render(stack, mouseX, mouseY, partialTicks);
		font.draw(stack, TEXT, (width/2)-(font.width(TEXT)/2), 8, -1);
	}
	
	@Override
	public void removed() {
		allEntries.forEach(this::cleanup);
		super.removed();
	}
	
	private void cleanup(Element element) {
		if(element instanceof WorldElement) {
			((WorldElement)element).cleanup();
		}
	}
	
	private static class WorldElement extends Element {
		IModConfig config;
		IConfigTarget target;
		Screen parent;
		Button button;
		Component title;
		Component path;
		DynamicTexture texture;
		Navigator nav;
		
		public WorldElement(IConfigTarget target, IModConfig config, Screen parent, Component prevName) {
			super(new TextComponent(target.getName()));
			nav = new Navigator(prevName);
			nav.setScreenForLayer(parent);
			this.target = target;
			this.config = config;
			this.parent = parent;
		}
		
		@Override
		public void init() {
			button = new CarbonButton(0, 0, 62, 20, new TranslatableComponent("gui.carbonconfig.pick"), this::onPick);
			if(target instanceof WorldConfigTarget) {
				WorldConfigTarget world = (WorldConfigTarget)target;
				LevelSummary sum = world.getSummary();
				loadIcon(sum.getIcon().toPath());
				title = new TextComponent(sum.getLevelName());
				path = new TextComponent(sum.getLevelId()).withStyle(ChatFormatting.GRAY);
			}
			else 
			{
				title = new TextComponent(target.getName());
				Path folder = target.getFolder();
				int index = folder.getNameCount();
				path = new TextComponent(folder.subpath(index-3, index).toString()).withStyle(ChatFormatting.GRAY);
			}
		}
		
		@Override
		public void render(PoseStack poseStack, int x, int top, int left, int width, int height, int mouseX, int mouseY, boolean selected, float partialTicks) {
			button.x = left+width-62;
			button.y = top + 2;
			button.render(poseStack, mouseX, mouseY, partialTicks);
			GuiUtils.drawScrollingString(poseStack, font, title, left+5, top+2, 150, 10, GuiAlign.LEFT, -1, 0);
			GuiUtils.drawScrollingString(poseStack, font, path, left+5, top+12, 150, 10, GuiAlign.LEFT, -1, 0);
			if(texture != null) {
				RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
				RenderSystem.setShaderTexture(0, texture.getId());
				GuiUtils.drawTextureRegion(poseStack, left-24, top, 0F, 0F, 24F, 24F, 64F, 64F, 64F, 64F);
			}
		}
		
		private void loadIcon(Path iconFile) {
			try(InputStream stream = Files.newInputStream(iconFile)) {
				NativeImage image = NativeImage.read(stream);
				if(image == null || image.getWidth() != 64 || image.getHeight() != 64) return;
				texture = new DynamicTexture(image);
				texture.upload();
			}
			catch(Exception e) { e.printStackTrace(); }
		}
		
		@Override
		public List<? extends GuiEventListener> children() {
			return ObjectLists.singleton(button);
		}
		
		private void onPick(Button button) {
			IModConfig config = this.config.loadFromFile(target.getConfigFile());
			if(config == null) {
				mc.setScreen(parent);
				return;
			}
			mc.setScreen(new ConfigScreen(nav.add(path.copy().withStyle(ChatFormatting.WHITE)), config, parent, owner.getCustomTexture()));
		}
		
		private void cleanup() {
			if(texture == null) return;
			texture.close();
			texture = null;
		}
	}
}
