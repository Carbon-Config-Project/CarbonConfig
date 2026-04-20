package carbonconfiglib.gui.impl.minecraft;

import com.mojang.serialization.Codec;

import carbonconfiglib.gui.api.types.DataType;
import carbonconfiglib.utils.Helpers;
import carbonconfiglib.utils.ParseResult;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.gamerules.GameRule;
import net.minecraft.world.level.gamerules.GameRules;

/**
 * Copyright 2023 Speiger, Meduris
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
public interface IGameRuleValue
{
	public static final Codec<GameRules> RULES = GameRules.codec(FeatureFlags.REGISTRY.allFlags());
	public void set(String value);
	public ParseResult<Boolean> isValid(String value);
	public String get();
	public String getDefault();
	public String getDescriptionId();
	public DataType getType();
	
	public static IGameRuleValue bool(GameRule<Boolean> key, GameRules rules) {
		return new BooleanEntry(key, rules);
	}
	
	public static IGameRuleValue ints(GameRule<Integer> key, GameRules rules) {
		return new IntegerEntry(key, rules);
	}
	
	public static GameRules copy(GameRules original) {
		return original.copy(FeatureFlags.REGISTRY.allFlags());
	}
	
	public static CompoundTag write(GameRules rules) {
		return RULES.encodeStart(NbtOps.INSTANCE, rules).getOrThrow().asCompound().get();
	}
	
	public static GameRules read(CompoundTag tag) {
		return RULES.decode(NbtOps.INSTANCE, tag).getOrThrow().getFirst();
	}
	
	public static class BooleanEntry implements IGameRuleValue {
		GameRule<Boolean> key;
		GameRules rules;
		
		private BooleanEntry(GameRule<Boolean> key, GameRules rules) {
			this.key = key;
			this.rules = rules;
		}
		
		@Override
		public void set(String value) { this.rules.set(key, Boolean.valueOf(value), null); }
		@Override
		public ParseResult<Boolean> isValid(String value) { return ParseResult.success(true); }
		@Override
		public String get() { return String.valueOf(rules.get(key)); }
		@Override
		public String getDefault() { return String.valueOf(key.defaultValue()); }
		@Override
		public String getDescriptionId() { return key.getDescriptionId(); }
		@Override
		public DataType getType() { return DataType.BOOLEAN; }
	}
	
	public static class IntegerEntry implements IGameRuleValue {
		GameRule<Integer> key;
		GameRules rules;
		
		private IntegerEntry(GameRule<Integer> key, GameRules rules) {
			this.key = key;
			this.rules = rules;
		}
		
		@Override
		public void set(String value) {
			ParseResult<Integer> result = Helpers.parseInt(value);
			if(result.isValid()) {
				rules.set(key, result.getValue(), null);
			}
		}
		
		@Override
		public ParseResult<Boolean> isValid(String value) {
			ParseResult<Integer> result = Helpers.parseInt(value);
			return result.withDefault(result.isValid());
		}
		
		@Override
		public String get() { return String.valueOf(rules.getAsString(key)); }
		@Override
		public String getDefault() { return String.valueOf(key.defaultValue()); }
		@Override
		public String getDescriptionId() { return key.getDescriptionId(); }
		@Override
		public DataType getType() { return DataType.INTEGER; }
	}
}
