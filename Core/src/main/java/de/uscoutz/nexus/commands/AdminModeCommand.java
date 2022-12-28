package de.uscoutz.nexus.commands;

import de.uscoutz.nexus.NexusPlugin;
import de.uscoutz.nexus.player.NexusPlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class AdminModeCommand implements CommandExecutor {

    private NexusPlugin plugin;

    public AdminModeCommand(NexusPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if(commandSender instanceof Player player) {
            if(player.hasPermission("nexus.command.adminmode")) {
                NexusPlayer nexusPlayer = plugin.getPlayerManager().getPlayersMap().get(player.getUniqueId());
                if(nexusPlayer.isAdminMode()) {
                    player.sendMessage("§cDu bist nun nicht mehr im Adminmodus");
                } else {
                    player.sendMessage("§aDu bist nun im Adminmodus");
                }
                nexusPlayer.setAdminMode(!nexusPlayer.isAdminMode());
            }
        }
        return false;
    }
}
