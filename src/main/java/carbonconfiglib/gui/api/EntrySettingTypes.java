package carbonconfiglib.gui.api;

import java.util.function.BiFunction;

import carbonconfiglib.api.IEntrySettings;

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
