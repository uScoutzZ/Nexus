package de.uscoutz.nexus.wave.profile;

import de.uscoutz.nexus.profile.Profile;
import de.uscoutz.nexus.wave.NexusWavePlugin;
import de.uscoutz.nexus.wave.customentities.NexusEntityType;
import de.uscoutz.nexus.wave.customentities.NexusZombie;
import lombok.Getter;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_19_R1.CraftWorld;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class Raid {

    private NexusWavePlugin plugin;

    @Getter
    private Profile profile;

    private long started;
    private BossBar bossBar;
    private List<Player> players;
    private RaidType raidType;
    private int wave;


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
                            startWave(1);
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

    public void startWave(int wave) {
        this.wave = wave;
        profile.getWorld().changeTime(13000);
        for(Player player : players) {
            player.sendMessage(plugin.getNexusPlugin().getLocaleManager().translate("de_DE", "raids_wave-start", wave));
        }

        final int[] mobs = {raidType.getMobsPerWave().get(wave)};

        new BukkitRunnable() {
            @Override
            public void run() {
                if(mobs[0] != 0) {
                    spawnRandomMonster();
                    mobs[0]--;
                } else {
                    players.forEach(player -> player.sendMessage("§aWave ended"));
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 0, 20);
    }

    private void spawnRandomMonster() {
        players.forEach(player -> player.sendMessage("§eSpawning monster"));
        NexusEntityType nexusEntityType = raidType.getMobsByWave().get(wave).get((int)(Math.random() * raidType.getMobsByWave().get(wave).size()));
        ServerLevel world = ((CraftWorld) profile.getWorld().getWorld()).getHandle();
        Location randomLocation = plugin.getRaidManager().getSpawnLocations().get((int)(Math.random() * plugin.getRaidManager().getSpawnLocations().size()));
        randomLocation.setWorld(profile.getWorld().getWorld());
        Entity entity = null;
        if(nexusEntityType == NexusEntityType.ZOMBIE) {
            entity = new NexusZombie(randomLocation, plugin);
        } else if(nexusEntityType == NexusEntityType.GOLEM) {
            entity = new NexusZombie(randomLocation, plugin);
        }

        players.forEach(player -> player.teleport(randomLocation));
        world.tryAddFreshEntityWithPassengers(entity, CreatureSpawnEvent.SpawnReason.CUSTOM);
    }
}
