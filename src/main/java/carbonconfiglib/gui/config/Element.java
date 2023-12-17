package carbonconfiglib.gui.config;

import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;

import carbonconfiglib.gui.config.ConfigElement.GuiAlign;
import carbonconfiglib.gui.widgets.GuiUtils;
import it.unimi.dsi.fastutil.objects.ObjectLists;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

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
public class Element extends ContainerObjectSelectionList.Entry<Element> {
	protected Minecraft mc = Minecraft.getInstance();
	protected Font font = mc.font;
	protected Component name;
	protected Component unchanged;
	protected Component changed;
	protected IListOwner owner;
	protected int hash = hashCode();
	
	public Element(Component name) {
		setName(name);
	}
	
	public String getName() {
		return name.getString();
	}
	
	public void setName(Component newName) {
		this.name = newName;
		this.unchanged = name.copy().withStyle(ChatFormatting.GRAY);
		this.changed = name.copy().withStyle(ChatFormatting.ITALIC);
	}
	
	public void updateValues() {
	}
	
	public boolean isChanged() {
		return false;
	}
	
	public boolean isDefault() {
		return false;
	}
	
	public void init() {
	}
	
	public void tick() {
	}
	
	@Override
	public void render(GuiGraphics poseStack, int x, int top, int left, int width, int height, int mouseX, int mouseY, boolean selected, float partialTicks) {
	}

	protected void renderName(GuiGraphics graphics, int x, int y, boolean changed, int width, int height) {
		GuiUtils.drawScrollingString(graphics, font, (changed ? this.changed : unchanged), x, y-1, width, height, GuiAlign.LEFT, -1, hash);
	}

	protected void renderText(GuiGraphics graphics, Component text, int x, int y, float width, float height, GuiAlign align, int color) {
		GuiUtils.drawScrollingString(graphics, font, text, x, y, width, height, align, -1, hash);
	}
	
	@Override
	public List<? extends GuiEventListener> children() {
		return ObjectLists.emptyList();
	}
	
	@Override
	public List<? extends NarratableEntry> narratables() {
		return ObjectLists.singleton(new NarratableEntry() {
			@Override
			public NarrationPriority narrationPriority() {
                return NarrationPriority.HOVERED;
            }
			
            @Override
            public void updateNarration(NarrationElementOutput output) {
                output.add(NarratedElementType.TITLE, name);
            }
        });
	}
}