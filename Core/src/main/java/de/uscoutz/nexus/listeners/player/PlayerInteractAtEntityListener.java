package de.uscoutz.nexus.listeners.player;

import de.uscoutz.nexus.NexusPlugin;
import de.uscoutz.nexus.profile.Profile;
import de.uscoutz.nexus.quests.Task;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;

public class PlayerInteractAtEntityListener implements Listener {

    private NexusPlugin plugin;

    public PlayerInteractAtEntityListener(NexusPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerInteractAtEntity(PlayerInteractAtEntityEvent event) {
        Player player = event.getPlayer();
        Profile profile = plugin.getWorldManager().getWorldProfileMap().get(player.getWorld());
        player.sendMessage(profile.getQuests().size() + "");

        if(event.getRightClicked() instanceof Villager) {
            if(profile.getUnfinishedQuests().containsKey(Task.TALK_TO_GEORGE)) {
                profile.getQuests().get(Task.TALK_TO_GEORGE).finish();
            } else {
                if(profile.getUnfinishedQuests().containsKey(Task.COLLECT_LOG)) {
                    profile.getQuests().get(Task.COLLECT_LOG).addProgress(1);
                }
            }
        }
    }
}
