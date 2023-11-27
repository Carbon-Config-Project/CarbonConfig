package carbonconfiglib.gui.screen;

import java.util.function.Consumer;

import com.mojang.blaze3d.matrix.MatrixStack;

import carbonconfiglib.gui.widgets.CarbonButton;
import net.minecraft.client.gui.IBidiRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;

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
	private final ITextComponent message;
	private IBidiRenderer multilineMessage = IBidiRenderer.EMPTY;
	protected ITextComponent mainButton;
	protected ITextComponent otherButton;
	protected ITextComponent cancelButton;
	protected final Consumer<Result> callback;

	public MultiChoiceScreen(Consumer<Result> callback, ITextComponent title, ITextComponent message, ITextComponent mainButton) {
		this(callback, title, message, mainButton, null, null);
	}
	
	public MultiChoiceScreen(Consumer<Result> callback, ITextComponent title, ITextComponent message, ITextComponent mainButton, ITextComponent otherButton, ITextComponent cancelButton) {
	      super(title);
	      this.callback = callback;
	      this.message = message;
	      this.mainButton = mainButton;
	      this.otherButton = otherButton;
	      this.cancelButton = cancelButton;
	}
	
	@Override
	protected void init() {
		super.init();
		this.multilineMessage = IBidiRenderer.create(this.font, this.message, this.width - 50);
		this.addButtons(MathHelper.clamp(this.messageTop() + this.messageHeight() + 20, this.height / 6 + 96, this.height - 24));
	}
	
	protected void addButtons(int y) {
		boolean singleOption = otherButton == null && cancelButton == null;
		addButton(new CarbonButton(this.width / 2 - 50 - (singleOption ? 50 : 105), y, singleOption ? 200 : 100, 20, this.mainButton, T -> callback.accept(Result.MAIN)));
		if(singleOption) return;
		addButton(new CarbonButton(this.width / 2 - 50, y, 100, 20, this.otherButton, T -> callback.accept(Result.OTHER)));
		addButton(new CarbonButton(this.width / 2 - 50 + 105, y, 100, 20, this.cancelButton, T -> callback.accept(Result.CANCEL)));
	}
	
	@Override
	public void render(MatrixStack stack, int mouseX, int mouseY, float partialTicks){
		this.renderBackground(stack);
		drawCenteredString(stack, this.font, this.title, this.width / 2, this.titleTop(), 16777215);
		this.multilineMessage.renderCentered(stack, this.width / 2, this.messageTop());
		super.render(stack, mouseX, mouseY, partialTicks);
	}
	
	private int titleTop() {
		int i = (this.height - this.messageHeight()) / 2;
		return MathHelper.clamp(i - 20 - 9, 10, 80);
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
