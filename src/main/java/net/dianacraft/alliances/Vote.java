package net.dianacraft.alliances;

import net.dianacraft.alliances.util.PlayerUtils;
import net.dianacraft.alliances.util.TaskScheduler;
import net.dianacraft.alliances.util.TextUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Vote {
    Map<String, Boolean> votes = new HashMap<>();
    int duration = 120;
    int result = 0;

    Alliance alliance;
    List<String> eligibleVoters;
    String type;
    VoteContext context;

    public Vote(Alliance a, String key, List<String> blacklist, Integer initial, VoteContext context) {
        alliance = a;
        eligibleVoters = new ArrayList<>(a.getPlayers());
        type = key;
        if (blacklist != null) for (String player : blacklist) eligibleVoters.remove(player);
        if (initial != null) result = initial;
        this.context = context;
        TaskScheduler.scheduleTask(duration * 20, () -> alliance.endVote(type));
        if (type.equals("kick")){
            for (String username : eligibleVoters){
                ServerPlayer player = PlayerUtils.getPlayer(username);
                if (player != null){
                    player.sendSystemMessage(getVoteKickButtons(player, ((VoteKickContext) context).target));
                }
            }
        } else if (type.equals("rename")){
            for (String username : eligibleVoters){
                ServerPlayer player = PlayerUtils.getPlayer(username);
                if (player != null){
                    player.sendSystemMessage(getVoteRenameButtons(player, ((VoteRenameContext) context).newName, ((VoteRenameContext) context).newDisplay));
                }
            }
        }
    }

    public boolean calcVotes() {
        for (boolean vote : votes.values()) {
          if (vote) result++; else result--;
        }
        return result > 0;
    }

    public int castVote(String player, boolean vote){
        if (eligibleVoters.contains(player)) {
            votes.put(player, vote);
            return 1;
        } //else {
            //alliance.sendMessage(PlayerUtils.getPlayer(player), "There is no ongoing " + type + " vote or you can't vote in it");
        //}
        return 0;
    }

    public Component getVoteKickButtons(ServerPlayer actor, String target) {
        return TextUtils.format("§6["+alliance.getDisplayName()+"]§r " + actor.getScoreboardName() + " started a votekick for " + target + "\n {}     {}\nThe poll will last for 2 minutes.", TextUtils.hereText(vote("kick", true),"[Kick]", ChatFormatting.RED), TextUtils.hereText(vote("kick", false),"[Don't Kick]", ChatFormatting.DARK_GREEN));
    }

    public Component getVoteRenameButtons(ServerPlayer actor, String newName, String newDisplay) {
        if (newDisplay == null)
            return TextUtils.format("§6["+alliance.getDisplayName()+"]§r " + actor.getScoreboardName() + " is suggesting renaming the alliance to " + newName + "\n {}     {}\nThe poll will last for 2 minutes.", TextUtils.hereText(vote("rename", true),"[Rename]", ChatFormatting.DARK_GREEN), TextUtils.hereText(vote("rename", false),"[Don't Rename]", ChatFormatting.RED));
        else
            return TextUtils.format("§6["+alliance.getDisplayName()+"]§r " + actor.getScoreboardName() + " is suggesting renaming the alliance to " + newName + " ["+newDisplay+"]" + "\n {}     {}\nThe poll will last for 2 minutes.", TextUtils.hereText(vote("rename", true),"[Rename]", ChatFormatting.DARK_GREEN), TextUtils.hereText(vote("rename", false),"[Don't Rename]", ChatFormatting.RED));
    }

    public ClickEvent vote(String vote, boolean value){
        return new ClickEvent.RunCommand("/alliance vote "+alliance.getName()+" "+vote+" "+value);
    }
}

abstract class VoteContext{

}

class VoteKickContext extends VoteContext {
    String target;

    public VoteKickContext(String target){this.target = target;}
}

class VoteRenameContext extends VoteContext {
    String newName;
    String newDisplay;

    public VoteRenameContext(String newName, String newDisplay){
        this.newName = newName;
        this.newDisplay = newDisplay;
    }
}