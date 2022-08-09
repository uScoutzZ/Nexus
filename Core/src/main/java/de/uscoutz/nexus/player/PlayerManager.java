package de.uscoutz.nexus.player;

import de.uscoutz.nexus.NexusPlugin;
import de.uscoutz.nexus.profile.Profile;
import lombok.Getter;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerManager {

    private NexusPlugin plugin;

    @Getter
    private Map<Player, NexusPlayer> playersMap;

    public PlayerManager(NexusPlugin plugin) {
        this.plugin = plugin;
        playersMap = new HashMap<>();
    }
}
