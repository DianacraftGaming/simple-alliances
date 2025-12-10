package net.dianacraft.alliances;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import java.util.ArrayList;
import java.util.List;

public class SavedAlliancesData extends SavedData {
    private List<Alliance> alliances = new ArrayList<>();
    public List<Alliance> getAlliances() {return alliances;}

    public SavedAlliancesData(List<Alliance> alliances){
        this.alliances = alliances;
    }

    private static final Codec<SavedAlliancesData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Alliance.CODEC.listOf().xmap(SavedAlliancesData::reList, SavedAlliancesData::reList).fieldOf("alliances").forGetter(SavedAlliancesData::getAlliances)
    ).apply(instance, SavedAlliancesData::new));
    private static final SavedDataType<SavedAlliancesData> TYPE = new SavedDataType<>(
            "saved_alliance_data",
            SavedAlliancesData::new,
            CODEC,
            null
    );

    public SavedAlliancesData(){

    }

    public int createAlliance(String name, String displayName){
        Alliance alliance = new Alliance(name, displayName);
        if (allianceExists(name)) {
            Main.LOGGER.info("Alliance already exists");
            return 0;
        } else {
            Main.LOGGER.info("Created the alliance");
            alliances.add(alliance);
            setDirty();
            return 1;
        }
    }

    public int createAlliance(String name){
        return createAlliance(name, name);
    }

    public void deleteAlliance(String name){
        Alliance alliance = getAlliance(name);
        if (alliance != null){
            alliances.remove(alliance);
            setDirty();
        }
    }

    public boolean allianceExists(String name){
        for (Alliance alliance : alliances){
            if (alliance.getName().equals(name)) return true;
        }
        return false;
    }

    public Alliance getAlliance(String name){
        for (Alliance alliance : alliances){
            if (alliance.getName().equals(name)) return alliance;
        }
        return null;
    }

    public static SavedAlliancesData getSavedAllianceData(MinecraftServer server){
        ServerLevel level = server.getLevel(ServerLevel.OVERWORLD);
        if (level == null) return new SavedAlliancesData();
        return level.getDataStorage().computeIfAbsent(TYPE);
    }

    private static <T> List<T> reList(List<T> list){
        return new ArrayList<>(list);
    }
}
