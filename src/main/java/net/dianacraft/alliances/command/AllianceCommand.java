package net.dianacraft.alliances.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.dianacraft.alliances.SavedAlliancesData;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;


public class AllianceCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher){
        dispatcher.register(Commands.literal("alliance")
                .then(Commands.literal("create")
                    .then(Commands.argument("name", StringArgumentType.string())
                            .executes(AllianceCommand::createAlliance)
                            .then(Commands.argument("displayName", StringArgumentType.greedyString())
                                .executes(AllianceCommand::createAlliance)))));
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

        if (context.getSource().getPlayer() != null){
            if (result == 0){

            }
        }

        return result;
    }
}
