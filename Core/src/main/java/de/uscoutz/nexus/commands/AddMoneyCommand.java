package de.uscoutz.nexus.commands;

import de.uscoutz.nexus.NexusPlugin;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class AddMoneyCommand implements CommandExecutor {

    private NexusPlugin plugin;

    public AddMoneyCommand(NexusPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if(sender.hasPermission("nexus.command.addmoney")) {
            if (args.length == 2) {
                Player player = Bukkit.getPlayer(args[0]);
                int amount = Integer.parseInt(args[1]);
                if(player != null && player.isOnline()) {
                    plugin.getWorldManager().getWorldProfileMap().get(player.getWorld()).getMembers().get(player.getUniqueId()).addMoney(amount);
                    sender.sendMessage("§aAdded " + amount + " to " + player.getName());
                } else {
                    sender.sendMessage("§cPlayer is offline");
                }
            } else {
                sender.sendMessage("§cUsage: /addmoney <player> <amount>");
            }
        }

        return false;
    }
}
