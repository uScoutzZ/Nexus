package de.uscoutz.nexus.wave.player;

import de.uscoutz.nexus.wave.NexusWavePlugin;
import lombok.Getter;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerManager {

    private NexusWavePlugin plugin;

    @Getter
    private Map<UUID, RaidPlayer> raidPlayerMap;

    public PlayerManager(NexusWavePlugin plugin) {
        this.plugin = plugin;
        raidPlayerMap = new HashMap<>();
    }
}
