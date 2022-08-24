package de.uscoutz.nexus.wave.profile;

import de.uscoutz.nexus.profile.Profile;
import de.uscoutz.nexus.wave.NexusWavePlugin;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class RaidProfile {

    private NexusWavePlugin plugin;

    @Getter
    private Profile profile;
    @Getter
    private UUID profileId;
    @Getter
    private long lastRaid;

    public RaidProfile(Profile profile, NexusWavePlugin plugin) {
        this.plugin = plugin;
        this.profile = profile;
        this.profileId = profile.getProfileId();
        plugin.getRaidManager().getRaidProfileMap().put(profileId, this);
    }

    public void load() {
        ResultSet resultSet = plugin.getNexusPlugin().getDatabaseAdapter().getAsync("raids", "profileId",
                String.valueOf(profile.getProfileId()), "ended DESC");
        try {
            if(resultSet.next()) {
                UUID raidId = UUID.fromString(resultSet.getString("raidId"));
                lastRaid = resultSet.getLong("ended");
            } else {
                lastRaid = 0;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void scheduleRaid() {
        long rangeMin = plugin.getConfig().getLong("raid-range-min");
        long rangeMax = plugin.getConfig().getLong("raid-range-max");
        long startIn = new Random().nextLong(rangeMax-rangeMin)+rangeMin;

        Bukkit.broadcastMessage("Raid starts in " + TimeUnit.MILLISECONDS.toSeconds(startIn));

        new BukkitRunnable() {
            @Override
            public void run() {
                new Raid(profile, plugin).schedule();
            }
        }.runTaskLater(plugin, TimeUnit.MILLISECONDS.toSeconds(startIn)*20);
    }
}
