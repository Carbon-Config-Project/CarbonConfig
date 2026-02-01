package carbonconfiglib.gui.api;

import carbonconfiglib.api.IEntrySettings;
import carbonconfiglib.utils.structure.IStructuredData.StructureType;
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
public interface INode
{	
	public boolean isDefault();
	public boolean isChanged();
	
	public void setDefault();
	public void setPrevious();
	public void createTemp();
	@Deprecated
	public void apply();
	public void deleteTempIfNeeded();
	
	
	public StructureType getNodeType();
	public IEntrySettings getSettings();
	public default <T> T getSetting(Class<T> clz) {
		IEntrySettings setting = getSettings();
		return setting == null ? null : setting.get(clz);
	}
	public boolean requiresRestart();
	public boolean requiresReload();
	
	public Component getName();
	public Component getTooltip();
	
	public static IValueNode asValue(IConfigNode config) {
		return config.asNode().asValue();
	}
	
	public static IArrayNode asArray(IConfigNode config) {
		return config.asNode().asArray();
	}
	
	public static ICompoundNode asCompound(IConfigNode config) {
		return config.asNode().asCompound();
	}
	
	public default IValueNode asValue() {
		return this instanceof IValueNode ? (IValueNode)this : null;
	}
	
	public default IArrayNode asArray() {
		return this instanceof IArrayNode ? (IArrayNode)this : null;
	}
	
	public default ICompoundNode asCompound() {
		return this instanceof ICompoundNode ? (ICompoundNode)this : null;
	}
}
