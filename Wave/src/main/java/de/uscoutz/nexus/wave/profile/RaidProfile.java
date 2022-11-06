package de.uscoutz.nexus.wave.profile;

import de.uscoutz.nexus.profile.Profile;
import de.uscoutz.nexus.quests.Task;
import de.uscoutz.nexus.schematic.schematics.BuiltSchematic;
import de.uscoutz.nexus.schematic.schematics.Condition;
import de.uscoutz.nexus.schematic.schematics.SchematicProfile;
import de.uscoutz.nexus.schematic.schematics.SchematicType;
import de.uscoutz.nexus.wave.NexusWavePlugin;
import de.uscoutz.nexus.wave.raids.Raid;
import de.uscoutz.nexus.wave.raids.RaidType;
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
                lastRaid = resultSet.getLong("ended");
            } else {
                lastRaid = 0;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void scheduleRaid() {
        Bukkit.getConsoleSender().sendMessage("[NexusWave] Scheduling raid for " + profile.getProfileId());
        long rangeMin = plugin.getConfig().getLong("raid-range-min");
        long rangeMax = plugin.getConfig().getLong("raid-range-max");
        long startIn = new Random().nextLong(rangeMax-rangeMin)+rangeMin;

        task = new BukkitRunnable() {
            @Override
            public void run() {
                if(task.isCancelled()) {
                    return;
                }
                boolean isNexusIntact;
                SchematicProfile schematicProfile = plugin.getSchematicPlugin().getSchematicManager().getSchematicProfileMap().get(profile.getProfileId());
                BuiltSchematic builtNexusSchematic, builtWorkshopSchematic = null;
                if(schematicProfile.getSchematics().get(SchematicType.NEXUS).size() != 0) {
                    builtNexusSchematic = schematicProfile.getSchematicsByRegion().get(schematicProfile.getSchematics().get(SchematicType.NEXUS).get(0));
                    builtWorkshopSchematic = schematicProfile.getSchematicsByRegion().get(schematicProfile.getSchematics().get(SchematicType.WORKSHOP).get(0));
                    isNexusIntact = builtNexusSchematic.getCondition(builtNexusSchematic.getPercentDamage()) == Condition.INTACT;
                } else {
                    isNexusIntact = false;
                }

                if((isNexusIntact && profile.getNexusLevel() != 0 && profile.getActivePlayers().size() != 0
                        && profile.getQuests().containsKey(Task.BUILD_TOWER) && profile.getQuests().get(Task.BUILD_TOWER).isFinished())
                        && builtWorkshopSchematic == null || builtWorkshopSchematic.isBuilt()) {
                    List<RaidType> raidTypes = plugin.getRaidManager().getRaidTypesByNexuslevel().get(profile.getNexusLevel());
                    RaidType raidType;
                    try {
                        raidType = raidTypes.get((int)(Math.random() * raidTypes.size())).clone();
                    } catch (CloneNotSupportedException e) {
                        throw new RuntimeException(e);
                    }
                    if(raid != null) {
                        return;
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
