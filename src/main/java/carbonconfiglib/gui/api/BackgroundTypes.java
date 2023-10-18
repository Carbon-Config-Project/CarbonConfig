package carbonconfiglib.gui.api;

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
public enum BackgroundTypes
{
	PLANKS(BackgroundTexture.of().build()),
	STONE(BackgroundTexture.of("minecraft:textures/block/stone.png").withBrightness(192).build()),
	DIRT(BackgroundTexture.of("minecraft:textures/block/dirt.png").withBrightness(192).build()),
	SEA_PRISM(BackgroundTexture.of("minecraft:textures/block/prismarine_bricks.png").withBrightness(192).build()),
	BUBBLE_CORAL(BackgroundTexture.of("minecraft:textures/block/bubble_coral_block.png").withBrightness(192).build()),
	MISSING(BackgroundTexture.of("minecraft:textures/block/missing_texture.png").withBrightness(192).build()),
	NETHERRACK(BackgroundTexture.of("minecraft:textures/block/netherrack.png").withBrightness(192).build()),
	RED_NETHER_BRICKS(BackgroundTexture.of("minecraft:textures/block/red_nether_bricks.png").withBrightness(192).build()),
	TUFF(BackgroundTexture.of("minecraft:textures/block/tuff.png").withBrightness(192).build()),
	CALCITE(BackgroundTexture.of("minecraft:textures/block/calcite.png").withBrightness(192).build()),
	AMETHYST(BackgroundTexture.of("minecraft:textures/block/amethyst_block.png").withBrightness(192).build()),
	GRANITE(BackgroundTexture.of("minecraft:textures/block/granite.png").withBrightness(192).build()),
	POLISHED_ANDESITE(BackgroundTexture.of("minecraft:textures/block/polished_andesite.png").withBrightness(192).build()),
	DEEPSLATE_BRICKS(BackgroundTexture.of("minecraft:textures/block/deepslate_bricks.png").withBrightness(192).build()),
	PURPUR_PILLAR(BackgroundTexture.of("minecraft:textures/block/purpur_pillar.png").withBrightness(192).build()),
	RED_SAND(BackgroundTexture.of("minecraft:textures/block/red_sand.png").withBrightness(192).build()),
	PACKED_ICE(BackgroundTexture.of("minecraft:textures/block/packed_ice.png").withBrightness(192).build()),
	CRYING_OBSIDIAN(BackgroundTexture.of("minecraft:textures/block/crying_obsidian.png").withBrightness(192).build()),
	CYAN_CONCRETE_POWDER(BackgroundTexture.of("minecraft:textures/block/cyan_concrete_powder.png").withBrightness(192).build()),
	EXPOSED_COPPER(BackgroundTexture.of("minecraft:textures/block/exposed_copper.png").withBrightness(192).build()),
	MUD(BackgroundTexture.of("minecraft:textures/block/mud.png").withBrightness(192).build()),
	RAW_GOLD(BackgroundTexture.of("minecraft:textures/block/raw_gold_block.png").withBrightness(192).build()),
	RAW_IRON(BackgroundTexture.of("minecraft:textures/block/raw_iron_block.png").withBrightness(192).build()),
	HONEY(BackgroundTexture.of("minecraft:textures/block/honey_block_side.png").withBrightness(192).build()),
	HONEY_COMB(BackgroundTexture.of("minecraft:textures/block/honeycomb_block.png").withBrightness(192).build()),
	HAY(BackgroundTexture.of("minecraft:textures/block/hay_block_side.png").withBrightness(192).build());
	BackgroundTexture texture;

	private BackgroundTypes(BackgroundTexture texture) {
		this.texture = texture;
	}
	
	public BackgroundTexture getTexture() {
		return texture;
	}
}
