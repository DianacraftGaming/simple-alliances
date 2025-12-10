package net.dianacraft.alliances.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.dianacraft.alliances.SavedAlliancesData;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;


public class AllianceCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher){
        dispatcher.register(Commands.literal("alliance")
            .then(Commands.literal("create")
                .then(Commands.argument("name", StringArgumentType.string())
                        .executes(AllianceCommand::createAlliance)
                        .then(Commands.argument("displayName", StringArgumentType.greedyString())
                            .executes(AllianceCommand::createAlliance))))
            .then(Commands.literal("invite"))
            .then(Commands.literal("leave"))
            .then(Commands.literal("members"))
            .then(Commands.literal("kick"))
        );

        dispatcher.register(Commands.literal("allymsg")
            .then(Commands.argument("name", StringArgumentType.string())
                .executes(AllianceCommand::createAlliance)
                .then(Commands.argument("message", StringArgumentType.greedyString())
                    //.executes(AllianceCommand::createAlliance)
        )));
    }

    public static int createAlliance(CommandContext<CommandSourceStack> context){
        SavedAlliancesData alliancesData = SavedAlliancesData.getSavedAllianceData(context.getSource().getServer());
        String name = context.getArgument("name", String.class);
        String displayName = context.getArgument("displayName", String.class);
        int result;
        if (displayName == null) {
            result = alliancesData.createAlliance(name);
        } else {
            result = alliancesData.createAlliance(name, displayName);
        }

        ServerPlayer player = context.getSource().getPlayer();
        if (player != null){
            if (result == 0){
                player.displayClientMessage(Component.literal("§c[Alliances] Alliance "+name+" already exists."), false);
            } else {
                player.displayClientMessage(Component.literal("§6[Alliances] §rAlliance "+name+" created successfully."), false);
                alliancesData.getAlliance(name).join(player);
            }
        }

        return result;
    }


}
