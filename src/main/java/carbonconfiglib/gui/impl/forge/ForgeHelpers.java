package carbonconfiglib.gui.impl.forge;

import java.nio.file.Path;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.io.WritingMode;

import carbonconfiglib.gui.nodes.ColorElement.FormatType;
import carbonconfiglib.utils.Helpers;
import carbonconfiglib.utils.ParseResult;
import net.neoforged.fml.config.IConfigEvent;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.ModConfigSpec.Range;
import net.neoforged.neoforge.common.ModConfigSpec.ValueSpec;

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
public class ForgeHelpers
{
	public static void saveConfig(Path path, CommentedConfig data) {
		Helpers.ensureFolder(path.getParent());
		data.configFormat().createWriter().write(data, path, WritingMode.REPLACE);
	}
	
	public static void saveConfig(CommentedConfig data, ModConfig config) {
		config.getConfigData().putAll(data);
		config.save();
        IConfigEvent.reloading(config).post();
	}
	
	public static boolean isColor(Object value) {
		return value instanceof String && FormatType.guessType((String)value) != null;
	}
	
	public static String removeExtension(String file) {
		int index = file.lastIndexOf('.');
		return index == -1 ? file : file.substring(0, index);
	}
	
	public static ParseResult<Boolean> parseBoolean(String value) {
		 return ParseResult.success(Boolean.parseBoolean(value)); 
	}
	
	public static ParseResult<String> parseString(String value) {
		 return ParseResult.success(value); 
	}
	
	public static ParseResult<Long> parseLong(String value) {
		try { return ParseResult.success(Long.parseLong(value)); }
		catch (Exception e) { return ParseResult.error(value, e, "Couldn't parse Number"); }
	}

	public static ParseResult<Float> parseFloat(String value) {
		try { return ParseResult.success(Float.parseFloat(value)); } 
		catch (Exception e) { return ParseResult.error(value, e, "Couldn't parse Number"); }
	}
	
	public static String getFloatLimit(Object[] value) {
		return getLimit(value, -Float.MAX_VALUE, Float.MAX_VALUE);
	}
	
	public static String getDoubleLimit(Object[] value) {
		return getLimit(value, -Double.MAX_VALUE, Double.MAX_VALUE);
	}
	
	public static String getLongLimit(Object[] value) {
		return getLimit(value, Long.MIN_VALUE, Long.MAX_VALUE);
	}
	
	public static String getIntLimit(Object[] value) {
		return getLimit(value, Integer.MIN_VALUE, Integer.MAX_VALUE);
	}
	
	public static String getLimit(Object[] value, Object min, Object max) {
		if(value[0] instanceof Number && value[1] instanceof Number) {
			if(value[0].equals(min)) {
				if(value[1].equals(max)) return "";
				return "Range: < "+value[1];
			}
			if(value[1].equals(max)) {
				if(value[0].equals(min)) return "";
				return "Range: > "+value[0];
			}
			return "Range: "+value[0]+" ~ "+value[1];
		}
		return "";
	}
	
	public static Object[] getRangeInfo(ValueSpec spec) {
		Range<?> range = spec.getRange();
		if(range == null) return null;
		return new Object[] {range.getMin(), range.getMax()};
	}
	
}
