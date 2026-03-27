package carbonconfiglib.gui.screens;


import java.util.function.Consumer;

import org.lwjgl.input.Keyboard;

import carbonconfiglib.gui.base.helpers.Align;
import carbonconfiglib.gui.base.helpers.GuiUtils;
import carbonconfiglib.gui.base.screen.BaseCarbonScreen;
import carbonconfiglib.utils.Helpers;
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
public class MultiChoiceScreen extends BaseCarbonScreen
{
	private final ITextComponent title;
	private final ITextComponent message;
	protected ITextComponent mainButton;
	protected ITextComponent otherButton;
	protected ITextComponent cancelButton;
	protected final Consumer<Result> callback;

	public MultiChoiceScreen(Consumer<Result> callback, ITextComponent title, ITextComponent message, ITextComponent mainButton) {
		this(callback, title, message, mainButton, null, null);
	}
	
	public MultiChoiceScreen(Consumer<Result> callback, ITextComponent title, ITextComponent message, ITextComponent mainButton, ITextComponent cancel) {
		this(callback, title, message, mainButton, null, cancel);
	}
	
	public MultiChoiceScreen(Consumer<Result> callback, ITextComponent title, ITextComponent message, ITextComponent mainButton, ITextComponent otherButton, ITextComponent cancelButton) {
		this.title = title;
		this.callback = callback;
		this.message = message;
		this.mainButton = mainButton;
		this.otherButton = otherButton;
		this.cancelButton = cancelButton;
	}
	
	@Override
	public void initGui() {
		super.initGui();
		this.addButtons(Helpers.clamp(this.messageTop() + this.messageHeight() + 20, this.height / 6 + 96, this.height - 24) - centerY);
	}
	
	protected void addButtons(int y) {
		boolean singleOption = otherButton == null && cancelButton == null;
		boolean dualOption = otherButton == null && cancelButton != null;
		int start = dualOption ? 50 : (singleOption ? 50 : 105);
		int size = dualOption ? 100 : (singleOption ? 200 : 100);
		
		button(-50 - start, y, size, 20, Align.CENTER, Align.CENTER, mainButton, T -> callback.accept(Result.MAIN));
		if(singleOption) return;
		button(dualOption ? 5 : 55, y, size, 20, Align.CENTER, Align.CENTER, cancelButton, T -> callback.accept(Result.CANCEL));
		if(dualOption) return;
		button(-50, y, size, 20, Align.CENTER, Align.CENTER, otherButton, T -> callback.accept(Result.OTHER));
	}
	
	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks){
		this.drawDefaultBackground();
		drawCenteredString(this.fontRendererObj, this.title.getFormattedText(), this.width / 2, this.titleTop(), 16777215);
		drawSplitText(message, 0, messageTop()-centerY, Align.CENTER, width-50, -1);
		super.drawScreen(mouseX, mouseY, partialTicks);
	}
	
	private int titleTop() {
		int i = (this.height - this.messageHeight()) / 2;
		return Helpers.clamp(i - 20 - 9, 10, 80);
	}
	
	private int messageTop() {
		return this.titleTop() + 20;
	}
	
	private int messageHeight() {
		return GuiUtils.splitLines(fontRendererObj, message, width-50).size() * 9;
	}
	
	@Override
	public boolean shouldCloseOnEsc() {
		return false;
	}
	
	@Override
	public boolean charTyped(char character, int keyCode) {
		if(keyCode == Keyboard.KEY_ESCAPE) {
			this.callback.accept(Result.CANCEL);
			return true;			
		}
		return super.charTyped(character, keyCode);
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
