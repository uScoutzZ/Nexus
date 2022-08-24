package de.uscoutz.nexus.player;

import de.uscoutz.nexus.NexusPlugin;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerManager {

    private NexusPlugin plugin;

    @Getter
    private Map<UUID, NexusPlayer> playersMap;

    public PlayerManager(NexusPlugin plugin) {
        this.plugin = plugin;
        playersMap = new HashMap<>();
    }
}
