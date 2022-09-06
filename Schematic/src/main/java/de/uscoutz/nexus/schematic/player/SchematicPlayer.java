package de.uscoutz.nexus.schematic.player;

import de.uscoutz.nexus.schematic.NexusSchematicPlugin;
import de.uscoutz.nexus.schematic.schematicitems.SchematicItem;
import de.uscoutz.nexus.schematic.schematics.BuiltSchematic;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class SchematicPlayer {

    private NexusSchematicPlugin plugin;
    @Getter
    private UUID playerUUID;
    @Getter @Setter
    private Map<Integer, Location> locations;
    @Getter
    private Player player;
    @Getter @Setter
    private BuiltSchematic breaking;

    private BukkitTask task;

    public SchematicPlayer(UUID playerUUID, NexusSchematicPlugin plugin) {
        this.plugin = plugin;
        this.playerUUID = playerUUID;
        player = Bukkit.getPlayer(playerUUID);
        locations = new HashMap<>();
        plugin.getPlayerManager().getPlayerMap().put(playerUUID, this);
    }

    public void startPreview(ItemStack itemStack, SchematicItem schematicItem) {
        task = new BukkitRunnable() {
            @Override
            public void run() {
                if(!player.isOnline() || !Objects.equals(player.getInventory().getItem(player.getInventory().getHeldItemSlot()), itemStack)) {
                    cancel();
                } else {
                    Block block = player.getTargetBlock(5);
                    if(block.getType() != Material.AIR) {
                        if(!block.isSolid()) {
                            block = block.getLocation().subtract(0, 1, 0).getBlock();
                        }

                        int rotation = getRotationFromFacing(player.getFacing());
                        schematicItem.getSchematic().preview(block.getLocation(), rotation, false);
                    }
                }
            }
        }.runTaskTimer(plugin, 0, 10);
    }

    public int getRotationFromFacing(BlockFace blockFace) {
        int rotation = 0;
        if(blockFace == BlockFace.EAST) {
            rotation = 0;
        } else if(blockFace == BlockFace.SOUTH) {
            rotation = 270;
        } else if(blockFace == BlockFace.WEST) {
            rotation = 180;
        } else if(blockFace == BlockFace.NORTH) {
            rotation = 90;
        }

        return rotation;
    }
}
