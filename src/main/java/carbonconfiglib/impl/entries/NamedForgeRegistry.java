package carbonconfiglib.impl.entries;

import java.util.function.Function;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ForgeRegistry;
import net.minecraftforge.registries.IForgeRegistry;

public class NamedForgeRegistry<T>
{
	public static final NamedForgeRegistry<Block> BLOCKS = new NamedForgeRegistry<>(ForgeRegistries.BLOCKS, T -> T.getName().getString());
	public static final NamedForgeRegistry<Item> ITEMS = new NamedForgeRegistry<>(ForgeRegistries.ITEMS, T -> T.getName(T.getDefaultInstance()).getString());
	public static final NamedForgeRegistry<Fluid> FLUID = new NamedForgeRegistry<>(ForgeRegistries.FLUIDS, T -> T.getFluidType().getDescription().getString());
	public static final NamedForgeRegistry<MobEffect> MOB_EFFECTS = new NamedForgeRegistry<>(ForgeRegistries.MOB_EFFECTS, T -> T.getDisplayName().getString());
	public static final NamedForgeRegistry<Enchantment> ENCHANTMENTS = new NamedForgeRegistry<>(ForgeRegistries.ENCHANTMENTS, T -> T.getFullname(0).plainCopy().getString());
	
	IForgeRegistry<T> registry;
	Function<T, String> nameFunction;
	
	public NamedForgeRegistry(IForgeRegistry<T> registry, Function<T, String> nameFunction) {
		this.registry = registry;
		this.nameFunction = nameFunction;
	}
	
	public ForgeRegistry<T> getImplRegistry() {
		return (ForgeRegistry<T>)registry;
	}
	
	public IForgeRegistry<T> getRegistry() {
		return registry;
	}
	
	public boolean hasNameGenerator() {
		return nameFunction != null;
	}
	
	public String getName(ResourceLocation id) {
		return nameFunction == null ? id.toString() : nameFunction.apply(registry.getValue(id));
	}
	
	public String getName(T value) {
		return nameFunction == null ? registry.getKey(value).toString() : nameFunction.apply(value);
	}
}
