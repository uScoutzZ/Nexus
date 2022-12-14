package de.uscoutz.nexus.worlds;

import de.uscoutz.nexus.NexusPlugin;
import de.uscoutz.nexus.profile.Profile;
import de.uscoutz.nexus.utilities.FileUtilities;
import lombok.Getter;
import org.bukkit.GameRule;
import org.bukkit.World;
import org.bukkit.WorldCreator;

import java.io.File;
import java.nio.file.Paths;
import java.util.*;

public class WorldManager {

    private NexusPlugin plugin;

    @Getter
    private List<World> emptyWorlds;
    @Getter
    private Map<World, Profile> worldProfileMap;
    @Getter
    private File template;

    public WorldManager(NexusPlugin plugin) {
        this.plugin = plugin;
        emptyWorlds = new ArrayList<>();
        worldProfileMap = new HashMap<>();
        template = new File("template");
        FileUtilities.deleteFolder(Paths.get("template/uid.dat"));
        FileUtilities.deleteFolder(Paths.get("template/session.lock"));
        loadBlankWorlds();
    }

    public void loadBlankWorlds() {
        for(int i = 0; i < plugin.getConfig().getInt("blank-worlds"); i++) {
            File playerWorld = new File("nexusmap_" + (i+1));
            FileUtilities.copyFolder(template, playerWorld);
            World world = new WorldCreator("nexusmap_" + (i+1)).createWorld();
            world.setGameRule(GameRule.DO_MOB_SPAWNING, true);
            emptyWorlds.add(world);
        }
    }
}
