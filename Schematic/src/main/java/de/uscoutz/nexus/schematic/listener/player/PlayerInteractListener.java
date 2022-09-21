package de.uscoutz.nexus.schematic.listener.player;

import de.uscoutz.nexus.NexusPlugin;
import de.uscoutz.nexus.profile.Profile;
import de.uscoutz.nexus.regions.Region;
import de.uscoutz.nexus.schematic.NexusSchematicPlugin;
import de.uscoutz.nexus.schematic.player.SchematicPlayer;
import de.uscoutz.nexus.schematic.schematicitems.SchematicItem;
import de.uscoutz.nexus.schematic.schematics.*;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class PlayerInteractListener implements Listener {

    private NexusSchematicPlugin plugin;

    public PlayerInteractListener(NexusSchematicPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        SchematicPlayer schematicPlayer = plugin.getPlayerManager().getPlayerMap().get(player.getUniqueId());

        if(player.getInventory().getItemInMainHand().getType() == Material.GOLDEN_AXE && player.getGameMode() == GameMode.CREATIVE) {
            event.setCancelled(true);
            int i = 0;
            if(event.getAction() == Action.LEFT_CLICK_BLOCK) {
                if(schematicPlayer.getLocations().containsKey(0)
                        && schematicPlayer.getLocations().get(0).getBlock().getLocation().equals(event.getClickedBlock().getLocation())) {
                    return;
                }
                schematicPlayer.getLocations().put(0, event.getClickedBlock().getLocation());
                i = 1;
            } else if(event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                if(schematicPlayer.getLocations().containsKey(1)
                        && schematicPlayer.getLocations().get(1).getBlock().getLocation().equals(event.getClickedBlock().getLocation())) {
                    return;
                }
                schematicPlayer.getLocations().put(1, event.getClickedBlock().getLocation());
                i = 2;
            }
            if(i != 0) {
                player.sendMessage("§6Position " + i + " set to " + event.getClickedBlock().getX() + ", " +
                        event.getClickedBlock().getY() + ", " + event.getClickedBlock().getZ());
            }
        }

        if(event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            Block storage = event.getClickedBlock();
            if(storage != null && storage.getState() instanceof Container) {
                Container container = (Container) storage.getState();
                Profile profile = plugin.getNexusPlugin().getWorldManager().getWorldProfileMap().get(player.getWorld());
                if(!profile.getStorageBlocks().containsValue(container)) {
                    event.setCancelled(true);
                }
            }
        } else if(event.getAction() == Action.LEFT_CLICK_BLOCK) {
            Location clicked = event.getClickedBlock().getLocation();
            Region region = plugin.getNexusPlugin().getRegionManager().getRegion(clicked);
            Profile profile = plugin.getNexusPlugin().getWorldManager().getWorldProfileMap().get(clicked.getWorld());
            SchematicProfile schematicProfile = plugin.getSchematicManager().getSchematicProfileMap().get(profile.getProfileId());
            BuiltSchematic builtSchematic = schematicProfile.getSchematicsByRegion().get(region);
            if(builtSchematic == null) {
                return;
            }
            if(builtSchematic.getSchematic().getSchematicType() == SchematicType.NEXUS) {
                player.sendMessage(plugin.getNexusPlugin().getLocaleManager().translate("de_DE", "schematic_nexus-not-breakable"));
            } else {
                if(BuiltSchematic.getCondition(builtSchematic.getPercentDamage()) != Condition.INTACT) {
                    player.sendMessage(plugin.getNexusPlugin().getLocaleManager().translate("de_DE", "schematic_damaged-not-breakable"));
                } else {
                    if(plugin.getSchematicItemManager().getSchematicItemBySchematic().containsKey(builtSchematic.getSchematic())) {
                        if(schematicPlayer.getBreaking() == null || !schematicPlayer.getBreaking().equals(builtSchematic)) {
                            player.sendMessage(plugin.getNexusPlugin().getLocaleManager().translate("de_DE", "schematic_break"));
                            schematicPlayer.setBreaking(builtSchematic);
                            new BukkitRunnable() {
                                @Override
                                public void run() {
                                    schematicPlayer.setBreaking(null);
                                }
                            }.runTaskLater(plugin, 40);
                        } else {
                            SchematicItem schematicItem = plugin.getSchematicItemManager().getSchematicItemBySchematic().get(builtSchematic.getSchematic());
                            Schematic.destroy(profile, builtSchematic.getSchematicId(), plugin, DestroyAnimation.PLAYER, builtSchematic.getSchematic().getSchematicType());
                            player.getInventory().addItem(schematicItem.getItemStack(builtSchematic.getSchematicId()));
                        }
                    } else {
                        player.sendMessage("§cThe schematic item is not set up");
                    }
                }
            }
        }
    }
}
