package carbonconfiglib.gui.api.types;

import java.util.Map;
import java.util.function.Function;

import carbonconfiglib.api.IConfigSerializer;
import carbonconfiglib.gui.api.node.ICompoundNode;
import carbonconfiglib.gui.api.types.EntrySettingTypes.CompoundOverride;
import carbonconfiglib.gui.nodes.CustomCompoundElement;
import carbonconfiglib.gui.nodes.base.BaseElement;
import carbonconfiglib.gui.screens.WidgetAlignerScreen;
import carbonconfiglib.gui.screens.WidgetAlignerScreen.OverlayRenderer;
import carbonconfiglib.impl.entries.WidgetAligner;
import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

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
public class CompoundType
{
	private static final Map<Class<?>, CompoundType> AUTO_COMPOUND_TYPES = Object2ObjectMaps.synchronize(new Object2ObjectOpenHashMap<>());
	Function<ICompoundNode, CustomCompoundElement> creator;
	
	public CompoundType(Function<ICompoundNode, CustomCompoundElement> creator) {
		this.creator = creator;
	}
	
	public BaseElement create(ICompoundNode node) {
		return creator.apply(node);
	}
	
	public static CompoundType by(ICompoundNode node) {
		CompoundOverride override = node.getSetting(CompoundOverride.class);
		return override == null ? null : AUTO_COMPOUND_TYPES.get(override.getType());
	}
	
	public static void registerType(Class<?> clz, CompoundType type) {
		AUTO_COMPOUND_TYPES.putIfAbsent(clz, type);
	}
	
	public static void registerWidgetAligner(Class<?> type, OverlayRenderer renderer, IConfigSerializer<WidgetAligner> serializer) {
		registerType(type, new CompoundType(T -> new CustomCompoundElement(T, (K, V) -> new WidgetAlignerScreen(K, V, renderer, serializer))));
	}

}
