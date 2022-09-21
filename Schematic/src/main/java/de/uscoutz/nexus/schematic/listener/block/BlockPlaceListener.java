package de.uscoutz.nexus.schematic.listener.block;

import de.uscoutz.nexus.NexusPlugin;
import de.uscoutz.nexus.profile.Profile;
import de.uscoutz.nexus.quests.Task;
import de.uscoutz.nexus.schematic.NexusSchematicPlugin;
import de.uscoutz.nexus.schematic.player.SchematicPlayer;
import de.uscoutz.nexus.schematic.schematicitems.SchematicItem;
import de.uscoutz.nexus.schematic.schematics.Schematic;
import de.uscoutz.nexus.schematic.schematics.SchematicProfile;
import de.uscoutz.nexus.schematic.schematics.SchematicType;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.UUID;

public class BlockPlaceListener implements Listener {

    private NexusSchematicPlugin plugin;

    public BlockPlaceListener(NexusSchematicPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBlockBreak(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        SchematicPlayer schematicPlayer = plugin.getPlayerManager().getPlayerMap().get(player.getUniqueId());
        SchematicProfile schematicProfile = plugin.getSchematicManager().getSchematicProfileMap().get(
                plugin.getNexusPlugin().getWorldManager().getWorldProfileMap().get(player.getWorld()).getProfileId());

        event.getPlayer().getInventory().getItemInMainHand();
        ItemStack itemStack = player.getInventory().getItem(player.getInventory().getHeldItemSlot());
        if(itemStack != null && itemStack.getItemMeta() != null) {
            ItemMeta itemMeta = itemStack.getItemMeta();
            if(plugin.getSchematicItemManager().isSchematicItem(itemMeta)) {
                PersistentDataContainer dataContainer = itemMeta.getPersistentDataContainer();
                String key = dataContainer.get(new NamespacedKey(plugin.getNexusPlugin().getName().toLowerCase(), "key"), PersistentDataType.STRING);
                SchematicItem schematicItem = plugin.getSchematicItemManager().getSchematicItemMap().get(key);
                Schematic schematic = schematicItem.getSchematic();

                String id = dataContainer.get(new NamespacedKey(plugin.getNexusPlugin().getName().toLowerCase(),
                        "schematicid"), PersistentDataType.STRING);
                if(id != null) {
                    UUID schematicId = UUID.fromString(id);
                    if(schematicProfile.getBuiltSchematics().containsKey(schematicId)) {
                        event.setCancelled(true);
                        player.sendMessage("§cThis schematic is already placed");
                    } else {
                        Location location = event.getBlock().getLocation().subtract(0, 1, 0);
                        int rotation = schematicPlayer.getRotationFromFacing(player.getFacing());
                        int maxConcurrentBuildings = plugin.getNexusPlugin().getConfig().getInt("concurrently-building");
                        Profile profile = plugin.getNexusPlugin().getWorldManager().getWorldProfileMap().get(player.getWorld());
                        if(profile.getConcurrentlyBuilding() >= maxConcurrentBuildings) {
                            event.setCancelled(true);
                            player.sendMessage(plugin.getNexusPlugin().getLocaleManager().translate("de_DE", "schematic_too-much-concurrent-buildings", maxConcurrentBuildings));
                        } else {
                            if(!schematicItem.getSchematic().preview(location, rotation, true)) {
                                event.setCancelled(true);
                                if(schematic.getTimeToFinish() != 0) {
                                    long finished = System.currentTimeMillis()+ schematic.getTimeToFinish();
                                    schematic.build(location, rotation, finished, schematicId, 0);
                                } else {
                                    schematic.build(location, rotation, schematicId, 0, false);
                                }
                                String nexusLocation = location.getBlockX() + ", " + location.getBlockY() + ", " + location.getBlockZ();
                                plugin.getNexusPlugin().getDatabaseAdapter().set("schematics",
                                        plugin.getNexusPlugin().getPlayerManager().getPlayersMap().get(player.getUniqueId()).getCurrentProfile().getProfileId(),
                                        schematicId, schematic.getSchematicType(), schematic.getLevel(), rotation, nexusLocation, System.currentTimeMillis(), 0);
                                if(schematic.getSchematicType() == SchematicType.WALL) {
                                    if(profile.getUnfinishedQuests().containsKey(Task.BUILD_WALLS)) {
                                        profile.getUnfinishedQuests().get(Task.BUILD_WALLS).addProgress(player, 1);
                                    }
                                } else if(schematic.getSchematicType() == SchematicType.HOME) {
                                    if(profile.getUnfinishedQuests().containsKey(Task.BUILD_HOME)) {
                                        profile.getUnfinishedQuests().get(Task.BUILD_HOME).finish(player);
                                    }
                                } else if(schematic.getSchematicType() == SchematicType.TOWER) {
                                    if(profile.getUnfinishedQuests().containsKey(Task.BUILD_TOWER)) {
                                        profile.getUnfinishedQuests().get(Task.BUILD_TOWER).finish(player);
                                    }
                                }
                                player.getInventory().setItem(player.getInventory().getHeldItemSlot(), new ItemStack(Material.AIR));
                            } else {
                                event.setCancelled(true);
                                player.sendMessage(plugin.getNexusPlugin().getLocaleManager().translate("de_DE", "schematic_not-enough-space"));
                            }
                        }
                    }
                } else {
                    event.setCancelled(true);
                    player.sendMessage("§cThe schematic item is not set up");
                }
            } else {
                if(player.getGameMode() != GameMode.CREATIVE) {
                    event.setCancelled(true);
                } else {
                    event.setCancelled(false);
                }
            }
        } else {
            event.setCancelled(true);
        }
    }
}
