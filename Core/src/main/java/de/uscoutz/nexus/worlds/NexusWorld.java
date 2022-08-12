package de.uscoutz.nexus.worlds;

import de.uscoutz.nexus.NexusPlugin;
import de.uscoutz.nexus.profile.Profile;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.scheduler.BukkitRunnable;

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
        assign();
    }

    public void assign() {
        Block block = world.getSpawnLocation().add(0, 1, 0).getBlock();
        new BukkitRunnable() {
            @Override
            public void run() {
                block.setType(Material.OAK_SIGN);
                Sign sign = (Sign) block.getState();
                sign.line(0, Component.text(profile.getOwner() + ""));
                sign.update();
            }
        }.runTask(plugin);
    }

    public void unload() {

    }
}
