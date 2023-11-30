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
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;

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
	static final IChatComponent REQUEST = new ChatComponentTranslation("gui.carbonconfig.requesting_config");
	static final IChatComponent[] ANIMATION = new IChatComponent[] {
			new ChatComponentText("Ooooo").setChatStyle(new ChatStyle().setColor(EnumChatFormatting.GRAY)),
			new ChatComponentText("oOooo").setChatStyle(new ChatStyle().setColor(EnumChatFormatting.GRAY)),
			new ChatComponentText("ooOoo").setChatStyle(new ChatStyle().setColor(EnumChatFormatting.GRAY)),
			new ChatComponentText("oooOo").setChatStyle(new ChatStyle().setColor(EnumChatFormatting.GRAY)),
			new ChatComponentText("ooooO").setChatStyle(new ChatStyle().setColor(EnumChatFormatting.GRAY)),
	};
	GuiScreen parent;
	IModConfig config;
	UUID requestId;
	Predicate<PacketBuffer> result;
	Navigator nav;
	int tick = 0;
	
	public RequestScreen(BackgroundHolder customTexture, Navigator nav, GuiScreen parent, IModConfig config) {
		super(new ChatComponentText("Request GuiScreen"), customTexture);
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
		fontRendererObj.drawString(request, width / 2 - fontRendererObj.getStringWidth(request) / 2, height / 2 - 12, -1);
		int index = (tick / 5) % 8;
		if(index >= 5) index = 8-index;
		String animation = ANIMATION[index].getFormattedText();
		fontRendererObj.drawString(animation, width / 2 - fontRendererObj.getStringWidth(animation) / 2, height / 2, -1);
		int timeout = (401 - tick) / 20;
		if(timeout <= 18) {
			IChatComponent draw = new ChatComponentTranslation("gui.carbonconfig.timeout", timeout).setChatStyle(new ChatStyle().setColor(EnumChatFormatting.RED));
			String drawing = draw.getFormattedText();
			fontRendererObj.drawString(drawing, width / 2 - fontRendererObj.getStringWidth(drawing) / 2, height / 2 + 12, -1);
		}
	}
	
}
