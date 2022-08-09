package de.uscoutz.nexus.worlds;

import de.uscoutz.nexus.NexusPlugin;
import de.uscoutz.nexus.profile.Profile;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.World;

public class NexusWorld {

    private NexusPlugin plugin;

    @Getter
    private Profile profile;
    @Getter
    private World world;
    @Getter
    private Location spawn;

    public NexusWorld(Profile profile, NexusPlugin plugin) {
        this.plugin = plugin;
        this.profile = profile;
        world = plugin.getWorldManager().getEmptyWorlds().remove(0);
        spawn = world.getSpawnLocation();
    }

    public void unload() {

    }
}
