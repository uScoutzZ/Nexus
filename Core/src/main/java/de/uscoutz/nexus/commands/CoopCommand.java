package de.uscoutz.nexus.commands;

import de.uscoutz.nexus.NexusPlugin;
import de.uscoutz.nexus.networking.packet.packets.coop.PacketCoopDenied;
import de.uscoutz.nexus.networking.packet.packets.coop.PacketCoopInvite;
import de.uscoutz.nexus.networking.packet.packets.coop.PacketCoopKicked;
import de.uscoutz.nexus.player.NexusPlayer;
import de.uscoutz.nexus.profile.Profile;
import eu.thesimplecloud.api.CloudAPI;
import eu.thesimplecloud.api.player.ICloudPlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
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
            NexusPlayer nexusPlayer = plugin.getPlayerManager().getPlayersMap().get(player.getUniqueId());
            Profile profile = nexusPlayer.getCurrentProfile();

            if(args.length == 2) {
                if(args[0].equalsIgnoreCase("kick") || args[0].equalsIgnoreCase("invite")) {
                    if(profile.getOwner().equals(player.getUniqueId())) {
                        ICloudPlayer cloudPlayer = CloudAPI.getInstance().getCloudPlayerManager().getCloudPlayer(args[1]).getBlockingOrNull();
                        if(cloudPlayer != null) {
                            UUID uuid = cloudPlayer.getUniqueId();
                            if(args[0].equalsIgnoreCase("invite")) {
                                if(cloudPlayer.isOnline() && cloudPlayer.getConnectedServer().getGroupName().equals(plugin.getConfig().getString("cloudtype"))) {
                                    if(!nexusPlayer.getCurrentProfile().getMembers().containsKey(uuid)) {
                                        player.sendMessage(plugin.getLocaleManager().translate("de_DE", "command_coop-request-sent", args[1]));
                                        new PacketCoopInvite("123", uuid, nexusPlayer.getCurrentProfile().getProfileId(), player.getName()).send(cloudPlayer.getConnectedServer());
                                    } else {
                                        player.sendMessage(plugin.getLocaleManager().translate("de_DE", "command_coop_already-in-coop"));
                                    }
                                } else {
                                    player.sendMessage(plugin.getLocaleManager().translate("de_DE", "command_coop_player-offline"));
                                }
                            } else if(args[0].equalsIgnoreCase("kick")) {
                                if(!uuid.equals(player.getUniqueId())) {
                                    if(!nexusPlayer.getCurrentProfile().getMembers().containsKey(uuid)) {
                                        player.sendMessage(plugin.getLocaleManager().translate("de_DE", "command_coop__kick_player-not-in-coop"));
                                    } else {
                                        if(cloudPlayer.isOnline() && cloudPlayer.getConnectedServer().getGroupName().equals(plugin.getConfig().getString("cloudtype"))) {
                                            new PacketCoopKicked("123", uuid, nexusPlayer.getCurrentProfile().getProfileId()).send(cloudPlayer.getConnectedServer());
                                        } else {
                                            nexusPlayer.getCurrentProfile().kickPlayer(uuid);
                                        }
                                        player.sendMessage(plugin.getLocaleManager().translate("de_DE", "command_coop__kick_player-kicked", args[1]));
                                        new BukkitRunnable() {
                                            @Override
                                            public void run() {
                                                nexusPlayer.getCurrentProfile().getMembers().remove(uuid);
                                                nexusPlayer.getCurrentProfile().loadMembers();
                                            }
                                        }.runTaskLater(plugin, 20);
                                    }
                                } else {
                                    player.sendMessage(plugin.getLocaleManager().translate("de_DE", "command_coop_kick-self"));
                                }
                            }
                        } else {
                            player.sendMessage(plugin.getLocaleManager().translate("de_DE", "command_coop_kick_player-not-found"));
                        }
                    } else {
                        player.sendMessage(plugin.getLocaleManager().translate("de_DE", "command_coop_not-owner"));
                        return false;
                    }

                } else {
                    sendHelp(player);
                }
            } else if(args.length == 3) {
                if(args[0].equalsIgnoreCase("accept") || args[0].equalsIgnoreCase("deny")) {
                    try {
                        UUID profileId = UUID.fromString(args[1]);
                        if(plugin.getProfileManager().getCoopInvitations().get(player.getUniqueId()) != null &&
                                plugin.getProfileManager().getCoopInvitations().get(player.getUniqueId()).contains(profileId)) {
                            ICloudPlayer cloudPlayer = CloudAPI.getInstance().getCloudPlayerManager().getCloudPlayer(args[2]).getBlocking();
                            if(args[0].equalsIgnoreCase("accept")) {
                                if(nexusPlayer.getProfilesMap().size() >= plugin.getConfig().getInt("profile-slots")) {
                                    player.sendMessage(plugin.getLocaleManager().translate("de_DE", "command_coop_accept_no-slot"));
                                } else {
                                    nexusPlayer.openProfiles(profileId.toString());
                                }
                            } else if(args[0].equalsIgnoreCase("deny")) {
                                player.sendMessage(plugin.getLocaleManager().translate("de_DE", "command_coop_denied"));
                                plugin.getProfileManager().getCoopInvitations().get(player.getUniqueId()).remove(profileId);
                                new PacketCoopDenied("123", profileId, player.getName()).send(cloudPlayer.getConnectedServer());
                            }
                        } else {
                            player.sendMessage(plugin.getLocaleManager().translate("de_DE", "command_coop_no-invitation"));
                        }
                    } catch (IllegalArgumentException exception) {
                        player.sendMessage(plugin.getLocaleManager().translate("de_DE", "command_coop_no-invitation"));
                    }
                } else {
                    sendHelp(player);
                }
            } else {
                sendHelp(player);
            }
        }

        return false;
    }

    private void sendHelp(Player player) {
        player.sendMessage(plugin.getLocaleManager().translate("de_DE", "command_coop_help"));
    }
}
