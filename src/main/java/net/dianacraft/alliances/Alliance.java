package net.dianacraft.alliances;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
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
        player.displayClientMessage(Component.literal("§6["+displayName+"] §r"+message), false);
    }

    public void join(ServerPlayer player){
        if (!players.contains(player.getScoreboardName())){
            players.add(player.getScoreboardName());
            sendMessage(player, "You have been added to the alliance.");
        }
    }

    public void join(ServerPlayer target, ServerPlayer actor){
        if (players.contains(target.getScoreboardName())) {
            sendMessage(actor, target.getScoreboardName()+" is already in the alliance.");
        } else {
            players.add(target.getScoreboardName());
            sendMessage(target, "You have been added to the alliance by " + actor.getScoreboardName() + ".");
        }
    }

    private List<ServerPlayer> getServerPlayers(){
        List<ServerPlayer> result = new ArrayList<>();
        for (String username : players){

        }
        return result;
    }

    public boolean isMember(ServerPlayer player){
        return players.contains(player.getScoreboardName());
    }
}
