package de.uscoutz.nexus.wave.profile;

import de.uscoutz.nexus.profile.Profile;
import de.uscoutz.nexus.wave.NexusWavePlugin;
import lombok.Getter;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class Raid {

    private NexusWavePlugin plugin;

    @Getter
    private Profile profile;
    @Getter
    private long started;
    @Getter
    private BossBar bossBar;
    @Getter
    private List<Player> players;
    @Getter
    private RaidType raidType;

    public Raid(RaidType raidType, Profile profile, NexusWavePlugin plugin) {
        this.plugin = plugin;
        this.raidType = raidType;
        this.profile = profile;
        started = System.currentTimeMillis();
        players = new ArrayList<>();
    }

    public void schedule() {
        long raidCounter = plugin.getConfig().getLong("raid-counter");
        int raidCounterSeconds = (int) TimeUnit.MILLISECONDS.toSeconds(raidCounter);
        final int[] countdown = {(int) TimeUnit.MILLISECONDS.toSeconds(raidCounter)};
        bossBar = BossBar.bossBar(Component.text(""), 1, BossBar.Color.RED, BossBar.Overlay.NOTCHED_6);

        new BukkitRunnable() {
            @Override
            public void run() {
                if(profile.loaded()) {
                    if(countdown[0] == 0) {
                        for(Player all : profile.getWorld().getWorld().getPlayers()) {
                            all.hideBossBar(bossBar);
                            all.sendMessage("§aRaid started");
                            cancel();
                        }
                    } else {
                        String counter = String.format("%02d:%02d:%02d", TimeUnit.SECONDS.toHours(countdown[0]),
                                TimeUnit.SECONDS.toMinutes(countdown[0]) - TimeUnit.HOURS.toMinutes(TimeUnit.SECONDS.toHours(countdown[0])),
                                TimeUnit.SECONDS.toSeconds(countdown[0]) - TimeUnit.MINUTES.toSeconds(TimeUnit.SECONDS.toMinutes(countdown[0])));
                        bossBar.name(Component.text(plugin.getNexusPlugin().getLocaleManager().translate(
                                "de_DE", "raid_starts-in", counter)));
                        double progress = (double) countdown[0]/raidCounterSeconds;
                        bossBar.progress((float) progress);
                        for(Player all : profile.getWorld().getWorld().getPlayers()) {
                            if(!players.contains(all)) {
                                players.add(all);
                                all.showBossBar(bossBar);
                            }
                        }
                        countdown[0]--;
                    }
                } else {
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 0, 20);
    }
}
