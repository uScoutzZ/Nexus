package de.uscoutz.nexus.schematic.commands;

import de.uscoutz.nexus.schematic.NexusSchematicPlugin;
import de.uscoutz.nexus.schematic.player.SchematicPlayer;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class SchematicWand implements CommandExecutor {

    private NexusSchematicPlugin plugin;

    public SchematicWand(NexusSchematicPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(sender instanceof Player) {
            Player player = (Player) sender;
            SchematicPlayer schematicPlayer = plugin.getPlayerManager().getPlayerMap().get(player.getUniqueId());
            if(player.hasPermission("nexus.schematics")) {
                player.getInventory().addItem(new ItemStack(Material.GOLDEN_AXE));
            } else {
                player.sendMessage(plugin.getNO_PERMISSION());
            }
        }
        return false;
    }
}
