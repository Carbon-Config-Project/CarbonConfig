package carbonconfiglib.gui.api.types;

import java.util.function.BiFunction;

import carbonconfiglib.api.IEntrySettings;
import carbonconfiglib.gui.api.node.ICompoundNode;
import carbonconfiglib.gui.api.node.INode;
import carbonconfiglib.gui.api.node.IValueNode;

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
