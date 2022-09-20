package de.uscoutz.nexus.listeners.player;

import de.uscoutz.nexus.NexusPlugin;
import de.uscoutz.nexus.profile.Profile;
import de.uscoutz.nexus.quests.Quest;
import de.uscoutz.nexus.quests.Task;
import de.uscoutz.nexus.utilities.InventoryManager;
import net.minecraft.network.chat.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlayerInteractAtEntityListener implements Listener {

    private NexusPlugin plugin;

    public PlayerInteractAtEntityListener(NexusPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerInteractAtEntity(PlayerInteractAtEntityEvent event) {
        Player player = event.getPlayer();
        Profile profile = plugin.getWorldManager().getWorldProfileMap().get(player.getWorld());

        if(event.getRightClicked() instanceof Villager) {
            event.setCancelled(true);
            String name = event.getRightClicked().getCustomName();
            if(profile.getUnfinishedQuests().containsKey(Task.TALK_TO_GEORGE)) {
                profile.getQuests().get(Task.TALK_TO_GEORGE).finish(player);
            } else {
                if(profile.getUnfinishedQuests().containsKey(Task.COLLECT_LOG)) {
                    Quest quest = profile.getUnfinishedQuests().get(Task.COLLECT_LOG);

                    int progress = InventoryManager.removeNeededItems(player, Material.DARK_OAK_LOG, quest);
                    if(progress != 0) {
                        long finalProgress = profile.getQuests().get(Task.COLLECT_LOG).addProgress(player, progress);
                        if(finalProgress < quest.getTask().getGoal()) {
                            player.sendMessage(plugin.getLocaleManager().translate("de_DE", "george_collected-wood", name, progress));
                        }
                    } else {
                        player.sendMessage(plugin.getLocaleManager().translate("de_DE", "george_no-wood", name));
                    }
                }
            }
        }
    }
}
