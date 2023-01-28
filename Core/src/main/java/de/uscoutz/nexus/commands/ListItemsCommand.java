package de.uscoutz.nexus.commands;

import de.uscoutz.nexus.NexusPlugin;
import de.uscoutz.nexus.gamemechanics.tools.Tool;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class ListItemsCommand implements CommandExecutor {

    private NexusPlugin plugin;

    public ListItemsCommand(NexusPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (sender.hasPermission("command.nexus.listitems")) {
            for(Tool tool : plugin.getToolManager().getToolMap().values()) {
                sender.sendMessage("§e" + tool.getKey() + "§8 - §6" + tool.getPath());
            }
        }
        return false;
    }
}
