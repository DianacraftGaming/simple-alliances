package net.dianacraft.alliances;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.dianacraft.alliances.util.PlayerUtils;
import net.dianacraft.alliances.util.TextUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Alliance {
    List<String> players = new ArrayList<>();
    String name;
    String displayName;
    Map<String, Vote> votes = new HashMap<>();

    public Alliance(String name){
        this.name = name;
        this.displayName = name;
    }

    public Alliance(String name, String displayName){
        this.name = name;
        this.displayName = displayName;
    }

    public Alliance(String name, String displayName, List<String> players){
        this.name = name;
        this.displayName = displayName;
        this.players = players;
    }

    public List<String> getPlayers() {return players;}
    public String getName() {return name;}
    public String getDisplayName() {return displayName;}

    public static Codec<Alliance> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("name").forGetter(Alliance::getName),
            Codec.STRING.fieldOf("display_name").forGetter(Alliance::getDisplayName),
            Codec.STRING.listOf().xmap(Alliance::reList, Alliance::reList).fieldOf("players").forGetter(Alliance::getPlayers)
    ).apply(instance, Alliance::new));

    private static <T> List<T> reList(List<T> list){
        return new ArrayList<>(list);
    }

    public void sendMessage(ServerPlayer player, String message){
        PlayerUtils.sendMessage(player, "§6["+displayName+"]", message);
    }

    public void sendMessage(String message){
        List<ServerPlayer> players = getServerPlayers();
        for (ServerPlayer member : players){
            PlayerUtils.sendMessage(member, "§6["+displayName+"]", message);
        }
    }

    public void sendMessageExcluding(ServerPlayer player, String message){
        List<ServerPlayer> players = getServerPlayers();
        for (ServerPlayer member : players){
            if (member != player){
                PlayerUtils.sendMessage(member, "§6["+displayName+"]", message);
            }
        }
    }

    public void sendMessageAs(ServerPlayer player, String message){
        sendMessage("<"+player.getScoreboardName()+"> "+message);

    }

    public int join(ServerPlayer player){
        if (!players.contains(player.getScoreboardName())){
            players.add(player.getScoreboardName());
            //sendMessage(player, "You have been added to the alliance.");
            return 1;
        }
        return 0;
    }

    public int join(ServerPlayer target, ServerPlayer actor){
        if (players.contains(target.getScoreboardName())) {
            PlayerUtils.sendAnnouncement(actor, target.getScoreboardName()+" is already in the alliance.", false);
            return 0;
        } else {
            sendMessageExcluding(actor, target.getScoreboardName()+" has been added to the alliance by "+actor.getScoreboardName());
            players.add(target.getScoreboardName());
            sendMessage(target, "You have been added to the alliance by " + actor.getScoreboardName() + ".");
            PlayerUtils.sendAnnouncement(actor, "You added " + target.getScoreboardName() + " to ["+displayName+"]", true);
            return 1;
        }
    }

    public List<ServerPlayer> getServerPlayers(){
        List<ServerPlayer> result = new ArrayList<>();
        for (String username : players){
            ServerPlayer player = PlayerUtils.getPlayer(username);
            if (player!=null){
                result.add(player);
            }
        }
        return result;
    }

    public boolean isMember(ServerPlayer player){
        return players.contains(player.getScoreboardName());
    }
    public boolean isMember(String player){return players.contains(player);}

    public void castVote(ServerPlayer player, String type, boolean vote) {
        if (!isMember(player)){
            PlayerUtils.sendAnnouncement(player, "You are not in this alliance", false);
            return;
        }
        if (!votes.containsKey(type)) {
            sendMessage(player, "There is no ongoing "+type+" vote or you can't vote in it");
            return;
        }

        if (votes.get(type).castVote(player.getScoreboardName(), vote) == 0) {
            sendMessage(player, "There is no ongoing " + type + " vote or you can't vote in it");
            return;
        }
        sendMessage(player, "Voted successfully!");
    }

    public void startVotekick(ServerPlayer actor, String target){
        List<String> blacklist = List.of(actor.getScoreboardName(), target);
        if (votes.containsKey("kick")) {
            sendMessage(actor, "There's already a votekick in progress! Wait for it to end before starting another.");
            return;
        }
        Vote votekick = new Vote(this, "kick", blacklist, null, new VoteKickContext(target));
        sendMessage(actor, "Started a votekick for " + target);
        votes.put("kick", votekick);
    }

    public void startVoteRename(ServerPlayer actor, String newName, String newDisplay){
        List<String> blacklist = List.of(actor.getScoreboardName());
        if (votes.containsKey("rename")) {
            sendMessage(actor, "There's already a voterename in progress! Wait for it to end before starting another.");
            return;
        }
        Vote voterename = new Vote(this, "rename", blacklist, null, new VoteRenameContext(newName, newDisplay));
        sendMessage(actor, "Started a voterename");
        votes.put("rename", voterename);
    }

    public void endVote(String key){
        if (key.equals("kick")) {
            Vote vote = votes.get("kick");
            String target = ((VoteKickContext) vote.context).target;
            if (vote.calcVotes()) {
                sendMessageExcluding(PlayerUtils.getPlayer(target),target + " has been kicked from the alliance.");
                sendMessage(PlayerUtils.getPlayer(target),"You have been kicked from the alliance.");
                players.remove(target);
            } else {
                sendMessageExcluding(PlayerUtils.getPlayer(target), target + " remains in the alliance");
            }
        } else if (key.equals("rename")){
            Vote vote = votes.get("rename");
            String newName = ((VoteRenameContext) vote.context).newName;
            String newDisplay = ((VoteRenameContext) vote.context).newDisplay;

            if (vote.calcVotes()) {
                if (newDisplay == null) {
                    sendMessage("The alliance has been renamed to "+newName);
                    name = newName;
                    displayName = newName;
                } else {
                    sendMessage("The alliance has been renamed to "+newName+" ["+newDisplay+"]");
                    name = newName;
                    displayName = newDisplay;
                }
            } else {
                sendMessage("The name of the alliance stays the same");
            }
        }
        votes.remove(key);
    }
}