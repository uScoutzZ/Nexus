package de.uscoutz.nexus.quests;

import de.uscoutz.nexus.NexusPlugin;
import de.uscoutz.nexus.player.NexusPlayer;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Consumer;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

public enum Task {

    TALK_TO_GEORGE(true, 0, (player, quest) -> {
        List<String> messages = NexusPlugin.getInstance().getLocaleManager().split(NexusPlugin.getInstance().getLocaleManager()
                .translate(NexusPlugin.getInstance().getPlayerManager().getPlayersMap().get(player.getUniqueId()).getLanguage(), "george_collect-wood-assigned", NexusPlugin.getInstance().getConfig().get("villager-name"),
                        player.getName(), quest.getTask().next().getGoal()));
        sendMessages(player, messages);
    }),
    COLLECT_LOG(true, 5, (player, quest) -> {
        player.getInventory().addItem(NexusPlugin.getInstance().getToolManager().getToolMap().get("wooden_axe").getItemStack());
        List<String> messages = NexusPlugin.getInstance().getLocaleManager().split(NexusPlugin.getInstance().getLocaleManager()
                .translate(NexusPlugin.getInstance().getPlayerManager().getPlayersMap().get(player.getUniqueId()).getLanguage(), "george_upgrade-nexus-assigned", NexusPlugin.getInstance().getConfig().get("villager-name")));
        sendMessages(player, messages);
    }),
    UPGRADE_NEXUS(true, 0, (player, quest) -> {
        List<String> messages = NexusPlugin.getInstance().getLocaleManager().split(NexusPlugin.getInstance().getLocaleManager()
                .translate(NexusPlugin.getInstance().getPlayerManager().getPlayersMap().get(player.getUniqueId()).getLanguage(), "george_build-walls-assigned", NexusPlugin.getInstance().getConfig().get("villager-name"), quest.getTask().next().getGoal()));
        sendMessages(player, messages);
    }),
    BUILD_WALLS(true, 3, (player, quest) -> {
        List<String> messages = NexusPlugin.getInstance().getLocaleManager().split(NexusPlugin.getInstance().getLocaleManager()
                .translate(NexusPlugin.getInstance().getPlayerManager().getPlayersMap().get(player.getUniqueId()).getLanguage(), "george_build-home-assigned", NexusPlugin.getInstance().getConfig().get("villager-name")));
        sendMessages(player, messages);
    }),
    BUILD_HOME(true, 0, (player, quest) -> {
        List<String> messages = NexusPlugin.getInstance().getLocaleManager().split(NexusPlugin.getInstance().getLocaleManager()
                .translate(NexusPlugin.getInstance().getPlayerManager().getPlayersMap().get(player.getUniqueId()).getLanguage(), "george_repair-workshop-assigned", NexusPlugin.getInstance().getConfig().get("villager-name")));
        sendMessages(player, messages);
    }),
    REPAIR_WORKSHOP(true, 0, (player, quest) -> {
        List<String> messages = NexusPlugin.getInstance().getLocaleManager().split(NexusPlugin.getInstance().getLocaleManager()
                .translate(NexusPlugin.getInstance().getPlayerManager().getPlayersMap().get(player.getUniqueId()).getLanguage(), "george_build-tower-assigned", NexusPlugin.getInstance().getConfig().get("villager-name")));
        sendMessages(player, messages);
    }),
    BUILD_TOWER(false, 0, (player, quest) -> {
        List<String> messages = NexusPlugin.getInstance().getLocaleManager().split(NexusPlugin.getInstance().getLocaleManager()
                .translate(NexusPlugin.getInstance().getPlayerManager().getPlayersMap().get(player.getUniqueId()).getLanguage(), "george_build-tower-finished", NexusPlugin.getInstance().getConfig().get("villager-name")));
        sendMessages(player, messages);
    }),
    BUILD_PORTAL(false, 0, (player, quest) -> {
        List<String> messages = NexusPlugin.getInstance().getLocaleManager().split(NexusPlugin.getInstance().getLocaleManager()
                .translate(NexusPlugin.getInstance().getPlayerManager().getPlayersMap().get(player.getUniqueId()).getLanguage(), "george_build-portal-finished", NexusPlugin.getInstance().getConfig().get("villager-name")));
        sendMessages(player, messages);
    }, (player, quest) -> {
        List<String> messages = NexusPlugin.getInstance().getLocaleManager().split(NexusPlugin.getInstance().getLocaleManager()
                .translate(NexusPlugin.getInstance().getPlayerManager().getPlayersMap().get(player.getUniqueId()).getLanguage(), "george_build-portal-assigned", NexusPlugin.getInstance().getConfig().get("villager-name")));
        sendMessages(player, messages);
        for(NexusPlayer nexusPlayer : quest.getProfile().getActivePlayers()) {
            quest.display(nexusPlayer.getPlayer());
        }
    });

    Task(boolean chronological) {
        this.chronological = chronological;
    }

    Task(boolean chronological, long goal) {
        this.chronological = chronological;
        this.goal = goal;
    }

    Task(boolean chronological, long goal, BiConsumer<Player, Quest> actionWhenFinished) {
        this.chronological = chronological;
        this.goal = goal;
        this.actionWhenFinished = actionWhenFinished;
    }

    Task(boolean chronological, long goal, BiConsumer<Player, Quest> actionWhenFinished, BiConsumer<Player, Quest> actionWhenAssigned) {
        this.chronological = chronological;
        this.goal = goal;
        this.actionWhenFinished = actionWhenFinished;
        this.actionWhenAssiged = actionWhenAssigned;
    }

    @Getter
    private boolean chronological;
    @Getter
    private long goal;
    @Getter
    private BiConsumer<Player, Quest> actionWhenFinished, actionWhenAssiged;
    private static Task[] vals = values();

    public Task next() {
        return vals[(this.ordinal()+1) % vals.length];
    }

    private static void sendMessages(Player player, List<String> messages) {
        new BukkitRunnable() {
            @Override
            public void run() {
                if(messages.size() != 0) {
                    player.sendMessage(messages.remove(0));
                } else {
                    cancel();
                }
            }
        }.runTaskTimer(NexusPlugin.getInstance(), 10, 80);
    }
}
