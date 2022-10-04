package de.uscoutz.nexus.commands;

import de.uscoutz.nexus.NexusPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class FlyCommand implements CommandExecutor {

    private NexusPlugin plugin;

    public FlyCommand(NexusPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if(commandSender instanceof Player player) {
            if(player.hasPermission("command.nexus.fly")) {
                player.setAllowFlight(!player.getAllowFlight());
                player.sendMessage("§aFliegen umgeschaltet");
            }
        }
        return false;
    }
}
