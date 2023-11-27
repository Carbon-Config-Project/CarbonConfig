package carbonconfiglib.gui.screen;

import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Predicate;

import com.mojang.blaze3d.matrix.MatrixStack;

import carbonconfiglib.gui.api.BackgroundTexture.BackgroundHolder;
import carbonconfiglib.gui.api.IModConfig;
import carbonconfiglib.gui.api.IRequestScreen;
import carbonconfiglib.gui.config.Element;
import carbonconfiglib.gui.config.ListScreen;
import carbonconfiglib.gui.screen.ConfigScreen.Navigator;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

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
	static final ITextComponent REQUEST = new TranslationTextComponent("gui.carbonconfig.requesting_config");
	static final ITextComponent[] ANIMATION = new ITextComponent[] {
			new StringTextComponent("Ooooo").withStyle(TextFormatting.GRAY),
			new StringTextComponent("oOooo").withStyle(TextFormatting.GRAY),
			new StringTextComponent("ooOoo").withStyle(TextFormatting.GRAY),
			new StringTextComponent("oooOo").withStyle(TextFormatting.GRAY),
			new StringTextComponent("ooooO").withStyle(TextFormatting.GRAY),
	};
	Screen parent;
	IModConfig config;
	UUID requestId;
	Predicate<PacketBuffer> result;
	Navigator nav;
	int tick = 0;
	
	public RequestScreen(BackgroundHolder customTexture, Navigator nav, Screen parent, IModConfig config) {
		super(new StringTextComponent("Request Screen"), customTexture);
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
			minecraft.setScreen(new ConfigScreen(nav, config, parent, getCustomTexture()));
			return;
		}
		minecraft.setScreen(parent);
	}
	
	@Override
	public void tick() {
		super.tick();
		tick++;
		if(tick > 400) minecraft.setScreen(parent);
	}
	
	@Override
	public void render(MatrixStack stack, int mouseX, int mouseY, float partialTicks) {
		super.render(stack, mouseX, mouseY, partialTicks);
		font.draw(stack, REQUEST, width / 2 - font.width(REQUEST) / 2, height / 2 - 12, -1);
		int index = (tick / 5) % 8;
		if(index >= 5) index = 8-index;
		font.draw(stack, ANIMATION[index], width / 2 - font.width(ANIMATION[index]) / 2, height / 2, -1);
		int timeout = (401 - tick) / 20;
		if(timeout <= 18) {
			ITextComponent draw = new TranslationTextComponent("gui.carbonconfig.timeout", timeout).withStyle(TextFormatting.RED);
			font.draw(stack, draw, width / 2 - font.width(draw) / 2, height / 2 + 12, -1);
		}
	}
	
}
