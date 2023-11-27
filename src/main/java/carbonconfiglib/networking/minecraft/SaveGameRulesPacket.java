package carbonconfiglib.networking.minecraft;

import com.mojang.brigadier.context.CommandContextBuilder;
import com.mojang.brigadier.context.ParsedArgument;

import carbonconfiglib.CarbonConfig;
import carbonconfiglib.networking.ICarbonPacket;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.world.GameRules;
import net.minecraft.world.GameRules.BooleanValue;
import net.minecraft.world.GameRules.IRuleEntryVisitor;
import net.minecraft.world.GameRules.IntegerValue;
import net.minecraft.world.GameRules.RuleKey;
import net.minecraft.world.GameRules.RuleType;
import net.minecraft.world.GameRules.RuleValue;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

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
public class SaveGameRulesPacket implements ICarbonPacket
{
	GameRules rules;
	
	public SaveGameRulesPacket() {}
	public SaveGameRulesPacket(GameRules rules) {
		this.rules = rules;
	}

	@Override
	public void write(PacketBuffer buffer) {
		buffer.writeCompoundTag(rules.write());
	}
	
	@Override
	public void read(PacketBuffer buffer) {
		rules = new GameRules();
		rules.read(buffer.readCompoundTag());
	}
	
	@Override
	public void process(PlayerEntity player) {
		if(!canIgnorePermissionCheck() && !player.hasPermissionLevel(4)) {
			CarbonConfig.LOGGER.warn("Don't have Permission to Change configs");
			return;
		}
		MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
		if(server == null) return;
		GameRules rule = server.getGameRules();
		rule.read(rules.write());
		CommandSource source = server.getCommandSource();
		GameRules.visitAll(new IRuleEntryVisitor() {
			@Override
			public <T extends RuleValue<T>> void visit(RuleKey<T> key, RuleType<T> type) {
				RuleValue<T> value = rule.get(key);
				CommandContextBuilder<CommandSource> command = new CommandContextBuilder<>(null, source, null, 0);
				if(value instanceof IntegerValue) {
					command.withArgument("value", new ParsedArgument<>(0, 0, ((IntegerValue)value).get()));
				}
				else if(value instanceof BooleanValue) {
					command.withArgument("value", new ParsedArgument<>(0, 0, ((BooleanValue)value).get()));
				}
				value.updateValue(command.build(""), "value");
			}
		});
	}
	
	private boolean canIgnorePermissionCheck() {
		MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
		return !server.isDedicatedServer() && (server instanceof IntegratedServer ? ((IntegratedServer)server).getPublic() : false);
	}
}
