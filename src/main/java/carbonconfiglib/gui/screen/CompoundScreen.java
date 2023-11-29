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
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiYesNo;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.Style;
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
public class CompoundScreen extends ListScreen
{
	GuiScreen prev;
	IConfigNode entry;
	ICompoundNode compound;
	List<DataType> type;
	GuiButton applyValue;
	Runnable closeListener = null;
	
	public CompoundScreen(IConfigNode entry, ICompoundNode node, GuiScreen prev, BackgroundHolder customTexture) {
		super(entry.getName(), customTexture);
		this.prev = prev;
		this.entry = entry;
		this.compound = node;
		this.type = entry.getDataType();
		compound.createTemp();
	}
	
	@Override
	public void initGui() {
		super.initGui();
		int x = width / 2;
		int y = height;
		applyValue = addWidget(new CarbonButton(x-82, y-27, 80, 20, I18n.format("gui.carbonconfig.apply"), this::apply));
		addWidget(new CarbonButton(x+2, y-27, 80, 20, I18n.format("gui.carbonconfig.back"), this::goBack));
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
		applyValue.enabled = compound.isValid();
	}
	
	@Override
	public void onClose() {
		notifyClose();
		mc.displayGuiScreen(prev);
	}
	
	private void apply(GuiButton button) {
		compound.apply();
		mc.displayGuiScreen(prev);
	}
	
	private void notifyClose() {
		compound.setPrevious();
		if(closeListener == null) return;
		closeListener.run();
	}
	
	private void goBack(GuiButton button) {
		if(compound.isChanged()) {
			mc.displayGuiScreen(new GuiYesNo((T, K) -> {
				if(T) notifyClose();
				mc.displayGuiScreen(T ? prev : this);				
			}, new TextComponentTranslation("gui.carbonconfig.warn.changed").getFormattedText(), new TextComponentTranslation("gui.carbonconfig.warn.changed.desc").setStyle(new Style().setColor(TextFormatting.GRAY)).getFormattedText(), 0));
			return;
		}
		notifyClose();
		mc.displayGuiScreen(prev);
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
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		super.drawScreen(mouseX, mouseY, partialTicks);
		String title = this.title.getFormattedText();
		fontRenderer.drawString(title, (width/2)-(fontRenderer.getStringWidth(title)/2), 8, -1);
	}
}