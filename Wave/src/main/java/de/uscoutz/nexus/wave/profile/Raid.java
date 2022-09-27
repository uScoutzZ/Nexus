package de.uscoutz.nexus.wave.profile;

import de.uscoutz.nexus.player.NexusPlayer;
import de.uscoutz.nexus.profile.Profile;
import de.uscoutz.nexus.schematic.schematics.Condition;
import de.uscoutz.nexus.wave.NexusWavePlugin;
import de.uscoutz.nexus.wave.customentities.*;
import de.uscoutz.nexus.wave.player.RaidPlayer;
import lombok.Getter;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_19_R1.CraftWorld;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class Raid {

    private NexusWavePlugin plugin;

    @Getter
    private Profile profile;
    @Getter
    private RaidProfile raidProfile;
    @Getter
    private List<UUID> mobs;
    @Getter
    private Map<String, BossBar> bossBars;
    @Getter
    private List<Player> players;

    private long started;
    @Getter
    private RaidType raidType;
    @Getter
    private int killedInCurrentWave, wave, kills;

    public Raid(RaidType raidType, Profile profile, NexusWavePlugin plugin) {
        this.plugin = plugin;
        this.raidType = raidType;
        this.profile = profile;
        raidProfile = plugin.getRaidManager().getRaidProfileMap().get(profile.getProfileId());

        started = System.currentTimeMillis();
        players = new ArrayList<>();
        mobs = new ArrayList<>();
        bossBars = new HashMap<>();
    }

    public void end(boolean scheduleNew, boolean won) {
        for(UUID entityId : mobs) {
            org.bukkit.entity.Entity entity = Bukkit.getEntity(entityId);
            if(entity != null) {
                entity.remove();
            }
        }
        profile.getWorld().changeTime(6000);
        profile.getActivePlayers().forEach(player -> {
            RaidPlayer raidPlayer = plugin.getPlayerManager().getRaidPlayerMap().get(player.getPlayer().getUniqueId());
            raidPlayer.leaveRaid(this);
        });
        plugin.getRaidManager().getRaidProfileMap().get(profile.getProfileId()).setRaid(null);

        profile.getActivePlayers().forEach(nexusPlayer -> {
            nexusPlayer.getPlayer().sendMessage(plugin.getNexusPlugin().getLocaleManager().translate("de_DE", "raid_ended"));
        });
        if(won) {
            profile.setWonRaids(profile.getWonRaids() + 1);
        } else {
            profile.setLostRaids(profile.getLostRaids() + 1);
        }
        plugin.getNexusPlugin().getDatabaseAdapter().set("raids",
                String.valueOf(profile.getProfileId()), raidType.getRaidTypeId(), won ? 1:0, kills, String.valueOf(System.currentTimeMillis()));
        if(scheduleNew) {
            raidProfile.scheduleRaid();
        }
    }

    public void schedule() {
        long raidCounter = plugin.getConfig().getLong("raid-counter");
        int raidCounterSeconds = (int) TimeUnit.MILLISECONDS.toSeconds(raidCounter);
        final int[] countdown = {(int) TimeUnit.MILLISECONDS.toSeconds(raidCounter)};
        for(String key : plugin.getNexusPlugin().getLocaleManager().getLanguageKeys()) {
            bossBars.put(key, BossBar.bossBar(Component.text(""), 1, BossBar.Color.RED, BossBar.Overlay.NOTCHED_6));
        }

        for(NexusPlayer nexusPlayer : profile.getActivePlayers()) {
            RaidPlayer raidPlayer = plugin.getPlayerManager().getRaidPlayerMap().get(nexusPlayer.getPlayer().getUniqueId());
            raidPlayer.joinRaid(this);
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                if(profile.loaded()) {
                    profile.cancelCheckout();
                    if(countdown[0] == 0) {
                        startWave(1);
                        cancel();
                    } else {
                        String counter = String.format("%02d:%02d:%02d", TimeUnit.SECONDS.toHours(countdown[0]),
                                TimeUnit.SECONDS.toMinutes(countdown[0]) - TimeUnit.HOURS.toMinutes(TimeUnit.SECONDS.toHours(countdown[0])),
                                TimeUnit.SECONDS.toSeconds(countdown[0]) - TimeUnit.MINUTES.toSeconds(TimeUnit.SECONDS.toMinutes(countdown[0])));
                        double progress = (double) countdown[0]/raidCounterSeconds;
                        for(String key : bossBars.keySet()) {
                            BossBar bossBar = bossBars.get(key);
                            bossBar.name(Component.text(plugin.getNexusPlugin().getLocaleManager().translate(
                                    key, "raid_starts-in", counter)));
                            bossBar.progress((float) progress);
                        }
                        countdown[0]--;
                    }
                } else {
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 0, 20);
    }

    public void startWave(int wave) {
        this.wave = wave;
        killedInCurrentWave = 0;
        updateWaveProgress();
        profile.getWorld().changeTime(13000);
        for(BossBar bossBar : bossBars.values()) {
            bossBar.color(BossBar.Color.BLUE);
        }
        for(Player player : players) {
            player.sendMessage(plugin.getNexusPlugin().getLocaleManager().translate("de_DE", "raids_wave-start", wave));
        }

        final int[] mobs = {raidType.getMobsPerWave().get(wave)};

        new BukkitRunnable() {
            @Override
            public void run() {
                if(mobs[0] != 0 && profile.loaded()) {
                    spawnRandomMonster();
                    mobs[0]--;
                } else {
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 40, 30);
    }

    public void stopWave() {
        long waveCounter = plugin.getConfig().getLong("wave-counter");
        int raidCounterSeconds = (int) TimeUnit.MILLISECONDS.toSeconds(waveCounter);
        final int[] countdown = {(int) TimeUnit.MILLISECONDS.toSeconds(waveCounter)};
        for(BossBar bossBar : bossBars.values()) {
            bossBar.color(BossBar.Color.YELLOW);
        }

        if(wave == raidType.getMobsPerWave().size()) {
            end(true, true);
        } else {
            new BukkitRunnable() {
                @Override
                public void run() {
                    if(profile.loaded()) {
                        profile.cancelCheckout();
                        if(countdown[0] == 0) {
                            startWave(wave+1);
                            cancel();
                        } else {
                            String counter = String.format("%02d:%02d:%02d", TimeUnit.SECONDS.toHours(countdown[0]),
                                    TimeUnit.SECONDS.toMinutes(countdown[0]) - TimeUnit.HOURS.toMinutes(TimeUnit.SECONDS.toHours(countdown[0])),
                                    TimeUnit.SECONDS.toSeconds(countdown[0]) - TimeUnit.MINUTES.toSeconds(TimeUnit.SECONDS.toMinutes(countdown[0])));
                            double progress = (double) countdown[0]/raidCounterSeconds;

                            for(String key : bossBars.keySet()) {
                                BossBar bossBar = bossBars.get(key);
                                bossBar.name(Component.text(plugin.getNexusPlugin().getLocaleManager().translate(
                                        key, "raid_wave-starts-in", counter)));
                                bossBar.progress((float) progress);
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

    private void spawnRandomMonster() {
        NexusEntityType nexusEntityType = raidType.getMobsByWave().get(wave).get((int)(Math.random() * raidType.getMobsByWave().get(wave).size()));
        ServerLevel world = ((CraftWorld) profile.getWorld().getWorld()).getHandle();
        Location randomLocation = plugin.getRaidManager().getSpawnLocations().get((int)(Math.random() * plugin.getRaidManager().getSpawnLocations().size()));
        randomLocation.setWorld(profile.getWorld().getWorld());
        Entity entity = null;
        if(nexusEntityType == NexusEntityType.ZOMBIE) {
            entity = new NexusZombie(randomLocation, plugin, 1);
        } else if(nexusEntityType == NexusEntityType.GOLEM) {
            entity = new NexusGolem(randomLocation, plugin, 2);
        } else if(nexusEntityType == NexusEntityType.SKELETON) {
            entity = new NexusSkeleton(randomLocation, plugin, 2);
        }

        boolean loaded = randomLocation.getChunk().load();
        world.tryAddFreshEntityWithPassengers(entity, CreatureSpawnEvent.SpawnReason.CUSTOM);
        mobs.add(entity.getBukkitEntity().getUniqueId());
        updateWaveProgress();
    }

    public void addKill() {
        killedInCurrentWave++;
        kills++;
        updateWaveProgress();
    }

    private void updateWaveProgress() {
        double progress = (double) killedInCurrentWave/raidType.getMobsPerWave().get(wave);

        for(String key : bossBars.keySet()) {
            BossBar bossBar = bossBars.get(key);
            bossBar.progress(1-(float) progress);
            bossBar.name(Component.text(plugin.getNexusPlugin().getLocaleManager().translate(key, "raid_remaining-mobs")));
        }
    }
}
