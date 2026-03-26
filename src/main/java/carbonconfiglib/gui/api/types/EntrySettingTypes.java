package carbonconfiglib.gui.api.types;

import java.util.List;

import com.google.common.base.Function;
import com.google.common.base.Supplier;

import carbonconfiglib.api.IConfigSerializer;
import carbonconfiglib.api.IEntrySettings;
import carbonconfiglib.api.ISuggestionProvider.Suggestion;
import net.minecraft.util.text.ITextComponent;

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
public class EntrySettingTypes
{	
	public static class CompoundArrayNamer<T> implements IEntrySettings {
		final Supplier<IConfigSerializer<T>> serializer;
		final Function<T, ITextComponent> provider;
		
		public CompoundArrayNamer(Supplier<IConfigSerializer<T>> serializer, Function<T, ITextComponent> provider) {
			this.serializer = serializer;
			this.provider = provider;
		}
		
		public Function<T, ITextComponent> provider() {
			return provider;
		}
		
		public Supplier<IConfigSerializer<T>> serializer() {
			return serializer;
		}
	}
	public static class ForcedSelection implements IEntrySettings {
		final List<Suggestion> suggestions;

		public ForcedSelection(List<Suggestion> suggestions) {
			this.suggestions = suggestions;
		}
		
		public List<Suggestion> suggestions() {
			return suggestions;
		}
	}
	public static class FloatingSlider implements IEntrySettings {
		final double stepSize;

		public FloatingSlider(double stepSize) {
			this.stepSize = stepSize;
		}
		
		public double stepSize() {
			return stepSize;
		}
	}	
	public static class ForceMode implements IEntrySettings {
		final boolean isForcingText;

		public ForceMode(boolean isForcingText) {
			this.isForcingText = isForcingText;
		}
		
		public boolean isForcingText() {
			return isForcingText;
		}
	}
	public static class ColorType implements IEntrySettings {
		final boolean hasAlpha;
		
		public ColorType(boolean hasAlpha) {
			this.hasAlpha = hasAlpha;
		}
		
		public boolean hasAlpha() {
			return hasAlpha;
		}
	}
	public static class CompoundOverride implements IEntrySettings {
		final Class<?> type;
		
		public CompoundOverride(Class<?> type) {
			this.type = type;
		}
		
		public Class<?> type(){
			return type;
		}
	}
}
