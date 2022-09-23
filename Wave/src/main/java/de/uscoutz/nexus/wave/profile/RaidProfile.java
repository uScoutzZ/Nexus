package de.uscoutz.nexus.wave.profile;

import de.uscoutz.nexus.profile.Profile;
import de.uscoutz.nexus.quests.Task;
import de.uscoutz.nexus.wave.NexusWavePlugin;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
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
    @Getter @Setter
    private Raid raid;
    @Getter
    private BukkitTask task;

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

        task = new BukkitRunnable() {
            @Override
            public void run() {
                if(profile.getNexusLevel() != 0 && profile.getActivePlayers().size() != 0
                        && profile.getQuests().containsKey(Task.BUILD_TOWER) && profile.getQuests().get(Task.BUILD_TOWER).isFinished()) {
                    List<RaidType> raidTypes = plugin.getRaidManager().getRaidTypesByNexuslevel().get(profile.getNexusLevel());
                    RaidType raidType;
                    try {
                        raidType = raidTypes.get((int)(Math.random() * raidTypes.size())).clone();
                    } catch (CloneNotSupportedException e) {
                        throw new RuntimeException(e);
                    }
                    raid = new Raid(raidType, profile, plugin);
                    raid.schedule();
                } else {
                    if(profile.loaded()) {
                        scheduleRaid();
                    }
                }
            }
        }.runTaskLater(plugin, TimeUnit.MILLISECONDS.toSeconds(startIn)*20);
    }
}
