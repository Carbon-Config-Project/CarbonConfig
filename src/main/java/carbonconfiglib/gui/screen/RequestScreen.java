package carbonconfiglib.gui.screen;

import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Predicate;

import carbonconfiglib.gui.api.BackgroundTexture.BackgroundHolder;
import carbonconfiglib.gui.api.IModConfig;
import carbonconfiglib.gui.api.IRequestScreen;
import carbonconfiglib.gui.config.Element;
import carbonconfiglib.gui.config.ListScreen;
import carbonconfiglib.gui.screen.ConfigScreen.Navigator;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;

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
public class RequestScreen extends ListScreen implements IRequestScreen
{
	static final ITextComponent REQUEST = new TextComponentTranslation("gui.carbonconfig.requesting_config");
	static final ITextComponent[] ANIMATION = new ITextComponent[] {
			new TextComponentString("Ooooo").setStyle(new Style().setColor(TextFormatting.GRAY)),
			new TextComponentString("oOooo").setStyle(new Style().setColor(TextFormatting.GRAY)),
			new TextComponentString("ooOoo").setStyle(new Style().setColor(TextFormatting.GRAY)),
			new TextComponentString("oooOo").setStyle(new Style().setColor(TextFormatting.GRAY)),
			new TextComponentString("ooooO").setStyle(new Style().setColor(TextFormatting.GRAY)),
	};
	GuiScreen parent;
	IModConfig config;
	UUID requestId;
	Predicate<PacketBuffer> result;
	Navigator nav;
	int tick = 0;
	
	public RequestScreen(BackgroundHolder customTexture, Navigator nav, GuiScreen parent, IModConfig config) {
		super(new TextComponentString("Request GuiScreen"), customTexture);
		this.parent = parent;
		this.nav = nav;
		requestId = UUID.randomUUID();
		this.config = config.loadFromNetworking(requestId, T -> result = T);
	}
	
	@Override
	protected void collectElements(Consumer<Element> elements) {}
	
	@Override
	public void receiveConfigData(UUID requestId, PacketBuffer buf) {
		if(!this.requestId.equals(requestId)) return;
		if(result == null) return;
		if(result.test(buf)) {
			mc.displayGuiScreen(new ConfigScreen(nav, config, parent, getCustomTexture()));
			return;
		}
		mc.displayGuiScreen(parent);
	}
	
	@Override
	public void tick() {
		super.tick();
		tick++;
		if(tick > 400) mc.displayGuiScreen(parent);
	}
	
	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		super.drawScreen(mouseX, mouseY, partialTicks);
		String request = REQUEST.getFormattedText();
		fontRenderer.drawString(request, width / 2 - fontRenderer.getStringWidth(request) / 2, height / 2 - 12, -1);
		int index = (tick / 5) % 8;
		if(index >= 5) index = 8-index;
		String animation = ANIMATION[index].getFormattedText();
		fontRenderer.drawString(animation, width / 2 - fontRenderer.getStringWidth(animation) / 2, height / 2, -1);
		int timeout = (401 - tick) / 20;
		if(timeout <= 18) {
			ITextComponent draw = new TextComponentTranslation("gui.carbonconfig.timeout", timeout).setStyle(new Style().setColor(TextFormatting.RED));
			String drawing = draw.getFormattedText();
			fontRenderer.drawString(drawing, width / 2 - fontRenderer.getStringWidth(drawing) / 2, height / 2 + 12, -1);
		}
	}
	
}
