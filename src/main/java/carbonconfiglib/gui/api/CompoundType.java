package carbonconfiglib.gui.api;

import java.util.Map;
import java.util.function.Function;

import carbonconfiglib.gui.api.EntrySettingTypes.CompoundOverride;
import carbonconfiglib.gui.nodes.CustomCompoundElement;
import carbonconfiglib.gui.nodes.base.BaseElement;
import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

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
	
	

}
