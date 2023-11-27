package carbonconfiglib.gui.impl.minecraft;

import com.mojang.brigadier.context.CommandContextBuilder;
import com.mojang.brigadier.context.ParsedArgument;

import carbonconfiglib.gui.api.DataType;
import carbonconfiglib.utils.Helpers;
import carbonconfiglib.utils.ParseResult;
import net.minecraft.command.CommandSource;
import net.minecraft.world.GameRules.BooleanValue;
import net.minecraft.world.GameRules.IntegerValue;
import net.minecraft.world.GameRules.RuleKey;

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
	public void set(String value);
	public ParseResult<Boolean> isValid(String value);
	public String get();
	public String getDefault();
	public String getDescriptionId();
	public DataType getType();
	
	public static IGameRuleValue bool(RuleKey<BooleanValue> key, BooleanValue value) {
		return new BooleanEntry(key, value);
	}
	
	public static IGameRuleValue ints(RuleKey<IntegerValue> key, IntegerValue value) {
		return new IntegerEntry(key, value);
	}
	
	public static class BooleanEntry implements IGameRuleValue {
		RuleKey<BooleanValue> key;
		BooleanValue value;
		
		private BooleanEntry(RuleKey<BooleanValue> key, BooleanValue value) {
			this.key = key;
			this.value = value;
		}
		
		@Override
		public void set(String value) { this.value.set(Boolean.valueOf(value), null); }
		@Override
		public ParseResult<Boolean> isValid(String value) { return ParseResult.success(true); }
		@Override
		public String get() { return String.valueOf(value.get()); }
		@Override
		public String getDefault() { return String.valueOf(MinecraftConfig.DEFAULTS.getBoolean(key)); }
		@Override
		public String getDescriptionId() { return key.getName(); }
		@Override
		public DataType getType() { return DataType.BOOLEAN; }
	}
	
	public static class IntegerEntry implements IGameRuleValue {
		private static final CommandSource SOURCE = new CommandSource(null, null, null, null, 4, "Server", null, null, null);;
		RuleKey<IntegerValue> key;
		IntegerValue value;
		
		private IntegerEntry(RuleKey<IntegerValue> key, IntegerValue value) {
			this.key = key;
			this.value = value;
		}
		
		@Override
		public void set(String value) {
			ParseResult<Integer> result = Helpers.parseInt(value);
			if(result.isValid()) {
				this.value.updateValue(new CommandContextBuilder<>(null, SOURCE, null, 0).withArgument("value", new ParsedArgument<>(0, 0, result.getValue())).build(""), "value");
			}
		}
		
		@Override
		public ParseResult<Boolean> isValid(String value) {
			ParseResult<Integer> result = Helpers.parseInt(value);
			return result.withDefault(result.isValid());
		}
		
		@Override
		public String get() { return String.valueOf(value.get()); }
		@Override
		public String getDefault() { return String.valueOf(MinecraftConfig.DEFAULTS.getInt(key)); }
		@Override
		public String getDescriptionId() { return key.getName(); }
		@Override
		public DataType getType() { return DataType.INTEGER; }
	}
}
