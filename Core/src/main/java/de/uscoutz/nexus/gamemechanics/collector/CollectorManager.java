package de.uscoutz.nexus.gamemechanics.collector;

import de.uscoutz.nexus.NexusPlugin;
import lombok.Getter;
import org.bukkit.block.Block;

import java.util.HashMap;
import java.util.Map;

public class CollectorManager {

    private NexusPlugin plugin;

    @Getter
    private Map<Block, Collector> collectors;

    public CollectorManager(NexusPlugin plugin) {
        collectors = new HashMap<>();
    }
}
