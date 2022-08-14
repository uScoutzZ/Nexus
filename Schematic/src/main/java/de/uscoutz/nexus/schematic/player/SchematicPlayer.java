package de.uscoutz.nexus.schematic.player;

import de.uscoutz.nexus.schematic.NexusSchematicPlugin;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SchematicPlayer {

    private NexusSchematicPlugin plugin;
    @Getter
    private UUID playerUUID;
    @Getter @Setter
    private Map<Integer, Location> locations;

    public SchematicPlayer(UUID playerUUID, NexusSchematicPlugin plugin) {
        this.plugin = plugin;
        this.playerUUID = playerUUID;
        locations = new HashMap<>();
        plugin.getPlayerManager().getPlayerMap().put(playerUUID, this);
    }
}
