package carbonconfiglib.gui.api.background;

import java.util.function.BooleanSupplier;

import carbonconfiglib.CarbonConfig;
import carbonconfiglib.utils.Helpers;
import net.minecraft.resources.Identifier;

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
public class BackgroundTexture
{
	public static final BackgroundTexture DEFAULT = of().build();
	
	Identifier backgroundTexture;
	Identifier foregroundTexture;
	int backgroundBrightness = 32;
	int foregroundBrightness = 64;
	BooleanSupplier disableBackgroundInLevel = () -> !CarbonConfig.INGAME_BACKGROUND.get();
	BackgroundHolder holder = new BackgroundHolder(this);
	
	private BackgroundTexture() {}
	
	public static Builder of() {
		return new Builder().withTexture("minecraft:textures/block/oak_planks.png").withBrightness(192);
	}
	
	public static Builder of(String id) {
		return new Builder().withTexture(id);
	}
	
	public static Builder of(String namespace, String path) {
		return new Builder().withTexture(namespace, path);
	}
	
	public static Builder of(Identifier location) {
		return new Builder().withTexture(location);
	}
	
	public BackgroundHolder asHolder() {
		return holder;
	}
	
	public Identifier getBackgroundTexture() {
		return backgroundTexture;
	}
	
	public Identifier getForegroundTexture() {
		return foregroundTexture;
	}
	
	public boolean shouldDisableInLevel() {
		return disableBackgroundInLevel.getAsBoolean();
	}
	
	public int getBackgroundBrightness() {
		return backgroundBrightness;
	}
	
	public int getForegroundBrightness() {
		return foregroundBrightness;
	}
	
	public static class BackgroundHolder {
		BackgroundTexture texture;

		private BackgroundHolder(BackgroundTexture texture) {
			this.texture = texture;
		}

		public boolean shouldDisableInLevel() {
			return getTexture().shouldDisableInLevel();
		}
		
		public BackgroundTexture getTexture() {
			if(texture == DEFAULT || CarbonConfig.FORCE_CUSTOM_BACKGROUND.get()) return CarbonConfig.BACKGROUNDS.get().getTexture();
			return texture;
		}
	}
	
	public static class Builder {
		BackgroundTexture texture = new BackgroundTexture();
		
		public Builder withBackground(String id) {
			return withBackground(Identifier.tryParse(id));
		}
		
		public Builder withBackground(String namespace, String path) {
			return withBackground(Identifier.fromNamespaceAndPath(namespace, path));
		}
		
		public Builder withBackground(Identifier texture) {
			this.texture.backgroundTexture = texture;
			return this;
		}
		
		public Builder withForeground(String id) {
			return withForeground(Identifier.tryParse(id));
		}
		
		public Builder withForeground(String namespace, String path) {
			return withForeground(Identifier.fromNamespaceAndPath(namespace, path));
		}
		
		public Builder withForeground(Identifier texture) {
			this.texture.foregroundTexture = texture;
			return this;
		}
		
		public Builder withTexture(String id) {
			return withTexture(Identifier.tryParse(id));
		}
		
		public Builder withTexture(String namespace, String path) {
			return withTexture(Identifier.fromNamespaceAndPath(namespace, path));
		}
		
		public Builder withTexture(Identifier texture) {
			this.texture.backgroundTexture = texture;
			this.texture.foregroundTexture = texture;
			return this;
		}
		
		public Builder withBrightness(int brightness) {
			int sanitized = Helpers.clamp(brightness, 0, 255);
			return withBrightness(sanitized, sanitized / 2);
		}
		
		public Builder withBrightness(int foreground, int background) {
			texture.backgroundBrightness = Helpers.clamp(background, 0, 255);
			texture.foregroundBrightness = Helpers.clamp(foreground, 0, 255);
			return this;
		}
		
		public Builder withBackground(int brightness) {
			this.texture.backgroundBrightness = Helpers.clamp(brightness, 0, 255);
			return this;
		}
		
		public Builder withForeground(int brightness) {
			this.texture.foregroundBrightness = Helpers.clamp(brightness, 0, 255);
			return this;
		}
		
		public Builder withBackgroundPresentIngame(boolean value) {
			texture.disableBackgroundInLevel = new Constant(!value);
			return this;
		}
		
		public BackgroundTexture build() {
			BackgroundTexture result = texture;
			texture = null;
			return result;
		}
	}
	
	private static class Constant implements BooleanSupplier {
		boolean value;

		public Constant(boolean value) {
			this.value = value;
		}
		
		@Override
		public boolean getAsBoolean() {
			return value;
		}
	}
	
}
