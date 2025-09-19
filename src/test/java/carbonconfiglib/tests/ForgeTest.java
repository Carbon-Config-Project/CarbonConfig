package carbonconfiglib.tests;

import java.util.List;

import com.google.common.collect.Lists;

import it.unimi.dsi.fastutil.ints.IntImmutableList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.common.ModConfigSpec.BooleanValue;
import net.neoforged.neoforge.common.ModConfigSpec.Builder;
import net.neoforged.neoforge.common.ModConfigSpec.ConfigValue;
import net.neoforged.neoforge.common.ModConfigSpec.DoubleValue;
import net.neoforged.neoforge.common.ModConfigSpec.IntValue;

public class ForgeTest
{
    private static final IntList LIST_TEST_NUMBERS = new IntImmutableList(new int[]{2, 3, 5, 7});

    private static final Builder BUILDER = new Builder();
    public static final IntValue INT_RANGE_TEST = BUILDER
            .comment("Testing the Functionality of Integer Ranges")
            .defineInRange("Integer Multipliers", 8, 2, 128);

    public static final DoubleValue DOUBLE_RANGE_TEST = BUILDER
            .comment("Testing the Functionality of Double Ranges")
            .defineInRange("Double Multipliers", 8.0, 0.1, 64.0);

    public static final ConfigValue<List<? extends String>> LIST_ENTRIES_TEST = BUILDER
            .comment("Testing a String list of ResourceLocations")
            .defineList("blocks", Lists.newArrayList("minecraft:water", "minecraft:cobblestone"), () -> "minecraft:dirt", ForgeTest::isResourceLocation);

    public static final ConfigValue<List<? extends Integer>> LIST_INTEGER_TEST = BUILDER
            .comment("Testing a Integer List")
            .defineList("Integer Multiplier List", LIST_TEST_NUMBERS, () -> 0, ForgeTest::isPositive);

    public static final BooleanValue INSCRIBER_RENDER = BUILDER
            .comment("Testing a Boolean")
            .define("Is This Cool?", true);

    public static final ModConfigSpec SPEC = BUILDER.build();
    
    public ForgeTest()
	{
        ModLoadingContext.get().getActiveContainer().registerConfig(ModConfig.Type.COMMON, SPEC);

	}
    
    private static boolean isResourceLocation(Object o) {
        return o instanceof String && ResourceLocation.tryParse((String)o) != null;
    }

    private static boolean isPositive(Object o) {
        return o instanceof Integer && (int) o > 0;
    }
}
