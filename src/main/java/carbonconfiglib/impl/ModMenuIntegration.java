package carbonconfiglib.impl;

import java.util.Map;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

import carbonconfiglib.CarbonConfig;
import carbonconfiglib.gui.api.IModConfigs;
import carbonconfiglib.gui.screen.ConfigSelectorScreen;
import carbonconfiglib.impl.internal.EventHandler;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screens.Screen;
import speiger.src.collections.objects.maps.impl.hash.Object2ObjectLinkedOpenHashMap;
import speiger.src.collections.objects.maps.interfaces.Object2ObjectMap;
import speiger.src.collections.objects.utils.maps.Object2ObjectMaps;

public class ModMenuIntegration implements ModMenuApi
{

	@Override
	public ConfigScreenFactory<?> getModConfigScreenFactory() {
		if(!CarbonConfig.MOD_MENU_SUPPORT.get()) return null;
		return T -> create(T, EventHandler.INSTANCE.createConfigs().get("carbonconfig"));
	}

	@Override
	public Map<String, ConfigScreenFactory<?>> getProvidedConfigScreenFactories() {
		if(!CarbonConfig.MOD_MENU_SUPPORT.get()) return Object2ObjectMaps.empty();
		Map<String, IModConfigs> configs = EventHandler.INSTANCE.createConfigs();
		Object2ObjectMap<String, ConfigScreenFactory<?>> mappedConfigs = new Object2ObjectLinkedOpenHashMap<>();
		configs.forEach((K, V) -> mappedConfigs.put(K, T -> create(T, V)));
		return mappedConfigs;
	}
	
	@Environment(EnvType.CLIENT)
	private Screen create(Screen screen, IModConfigs configs) {	
		return new ConfigSelectorScreen(configs, screen);
	}
	
}
