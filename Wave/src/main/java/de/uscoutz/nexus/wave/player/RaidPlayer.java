package de.uscoutz.nexus.wave.player;

import de.uscoutz.nexus.wave.NexusWavePlugin;
import de.uscoutz.nexus.wave.raids.Raid;
import org.bukkit.entity.Player;

public class RaidPlayer {

    private NexusWavePlugin plugin;
    private Player player;

    public RaidPlayer(Player player, NexusWavePlugin plugin) {
        this.plugin = plugin;
        this.player = player;
        plugin.getPlayerManager().getRaidPlayerMap().put(player.getUniqueId(), this);
    }

    public void joinRaid(Raid raid) {
        player.showBossBar(raid.getBossBars().get("de_DE"));
        raid.getPlayers().add(player);
    }

    public void leaveRaid(Raid raid) {
        player.hideBossBar(raid.getBossBars().get("de_DE"));
        raid.getPlayers().remove(player);
    }
}
