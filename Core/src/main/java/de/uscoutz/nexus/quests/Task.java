package de.uscoutz.nexus.quests;

import de.uscoutz.nexus.NexusPlugin;
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
                .translate("de_DE", "george_collect-wood-assigned", player.getName(), quest.getTask().next().getGoal()));
        sendMessages(player, messages);
    }),
    COLLECT_LOG(false, 64, (player, quest) -> {
        player.getInventory().addItem(NexusPlugin.getInstance().getToolManager().getToolMap().get("wooden_axe").getItemStack());
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

    @Getter
    private boolean chronological;
    @Getter
    private long goal;
    @Getter
    private BiConsumer<Player, Quest> actionWhenFinished;
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
        }.runTaskTimer(NexusPlugin.getInstance(), 0, 50);
    }
}
