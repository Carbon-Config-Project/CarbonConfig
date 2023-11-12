package carbonconfiglib.gui.screen;

import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Predicate;

import com.mojang.blaze3d.vertex.PoseStack;

import carbonconfiglib.gui.api.BackgroundTexture.BackgroundHolder;
import carbonconfiglib.gui.api.IModConfig;
import carbonconfiglib.gui.api.IRequestScreen;
import carbonconfiglib.gui.config.Element;
import carbonconfiglib.gui.config.ListScreen;
import carbonconfiglib.gui.screen.ConfigScreen.Navigator;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;

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
	static final Component REQUEST = new TranslatableComponent("gui.carbonconfig.requesting_config");
	static final Component[] ANIMATION = new Component[] {
			new TextComponent("Ooooo").withStyle(ChatFormatting.GRAY),
			new TextComponent("oOooo").withStyle(ChatFormatting.GRAY),
			new TextComponent("ooOoo").withStyle(ChatFormatting.GRAY),
			new TextComponent("oooOo").withStyle(ChatFormatting.GRAY),
			new TextComponent("ooooO").withStyle(ChatFormatting.GRAY),
	};
	Screen parent;
	IModConfig config;
	UUID requestId;
	Predicate<FriendlyByteBuf> result;
	Navigator nav;
	int tick = 0;
	
	public RequestScreen(BackgroundHolder customTexture, Navigator nav, Screen parent, IModConfig config) {
		super(new TextComponent("Request Screen"), customTexture);
		this.parent = parent;
		this.nav = nav;
		requestId = UUID.randomUUID();
		this.config = config.loadFromNetworking(requestId, T -> result = T);
	}
	
	@Override
	protected void collectElements(Consumer<Element> elements) {}
	
	@Override
	public void receiveConfigData(UUID requestId, FriendlyByteBuf buf) {
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
	public void render(PoseStack stack, int mouseX, int mouseY, float partialTicks) {
		super.render(stack, mouseX, mouseY, partialTicks);
		font.draw(stack, REQUEST, width / 2 - font.width(REQUEST) / 2, height / 2 - 12, -1);
		int index = (tick / 5) % 8;
		if(index >= 5) index = 8-index;
		font.draw(stack, ANIMATION[index], width / 2 - font.width(ANIMATION[index]) / 2, height / 2, -1);
		int timeout = (401 - tick) / 20;
		if(timeout <= 18) {
			Component draw = new TranslatableComponent("gui.carbonconfig.timeout", timeout).withStyle(ChatFormatting.RED);
			font.draw(stack, draw, width / 2 - font.width(draw) / 2, height / 2 + 12, -1);
		}
	}
	
}
