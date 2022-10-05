package de.uscoutz.nexus.commands;

import de.uscoutz.nexus.NexusPlugin;
import de.uscoutz.nexus.player.NexusPlayer;
import de.uscoutz.nexus.profile.Profile;
import eu.thesimplecloud.api.CloudAPI;
import eu.thesimplecloud.api.player.ICloudPlayer;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.sql.ResultSet;
import java.util.UUID;

public class CheckPlayerCommand implements CommandExecutor {

    private NexusPlugin plugin;

    public CheckPlayerCommand(NexusPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(sender.hasPermission("nexus.command.checkplayer")) {
            if(args.length == 1) {
                ICloudPlayer cloudPlayer = CloudAPI.getInstance().getCloudPlayerManager().getCloudPlayer(args[0]).getBlockingOrNull();
                Player target = Bukkit.getPlayer(args[0]);
                if(target != null && target.isOnline()) {
                    sender.sendMessage("isPlayerLoaded=" + plugin.getPlayerManager().getPlayersMap().containsKey(target.getUniqueId()));
                    if(plugin.getPlayerManager().getPlayersMap().containsKey(target.getUniqueId())) {
                        NexusPlayer nexusPlayer = plugin.getPlayerManager().getPlayersMap().get(target.getUniqueId());
                        sender.sendMessage("isProfilePrepared=" + plugin.getProfileManager().getProfilesMap().containsKey(nexusPlayer.getCurrentProfile().getProfileId()));
                        if(plugin.getProfileManager().getProfilesMap().containsKey(nexusPlayer.getCurrentProfile().getProfileId())) {
                            Profile profile = plugin.getProfileManager().getProfilesMap().get(nexusPlayer.getCurrentProfile().getProfileId());
                            sender.sendMessage("isProfileLoaded=" + profile.loaded());
                        }
                    }
                } else {
                    sender.sendMessage("§cPlayer is offline, no data is cached");
                }

                ResultSet resultSet = plugin.getDatabaseAdapter().get("playerProfiles", "player", cloudPlayer.getUniqueId().toString());
                try {
                    while(resultSet.next()) {
                        String profileId  = resultSet.getString("profileId");
                        TextComponent message = new TextComponent("- #" + resultSet.getString("slot") + " " + profileId + " §7(Clickt to join)");
                        message.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/joinprofile " + profileId));
                        sender.sendMessage(message);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            } else {
                sender.sendMessage("§c/checkplayer <Name>");
            }
        }
        return false;
    }
}
