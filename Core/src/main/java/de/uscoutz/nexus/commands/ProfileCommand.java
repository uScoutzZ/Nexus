package de.uscoutz.nexus.commands;

import de.uscoutz.nexus.NexusPlugin;
import de.uscoutz.nexus.player.NexusPlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;


public class ProfileCommand implements CommandExecutor {

    private NexusPlugin plugin;

    public ProfileCommand(NexusPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(sender instanceof Player) {
            Player player = (Player) sender;
            NexusPlayer nexusPlayer = plugin.getPlayerManager().getPlayersMap().get(player.getUniqueId());

            nexusPlayer.openProfiles();

            /*if(args.length == 0) {
                nexusPlayer.openProfiles();
            } else {
                if(args[0].equalsIgnoreCase("delete")) {
                    if(args.length > 1) {
                        if(args.length == 3) {

                        } else if(args.length == 2) {

                        }
                    } else {
                        sendHelp(player);
                    }
                } else {
                    sendHelp(player);
                }
            }*/
        }
        return false;
    }

    private void sendHelp(Player player) {
        player.sendMessage(plugin.getLocaleManager().translate("de_DE", "command_profile_help"));
    }
}
