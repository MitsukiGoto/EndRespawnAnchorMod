package com.github.mikn.end_respawn_anchor.command;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.TextComponent;

public class RespawnPositionCheckCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("respawn_position_check").executes(context -> {
            context.getSource().sendSuccess(new TextComponent("aaa"), false);
            return 1;
        }));
    }
}
