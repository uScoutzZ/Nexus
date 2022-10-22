package de.uscoutz.nexus.schematic.listener.player;

import de.uscoutz.nexus.NexusPlugin;
import de.uscoutz.nexus.profile.Profile;
import de.uscoutz.nexus.quests.Quest;
import de.uscoutz.nexus.quests.Task;
import de.uscoutz.nexus.regions.Region;
import de.uscoutz.nexus.schematic.NexusSchematicPlugin;
import de.uscoutz.nexus.schematic.autominer.AutoMiner;
import de.uscoutz.nexus.schematic.schematics.BuiltSchematic;
import de.uscoutz.nexus.schematic.schematics.SchematicType;
import de.uscoutz.nexus.utilities.InventoryManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Arrays;

public class PlayerInteractAtEntityListener implements Listener {

    private NexusSchematicPlugin plugin;

    public PlayerInteractAtEntityListener(NexusSchematicPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerInteractAtEntity(PlayerInteractAtEntityEvent event) {
        Player player = event.getPlayer();
        Profile profile = plugin.getNexusPlugin().getWorldManager().getWorldProfileMap().get(player.getWorld());

        if(event.getRightClicked() instanceof Villager) {
            event.setCancelled(true);
            String name = event.getRightClicked().getCustomName();
            Region region = plugin.getNexusPlugin().getRegionManager().getRegion(event.getRightClicked().getLocation());
            BuiltSchematic builtSchematic = plugin.getSchematicManager().getSchematicProfileMap().get(profile.getProfileId()).getSchematicsByRegion().get(region);
            if(builtSchematic.getSchematic().getSchematicType() == SchematicType.AUTOMINER) {
                AutoMiner autoMiner = plugin.getAutoMinerManager().getAutoMinersPerProfile().get(profile.getProfileId()).get(builtSchematic.getSchematicId());
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        autoMiner.getInventory().open(player);
                        //player.openInventory(autoMiner.getInventory());
                    }
                }.runTaskLater(plugin, 1);
            } else if(builtSchematic.getSchematic().getSchematicType() == SchematicType.WORKSHOP) {
                if(profile.getUnfinishedQuests().containsKey(Task.TALK_TO_GEORGE)) {
                    profile.getQuests().get(Task.TALK_TO_GEORGE).finish(player);
                } else {
                    if(profile.getUnfinishedQuests().containsKey(Task.COLLECT_LOG)) {
                        Quest quest = profile.getUnfinishedQuests().get(Task.COLLECT_LOG);

                        int progress = InventoryManager.removeNeededItems(player, Arrays.asList(new ItemStack(Material.DARK_OAK_LOG,
                                (int) (quest.getTask().getGoal()-quest.getProgress()))));
                        if(progress != 0) {
                            long finalProgress = profile.getQuests().get(Task.COLLECT_LOG).addProgress(player, progress);
                            if(finalProgress < quest.getTask().getGoal()) {
                                player.sendMessage(plugin.getNexusPlugin().getLocaleManager().translate("de_DE", "george_collected-wood", name, progress));
                            }
                        } else {
                            player.sendMessage(plugin.getNexusPlugin().getLocaleManager().translate("de_DE", "george_no-wood", name));
                        }
                    } else {
                        if(profile.getUnfinishedQuests().containsKey(Task.UPGRADE_NEXUS)) {
                            player.sendMessage(plugin.getNexusPlugin().getLocaleManager().translate("de_DE", "george_upgrade-nexus", name));
                        } else {
                            if(profile.getQuests().containsKey(Task.BUILD_WALLS)) {
                                new BukkitRunnable() {
                                    @Override
                                    public void run() {
                                        plugin.getNexusPlugin().getInventoryManager().openWorkshopSchematics(player);
                                    }
                                }.runTaskLater(plugin, 1);
                            }
                        }
                    }
                }
            }
        }
    }
}
