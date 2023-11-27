package carbonconfiglib.gui.impl.forge;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.UnmodifiableConfig;
import com.google.common.collect.Iterables;

import carbonconfiglib.gui.api.IConfigFolderNode;
import carbonconfiglib.gui.api.IConfigNode;
import carbonconfiglib.impl.Reflects;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;

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
public class ForgeNode implements IConfigFolderNode
{
	List<String> paths;
	CommentedConfig config;
	ForgeConfigSpec spec;
	UnmodifiableConfig specConfig;
	List<IConfigNode> children;
	ITextComponent tooltip;
	
	public ForgeNode(List<String> paths, CommentedConfig config, ForgeConfigSpec spec) {
		this(paths, config, spec, spec.getValues());
	}
	
 	public ForgeNode(List<String> paths, CommentedConfig config, ForgeConfigSpec spec, UnmodifiableConfig specConfig) {
		this.paths = paths;
		this.config = config;
		this.spec = spec;
		this.specConfig = specConfig;
		loadComment();
	}
 	
 	private void loadComment() {
 		String value = Reflects.getComment(spec).get(paths);
 		if(value == null) return;
		String[] array = value.split("\n");
		if(array != null && array.length > 0) {
			TextComponent comp = new StringTextComponent("");
			for(int i = 0;i<array.length;comp.appendText("\n").appendText(array[i++]));
			tooltip = comp;
		}
 	}

	@Override
	public List<IConfigNode> getChildren() {
		if(children == null) {
			children = new ObjectArrayList<>();
			for(Map.Entry<String, Object> entry : specConfig.valueMap().entrySet()) {
				Object value = entry.getValue();
				if(value instanceof UnmodifiableConfig) {
					List<String> list = new ObjectArrayList<>(paths);
					list.add(entry.getKey());
					children.add(new ForgeNode(list, config, spec, (UnmodifiableConfig)value));
				}
				else if(value instanceof ConfigValue) {
					children.add(new ForgeLeaf(spec, (ConfigValue<?>)value, config));
				}
			}
		}
		return children;
	}
	@Override
	public String getNodeName() { return paths.isEmpty() ? null : Iterables.getLast(paths, "Root").toLowerCase(Locale.ROOT); }
	@Override
	public ITextComponent getName() { return IConfigNode.createLabel(Iterables.getLast(paths, "Root")); }
	@Override
	public ITextComponent getTooltip() {
		TextComponent comp = new StringTextComponent("");
		comp.appendSibling(new StringTextComponent(Iterables.getLast(paths, "Root")).applyTextStyle(TextFormatting.YELLOW));
		if(tooltip != null) comp.appendSibling(tooltip);
		return comp;
	}
	
	@Override
	public boolean isRoot() {
		return paths.isEmpty();
	}
}
