package carbonconfiglib.gui.screen;

import java.util.List;
import java.util.function.Consumer;

import org.lwjgl.input.Keyboard;

import carbonconfiglib.gui.widgets.CarbonButton;
import carbonconfiglib.gui.widgets.screen.CarbonScreen;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.MathHelper;

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
public class MultiChoiceScreen extends CarbonScreen
{
	protected IChatComponent title;
	private final IChatComponent message;
	protected IChatComponent mainButton;
	protected IChatComponent otherButton;
	protected IChatComponent cancelButton;
	protected List<String> output;
	protected final Consumer<Result> callback;

	public MultiChoiceScreen(Consumer<Result> callback, IChatComponent title, IChatComponent message, IChatComponent mainButton) {
		this(callback, title, message, mainButton, null, null);
	}
	
	public MultiChoiceScreen(Consumer<Result> callback, IChatComponent title, IChatComponent message, IChatComponent mainButton, IChatComponent otherButton, IChatComponent cancelButton) {
	      this.title = title;
	      this.callback = callback;
	      this.message = message;
	      this.mainButton = mainButton;
	      this.otherButton = otherButton;
	      this.cancelButton = cancelButton;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public void initGui() {
		super.initGui();
		output = fontRendererObj.listFormattedStringToWidth(message.getFormattedText(), width-50);
		this.addButtons(MathHelper.clamp_int(this.messageTop() + this.messageHeight() + 20, this.height / 6 + 96, this.height - 24));
	}
	
	protected void addButtons(int y) {
		boolean singleOption = otherButton == null && cancelButton == null;
		addWidget(new CarbonButton(this.width / 2 - 50 - (singleOption ? 50 : 105), y, singleOption ? 200 : 100, 20, this.mainButton.getFormattedText(), T -> callback.accept(Result.MAIN)));
		if(singleOption) return;
		addWidget(new CarbonButton(this.width / 2 - 50, y, 100, 20, this.otherButton.getFormattedText(), T -> callback.accept(Result.OTHER)));
		addWidget(new CarbonButton(this.width / 2 - 50 + 105, y, 100, 20, this.cancelButton.getFormattedText(), T -> callback.accept(Result.CANCEL)));
	}
	
	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks){
		drawDefaultBackground();
		drawCenteredString(this.fontRendererObj, this.title.getFormattedText(), this.width / 2, this.titleTop(), 16777215);
		int yOffset = messageTop();
		for(String s : output) {
			drawCenteredString(this.fontRendererObj, s, this.width / 2, yOffset, 16777215);
			yOffset += 9;
		}
		super.drawScreen(mouseX, mouseY, partialTicks);
	}
	
	private int titleTop() {
		int i = (this.height - this.messageHeight()) / 2;
		return MathHelper.clamp_int(i - 20 - 9, 10, 80);
	}
	
	private int messageTop() {
		return this.titleTop() + 20;
	}
	
	private int messageHeight() {
		return output.size() * 9;
	}
	
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
