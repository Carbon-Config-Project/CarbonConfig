package carbonconfiglib.impl.entries;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

import carbonconfiglib.api.ISuggestionProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import speiger.src.collections.objects.lists.ObjectArrayList;

public class EnchantmentSuggestions implements ISuggestionProvider
{
	public static final ISuggestionProvider INSTANCE = new EnchantmentSuggestions();
	
	@Override
	public void provideSuggestions(Consumer<Suggestion> output, Predicate<Suggestion> filter) {
		for(Identifier key : getEnchantments()) {
			Suggestion suggestion = Suggestion.namedTypeValue(key.toString(), key.toString(), Enchantment.class);
			if(filter.test(suggestion)) output.accept(suggestion);
		}
	}
	
	private List<Identifier> getEnchantments() {
		Level level = Minecraft.getInstance().level;
		if(level == null) return getDefaults();
		Registry<Enchantment> registry = level.registryAccess().lookup(Registries.ENCHANTMENT).orElse(null);
		return registry == null ? null : new ObjectArrayList<>(registry.keySet());
	}
	
	private List<Identifier> getDefaults() {
		List<Identifier> enchantments = new ObjectArrayList<>();
		enchantments.add(Enchantments.PROTECTION.identifier());
		enchantments.add(Enchantments.FIRE_PROTECTION.identifier());
		enchantments.add(Enchantments.FEATHER_FALLING.identifier());
		enchantments.add(Enchantments.BLAST_PROTECTION.identifier());
		enchantments.add(Enchantments.PROJECTILE_PROTECTION.identifier());
		enchantments.add(Enchantments.RESPIRATION.identifier());
		enchantments.add(Enchantments.AQUA_AFFINITY.identifier());
		enchantments.add(Enchantments.THORNS.identifier());
		enchantments.add(Enchantments.DEPTH_STRIDER.identifier());
		enchantments.add(Enchantments.FROST_WALKER.identifier());
		enchantments.add(Enchantments.BINDING_CURSE.identifier());
		enchantments.add(Enchantments.SOUL_SPEED.identifier());
		enchantments.add(Enchantments.SWIFT_SNEAK.identifier());
		enchantments.add(Enchantments.SHARPNESS.identifier());
		enchantments.add(Enchantments.SMITE.identifier());
		enchantments.add(Enchantments.BANE_OF_ARTHROPODS.identifier());
		enchantments.add(Enchantments.KNOCKBACK.identifier());
		enchantments.add(Enchantments.FIRE_ASPECT.identifier());
		enchantments.add(Enchantments.LOOTING.identifier());
		enchantments.add(Enchantments.SWEEPING_EDGE.identifier());
		enchantments.add(Enchantments.EFFICIENCY.identifier());
		enchantments.add(Enchantments.SILK_TOUCH.identifier());
		enchantments.add(Enchantments.UNBREAKING.identifier());
		enchantments.add(Enchantments.FORTUNE.identifier());
		enchantments.add(Enchantments.POWER.identifier());
		enchantments.add(Enchantments.PUNCH.identifier());
		enchantments.add(Enchantments.FLAME.identifier());
		enchantments.add(Enchantments.INFINITY.identifier());
		enchantments.add(Enchantments.LUCK_OF_THE_SEA.identifier());
		enchantments.add(Enchantments.LURE.identifier());
		enchantments.add(Enchantments.LOYALTY.identifier());
		enchantments.add(Enchantments.IMPALING.identifier());
		enchantments.add(Enchantments.RIPTIDE.identifier());
		enchantments.add(Enchantments.CHANNELING.identifier());
		enchantments.add(Enchantments.MULTISHOT.identifier());
		enchantments.add(Enchantments.QUICK_CHARGE.identifier());
		enchantments.add(Enchantments.PIERCING.identifier());
		enchantments.add(Enchantments.DENSITY.identifier());
		enchantments.add(Enchantments.BREACH.identifier());
		enchantments.add(Enchantments.WIND_BURST.identifier());
		enchantments.add(Enchantments.MENDING.identifier());
		enchantments.add(Enchantments.VANISHING_CURSE.identifier());
		return enchantments;
	}
}