package de.uscoutz.nexus.schematic.listener.player;

import de.uscoutz.nexus.NexusPlugin;
import de.uscoutz.nexus.profile.Profile;
import de.uscoutz.nexus.regions.Region;
import de.uscoutz.nexus.schematic.NexusSchematicPlugin;
import de.uscoutz.nexus.schematic.player.SchematicPlayer;
import de.uscoutz.nexus.schematic.schematicitems.SchematicItem;
import de.uscoutz.nexus.schematic.schematics.*;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.block.data.Openable;
import org.bukkit.block.data.type.Gate;
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
                    for(Container container1 : profile.getStorageBlocks().values()) {
                        if(container1.getLocation().equals(container.getLocation())) {
                            return;
                        }
                    }
                    event.setCancelled(true);
                }
            } else {
                if(storage.getBlockData() instanceof Openable) {
                    Location clicked = event.getClickedBlock().getLocation();
                    Region region = plugin.getNexusPlugin().getRegionManager().getRegion(clicked);
                    Profile profile = plugin.getNexusPlugin().getWorldManager().getWorldProfileMap().get(clicked.getWorld());
                    SchematicProfile schematicProfile = plugin.getSchematicManager().getSchematicProfileMap().get(profile.getProfileId());
                    BuiltSchematic builtSchematic = schematicProfile.getSchematicsByRegion().get(region);
                    if(builtSchematic == null || !(storage.getBlockData() instanceof Gate)) {
                        event.setCancelled(true);
                    }
                } else {
                    if(event.getClickedBlock().getType() == Material.COMPOSTER) {
                        event.setCancelled(true);
                    } else {
                        if(event.getItem() != null && event.getItem().getType() == Material.BONE_MEAL) {
                            event.setCancelled(true);
                        }
                    }
                }
            }
        } else if(event.getAction() == Action.LEFT_CLICK_BLOCK) {
            Location clicked = event.getClickedBlock().getLocation();
            Region region = plugin.getNexusPlugin().getRegionManager().getRegion(clicked);
            Profile profile = plugin.getNexusPlugin().getWorldManager().getWorldProfileMap().get(clicked.getWorld());
            SchematicProfile schematicProfile = plugin.getSchematicManager().getSchematicProfileMap().get(profile.getProfileId());
            BuiltSchematic builtSchematic = schematicProfile.getSchematicsByRegion().get(region);
            if(builtSchematic == null || !builtSchematic.isBuilt()) {
                return;
            }
            if(builtSchematic.getSchematic().getSchematicType() == SchematicType.NEXUS) {
                player.sendMessage(plugin.getNexusPlugin().getLocaleManager().translate("de_DE", "schematic_nexus-not-breakable"));
            } else {
                if(profile.getWorld().getWorld().getTime() == 13000) {
                    player.sendMessage(plugin.getNexusPlugin().getLocaleManager().translate("de_DE", "schematic_cant-destroy-when-raid"));
                } else {
                    if(BuiltSchematic.getCondition(builtSchematic.getPercentDamage()) != Condition.INTACT) {
                        player.sendMessage(plugin.getNexusPlugin().getLocaleManager().translate("de_DE", "schematic_damaged-not-breakable"));
                    } else {
                        if(plugin.getSchematicItemManager().getSchematicItemBySchematic().containsKey(builtSchematic.getSchematic())) {
                            if(schematicPlayer.getBreaking() == null || !schematicPlayer.getBreaking().equals(builtSchematic)) {
                                player.sendMessage(plugin.getNexusPlugin().getLocaleManager().translate("de_DE", "schematic_break"));
                                ComponentBuilder message = new ComponentBuilder(NexusPlugin.getInstance().getLocaleManager().translate(
                                        "de_DE", "questions_click"));
                                message.append(NexusPlugin.getInstance().getLocaleManager().translate("de_DE", "schematic_break-confirmation"));
                                message.event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/removeschematic " + builtSchematic.getSchematicId()));
                                player.spigot().sendMessage(message.create());

                                schematicPlayer.setBreaking(builtSchematic);
                                new BukkitRunnable() {
                                    @Override
                                    public void run() {
                                        if(schematicPlayer.getBreaking().equals(builtSchematic)) {
                                            schematicPlayer.setBreaking(null);
                                        }
                                    }
                                }.runTaskLater(plugin, 100);
                            }
                        } else {
                            player.sendMessage("§cThe schematic item is not set up");
                        }
                    }
                }
            }
        }
    }
}
