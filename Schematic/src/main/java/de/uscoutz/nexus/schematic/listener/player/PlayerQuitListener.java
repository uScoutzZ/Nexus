package de.uscoutz.nexus.schematic.listener.player;

import de.uscoutz.nexus.schematic.NexusSchematicPlugin;
import de.uscoutz.nexus.schematic.player.SchematicPlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerQuitListener implements Listener {

    private NexusSchematicPlugin plugin;

    public PlayerQuitListener(NexusSchematicPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        SchematicPlayer schematicPlayer = plugin.getPlayerManager().getPlayerMap().get(event.getPlayer().getUniqueId());
    }
}
