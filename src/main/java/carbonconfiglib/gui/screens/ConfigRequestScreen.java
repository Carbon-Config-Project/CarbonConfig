package carbonconfiglib.gui.screens;

import java.util.UUID;
import java.util.function.Predicate;

import carbonconfiglib.gui.api.IModConfig;
import carbonconfiglib.gui.api.IRequestReceiver;
import carbonconfiglib.gui.api.background.BackgroundTexture.BackgroundHolder;
import carbonconfiglib.gui.base.helpers.Align;
import carbonconfiglib.gui.base.helpers.GuiUtils;
import carbonconfiglib.gui.base.screen.BaseCarbonScreen;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import speiger.src.collections.objects.lists.ObjectArrayList;

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
public class ConfigRequestScreen extends BaseCarbonScreen implements IRequestReceiver
{
	static final Component REQUEST = Component.translatable("gui.carbonconfig.requesting_config");
	static final Component[] ANIMATION = new Component[] {
			Component.literal("Ooooo").withStyle(ChatFormatting.GRAY),
			Component.literal("oOooo").withStyle(ChatFormatting.GRAY),
			Component.literal("ooOoo").withStyle(ChatFormatting.GRAY),
			Component.literal("oooOo").withStyle(ChatFormatting.GRAY),
			Component.literal("ooooO").withStyle(ChatFormatting.GRAY),
	};
	Screen parent;
	IModConfig config;
	UUID requestId;
	BackgroundHolder texture;
	Predicate<FriendlyByteBuf> result;
	String[] walker;
	int tick = 0;
	
	public ConfigRequestScreen(BackgroundHolder customTexture, Screen parent, IModConfig config, String...walker) {
		this.texture = customTexture;
		this.parent = parent;
		this.walker = walker;
		IRequestReceiver.Impl.register(this);
		requestId = UUID.randomUUID();
		this.config = config.loadFromNetworking(requestId, T -> result = T);
	}
	
	@Override
	public void receiveConfigData(UUID requestId, FriendlyByteBuf buf) {
		if(!this.requestId.equals(requestId)) return;
		if(result == null) return;
		if(result.test(buf)) {
			minecraft.setScreen(new ConfigScreen(config, texture, parent).withWalker(walker == null || walker.length <= 0 ? null : ObjectArrayList.wrap(walker)));
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
	public void removed() {
		IRequestReceiver.Impl.unregister(this);
		super.removed();
	}
	
	@Override
	public void drawBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
		int minY = (int)(height * 0.15F);
		if(!texture.shouldDisableInLevel() || minecraft.level == null) GuiUtils.renderBackground(0, width, 0, height, 0F, texture.getTexture());
		GuiUtils.renderListOverlay(0, width, minY, (int)(height * 0.8F), width, height, texture.getTexture());
	}
	
	@Override
	public void drawForeground(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
		GuiUtils.renderListShadow(0, width, (int)(height * 0.15F), (int)(height * 0.8F), width, height);
		drawText(graphics, REQUEST, 0, -15, Align.CENTER, -1);
		int index = (tick / 5) % 8;
		if(index >= 5) index = 8-index;
		drawText(graphics, ANIMATION[index], 0, -3, Align.CENTER, -1);
		int timeout = (401 - tick) / 20;
		if(timeout > 18) return;
		drawText(graphics,  Component.translatable("gui.carbonconfig.timeout", timeout).withStyle(ChatFormatting.RED), 0, 9, Align.CENTER, -1);

	}
}
