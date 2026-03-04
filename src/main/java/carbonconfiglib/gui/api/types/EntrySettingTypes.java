package carbonconfiglib.gui.api.types;

import java.util.function.BiFunction;

import carbonconfiglib.api.IEntrySettings;
import carbonconfiglib.gui.api.node.ICompoundNode;
import carbonconfiglib.gui.api.node.INode;
import carbonconfiglib.gui.api.node.IValueNode;

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
	public static class ArrayRenamer implements IEntrySettings {
		BiFunction<Integer, ? extends INode, String> function;

		public ArrayRenamer(BiFunction<Integer, ? extends INode, String> function) {
			this.function = function;
		}
		
		@SuppressWarnings("unchecked")
		public BiFunction<Integer, INode, String> getFunction() {
			return (BiFunction<Integer, INode, String>)function;
		}
		
		public static ArrayRenamer ofValue(BiFunction<Integer, IValueNode, String> value) {
			return new ArrayRenamer(value);
		}
		
		public static ArrayRenamer ofCompound(BiFunction<Integer, ICompoundNode, String> compound) {
			return new ArrayRenamer(compound);
		}
	}
	
	public static class ForceMode implements IEntrySettings {
		boolean text;
		
		public ForceMode(boolean text) {
			this.text = text;
		}
		
		public boolean isForcingText() {
			return text;
		}
	}
	
	public static class ColorType implements IEntrySettings {
		boolean hasAlpha;

		public ColorType(boolean hasAlpha) {
			this.hasAlpha = hasAlpha;
		}
		
		public boolean hasAlpha() {
			return hasAlpha;
		}
	}
	
	public static class CompoundOverride implements IEntrySettings {
		Class<?> clz;

		public CompoundOverride(Class<?> clz) {
			this.clz = clz;
		}
		
		public Class<?> getType() {
			return clz;
		}
	}
}
