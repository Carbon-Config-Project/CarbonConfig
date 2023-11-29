package carbonconfiglib.gui.impl.minecraft;

import carbonconfiglib.gui.api.DataType;
import carbonconfiglib.utils.Helpers;
import carbonconfiglib.utils.ParseResult;
import net.minecraft.world.GameRules;

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
	
	public static IGameRuleValue bool(String key, GameRules rules) {
		return new BooleanEntry(key, rules);
	}
	
	public static IGameRuleValue ints(String key, GameRules rules) {
		return new IntegerEntry(key, rules);
	}
	
	public static class BooleanEntry implements IGameRuleValue {
		String key;
		boolean value;
		GameRules rules;
		
		private BooleanEntry(String key, GameRules rules) {
			this.key = key;
			this.value = rules.getBoolean(key);
			this.rules = rules;
		}
		
		@Override
		public void set(String value) {
			this.value = Boolean.parseBoolean(value);
			this.rules.setOrCreateGameRule(key, Boolean.toString(this.value));
		}
		@Override
		public ParseResult<Boolean> isValid(String value) { return ParseResult.success(true); }
		@Override
		public String get() { return Boolean.toString(this.value); }
		@Override
		public String getDefault() { return String.valueOf(MinecraftConfig.DEFAULTS.getBoolean(key)); }
		@Override
		public String getDescriptionId() { return "gamerule."+key; }
		@Override
		public DataType getType() { return DataType.BOOLEAN; }
	}
	
	public static class IntegerEntry implements IGameRuleValue {
		String key;
		int value;
		GameRules rules;
		
		private IntegerEntry(String key, GameRules rules) {
			this.key = key;
			this.value = rules.getInt(key);
			this.rules = rules;
		}
		
		@Override
		public void set(String value) {
			ParseResult<Integer> result = Helpers.parseInt(value);
			if(result.isValid()) {
				this.value = result.getValue();
				rules.setOrCreateGameRule(key, Integer.toString(this.value));
			}
		}
		
		@Override
		public ParseResult<Boolean> isValid(String value) {
			ParseResult<Integer> result = Helpers.parseInt(value);
			return result.withDefault(result.isValid());
		}
		
		@Override
		public String get() { return Integer.toString(this.value); }
		@Override
		public String getDefault() { return String.valueOf(MinecraftConfig.DEFAULTS.getInt(key)); }
		@Override
		public String getDescriptionId() { return "gamerule."+key; }
		@Override
		public DataType getType() { return DataType.INTEGER; }
	}
}
