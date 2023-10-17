package carbonconfiglib.gui.widgets;

import carbonconfiglib.gui.config.IListOwner;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.TextComponent;

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
public class CarbonEditBox extends EditBox implements IOwnable
{
	IListOwner owner;
	boolean bordered = true;
	int innerDiff = 8;
	
	public CarbonEditBox(Font font, int x, int y, int width, int height) {
		super(font, x, y, width, height, new TextComponent(""));
	}
	
	public CarbonEditBox setInnerDiff(int innerDiff) {
		this.innerDiff = innerDiff;
		return this;
	}
	
	public void setOwner(IListOwner owner) {
		this.owner = owner;
	}
	
	@Override
	public void setFocus(boolean focus) {
		super.setFocus(focus);
		if(focus && owner != null) {
			owner.setActiveWidget(this);
		}
	}
	
	@Override
	public int getInnerWidth() {
		return bordered ? this.width - innerDiff : this.width;
	}
	
	@Override
	public void setBordered(boolean value) {
		super.setBordered(value);
		this.bordered = value;
	}
	
	@Override
	public void render(PoseStack stack, int mouseX, int mouseY, float partialTicks) {
		if(this.isFocused() && owner != null && !owner.isActiveWidget(this)) {
			setFocus(false);
		}
		super.render(stack, mouseX, mouseY, partialTicks);
	}
}
