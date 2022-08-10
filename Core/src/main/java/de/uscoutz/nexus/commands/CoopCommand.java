package de.uscoutz.nexus.commands;

import de.uscoutz.nexus.NexusPlugin;
import de.uscoutz.nexus.networking.packet.packets.coop.PacketCoopInvite;
import de.uscoutz.nexus.player.NexusPlayer;
import de.uscoutz.nexus.profile.Profile;
import eu.thesimplecloud.api.CloudAPI;
import eu.thesimplecloud.api.player.ICloudPlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class CoopCommand implements CommandExecutor {

    private NexusPlugin plugin;

    public CoopCommand(NexusPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            NexusPlayer nexusPlayer = plugin.getPlayerManager().getPlayersMap().get(player);
            Profile profile = nexusPlayer.getCurrentProfile();

            if(args.length >= 1) {
                if(args[0].equalsIgnoreCase("kick") || args[0].equalsIgnoreCase("invite")) {
                    if(profile.getOwner().equals(player.getUniqueId())) {
                        if(args.length == 2) {
                            ICloudPlayer cloudPlayer = CloudAPI.getInstance().getCloudPlayerManager().getCloudPlayer(args[1]).getBlocking();
                            if(cloudPlayer.isOnline() && cloudPlayer.getConnectedServer().getGroupName().equals(plugin.getConfig().getString("cloudtype"))) {
                                UUID uuid = cloudPlayer.getUniqueId();
                                if(!nexusPlayer.getCurrentProfile().getMembers().containsKey(uuid)) {
                                    new PacketCoopInvite("123", uuid, nexusPlayer.getCurrentProfile().getProfileId()).send(cloudPlayer.getConnectedServer());
                                } else {
                                    player.sendMessage(plugin.getMessage().get("command_coop_already-in-coop"));
                                }
                            } else {
                                player.sendMessage(plugin.getMessage().get("command_coop_player-offline"));
                            }
                        } else {
                            sendHelp(player);
                            return false;
                        }
                    } else {
                        player.sendMessage(plugin.getMessage().get("command_coop_not-owner"));
                        return false;
                    }

                }
            } else {
                sendHelp(player);
            }
        }

        return false;
    }

    private void sendHelp(Player player) {
        player.sendMessage(plugin.getMessage().get("command_coop_help"));
    }
}
