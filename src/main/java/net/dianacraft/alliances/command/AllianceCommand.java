package net.dianacraft.alliances.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.dianacraft.alliances.Alliance;
import net.dianacraft.alliances.Main;
import net.dianacraft.alliances.SavedAlliancesData;
import net.dianacraft.alliances.util.PlayerUtils;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.List;


public class AllianceCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher){
        dispatcher.register(Commands.literal("alliance")
            .then(Commands.literal("create")
                .then(Commands.argument("name", StringArgumentType.string())
                    .executes(AllianceCommand::createAlliance)
                    .then(Commands.argument("displayName", StringArgumentType.greedyString())
                        .executes(AllianceCommand::createAlliance))))
            .then(Commands.literal("add")
                .then(Commands.argument("name", StringArgumentType.string())
                .suggests((context, builder) -> SharedSuggestionProvider.suggest(getAllianceSuggestions(context), builder))
                    .then(Commands.argument("username", EntityArgument.player())
                            .executes(AllianceCommand::invitePlayer))))
            .then(Commands.literal("leave")
                .then(Commands.argument("name", StringArgumentType.string())
                .suggests((context, builder) -> SharedSuggestionProvider.suggest(getAllianceSuggestions(context), builder))
                    .executes(AllianceCommand::leaveAlliance)))
            .then(Commands.literal("members")
                .then(Commands.argument("name", StringArgumentType.string())
                .suggests((context, builder) -> SharedSuggestionProvider.suggest(getAdminAllianceSuggestions(context), builder))
                    .executes(AllianceCommand::listMembers)))
            /*.then(Commands.literal("kick")
                .then(Commands.argument("name", StringArgumentType.string())
                .suggests((context, builder) -> SharedSuggestionProvider.suggest(getAllianceSuggestions(context), builder))
                    .then(Commands.argument("username", EntityArgument.player())
                        .executes(AllianceCommand::startVoteKick)
                        .then(Commands.argument("message", StringArgumentType.greedyString())
                            .executes(AllianceCommand::startVoteKick)))))
            .then(Commands.literal("rename")
                .then(Commands.argument("name", StringArgumentType.string())
                .suggests((context, builder) -> SharedSuggestionProvider.suggest(getAllianceSuggestions(context), builder))
                    .then(Commands.argument("newName", StringArgumentType.string())
                        .executes(AllianceCommand::startVoteRename)
                        .then(Commands.argument("newDisplayName", StringArgumentType.greedyString())
                            .executes(AllianceCommand::startVoteRename)))))*/);

        dispatcher.register(Commands.literal("allymsg")
            .then(Commands.argument("name", StringArgumentType.string())
                .suggests((context, builder) -> SharedSuggestionProvider.suggest(getAllianceSuggestions(context), builder))
                .then(Commands.argument("message", StringArgumentType.greedyString())
                    .executes(AllianceCommand::sendMessage))));
    }

    public static int createAlliance(CommandContext<CommandSourceStack> context){
        SavedAlliancesData alliancesData = SavedAlliancesData.getSavedAllianceData(context.getSource().getServer());
        String name = context.getArgument("name", String.class);
        String displayName = null;
        try {
            displayName = context.getArgument("displayName", String.class);
        } catch (Exception ignored){}

        int result;
        if (displayName == null) {
            result = alliancesData.createAlliance(name);
        } else {
            result = alliancesData.createAlliance(name, displayName);
        }

        ServerPlayer player = context.getSource().getPlayer();
        if (player != null){
            if (result == 0){
                PlayerUtils.sendAnnouncement(player, "Alliance ["+name+"] already exists.",false);
            } else {
                PlayerUtils.sendAnnouncement(player, "Alliance ["+displayName+"] created successfully.",true);
                return alliancesData.getAlliance(name).join(player);
            }
        }

        return 0;
    }

    public static int invitePlayer(CommandContext<CommandSourceStack> context){
        SavedAlliancesData alliancesData = SavedAlliancesData.getSavedAllianceData(context.getSource().getServer());
        String name = context.getArgument("name", String.class);
        Alliance alliance = alliancesData.getAlliance(name);
        ServerPlayer player = context.getSource().getPlayer();

        ServerPlayer member;
        try {
            member = EntityArgument.getPlayer(context, "username");
        } catch (CommandSyntaxException e) {
            if (player != null) {
                PlayerUtils.sendAnnouncement(player, "This player is not currently online.", false);
            }
            return 0;
        }

        if (player != null){
            if (alliance == null){
                PlayerUtils.sendAnnouncement(player, "You are not in this alliance or it doesn't exist", false);
                return 0;
            }
            if (alliance.isMember(player)){
                if (member == null){
                    PlayerUtils.sendAnnouncement(player, "This player is not currently online.", false);
                    return 0;
                } else {
                    return alliance.join(member, player);
                }
            } else {
                PlayerUtils.sendAnnouncement(player, "You are not in this alliance or it doesn't exist", false);
                return 0;
            }
        }
        return 0;
    }

    public static int leaveAlliance(CommandContext<CommandSourceStack> context){
        SavedAlliancesData alliancesData = SavedAlliancesData.getSavedAllianceData(context.getSource().getServer());
        String name = context.getArgument("name", String.class);
        Alliance alliance = alliancesData.getAlliance(name);
        ServerPlayer player = context.getSource().getPlayer();

        if (player == null){
            return 0;
        } else if (alliance.isMember(player)){
            alliance.getPlayers().remove(player.getScoreboardName());
            PlayerUtils.sendAnnouncement(player, "You left the alliance ["+ alliance.getDisplayName() +"]", true);
            alliance.sendMessageExcluding(player, player.getScoreboardName()+" left the alliance.");
            if (alliance.getPlayers().isEmpty()){
                PlayerUtils.sendAnnouncement(player, "Alliance ["+name+"] has been deleted as there are no players left in it.", true);
                alliancesData.deleteAlliance(name);
            }
            return 1;
        }
        PlayerUtils.sendAnnouncement(player, "You are not in this alliance or it doesn't exist", false);
        return 0;
    }

    public static int listMembers(CommandContext<CommandSourceStack> context){
        SavedAlliancesData alliancesData = SavedAlliancesData.getSavedAllianceData(context.getSource().getServer());
        String name = context.getArgument("name", String.class);
        Alliance alliance = alliancesData.getAlliance(name);
        ServerPlayer player = context.getSource().getPlayer();

        if (player == null){
            context.getSource().sendSystemMessage(Component.literal("Members of ["+alliance.getDisplayName()+"]: "+alliance.getPlayers().toString()));
            //Main.LOGGER.info();
            return 0;
        } else if (alliance != null) {
            if (alliance.isMember(player) || PlayerUtils.isAdmin(player)){
                PlayerUtils.sendAnnouncement(player, "Members of ["+alliance.getDisplayName()+"]: "+alliance.getPlayers().toString(), true);
                return 1;
            } else {
                PlayerUtils.sendAnnouncement(player, "You are not in this alliance or it doesn't exist", false);
                return 0;
            }
        }
        PlayerUtils.sendAnnouncement(player, "You are not in this alliance or it doesn't exist", false);
        return 0;
    }

    public static int startVoteKick(CommandContext<CommandSourceStack> context){
        SavedAlliancesData alliancesData = SavedAlliancesData.getSavedAllianceData(context.getSource().getServer());
        String name = context.getArgument("name", String.class);
        return 0;
    }

    public static int startVoteRename(CommandContext<CommandSourceStack> context){
        return 0;
    }

    public static int sendMessage(CommandContext<CommandSourceStack> context){
        SavedAlliancesData alliancesData = SavedAlliancesData.getSavedAllianceData(context.getSource().getServer());
        String name = context.getArgument("name", String.class);
        String message = context.getArgument("message", String.class);
        Alliance alliance = alliancesData.getAlliance(name);
        ServerPlayer player = context.getSource().getPlayer();

        if (player != null) {
            if (alliance == null) {
                PlayerUtils.sendAnnouncement(player, "You are not in this alliance or it doesn't exist", false);
                return 0;
            }
            if (alliance.isMember(player)) {
                alliance.sendMessageAs(player, message);
                return 1;
            }
        }
        return 0;
    }

    private static List<String> getAllianceSuggestions(CommandContext<CommandSourceStack> context){
        SavedAlliancesData alliancesData = SavedAlliancesData.getSavedAllianceData(context.getSource().getServer());
        ServerPlayer player = context.getSource().getPlayer();
        List<String> result = new ArrayList<>();
        if (player == null) return result;

        for (Alliance alliance : alliancesData.getAlliances()){
            if (alliance.isMember(player)){
                result.add(alliance.getName());
            }
        }
        return result;
    }

    private static List<String> getAdminAllianceSuggestions(CommandContext<CommandSourceStack> context){
        SavedAlliancesData alliancesData = SavedAlliancesData.getSavedAllianceData(context.getSource().getServer());
        ServerPlayer player = context.getSource().getPlayer();
        List<String> result = new ArrayList<>();

        if (PlayerUtils.isAdmin(player)){
            for (Alliance alliance : alliancesData.getAlliances()){
                result.add(alliance.getName());
            }
        } else {
            result = getAllianceSuggestions(context);
        }

        return result;
    }
}
