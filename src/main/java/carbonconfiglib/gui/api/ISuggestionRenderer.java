package carbonconfiglib.gui.api;

import java.util.Map;

import com.mojang.blaze3d.vertex.PoseStack;

import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
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
public interface ISuggestionRenderer
{
	public Component renderSuggestion(PoseStack stack, String value, int x, int y);
	
	public static class Registry {
		private static final Map<Class<?>, ISuggestionRenderer> REGISTRY = Object2ObjectMaps.synchronize(new Object2ObjectOpenHashMap<>());
		
		public static void register(Class<?> clz, ISuggestionRenderer suggestion) {
			REGISTRY.putIfAbsent(clz, suggestion);
		}
		
		public static ISuggestionRenderer getRendererForType(Class<?> clz) {
			return REGISTRY.get(clz);
		}
		
		public static ISuggestionRenderer getRendererForType(Object obj) {
			if(obj == null) return null;
			return getRendererForType(obj instanceof Class ? (Class<?>)obj : obj.getClass()); 
		}
	}
}
