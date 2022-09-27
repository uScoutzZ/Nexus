package de.uscoutz.nexus.schematic.listener.entity;

import de.uscoutz.nexus.biomes.Biome;
import de.uscoutz.nexus.profile.Profile;
import de.uscoutz.nexus.schematic.NexusSchematicPlugin;
import de.uscoutz.nexus.schematic.schematics.SchematicType;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPortalEnterEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EntityPortalEnterListener implements Listener {

    private NexusSchematicPlugin plugin;
    private Map<Player, Location> entering;

    public EntityPortalEnterListener(NexusSchematicPlugin plugin) {
        this.plugin = plugin;
        entering = new HashMap<>();
    }

    @EventHandler
    public void onEntityPortalEnter(EntityPortalEnterEvent event) {
        if(event.getEntity() instanceof Player player) {
            Biome biome = plugin.getNexusPlugin().getBiomeManager().getBiome(player.getLocation());
            if(!entering.containsKey(player)) {
                Profile profile = plugin.getNexusPlugin().getWorldManager().getWorldProfileMap().get(player.getWorld());
                Location location;

                if(plugin.getSchematicManager().getSchematicProfileMap().get(profile.getProfileId()).getSchematics().get(SchematicType.PORTAL).size() == 0) {
                    return;
                }

                if(biome != null && biome.getRegion().getBoundingBox().contains(0, 0, 0)) {
                    location = plugin.getNexusPlugin().getLocationManager().getLocation("nether-spawn", player.getWorld());
                } else {
                    location = plugin.getSchematicManager().getSchematicProfileMap().get(profile.getProfileId()).getSchematics().get(SchematicType.PORTAL).get(0).getBoundingBox().getCenter().toLocation(player.getWorld()).clone();
                    location.setY(-51);
                }
                entering.put(player, location);

                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if(player.getEyeLocation().getBlock().getType() == Material.NETHER_PORTAL) {
                            player.teleport(location);
                        }
                        entering.remove(player);
                    }
                }.runTaskLater(plugin, 70);
            }
        }
    }
}
