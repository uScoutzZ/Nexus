package de.uscoutz.nexus.schematic.listener.profile;

import de.uscoutz.nexus.NexusPlugin;
import de.uscoutz.nexus.events.ProfileLoadEvent;
import de.uscoutz.nexus.profile.Profile;
import de.uscoutz.nexus.schematic.NexusSchematicPlugin;
import de.uscoutz.nexus.schematic.schematics.*;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
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

        plugin.getSchematicManager().getSchematicProfileMap().put(profile.getProfileId(), new SchematicProfile(profile, plugin));
        ResultSet resultSet = plugin.getNexusPlugin().getDatabaseAdapter().getAsync("schematics", "profileId",
                String.valueOf(profile.getProfileId()));
        try {
            while(resultSet.next()) {
                UUID schematicId = UUID.fromString(resultSet.getString("schematicId"));
                SchematicType schematicType = SchematicType.valueOf(resultSet.getString("schematicType"));
                int level = resultSet.getInt("level");
                int rotation = resultSet.getInt("rotation");
                double damage = resultSet.getDouble("damage");
                String stringLocation = resultSet.getString("location");
                long placed = resultSet.getLong("placed");
                int x = Integer.parseInt(stringLocation.split(", ")[0]),
                        y = Integer.parseInt(stringLocation.split(", ")[1]),
                        z = Integer.parseInt(stringLocation.split(", ")[2]);

                Schematic schematic = plugin.getSchematicManager().getSchematicsMap().get(schematicType).get(Condition.INTACT).get(level);
                Condition condition = BuiltSchematic.getCondition(damage/schematic.getDurability());
                schematic = plugin.getSchematicManager().getSchematicsMap().get(schematicType).get(condition).get(level);

                schematic.build(new Location(profile.getWorld().getWorld(), x, y, z), rotation, placed+schematic.getTimeToFinish(), schematicId, damage);
                profile.getSchematicIds().add(schematicId);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                World world = profile.getWorld().getWorld();
                for(int i = 0; i < plugin.getSchematicManager().getSchematicsMap().get(SchematicType.TOWER).get(Condition.INTACT).size()-1; i++) {
                    Material material = Material.RED_STAINED_GLASS;
                    if(i <= profile.getHighestTower()) {
                        material = Material.AIR;
                    }
                    Location location = plugin.getGatewayManager().getHolograms().get(i);
                    location.setWorld(profile.getWorld().getWorld());
                    ArmorStand armorStand = (ArmorStand) profile.getWorld().getWorld().spawnEntity(location, EntityType.ARMOR_STAND);
                    armorStand.setVisible(false);
                    armorStand.setCustomNameVisible(true);
                    armorStand.customName(Component.text("§7Required tower level: §3§l" + (i+1)));
                    for(Block block : plugin.getGatewayManager().getGateways().get(i).getBlocksInRegion(world)) {
                        block.setType(material);
                    }
                }
            }
        }.runTaskLater(plugin, 5);
    }
}
