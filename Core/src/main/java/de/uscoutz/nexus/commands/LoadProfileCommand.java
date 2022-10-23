package de.uscoutz.nexus.commands;

import de.uscoutz.nexus.NexusPlugin;
import de.uscoutz.nexus.networking.packet.packets.player.PacketPlayerChangeServer;
import eu.thesimplecloud.api.CloudAPI;
import eu.thesimplecloud.api.player.ICloudPlayer;
import eu.thesimplecloud.api.service.ICloudService;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class LoadProfileCommand implements CommandExecutor {

    private NexusPlugin plugin;

    public LoadProfileCommand(NexusPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(sender.hasPermission("nexus.command.loadprofile")) {
            if(sender instanceof Player player) {
                if (args.length == 1) {
                    ICloudPlayer cloudPlayer = CloudAPI.getInstance().getCloudPlayerManager().getCloudPlayer(args[0]).getBlockingOrNull();
                    if (cloudPlayer != null && cloudPlayer.isOnline() && cloudPlayer.getConnectedServer() != null && cloudPlayer.getConnectedServer().getName().startsWith(
                            plugin.getConfig().getString("cloudtype"))) {
                        if(cloudPlayer.getConnectedServer().getName().equals(plugin.getNexusServer().getThisServiceName())) {
                            player.teleport(Bukkit.getPlayer(cloudPlayer.getUniqueId()));
                        } else {
                            plugin.getNexusServer().getSpectators().put(player.getUniqueId(), cloudPlayer.getUniqueId());
                            CloudAPI.getInstance().getCloudPlayerManager().connectPlayer(
                                    CloudAPI.getInstance().getCloudPlayerManager().getCachedCloudPlayer(player.getUniqueId()), cloudPlayer.getConnectedServer());
                        }
                    } else {
                        player.sendMessage("§cDer Spieler ist nicht online oder nicht auf Nexus");
                    }
                } else {
                    sender.sendMessage("§cUsage: /spectate <player>");
                }
            }

        }
        return false;
    }
}
