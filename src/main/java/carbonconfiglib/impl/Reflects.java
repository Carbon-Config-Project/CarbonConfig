package carbonconfiglib.impl;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.mojang.brigadier.context.CommandContext;

import net.minecraft.command.CommandSource;
import net.minecraft.world.GameRules.RuleValue;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ExtensionPoint;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.config.ConfigTracker;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.config.ModConfig.ModConfigEvent;

public class Reflects
{
	@SuppressWarnings("unchecked")
	public static <T> ExtensionPoint<T> createExtension() {
		try {
			Constructor<?> entry = ObfuscationReflectionHelper.findConstructor(ExtensionPoint.class);
			entry.setAccessible(true);
			return (ExtensionPoint<T>)entry.newInstance();
		}
		catch(Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static <T extends ModConfigEvent> T createEvent(Class<T> clz, ModConfig config) {
		try {
			Constructor<T> entry = clz.getDeclaredConstructor(ModConfig.class);
			entry.setAccessible(true);
			return entry.newInstance(config);
		}
		catch(Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static void setConfigData(ModConfig config, CommentedConfig data) {
		try {
			Method method = ObfuscationReflectionHelper.findMethod(ModConfig.class, "setConfigData", CommentedConfig.class);
			method.setAccessible(true);
			method.invoke(config, data);
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	@SuppressWarnings("unchecked")
	public static Map<List<String>, String> getComment(ForgeConfigSpec spec) {
		try {
			Field field = ObfuscationReflectionHelper.findField(ForgeConfigSpec.class, "levelComments");
			field.setAccessible(true);
			return (Map<List<String>, String>)field.get(spec);
		}
		catch(Exception e) {
			e.printStackTrace();
			return Collections.emptyMap();
		}
	}
	
	@SuppressWarnings("unchecked")
	public static EnumMap<ModConfig.Type, Set<ModConfig>> getConfigs(ConfigTracker tracker) {
		try {
			Field field = ObfuscationReflectionHelper.findField(ConfigTracker.class, "configSets");
			field.setAccessible(true);
			return (EnumMap<ModConfig.Type, Set<ModConfig>>)field.get(tracker);
		}
		catch(Exception e) {
			e.printStackTrace();
			EnumMap<ModConfig.Type, Set<ModConfig>> result = new EnumMap<>(ModConfig.Type.class);
			for(ModConfig.Type type : ModConfig.Type.values()) {
				result.put(type, Collections.emptySet());
			}
			return result;
		}
	}
	
	public static <T extends RuleValue<T>> void setRuleValue(Class<T> clz, T obj, String value) {
		try {
			Method method = ObfuscationReflectionHelper.findMethod(clz, "func_223553_a", String.class);
			method.setAccessible(true);
			method.invoke(obj, value);
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static <T extends RuleValue<T>> void updateRule(Class<T> clz, T obj, CommandContext<CommandSource> source, String value) {
		try {
			Method method = ObfuscationReflectionHelper.findMethod(clz, "func_223555_a", CommandContext.class, String.class);
			method.setAccessible(true);
			method.invoke(obj, source, value);			
		}
		catch(Exception e) { e.printStackTrace(); }
	}
}
