package carbonconfiglib.gui.screen;

import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Consumer;

import javax.imageio.ImageIO;

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
import carbonconfiglib.gui.widgets.screen.IInteractable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.storage.WorldSummary;
import speiger.src.collections.objects.utils.ObjectLists;

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
	private static final ITextComponent TEXT = new TextComponentTranslation("gui.carbonconfig.select_world");
	IModConfig config;
	GuiScreen parent;
	
	public SelectFileScreen(ITextComponent name, BackgroundHolder customTexture, GuiScreen parent, IModConfig config) {
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
	public void initGui() {
		super.initGui();
		int x = width / 2;
		int y = height;
		addWidget(new CarbonButton(x-80, y-27, 160, 20, I18n.format("gui.carbonconfig.back"), T -> onClose()));
	}
	
	@Override
	protected int getElementHeight() {
		return 28;
	}
	
	@Override
	public void onClose() {
		mc.displayGuiScreen(parent);
	}
	
	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		super.drawScreen(mouseX, mouseY, partialTicks);
		String text = TEXT.getFormattedText();
		fontRendererObj.drawString(text, (width/2)-(fontRendererObj.getStringWidth(text)/2), 8, -1);
	}
	
	@Override
	public void onGuiClosed() {
		allEntries.forEach(this::cleanup);
		super.onGuiClosed();
	}
	
	private void cleanup(Element element) {
		if(element instanceof WorldElement) {
			((WorldElement)element).cleanup();
		}
	}
	
	private static class WorldElement extends Element {
		IModConfig config;
		IConfigTarget target;
		GuiScreen parent;
		CarbonButton button;
		ITextComponent title;
		ITextComponent path;
		DynamicTexture texture;
		Navigator nav;
		
		public WorldElement(IConfigTarget target, IModConfig config, GuiScreen parent, ITextComponent prevName) {
			super(new TextComponentString(target.getName()));
			nav = new Navigator(prevName);
			nav.setScreenForLayer(parent);
			this.target = target;
			this.config = config;
			this.parent = parent;
		}
		
		@Override
		public void init() {
			button = new CarbonButton(0, 0, 62, 20, I18n.format("gui.carbonconfig.pick"), this::onPick);
			if(target instanceof WorldConfigTarget) {
				WorldConfigTarget world = (WorldConfigTarget)target;
				WorldSummary sum = world.getSummary();
				loadIcon(Minecraft.getMinecraft().getSaveLoader().getFile(sum.getFileName(), "icon.png").toPath());
				title = new TextComponentString(sum.getDisplayName());
				path = new TextComponentString(sum.getFileName()).setStyle(new Style().setColor(TextFormatting.GRAY));
			}
			else 
			{
				title = new TextComponentString(target.getName());
				Path folder = target.getFolder();
				int index = folder.getNameCount();
				path = new TextComponentString(folder.subpath(index-3, index).toString()).setStyle(new Style().setColor(TextFormatting.GRAY));
			}
		}
		
		@Override
		public void render(int x, int top, int left, int width, int height, int mouseX, int mouseY, boolean selected, float partialTicks) {
			button.xPosition = left+width-62;
			button.yPosition = top + 2;
			button.render(mc, mouseX, mouseY, partialTicks);
			GuiUtils.drawScrollingString(font, title.getFormattedText(), left+5, top+2, 150, 10, GuiAlign.LEFT, -1, 0);
			GuiUtils.drawScrollingString(font, path.getFormattedText(), left+5, top+12, 150, 10, GuiAlign.LEFT, -1, 0);
			if(texture != null) {
				GlStateManager.bindTexture(texture.getGlTextureId());
				GuiUtils.drawTextureRegion(left-24, top, 0F, 0F, 24F, 24F, 64F, 64F, 64F, 64F);
			}
		}
		
		private void loadIcon(Path iconFile) {
			try(InputStream stream = Files.newInputStream(iconFile)) {
				BufferedImage image = ImageIO.read(stream);
				if(image == null || image.getWidth() != 64 || image.getHeight() != 64) return;
				texture = new DynamicTexture(image);
			}
			catch(Exception e) { e.printStackTrace(); }
		}
		
		@Override
		public List<? extends IInteractable> children() {
			return ObjectLists.singleton(button);
		}
		
		private void onPick(GuiButton button) {
			IModConfig config = this.config.loadFromFile(target.getConfigFile());
			if(config == null) {
				mc.displayGuiScreen(parent);
				return;
			}
			mc.displayGuiScreen(new ConfigScreen(nav.add(path.createCopy().setStyle(new Style().setColor(TextFormatting.WHITE))), config, parent, owner.getCustomTexture()));
		}
		
		private void cleanup() {
			if(texture == null) return;
			texture.deleteGlTexture();
			texture = null;
		}
	}
}
