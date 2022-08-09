package de.uscoutz.nexus.worlds;

import de.uscoutz.nexus.NexusPlugin;
import lombok.Getter;
import org.bukkit.World;

import java.util.*;

public class WorldManager {

    private NexusPlugin plugin;

    @Getter
    private List<World> emptyWorlds;

    public WorldManager(NexusPlugin plugin) {
        this.plugin = plugin;
        emptyWorlds = new ArrayList<>();
    }

    public void loadBlankWorlds() {
        for(int i = 0; i < plugin.getConfig().getInt("blank-worlds"); i++) {

        }
    }
}
