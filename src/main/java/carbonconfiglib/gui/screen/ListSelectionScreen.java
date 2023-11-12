package carbonconfiglib.gui.screen;

import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;

import com.mojang.blaze3d.vertex.PoseStack;

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
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
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
public abstract class ListSelectionScreen extends ListScreen
{
	IConfigNode node;
	INode value;
	Screen parent;
	Button apply;
	Runnable abortListener;
	Runnable successListener;
	boolean dontWarn;
	
	public ListSelectionScreen(Screen parent, IConfigNode node, INode value, BackgroundHolder customTexture) {
		super(node.getName(), customTexture);
		this.parent = parent;
		this.node = node;
		this.value = value;
		this.value.createTemp();
	}
	
	public static ListSelectionScreen ofValue(Screen parent, IConfigNode node, IValueNode value, BackgroundHolder customTexture) {
		return new Value(parent, node, value, customTexture);
	}
	
	public static ListSelectionScreen ofCompound(Screen parent, IConfigNode node, ICompoundNode value, BackgroundHolder customTexture) {
		return new Compound(parent, node, value, customTexture);
	}
	
	public static ListSelectionScreen ofCompoundValue(Screen parent, IConfigNode node, IValueNode value, ICompoundNode compound, int index, BackgroundHolder customTexture) {
		return new CompoundValue(parent, node, value, compound, index, customTexture);
	}
	
	@Override
	protected void init() {
		super.init();
		visibleList.setRenderSelection(true);
		loadDefault();
		visibleList.setCallback(T -> setValue(((SelectionElement)T).getSuggestion().getValue()));
		int x = width / 2 - 100;
		int y = height;
		apply = addRenderableWidget(new CarbonButton(x+10, y-27, 85, 20, new TranslatableComponent("gui.carbonconfig.pick"), this::save));
		addRenderableWidget(new CarbonButton(x+105, y-27, 85, 20, new TranslatableComponent("gui.carbonconfig.cancel"), this::cancel));
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
	public void render(PoseStack stack, int mouseX, int mouseY, float partialTicks) {
		apply.active = value.isChanged();
		super.render(stack, mouseX, mouseY, partialTicks);
		font.draw(stack, title, (width/2)-(font.width(title)/2), 8, -1);
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
		minecraft.setScreen(parent);
	}
	
	private void save(Button button) {
		value.apply();
		if(successListener != null) successListener.run();
		else minecraft.setScreen(parent);
	}
	
	private void cancel(Button button) {
		if(value.isChanged() && !dontWarn) {
			minecraft.setScreen(new ConfirmScreen(T -> {
				if(T) abort();
				minecraft.setScreen(T ? parent : this);	
			}, new TranslatableComponent("gui.carbonconfig.warn.changed"), new TranslatableComponent("gui.carbonconfig.warn.changed.desc").withStyle(ChatFormatting.GRAY)));
			return;
		}
		abort();
		minecraft.setScreen(parent);
	}
	
	private void abort() {
		value.setPrevious();
		if(abortListener != null) abortListener.run();
	}
	
	public static class Value extends ListSelectionScreen {

		public Value(Screen parent, IConfigNode node, IValueNode value, BackgroundHolder customTexture) {
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
	
	public static class Compound extends ListSelectionScreen {

		public Compound(Screen parent, IConfigNode node, ICompoundNode value, BackgroundHolder customTexture) {
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
	
	public static class CompoundValue extends ListSelectionScreen {
		ICompoundNode compound;
		int index;

		public CompoundValue(Screen parent, IConfigNode node, IValueNode value, ICompoundNode compound, int index, BackgroundHolder customTexture) {
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
	
	private class SelectionElement extends Element {
		Suggestion suggestion;
		ElementList myList;
		int lastClick = -1;
		ISuggestionRenderer renderer;
		boolean loaded = false;
		
		
		public SelectionElement(Suggestion suggestion, ElementList list) {
			super(new TranslatableComponent(suggestion.getName()));
			this.suggestion = suggestion;
			this.myList = list;
		}
		
		@Override
		public void render(PoseStack poseStack, int x, int top, int left, int width, int height, int mouseX, int mouseY, boolean selected, float partialTicks) {
			ISuggestionRenderer renderer = getRenderer();
			if(renderer != null) {
				Component comp = renderer.renderSuggestion(poseStack, suggestion.getValue(), left, top);
				if(comp != null && mouseX >= left && mouseX <= left + 20 && mouseY >= top && mouseY <= top + 20) {
					owner.addTooltips(comp);
				}
			}
			renderText(poseStack, new TextComponent("").withStyle(myList.getSelected() == this ? ChatFormatting.YELLOW : ChatFormatting.WHITE).append(name), left+(renderer != null ? 20 : 0), top, width - 5, height-1, GuiAlign.LEFT, 0xFFFFFFFF);
		}
		
		private ISuggestionRenderer getRenderer() {
			if(loaded) return renderer;
			loaded = true;
			if(suggestion.getType() != null) {
				renderer = ISuggestionRenderer.SuggestionRegistry.getRendererForType(suggestion.getType());	
			}
			return renderer;
		}
		
		public Suggestion getSuggestion() {
			return suggestion;
		}
		
		@Override
		public boolean mouseClicked(double p_94737_, double p_94738_, int p_94739_) {
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
