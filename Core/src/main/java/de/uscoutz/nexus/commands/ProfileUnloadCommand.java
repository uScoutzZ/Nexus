package de.uscoutz.nexus.commands;

import de.uscoutz.nexus.NexusPlugin;
import de.uscoutz.nexus.player.NexusPlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ProfileUnloadCommand implements CommandExecutor {

    private NexusPlugin plugin;

    public ProfileUnloadCommand(NexusPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            NexusPlayer nexusPlayer = plugin.getPlayerManager().getPlayersMap().get(player.getUniqueId());

            if(player.hasPermission("nexus.command.profileunload")) {
                player.sendMessage("§aDas Profil wird entladen");
                plugin.getWorldManager().getWorldProfileMap().get(player.getWorld()).checkout();
            }
        }

        return false;
    }
}
