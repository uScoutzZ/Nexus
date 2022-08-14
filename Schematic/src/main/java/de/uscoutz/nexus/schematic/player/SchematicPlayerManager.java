package de.uscoutz.nexus.schematic.player;

import de.uscoutz.nexus.schematic.NexusSchematicPlugin;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SchematicPlayerManager {

    private NexusSchematicPlugin plugin;

    @Getter
    private Map<UUID, SchematicPlayer> playerMap;

    public SchematicPlayerManager(NexusSchematicPlugin plugin) {
        this.plugin = plugin;
        playerMap = new HashMap<>();
    }
}
