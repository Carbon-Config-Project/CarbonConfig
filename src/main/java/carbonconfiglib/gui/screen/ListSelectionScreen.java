package carbonconfiglib.gui.screen;

import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;

import carbonconfiglib.api.ISuggestionProvider.Suggestion;
import carbonconfiglib.gui.api.BackgroundTexture.BackgroundHolder;
import carbonconfiglib.gui.api.ICompoundNode;
import carbonconfiglib.gui.api.IConfigNode;
import carbonconfiglib.gui.api.INode;
import carbonconfiglib.gui.api.ISuggestionRenderer;
import carbonconfiglib.gui.api.IValueNode;
import carbonconfiglib.gui.config.ConfigElement.GuiAlign;
import carbonconfiglib.gui.config.Element;
import carbonconfiglib.gui.config.ElementList;
import carbonconfiglib.gui.config.ListScreen;
import carbonconfiglib.gui.widgets.CarbonButton;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiYesNo;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
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
public abstract class ListSelectionScreen extends ListScreen
{
	IConfigNode node;
	INode value;
	GuiScreen parent;
	GuiButton apply;
	Runnable abortListener;
	Runnable successListener;
	boolean dontWarn;
	
	public ListSelectionScreen(GuiScreen parent, IConfigNode node, INode value, BackgroundHolder customTexture) {
		super(node.getName(), customTexture);
		this.parent = parent;
		this.node = node;
		this.value = value;
		this.value.createTemp();
	}
	
	public static ListSelectionScreen ofValue(GuiScreen parent, IConfigNode node, IValueNode value, BackgroundHolder customTexture) {
		return new Value(parent, node, value, customTexture);
	}
	
	public static ListSelectionScreen ofCompound(GuiScreen parent, IConfigNode node, ICompoundNode value, BackgroundHolder customTexture) {
		return new Compound(parent, node, value, customTexture);
	}
	
	public static ListSelectionScreen ofCompoundValue(GuiScreen parent, IConfigNode node, IValueNode value, ICompoundNode compound, int index, BackgroundHolder customTexture) {
		return new CompoundValue(parent, node, value, compound, index, customTexture);
	}
	
	@Override
	public void initGui() {
		super.initGui();
		visibleList.setRenderSelection(true);
		loadDefault();
		visibleList.setCallback(T -> setValue(((SelectionElement)T).getSuggestion().getValue()));
		int x = width / 2 - 100;
		int y = height;
		apply = addWidget(new CarbonButton(x+10, y-27, 85, 20, I18n.format("gui.carbonconfig.pick"), this::save));
		addWidget(new CarbonButton(x+105, y-27, 85, 20, I18n.format("gui.carbonconfig.cancel"), this::cancel));
	}
	
	public ListSelectionScreen withListener(Runnable success, Runnable abort) {
		this.successListener = success;
		this.abortListener = abort;
		return this;
	}
	
	public ListSelectionScreen disableAbortWarning() {
		dontWarn = true;
		return this;
	}
	
	protected abstract void loadDefault();
	protected abstract void setValue(String value);
	
	protected void findDefault(String defaultValue) {
		for(Element element : allEntries) {
			if(((SelectionElement) element).getSuggestion().getValue().equals(defaultValue)) {
				visibleList.setSelected(element);
				break;
			}
		}		
		visibleList.scrollToSelected(true);
	}
		
	@Override
	protected List<Element> sortElements(List<Element> list) {
		list.sort(Comparator.comparing(Element::getName, String.CASE_INSENSITIVE_ORDER));
		return list;
	}
	
	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		apply.enabled = value.isChanged();
		super.drawScreen(mouseX, mouseY, partialTicks);
		String title = this.title.getFormattedText();
		fontRenderer.drawString(title, (width/2)-(fontRenderer.getStringWidth(title)/2), 8, -1);
	}
	
	@Override
	protected void collectElements(Consumer<Element> elements) {
		for(Suggestion entry : node.getValidValues()) {
			elements.accept(new SelectionElement(entry, visibleList));
		}
	}
	
	@Override
	public void onClose() {
		abort();
		mc.displayGuiScreen(parent);
	}
	
	private void save(GuiButton button) {
		value.apply();
		if(successListener != null) successListener.run();
		else mc.displayGuiScreen(parent);
	}
	
	private void cancel(GuiButton button) {
		if(value.isChanged() && !dontWarn) {
			mc.displayGuiScreen(new GuiYesNo((T, V) -> {
				if(T) abort();
				mc.displayGuiScreen(T ? parent : this);	
			}, new TextComponentTranslation("gui.carbonconfig.warn.changed").getFormattedText(), new TextComponentTranslation("gui.carbonconfig.warn.changed.desc").setStyle(new Style().setColor(TextFormatting.GRAY)).getFormattedText(), 0));
			return;
		}
		abort();
		mc.displayGuiScreen(parent);
	}
	
	private void abort() {
		value.setPrevious();
		if(abortListener != null) abortListener.run();
	}
	
	public static class Value extends ListSelectionScreen {

		public Value(GuiScreen parent, IConfigNode node, IValueNode value, BackgroundHolder customTexture) {
			super(parent, node, value, customTexture);
		}
		
		@Override
		protected void loadDefault() {
			findDefault(((IValueNode)value).get());
		}
		
		@Override
		protected void setValue(String value) {
			((IValueNode)this.value).set(value);
		}
	}
	
	public static class CompoundValue extends ListSelectionScreen {
		ICompoundNode compound;
		int index;
		
		public CompoundValue(GuiScreen parent, IConfigNode node, IValueNode value, ICompoundNode compound, int index, BackgroundHolder customTexture) {
			super(parent, node, value, customTexture);
			this.compound = compound;
			this.index = index;
		}
		
		@Override
		protected void loadDefault() {
			findDefault(((IValueNode)value).get());
		}
		
		@Override
		protected void setValue(String value) {
			((IValueNode)this.value).set(value);
		}
		
		@Override
		protected void collectElements(Consumer<Element> elements) {
			for(Suggestion entry : compound.getValidValues(index)) {
				elements.accept(new SelectionElement(entry, visibleList));
			}
		}
	}
	
	public static class Compound extends ListSelectionScreen {

		public Compound(GuiScreen parent, IConfigNode node, ICompoundNode value, BackgroundHolder customTexture) {
			super(parent, node, value, customTexture);
		}
		
		@Override
		protected void loadDefault() {
			findDefault(((ICompoundNode)value).get());
		}
		
		@Override
		protected void setValue(String value) {
			((ICompoundNode)this.value).set(value);
		}
	}
	
	private class SelectionElement extends Element {
		Suggestion suggestion;
		ElementList myList;
		int lastClick = -1;
		ISuggestionRenderer renderer;
		boolean loaded = false;
		
		
		public SelectionElement(Suggestion suggestion, ElementList list) {
			super(new TextComponentTranslation(suggestion.getName()));
			this.suggestion = suggestion;
			this.myList = list;
		}
		
		@Override
		public void render(int x, int top, int left, int width, int height, int mouseX, int mouseY, boolean selected, float partialTicks) {
			ISuggestionRenderer renderer = getRenderer();
			if(renderer != null) {
				ITextComponent comp = renderer.renderSuggestion(suggestion.getValue(), left, top);
				if(comp != null && mouseX >= left && mouseX <= left + 20 && mouseY >= top && mouseY <= top + 20) {
					owner.addTooltips(comp);
				}
			}
			renderText(new TextComponentString("").setStyle(new Style().setColor(myList.getSelected() == this ? TextFormatting.YELLOW : TextFormatting.WHITE)).appendSibling(name), left+(renderer != null ? 20 : 0), top, width - 5, height-1, GuiAlign.LEFT, 0xFFFFFFFF);
		}
		
		private ISuggestionRenderer getRenderer() {
			if(loaded) return renderer;
			loaded = true;
			if(suggestion.getType() != null) {
				renderer = ISuggestionRenderer.Registry.getRendererForType(suggestion.getType());	
			}
			return renderer;
		}
		
		public Suggestion getSuggestion() {
			return suggestion;
		}
		
		@Override
		public boolean mouseClick(double p_94737_, double p_94738_, int p_94739_) {
			if(myList.getSelected() == this) {
				if(lastClick >= 0 && myList.getLastTick() - lastClick <= 5) {
					save(null);
					return true;
				}
				lastClick = myList.getLastTick();
			}
			else {
				lastClick = myList.getLastTick();
			}
			myList.setSelected(this);
			return true;
		}
	}
}
