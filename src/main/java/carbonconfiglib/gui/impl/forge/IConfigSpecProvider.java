package carbonconfiglib.gui.impl.forge;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import com.electronwill.nightconfig.core.UnmodifiableConfig;

import net.neoforged.fml.config.IConfigSpec;
import net.neoforged.neoforge.common.ModConfigSpec;

public interface IConfigSpecProvider {
	public UnmodifiableConfig getValues();
	public UnmodifiableConfig getSpec();
	public String getLevelComment(List<String> path);
	public void afterReload();
	
	public static void registerProvider(Class<? extends IConfigSpec> specClass, Function<IConfigSpec, IConfigSpecProvider> provider) {
		Objects.requireNonNull(specClass);
		Objects.requireNonNull(provider);
		Registry.PROVIDERS.put(specClass, provider);
	}
	
	public static IConfigSpecProvider get(IConfigSpec spec) {
		if(spec instanceof ModConfigSpec result) return new NeoforgeSpec(result);
		Function<IConfigSpec, IConfigSpecProvider> provider = Registry.PROVIDERS.get(spec.getClass());
		if(provider == null) {
			throw new IllegalStateException("Spec["+spec.getClass().getCanonicalName()+"] isn't supported in forges spec. Please contact the dev to add support");
		}
		return provider.apply(spec);
	}
	
	static class Registry {
		private static final Map<Class<?>, Function<IConfigSpec, IConfigSpecProvider>> PROVIDERS = new ConcurrentHashMap<>();
	}
	
	public record NeoforgeSpec(ModConfigSpec spec) implements IConfigSpecProvider {
		@Override
		public UnmodifiableConfig getValues() { return spec.getValues(); }
		@Override
		public UnmodifiableConfig getSpec() { return spec.getSpec(); }
		@Override
		public String getLevelComment(List<String> path) { return spec.getLevelComment(path); }
		@Override
		public void afterReload() { spec.afterReload(); }
		
	}
}
