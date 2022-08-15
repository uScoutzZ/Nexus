package de.uscoutz.nexus.commands;

import de.uscoutz.nexus.NexusPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class SetLocationCommand implements CommandExecutor {

    private NexusPlugin plugin;

    public SetLocationCommand(NexusPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if(player.hasPermission("nexus.command.setlocation")) {
                if(args.length == 1) {
                    if(args[0].equalsIgnoreCase("rl") || args[0].equalsIgnoreCase("reload")) {
                        plugin.getLocationManager().reloadFile();
                        player.sendMessage("§aReloaded the location config");
                    } else {
                        plugin.getLocationManager().saveLocation(args[0], player.getLocation());
                        player.sendMessage("§aSet location " + args[0]);
                    }
                } else {
                    player.sendMessage("§cSyntax: /setlocation <Location>");
                }
            }
        }
        return false;
    }
}
