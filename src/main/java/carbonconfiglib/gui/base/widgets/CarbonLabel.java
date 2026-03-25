package carbonconfiglib.gui.base.widgets;

import carbonconfiglib.gui.base.helpers.GuiUtils;
import carbonconfiglib.gui.base.helpers.Icon;
import carbonconfiglib.gui.base.helpers.Texts;
import net.minecraft.client.renderer.GlStateManager;


/**
 * Copyright 2026 Speiger, Meduris
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
public class CarbonLabel extends CarbonBaseButton
{
	protected Icon icon;
	public CarbonLabel(int x, int y, int width, int height, Icon icon) {
		super(x, y, width, height, Texts.empty(), null);
		this.icon = icon;
	}
	
	@Override
	public boolean mouseClick(double mouseX, double mouseY, int button) {
		return false;
	}
	
	@Override
	public void render(int mouseX, int mouseY, float partialTicks) {
		int j = this.enabled ? 16777215 : 10526880;
		GlStateManager.color(((j >> 16) & 0xFF) / 255F, ((j >> 8) & 0xFF) / 255F, (j & 0xFF) / 255F, 1F);
		GuiUtils.drawTextureRegion(x+2, y+2, width-4, height-4, icon, 16, 16);
		GlStateManager.color(1F, 1F, 1F, 1F);
	}
	
}
