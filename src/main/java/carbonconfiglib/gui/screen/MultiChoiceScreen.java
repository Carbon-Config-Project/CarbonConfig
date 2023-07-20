package carbonconfiglib.gui.screen;

import java.util.function.Consumer;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraftforge.client.gui.widget.ExtendedButton;

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
public class MultiChoiceScreen extends Screen
{
	private final Component message;
	private MultiLineLabel multilineMessage = MultiLineLabel.EMPTY;
	protected Component mainButton;
	protected Component otherButton;
	protected Component cancelButton;
	protected final Consumer<Result> callback;

	public MultiChoiceScreen(Consumer<Result> callback, Component title, Component message, Component mainButton) {
		this(callback, title, message, mainButton, null, null);
	}
	
	public MultiChoiceScreen(Consumer<Result> callback, Component title, Component message, Component mainButton, Component otherButton, Component cancelButton) {
	      super(title);
	      this.callback = callback;
	      this.message = message;
	      this.mainButton = mainButton;
	      this.otherButton = otherButton;
	      this.cancelButton = cancelButton;
	}
	
	@Override
	public Component getNarrationMessage() {
		return CommonComponents.joinForNarration(super.getNarrationMessage(), this.message);
	}
	
	@Override
	protected void init() {
		super.init();
		this.multilineMessage = MultiLineLabel.create(this.font, this.message, this.width - 50);
		this.addButtons(Mth.clamp(this.messageTop() + this.messageHeight() + 20, this.height / 6 + 96, this.height - 24));
	}
	
	protected void addButtons(int y) {
		boolean singleOption = otherButton == null && cancelButton == null;
		addRenderableWidget(new ExtendedButton(this.width / 2 - 50 - (singleOption ? 50 : 105), y, singleOption ? 200 : 100, 20, this.mainButton, T -> callback.accept(Result.MAIN)));
		if(singleOption) return;
		addRenderableWidget(new ExtendedButton(this.width / 2 - 50, y, 100, 20, this.otherButton, T -> callback.accept(Result.OTHER)));
		addRenderableWidget(new ExtendedButton(this.width / 2 - 50 + 105, y, 100, 20, this.cancelButton, T -> callback.accept(Result.CANCEL)));
	}
	
	@Override
	public void render(PoseStack stack, int mouseX, int mouseY, float partialTicks){
		this.renderBackground(stack);
		drawCenteredString(stack, this.font, this.title, this.width / 2, this.titleTop(), 16777215);
		this.multilineMessage.renderCentered(stack, this.width / 2, this.messageTop());
		super.render(stack, mouseX, mouseY, partialTicks);
	}
	
	private int titleTop() {
		int i = (this.height - this.messageHeight()) / 2;
		return Mth.clamp(i - 20 - 9, 10, 80);
	}
	
	private int messageTop() {
		return this.titleTop() + 20;
	}
	
	private int messageHeight() {
		return this.multilineMessage.getLineCount() * 9;
	}
	
	@Override
	public boolean shouldCloseOnEsc() {
		return false;
	}
	
	@Override
	public boolean keyPressed(int mouseButton, int mouseX, int mouseY){
		if(mouseButton == 256) {
			this.callback.accept(Result.CANCEL);
			return true;
		}
		return super.keyPressed(mouseButton, mouseX, mouseY);
	}
	
	public static enum Result {
		MAIN,
		OTHER,
		CANCEL;
		
		public boolean isCancel() {
			return this == CANCEL;
		}
		
		public boolean isMain() {
			return this == MAIN;
		}
		
		public boolean isOther() {
			return this == OTHER;
		}
	}
}
