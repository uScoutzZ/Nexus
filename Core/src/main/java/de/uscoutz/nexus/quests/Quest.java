package de.uscoutz.nexus.quests;

import de.uscoutz.nexus.NexusPlugin;
import de.uscoutz.nexus.database.DatabaseUpdate;
import de.uscoutz.nexus.player.NexusPlayer;
import de.uscoutz.nexus.profile.Profile;
import lombok.Getter;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Quest {

    @Getter
    private Task task;
    @Getter
    private long begun, finished, progress;
    @Getter
    private String titleKey, descriptionKey;
    @Getter
    private Map<String, BossBar> bossBars;
    private UUID profileId;
    private NexusPlugin plugin;
    @Getter
    private Profile profile;

    public Quest(UUID profileId, Task task, long progress, long begun, long finished, NexusPlugin plugin) {
        this.descriptionKey = task.toString().toLowerCase() + "_description";
        this.titleKey = task.toString().toLowerCase() + "_title";
        this.profileId = profileId;
        this.task = task;
        this.progress = progress;
        this.begun = begun;
        this.finished = finished;
        this.plugin = plugin;
        profile = plugin.getProfileManager().getProfilesMap().get(profileId);
        initLanguages();
    }

    public Quest(UUID profileId, Task task, NexusPlugin plugin) {
        this.descriptionKey = task.toString().toLowerCase() + "_description";
        this.titleKey = task.toString().toLowerCase() + "_title";
        this.profileId = profileId;
        this.task = task;
        this.plugin = plugin;
        progress = 0;
        begun = System.currentTimeMillis();
        profile = plugin.getProfileManager().getProfilesMap().get(profileId);
        initLanguages();
    }

    public void display(Player player) {
        player.showBossBar(bossBars.get(plugin.getPlayerManager().getPlayersMap().get(player.getUniqueId()).getLanguage()));
        if(task.getGoal() != 0) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    addProgress(player, 0); //That's for when a quests goal was changed and the player might have already reached it
                }
            }.runTaskLater(plugin, 20);
        }
    }

    public Quest assign() {
        plugin.getDatabaseAdapter().setAsync("quests", profileId, task.toString(), 0, System.currentTimeMillis(), 0);
        if(task.getActionWhenAssiged() != null && profile.getActivePlayers().size() != 0) {
            task.getActionWhenAssiged().accept(profile.getActivePlayers().get(0).getPlayer(), this);
        }

        if(profile != null && profile.getMainQuest() == this) {
            for(NexusPlayer nexusPlayer : profile.getActivePlayers()) {
                display(nexusPlayer.getPlayer());
            }
        }
        return this;
    }

    public void finish(Player player) {
        finished = System.currentTimeMillis();
        plugin.getDatabaseAdapter().updateTwoAsync("quests", "profileId", profileId,
                "task", task.toString(),
                new DatabaseUpdate("finished", finished));
        if(task.getActionWhenFinished() != null) {
            task.getActionWhenFinished().accept(player, this);
        }
        if(task.isChronological()) {
            if(task.next() != null) {
                profile.getQuests().put(task.next(), new Quest(profileId, task.next(), plugin).assign());
            } else {
                player.sendMessage("§cEven though chronological=true, there isn't another quest to do");
            }
        }

        Quest mainQuest = profile.getMainQuest();
        for(Player all : profile.getWorld().getWorld().getPlayers()) {
            all.hideBossBar(bossBars.get(plugin.getPlayerManager().getPlayersMap().get(player.getUniqueId()).getLanguage()));
            if(mainQuest != null) {
                mainQuest.display(all);
            }
        }
    }

    public long addProgress(Player player, long value) {
        progress += value;
        plugin.getDatabaseAdapter().updateTwoAsync("quests", "profileId", profileId,
                "task", task.toString(),
                new DatabaseUpdate("progress", progress));
        String progress = "§7(" + this.progress + "/" + task.getGoal() + ")";
        float barProgress = (float)this.progress/task.getGoal();
        for(String key : bossBars.keySet()) {
            BossBar bossBar = bossBars.get(key);
            bossBar.name(Component.text(plugin.getLocaleManager()
                    .translate(key, titleKey) + "§7: " + plugin.getLocaleManager()
                    .translate(key, descriptionKey) + " " + progress));
            bossBar.progress(barProgress);
        }
        if(finished == 0 && this.progress >= task.getGoal()) {
            finish(player);
        }
        return this.progress;
    }

    public boolean isFinished() {
        return finished != 0;
    }

    private void initLanguages() {
        bossBars = new HashMap<>();
        String progress = "";
        float barProgress = 1;
        if(task.getGoal() >= 2) {
            progress = "§7(" + this.progress + "/" + task.getGoal() + ")";
            barProgress = (float)this.progress/task.getGoal();
        }

        for(String key : plugin.getLocaleManager().getLanguageKeys()) {
            try {
                bossBars.put(key, BossBar.bossBar(Component.text(plugin.getLocaleManager()
                        .translate(key, titleKey) + "§7: " + plugin.getLocaleManager()
                        .translate(key, descriptionKey) + " " + progress), barProgress, BossBar.Color.PURPLE, BossBar.Overlay.NOTCHED_6));
            } catch (IllegalArgumentException exception) {
                bossBars.put(key, BossBar.bossBar(Component.text(plugin.getLocaleManager()
                        .translate(key, titleKey) + "§7: " + plugin.getLocaleManager()
                        .translate(key, descriptionKey) + " " + progress), 1, BossBar.Color.PURPLE, BossBar.Overlay.NOTCHED_6));
            }
        }
    }
}
