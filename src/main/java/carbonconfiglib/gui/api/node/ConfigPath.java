package carbonconfiglib.gui.api.node;

import java.util.Arrays;
import java.util.List;

import speiger.src.collections.objects.lists.ObjectArrayList;


/**
 * Copyright 2026 Speiger, Meduris
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
public record ConfigPath(String...path)
{
	public ConfigPath(String modId, String configFile) {
		this(new String[]{modId, configFile});
	}
	
	public ConfigPath append(String entry) {
		String[] array = Arrays.copyOf(path, path.length+1);
		array[path.length] = entry;
		return new ConfigPath(array);
	}
	
	public ConfigPath append(List<String> append) {
		List<String> list = new ObjectArrayList<>(path);
		list.addAll(append);
		return new ConfigPath(list.toArray(String[]::new));
	}
	
	public String toPath() {
		return String.join(".", path);
	}
}
