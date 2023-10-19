package carbonconfiglib.mixins;

import java.util.Map;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import com.google.common.collect.ImmutableMap;
import com.terraformersmc.modmenu.ModMenuModMenuCompat;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;

@Mixin(value = ModMenuModMenuCompat.class, remap = false)
public class ModMenuMixin
{
	/**
	 * Reason: Simply to make it compatible :)
	 * @return
	 */
	@Overwrite
	public Map<String, ConfigScreenFactory<?>> getProvidedConfigScreenFactories() {
		return ImmutableMap.of();
	}
}
