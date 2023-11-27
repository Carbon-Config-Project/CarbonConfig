package carbonconfiglib.gui.screen;

import java.util.List;
import java.util.function.Consumer;

import carbonconfiglib.gui.api.BackgroundTexture.BackgroundHolder;
import carbonconfiglib.gui.api.DataType;
import carbonconfiglib.gui.api.ICompoundNode;
import carbonconfiglib.gui.api.IConfigNode;
import carbonconfiglib.gui.api.IValueNode;
import carbonconfiglib.gui.config.ConfigElement;
import carbonconfiglib.gui.config.Element;
import carbonconfiglib.gui.config.ListScreen;
import carbonconfiglib.gui.config.SelectionElement;
import carbonconfiglib.gui.widgets.CarbonButton;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
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
public class CompoundScreen extends ListScreen
{
	Screen prev;
	IConfigNode entry;
	ICompoundNode compound;
	List<DataType> type;
	Button applyValue;
	Runnable closeListener = null;
	
	public CompoundScreen(IConfigNode entry, ICompoundNode node, Screen prev, BackgroundHolder customTexture) {
		super(entry.getName(), customTexture);
		this.prev = prev;
		this.entry = entry;
		this.compound = node;
		this.type = entry.getDataType();
		compound.createTemp();
	}
	
	@Override
	protected void init() {
		super.init();
		int x = width / 2;
		int y = height;
		applyValue = addButton(new CarbonButton(x-82, y-27, 80, 20, new TranslationTextComponent("gui.carbonconfig.apply"), this::apply));
		addButton(new CarbonButton(x+2, y-27, 80, 20, new TranslationTextComponent("gui.carbonconfig.back"), this::goBack));
	}
	
	@Override
	protected boolean shouldHaveTooltips() {
		return true;
	}
	
	public void setAbortListener(Runnable run) {
		this.closeListener = run;
	}
	
	@Override
	public void tick() {
		super.tick();
		applyValue.active = compound.isValid();
	}
	
	@Override
	public void onClose() {
		notifyClose();
		minecraft.displayGuiScreen(prev);
	}
	
	private void apply(Button button) {
		compound.apply();
		minecraft.displayGuiScreen(prev);
	}
	
	private void notifyClose() {
		compound.setPrevious();
		if(closeListener == null) return;
		closeListener.run();
	}
	
	private void goBack(Button button) {
		if(compound.isChanged()) {
			minecraft.displayGuiScreen(new ConfirmScreen(T -> {
				if(T) notifyClose();
				minecraft.displayGuiScreen(T ? prev : this);				
			}, new TranslationTextComponent("gui.carbonconfig.warn.changed"), new TranslationTextComponent("gui.carbonconfig.warn.changed.desc").applyTextStyle(TextFormatting.GRAY)));
			return;
		}
		notifyClose();
		minecraft.displayGuiScreen(prev);
	}
	
	@Override
	protected void collectElements(Consumer<Element> elements) {
		List<IValueNode> values = compound.getValues();
		for(int i = 0,m=type.size();i<m;i++) {
			if(compound.isForcedSuggestion(i)) {
				elements.accept(new SelectionElement(entry, values.get(i)));
				continue;
			}
			ConfigElement element = type.get(i).create(entry, values.get(i));
			if(element != null) {
				element.setCompound(compound, i);
				elements.accept(element);
			}
		}
	}
	
	@Override
	public void render(int mouseX, int mouseY, float partialTicks) {
		super.render(mouseX, mouseY, partialTicks);
		String title = this.title.getFormattedText();
		font.drawString(title, (width/2)-(font.getStringWidth(title)/2), 8, -1);
	}
}