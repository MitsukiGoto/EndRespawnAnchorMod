package com.github.mikn.end_respawn_anchor.command;

import com.github.mikn.end_respawn_anchor.EndRespawnAnchor;
import com.github.mikn.end_respawn_anchor.util.OtherDimensionSpawnPosition;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.GameProfileArgument;
import net.minecraft.network.chat.TextComponent;

import java.util.Collection;

public class RespawnPositionCheckCommand {
    public static void register(CommandDispatcher<CommandSourceStack> p_136559_) {
        p_136559_.register(Commands.literal("respawn_position_check")
                .requires((p_136563_) -> p_136563_.hasPermission(4))
                .then(Commands.argument("targets", GameProfileArgument.gameProfile()).executes((p_136569_) -> PositionCheck(p_136569_.getSource(), GameProfileArgument.getGameProfiles(p_136569_, "targets")))));
    }

    private static int PositionCheck(CommandSourceStack source, Collection<GameProfile> targets) {
        for(var gameProfile: targets) {
            if(EndRespawnAnchor.spawnPositions.entrySet().stream().anyMatch(entry -> entry.getKey().equals(gameProfile.getId()))) {
                OtherDimensionSpawnPosition pos = EndRespawnAnchor.spawnPositions.get(gameProfile.getId());
                source.sendSuccess(new TextComponent(pos.toString() + " Player: " + gameProfile.getName()), true);
            }
        }
        return 1;
    }
}
