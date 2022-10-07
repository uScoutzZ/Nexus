package de.uscoutz.nexus.commands;

import de.uscoutz.nexus.NexusPlugin;
import de.uscoutz.nexus.networking.packet.packets.coop.PacketCoopAccepted;
import de.uscoutz.nexus.profile.Profile;
import eu.thesimplecloud.api.CloudAPI;
import eu.thesimplecloud.api.service.ICloudService;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class JoinProfileCommand implements CommandExecutor {

    private NexusPlugin plugin;

    public JoinProfileCommand(NexusPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if(commandSender instanceof Player player) {
            if(player.hasPermission("command.nexus.joinprofile")) {
                if(args.length == 1) {
                    UUID profileId = UUID.fromString(args[0]);
                    boolean alreadyInProfile = false;
                    for(Profile profile : plugin.getPlayerManager().getPlayersMap().get(player.getUniqueId()).getProfilesMap().values()) {
                        if(profile.getProfileId().equals(profileId)) {
                            alreadyInProfile = true;
                            break;
                        }
                    }
                    if(alreadyInProfile) {
                        player.sendMessage("§cYou are already in this profile");
                        return false;
                    }
                    int freeSlot = -1;
                    int maxProfiles = NexusPlugin.getInstance().getConfig().getInt("profile-slots");
                    if(player.hasPermission("nexus.profile.unlimited")) {
                        maxProfiles = 45;
                    }
                    for(int i = 0; i < maxProfiles; i++) {
                        if(!plugin.getPlayerManager().getPlayersMap().get(player.getUniqueId()).getProfilesMap().containsKey(i)) {
                            freeSlot = i;
                            break;
                        }
                    }
                    if(freeSlot != -1) {
                        if(plugin.getNexusServer().getProfilesServerMap().containsKey(profileId)) {
                            new PacketCoopAccepted("123", profileId, player.getUniqueId(), player.getName(), freeSlot, plugin.getNexusServer().getThisServiceName()).send(
                                    CloudAPI.getInstance().getCloudServiceManager().getCloudServiceByName(plugin.getNexusServer().getProfilesServerMap().get(profileId)));
                        } else {
                            Profile toJoin;
                            if(plugin.getProfileManager().getProfilesMap().containsKey(profileId)) {
                                toJoin = plugin.getProfileManager().getProfilesMap().get(profileId);
                            } else {
                                toJoin = new Profile(profileId, plugin);
                            }
                            toJoin.addPlayer(freeSlot, player.getUniqueId());
                            new BukkitRunnable() {
                                @Override
                                public void run() {
                                    plugin.getPlayerManager().getPlayersMap().get(player.getUniqueId()).loadProfiles();
                                }
                            }.runTaskLater(plugin, 5);
                        }
                        player.sendMessage("Joining profile on slot " + freeSlot + "...");
                    }
                }
            }
        }
        return false;
    }
}
