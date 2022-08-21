package de.uscoutz.nexus.schematic.listener.profile;

import de.uscoutz.nexus.NexusPlugin;
import de.uscoutz.nexus.events.ProfileLoadEvent;
import de.uscoutz.nexus.profile.Profile;
import de.uscoutz.nexus.schematic.NexusSchematicPlugin;
import de.uscoutz.nexus.schematic.schematics.Schematic;
import de.uscoutz.nexus.schematic.schematics.SchematicType;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class ProfileLoadListener implements Listener {

    private NexusSchematicPlugin plugin;

    public ProfileLoadListener(NexusSchematicPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onProfileLoad(ProfileLoadEvent event) {
        Profile profile = event.getProfile();

        ResultSet resultSet = NexusPlugin.getInstance().getDatabaseAdapter().getAsync("schematics", "profileId",
                String.valueOf(profile.getProfileId()));
        try {
            while(resultSet.next()) {
                UUID schematicId = UUID.fromString(resultSet.getString("schematicId"));
                SchematicType schematicType = SchematicType.valueOf(resultSet.getString("schematicType"));
                int level = resultSet.getInt("level");
                int rotation = resultSet.getInt("rotation");
                String stringLocation = resultSet.getString("location");
                long placed = resultSet.getLong("placed");
                int x = Integer.parseInt(stringLocation.split(", ")[0]),
                        y = Integer.parseInt(stringLocation.split(", ")[1]),
                        z = Integer.parseInt(stringLocation.split(", ")[2]);
                Schematic schematic = plugin.getSchematicManager().getSchematicsMap().get(schematicType).get(level);
                schematic.build(new Location(profile.getWorld().getWorld(), x, y, z), rotation, placed+schematic.getTimeToFinish(), schematicId);
                profile.getSchematicIds().add(schematicId);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                World world = profile.getWorld().getWorld();
                for(int i = 1; i < plugin.getSchematicManager().getSchematicsMap().get(SchematicType.NEXUS).size()-1; i++) {
                    Material material = Material.AIR;
                    if(i > profile.getNexusLevel()) {
                        material = Material.RED_STAINED_GLASS;
                    }
                    for(Block block : plugin.getGatewayManager().getGateways().get(i).getBlocksInRegion(world)) {
                        block.setType(material);
                    }
                }
            }
        }.runTaskLater(plugin, 5);
    }
}
