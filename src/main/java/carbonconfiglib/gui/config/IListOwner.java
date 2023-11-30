package carbonconfiglib.gui.config;

import carbonconfiglib.gui.api.BackgroundTexture.BackgroundHolder;
import carbonconfiglib.gui.widgets.CarbonEditBox;
import net.minecraft.util.IChatComponent;

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
public interface IListOwner
{
	public void addTooltips(IChatComponent tooltip);
	public boolean isInsideList(double mouseX, double mouseY);
	public BackgroundHolder getCustomTexture();
	
	public boolean isActiveWidget(CarbonEditBox widget);
	public void setActiveWidget(CarbonEditBox widget);
	
	public void updateInformation();
	public void removeEntry(Element element);
}
