package carbonconfiglib.gui.screens;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import com.mojang.blaze3d.systems.RenderSystem;

import carbonconfiglib.gui.api.IModConfig;
import carbonconfiglib.gui.api.IModConfig.IConfigTarget;
import carbonconfiglib.gui.api.IModConfig.WorldConfigTarget;
import carbonconfiglib.gui.api.Texts;
import carbonconfiglib.gui.api.background.BackgroundTexture.BackgroundHolder;
import carbonconfiglib.gui.base.helpers.Align;
import carbonconfiglib.gui.base.helpers.GuiUtils;
import carbonconfiglib.gui.base.screen.BaseCarbonScreen;
import carbonconfiglib.gui.base.widgets.CarbonButton;
import carbonconfiglib.gui.base.widgets.CarbonEditBox.TextState;
import carbonconfiglib.gui.base.widgets.CarbonList.ListEntry;
import carbonconfiglib.gui.base.widgets.CarbonList.ListState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.NativeImage;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.storage.WorldSummary;

/**
 * Copyright 2026 Speiger, Meduris
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
public class ConfigSelectScreen extends BaseCarbonScreen
{
	ListState<WorldElement> listState = new ListState<>();
	TextState searchState = new TextState().setSuggestion(I18n.format("gui.carbonconfig.search")).setCallback(listState::search);
	BackgroundHolder holder;
	ITextComponent header;
	Screen parent;
	
	public ConfigSelectScreen(Screen parent, BackgroundHolder holder, IModConfig config) {
		this.parent = parent;
		this.holder = holder;
		header = Texts.translatable("gui.carbonconfig.select_world");
		for(IConfigTarget target : config.getPotentialFiles()) {
			listState.add(new WorldElement(config, target, holder, parent));
		}
	}

	@Override
	protected void init() {
		super.init();
		listState.forEach(WorldElement::init);
		int searchWidth = (int)(width * 0.3F);
		int minX = (int)(width * 0.15F);
		int maxX = (int)(width * 0.8F) - minX;
		int minY = (int)(height * 0.15F);
		int maxY = (int)(height * 0.8F) - minY;
		modlogo(2, 2, minY - 4, minY - 4);
		listArea(minX, minY, maxX, maxY, listState);
		text(-(searchWidth >> 1), minY - 20, searchWidth, 16, Align.CENTER, Align.START, searchState);
		button(-80, -35, 160, 20, Align.CENTER, Align.END, Texts.translatable("gui.carbonconfig.back"), T -> onClose());
	}
	
	@Override
	public void renderBackground(int mouseX, int mouseY, float partialTicks) {
		if(!holder.shouldDisableInLevel() || minecraft.world == null) GuiUtils.renderBackground(0, width, 0, height, 0F, holder.getTexture());
		GuiUtils.renderListOverlay(0, width, (int)(height * 0.15F), (int)(height * 0.8F), width, height, holder.getTexture());
	}
	
	@Override
	public void renderForeground(int mouseX, int mouseY, float partialTicks) {
		GuiUtils.renderListShadow(0, width, (int)(height * 0.15F), (int)(height * 0.8F), width, height);
		GuiUtils.drawScrollingShadowText(font, header, 0, 0, width, (int)(height * 0.15F)-20, Align.CENTER, -1, 0);
	}
	
	@Override
	public void onClose() {
		setScreen(parent);
	}
	
	@Override
	public void removed() {
		listState.getNodes().forEach(WorldElement::cleanup);
		super.removed();
	}
	
	
	public static class WorldElement extends ListEntry<WorldElement> {
		IModConfig config;
		IConfigTarget target;
		Screen parent;
		BackgroundHolder holder;
		DynamicTexture texture;
		ITextComponent title;
		ITextComponent path;
		CarbonButton button;
		
		public WorldElement(IModConfig config, IConfigTarget target, BackgroundHolder holder, Screen parent) {
			this.config = config;
			this.target = target;
			this.holder = holder;
			this.parent = parent;
			if(target instanceof WorldConfigTarget) {
				WorldSummary sum = ((WorldConfigTarget)target).getSummary();
				loadIcon(Minecraft.getInstance().getSaveLoader().getFile(sum.getFileName(), "icon.png").toPath());
				title = Texts.literal(sum.getDisplayName());
				path = Texts.literal(sum.getFileName()).applyTextStyle(TextFormatting.GRAY);
			}
			else {
				title = Texts.literal(target.getName());
				Path folder = target.getFolder();
				int index = folder.getNameCount();
				path = Texts.literal(folder.subpath(index-3, index).toString()).applyTextStyle(TextFormatting.GRAY);
			}
			button = addChild(new CarbonButton(0, 0, 52, 20, Texts.translatable(Files.exists(target.getConfigFile()) ? "gui.carbonconfig.pick" : "gui.carbonconfig.create"), T -> onPick()));
		}
		
		public void init() {
			if(texture != null) return;
			if(target instanceof WorldConfigTarget) {
				loadIcon(Minecraft.getInstance().getSaveLoader().getFile(((WorldConfigTarget)target).getSummary().getFileName(), "icon.png").toPath());
			}
		}
		
		@Override
		protected boolean containsSearch(String searchString) {
			return false;
		}

		@Override
		public void render(int x, int top, int left, int width, int height, int mouseX, int mouseY, boolean selected, float partialTicks) {
			button.x = left+width-62;
			button.y = top + 2;
			button.render(mouseX, mouseY, partialTicks);
			GuiUtils.drawScrollingText(font, title, left+29, top+2, 150, 10, Align.START, -1, 0);
			GuiUtils.drawScrollingText(font, path, left+29, top+12, 150, 10, Align.START, -1, 0);
			if(texture != null) {
				RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
				texture.bindTexture();
				GuiUtils.drawTextureRegion(left, top, 0F, 0F, 24F, 24F, 64F, 64F, 64F, 64F);
			}
		}
		
		private void onPick() {
			Path file = target.getConfigFile();
			if(Files.notExists(file)) {
				if(!config.createConfig(file)) {
					return;
				}
			}
			IModConfig config = this.config.loadFromFile(file);
			if(config == null) {
				Minecraft.getInstance().displayGuiScreen(parent);
				return;
			}
			Minecraft.getInstance().displayGuiScreen(new ConfigScreen(config, holder, parent));
		}
		
		private void loadIcon(Path iconFile) {
			try(InputStream stream = Files.newInputStream(iconFile)) {
				NativeImage image = NativeImage.read(stream);
				if(image == null || image.getWidth() != 64 || image.getHeight() != 64) return;
				texture = new DynamicTexture(image);
				texture.updateDynamicTexture();
			}
			catch(Exception e) { e.printStackTrace(); }
		}
		
		private void cleanup() {
			if(texture == null) return;
			texture.close();
			texture = null;
		}
	}
}
