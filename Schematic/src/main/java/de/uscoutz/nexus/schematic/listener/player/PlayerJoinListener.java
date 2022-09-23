package de.uscoutz.nexus.schematic.listener.player;

import de.uscoutz.nexus.schematic.NexusSchematicPlugin;
import de.uscoutz.nexus.schematic.player.SchematicPlayer;
import de.uscoutz.nexus.schematic.schematicitems.SchematicItem;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class PlayerJoinListener implements Listener {

    private NexusSchematicPlugin plugin;

    public PlayerJoinListener(NexusSchematicPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoined(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        SchematicPlayer schematicPlayer = new SchematicPlayer(player.getUniqueId(), plugin);

        /*new BukkitRunnable() {
            @Override
            public void run() {
                for(SchematicItem schematicItem : plugin.getSchematicItemManager().getSchematicItemMap().values()) {
                    player.getInventory().addItem(schematicItem.getItemStack());
                }
            }
        }.runTaskLater(plugin, 20);*/
    }
}
