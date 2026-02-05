package carbonconfiglib.test;

import java.util.EnumSet;
import java.util.UUID;

import com.mojang.util.UUIDTypeAdapter;

import carbonconfiglib.CarbonConfig;
import carbonconfiglib.api.IConfigSerializer;
import carbonconfiglib.config.Config;
import carbonconfiglib.config.ConfigHandler;
import carbonconfiglib.impl.entries.NamedForgeRegistry;
import carbonconfiglib.utils.ParseResult;
import carbonconfiglib.utils.ParsedCollections.ParsedList;
import carbonconfiglib.utils.ParsedCollections.ParsedMap;
import carbonconfiglib.utils.structure.IStructuredData.EntryDataType;
import carbonconfiglib.utils.structure.StructureCompound.CompoundBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.world.level.block.Block;
import speiger.src.collections.objects.utils.ObjectLists;

public class CompoundListTest
{
	public static void initCompoundList() {
		Config config = new Config("compoundlisttest");
		config.add("general").addParsedArray("testing", ObjectLists.empty(), TestObject.createPermission());
		config.add("special").add(CarbonConfig.createRegistryBuilder("testing", Block.class).build(NamedForgeRegistry.BLOCKS));
		ConfigHandler handler = CarbonConfig.CONFIGS.createConfig(config);
		handler.register();
	}
	
	public record TestObject(UUID id, EnumSet<ChatFormatting> formatting) {
		
		public TestObject() {
			this(UUID.fromString("84d6171c-8546-4309-bf3e-fc0956f32ea0"), EnumSet.allOf(ChatFormatting.class));
		}
		
		public TestObject(ParsedMap map) {
			this(UUIDTypeAdapter.fromString(map.getOrThrow("PlayerId", String.class)), map.getOrThrow("Permissions", ParsedList.class).collect(ChatFormatting.class, EnumSet.noneOf(ChatFormatting.class)));
		}
		
		public boolean isValid() {
			return id != null;
		}
		
		public ParsedMap serialize() {
			ParsedMap map = new ParsedMap();
			map.put("PlayerId", id.toString());
			map.put("Permissions", new ParsedList(formatting));
			return map;
		}
		
		public static IConfigSerializer<TestObject> createPermission() {
			CompoundBuilder builder = new CompoundBuilder().setNewLined(true)
				.simple("PlayerId", EntryDataType.STRING)
				.listEnum("Permissions", ChatFormatting.class, false);
			return IConfigSerializer.simple(builder.build(), new TestObject(), TestObject::new, TestObject::serialize, T -> ParseResult.result(T.isValid(), IllegalArgumentException::new, "Not a valid Player Id"));
		}
		
	}
}
