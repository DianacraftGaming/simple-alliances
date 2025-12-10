package net.dianacraft.alliances;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.dianacraft.alliances.util.PlayerUtils;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.List;

public class Alliance {
    List<String> players = new ArrayList<>();
    String name;
    String displayName;

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

    public void sendMessageExcluding(ServerPlayer player, String message){
        List<ServerPlayer> players = getServerPlayers();
        for (ServerPlayer member : players){
            if (member != player){
                PlayerUtils.sendMessage(member, "§6["+displayName+"]", message);
            }
        }
    }

    public void sendMessageAs(ServerPlayer player, String message){
        sendMessageExcluding(player, "<"+player.getScoreboardName()+"> "+message);
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
            sendMessage(actor, target.getScoreboardName()+" is already in the alliance.");
            return 0;
        } else {
            sendMessageExcluding(actor, target.getScoreboardName()+" has been invited to the alliance by "+actor.getScoreboardName());
            players.add(target.getScoreboardName());
            sendMessage(target, "You have been invited to the alliance by " + actor.getScoreboardName() + ".");
            return 1;
        }
    }

    private List<ServerPlayer> getServerPlayers(){
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
}