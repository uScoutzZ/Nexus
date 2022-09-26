package de.uscoutz.nexus.listeners.player;

import de.uscoutz.nexus.NexusPlugin;
import de.uscoutz.nexus.player.NexusPlayer;
import de.uscoutz.nexus.quests.Quest;
import de.uscoutz.nexus.quests.Task;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.time.Duration;

public class PlayerJoinListener implements Listener {

    private NexusPlugin plugin;

    public PlayerJoinListener(NexusPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        NexusPlayer nexusPlayer = plugin.getPlayerManager().getPlayersMap().get(player.getUniqueId());

        if(player.getGameMode() == GameMode.CREATIVE) {
            player.setGameMode(GameMode.SURVIVAL);
            player.setAllowFlight(true);
        }

        if(nexusPlayer.getCurrentProfile().getUnfinishedQuests().containsKey(Task.TALK_TO_GEORGE)) {
            Quest quest = nexusPlayer.getCurrentProfile().getQuests().get(Task.TALK_TO_GEORGE);
            player.showTitle(Title.title(Component.text(plugin.getLocaleManager().translate("de_DE", quest.getTitleKey())),
                    Component.text(plugin.getLocaleManager().translate("de_DE", quest.getDescriptionKey())), Title.Times.times(Duration.ofSeconds(1), Duration.ofSeconds(8), Duration.ofSeconds(1))));
        }
    }
}
